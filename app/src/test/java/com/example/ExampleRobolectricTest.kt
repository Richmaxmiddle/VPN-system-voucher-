package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.repository.VpnRepository
import com.example.data.model.User
import com.example.data.model.Voucher
import com.example.ui.VpnViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Zero-Data VPN", appName)
  }

  @Test
  fun `database and viewmodel initialization test`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    
    // Create an in-memory Room database
    val database = Room.inMemoryDatabaseBuilder(
      context,
      AppDatabase::class.java
    ).allowMainThreadQueries().build()
    
    val repository = VpnRepository(
      voucherDao = database.voucherDao(),
      routerDao = database.routerDao(),
      configDao = database.configDao(),
      sessionDao = database.sessionDao(),
      userDao = database.userDao()
    )
    
    val viewModel = VpnViewModel(context, repository)
    assertNotNull(viewModel)
    
    // Verify default screens/states
    assertEquals("AUTH", viewModel.currentScreen.value)
    assertEquals("DISCONNECTED", viewModel.connectionState.value)
    
    database.close()
  }

  @Test
  fun `user registration login and referral reward flow test`() = kotlinx.coroutines.test.runTest(testDispatcher) {
    val context = ApplicationProvider.getApplicationContext<Context>()
    
    // Create an in-memory Room database
    val database = Room.inMemoryDatabaseBuilder(
      context,
      AppDatabase::class.java
    ).allowMainThreadQueries().build()
    
    val repository = VpnRepository(
      voucherDao = database.voucherDao(),
      routerDao = database.routerDao(),
      configDao = database.configDao(),
      sessionDao = database.sessionDao(),
      userDao = database.userDao()
    )
    
    val viewModel = VpnViewModel(context, repository)
    
    // Let viewModel initial load run
    testDispatcher.scheduler.advanceUntilIdle()

    // 1. Register a host user who will act as referrer
    viewModel.registerNewUser("referrer", "pass123", "CLIENT")
    
    // Poll/wait for user creation on background IO threads
    var dbUser: User? = null
    for (i in 1..50) {
        dbUser = repository.getUserByUsername("referrer")
        if (dbUser != null) break
        delay(50)
    }
    assertNotNull("Referrer user should be created", dbUser)
    val referrerCode = dbUser!!.referralCode
    assert(referrerCode.startsWith("REF-"))

    // 2. Register a new user using the referrer code
    viewModel.registerNewUser("newuser", "secure456", "CLIENT", referralCodeUsed = referrerCode)
    
    // Poll/wait for new user creation
    var dbNewUser: User? = null
    for (i in 1..50) {
        dbNewUser = repository.getUserByUsername("newuser")
        if (dbNewUser != null) break
        delay(50)
    }
    assertNotNull("New user should be created", dbNewUser)
    assertEquals(referrerCode, dbNewUser!!.referredByCode)

    // Check if referrer's count increased (also poll for update)
    var updatedReferrer: User? = null
    for (i in 1..50) {
        updatedReferrer = repository.getUserByUsername("referrer")
        if (updatedReferrer != null && updatedReferrer.referredCount == 1) break
        delay(50)
    }
    assertEquals(1, updatedReferrer?.referredCount)

    // Verify getUsersByReferredByCode functions correctly
    val referredList = repository.getUsersByReferredByCode(referrerCode)
    assertEquals(1, referredList.size)
    assertEquals("newuser", referredList[0].username)

    // Check that bonus vouchers are generated
    var vouchers: List<Voucher> = emptyList()
    for (i in 1..50) {
        vouchers = repository.getAllVouchersDirect()
        if (vouchers.size >= 2) break // welcome + bonus + maybe default prepopulated
        delay(50)
    }
    assert(vouchers.isNotEmpty())
    
    // Clean up
    database.close()
  }

  @Test
  fun `update referral reward hours config persistence test`() = kotlinx.coroutines.test.runTest(testDispatcher) {
    val context = ApplicationProvider.getApplicationContext<Context>()
    
    val database = Room.inMemoryDatabaseBuilder(
      context,
      AppDatabase::class.java
    ).allowMainThreadQueries().build()
    
    val repository = VpnRepository(
      voucherDao = database.voucherDao(),
      routerDao = database.routerDao(),
      configDao = database.configDao(),
      sessionDao = database.sessionDao(),
      userDao = database.userDao()
    )
    
    val viewModel = VpnViewModel(context, repository)
    testDispatcher.scheduler.advanceUntilIdle()

    // Update to new custom hours value e.g. 72 hours (3 days)
    viewModel.updateReferralRewardHours(72)
    
    // Poll for ViewModel value to update (using real sleep to let non-test Dispatcher.IO do real work)
    var updatedHoursValue = 0
    for (i in 1..50) {
        updatedHoursValue = viewModel.referralRewardHours.value
        if (updatedHoursValue == 72) break
        Thread.sleep(50)
    }
    assertEquals(72, updatedHoursValue)

    // Poll for Room config database value to persist (using real sleep)
    var dbConfigValue = ""
    for (i in 1..50) {
        dbConfigValue = repository.getConfigValue("REFERRAL_REWARD_HOURS", "24")
        if (dbConfigValue == "72") break
        Thread.sleep(50)
    }
    assertEquals("72", dbConfigValue)

    database.close()
  }

  @Test
  fun `anti sharing dynamic device lock protection test`() = kotlinx.coroutines.test.runTest(testDispatcher) {
    val context = ApplicationProvider.getApplicationContext<Context>()
    
    val database = Room.inMemoryDatabaseBuilder(
      context,
      AppDatabase::class.java
    ).allowMainThreadQueries().build()
    
    val repository = VpnRepository(
      voucherDao = database.voucherDao(),
      routerDao = database.routerDao(),
      configDao = database.configDao(),
      sessionDao = database.sessionDao(),
      userDao = database.userDao()
    )
    
    // Insert a fresh test voucher in DB
    val tokenCode = "TEST-LOCK-TOKEN"
    val voucher = Voucher(
      token = tokenCode,
      durationHours = 24,
      priceTzs = 0.0,
      totalDataMb = 5000L,
      status = "UNUSED",
      createdAt = System.currentTimeMillis()
    )
    repository.insertVoucher(voucher)

    val viewModel1 = VpnViewModel(context, repository)
    testDispatcher.scheduler.advanceUntilIdle()

    // Connect (which activates the voucher on first device)
    viewModel1.connectVpn(tokenCode)
    
    // Poll/wait for activation from UNUSED to ACTIVE (using real Thread.sleep to coordinate with Dispatchers.IO)
    var activeVoucherInDb: Voucher? = null
    for (i in 1..100) {
        activeVoucherInDb = repository.getVoucherByToken(tokenCode)
        if (activeVoucherInDb != null && activeVoucherInDb.status == "ACTIVE") break
        Thread.sleep(50)
    }
    
    assertNotNull(activeVoucherInDb)
    assertEquals("ACTIVE", activeVoucherInDb!!.status)
    assertNotNull(activeVoucherInDb.deviceId)
    
    // Verify it is tied
    val isTied = activeVoucherInDb.deviceId == viewModel1.deviceId
    assert(isTied)

    database.close()
  }
}

