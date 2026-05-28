package com.example.data.repository

import com.example.data.db.VoucherDao
import com.example.data.db.RouterDao
import com.example.data.db.ConfigDao
import com.example.data.db.SessionDao
import com.example.data.db.UserDao
import com.example.data.model.Voucher
import com.example.data.model.Router
import com.example.data.model.VpnConfig
import com.example.data.model.Session
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.random.Random

class VpnRepository(
    private val voucherDao: VoucherDao,
    private val routerDao: RouterDao,
    private val configDao: ConfigDao,
    private val sessionDao: SessionDao,
    private val userDao: UserDao
) {
    val allVouchers: Flow<List<Voucher>> = voucherDao.getAllVouchersFlow()
    val allRouters: Flow<List<Router>> = routerDao.getAllRoutersFlow()
    val allConfigs: Flow<List<VpnConfig>> = configDao.getAllConfigsFlow()
    val allSessions: Flow<List<Session>> = sessionDao.getAllSessionsFlow()

    suspend fun getAllVouchersDirect(): List<Voucher> = voucherDao.getAllVouchers()
    suspend fun getAllRoutersDirect(): List<Router> = routerDao.getAllRouters()

    suspend fun getVoucherByToken(token: String): Voucher? = voucherDao.getVoucherByToken(token)

    suspend fun insertVoucher(voucher: Voucher) = voucherDao.insertVoucher(voucher)

    suspend fun updateVoucher(voucher: Voucher) = voucherDao.updateVoucher(voucher)

    suspend fun deleteVoucher(voucher: Voucher) = voucherDao.deleteVoucher(voucher)

    suspend fun clearAllVouchers() = voucherDao.clearAllVouchers()

    // Bulk voucher generator (TZS currency support)
    suspend fun generateBulkVouchers(count: Int, durationHours: Int, priceTzs: Double): List<Voucher> {
        val voucherList = mutableListOf<Voucher>()
        for (i in 1..count) {
            val token = generateSecureToken()
            val voucher = Voucher(
                token = token,
                durationHours = durationHours,
                priceTzs = priceTzs,
                status = "UNUSED",
                createdAt = System.currentTimeMillis()
            )
            voucherList.add(voucher)
        }
        voucherDao.insertVouchers(voucherList)
        return voucherList
    }

    private fun generateSecureToken(): String {
        val prefix = "LGO"
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val part1 = (1..4).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        val part2 = (1..4).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        return "$prefix-$part1-$part2"
    }

    // Router linking operations
    suspend fun registerRouter(name: String, routerIp: String, wireguardIp: String): Router {
        // Automatically generate simple mock Wireguard Keys
        val mockPrivateKey = UUID.randomUUID().toString().replace("-", "").take(32) + "="
        val mockPublicKey = UUID.randomUUID().toString().replace("-", "").take(32) + "="
        
        val router = Router(
            name = name,
            status = "CONNECTED",
            wireguardIp = wireguardIp,
            routerIp = routerIp,
            totalDataTransferMb = Random.nextDouble(5.0, 150.0),
            activeUsers = Random.nextInt(0, 8),
            connectedSince = System.currentTimeMillis() - (Random.nextInt(60, 1440) * 60000L),
            privateKey = mockPrivateKey,
            publicKey = mockPublicKey
        )
        routerDao.insertRouter(router)
        return router
    }

    suspend fun updateRouter(router: Router) {
        routerDao.updateRouter(router)
    }

    suspend fun deleteRouter(router: Router) {
        routerDao.deleteRouter(router)
    }

    // Config helpers (e.g., Bug Host / SNI configuration)
    suspend fun setConfigValue(key: String, value: String) {
        configDao.setConfig(VpnConfig(key, value))
    }

    suspend fun getConfigValue(key: String, defaultValue: String): String {
        val config = configDao.getConfigByKey(key)
        return config?.value ?: defaultValue
    }

    // Session adjustments
    suspend fun insertSession(session: Session) = sessionDao.insertSession(session)

    suspend fun disconnectSessionsForVoucher(token: String) {
        sessionDao.disconnectVoucherSessions(token)
    }

    suspend fun clearSessions() = sessionDao.clearAllSessions()

    suspend fun deleteSession(session: Session) = sessionDao.deleteSession(session)

    suspend fun getUserByUsername(username: String): User? = userDao.getUserByUsername(username)

    suspend fun getUserByReferralCode(referralCode: String): User? = userDao.getUserByReferralCode(referralCode)

    suspend fun getUsersByReferredByCode(referredByCode: String): List<User> = userDao.getUsersByReferredByCode(referredByCode)

    suspend fun getTopReferralUsers(limit: Int): List<User> = userDao.getTopReferralUsers(limit)

    suspend fun insertUser(user: User) = userDao.insertUser(user)

    suspend fun getUserCount(): Int = userDao.getUserCount()
}
