package com.example.data.db

import androidx.room.*
import com.example.data.model.Voucher
import com.example.data.model.Router
import com.example.data.model.VpnConfig
import com.example.data.model.Session
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface VoucherDao {
    @Query("SELECT * FROM vouchers ORDER BY createdAt DESC")
    fun getAllVouchersFlow(): Flow<List<Voucher>>

    @Query("SELECT * FROM vouchers ORDER BY createdAt DESC")
    suspend fun getAllVouchers(): List<Voucher>

    @Query("SELECT * FROM vouchers WHERE token = :token LIMIT 1")
    suspend fun getVoucherByToken(token: String): Voucher?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVouchers(vouchers: List<Voucher>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: Voucher)

    @Update
    suspend fun updateVoucher(voucher: Voucher)

    @Delete
    suspend fun deleteVoucher(voucher: Voucher)

    @Query("DELETE FROM vouchers")
    suspend fun clearAllVouchers()
}

@Dao
interface RouterDao {
    @Query("SELECT * FROM routers ORDER BY id DESC")
    fun getAllRoutersFlow(): Flow<List<Router>>

    @Query("SELECT * FROM routers ORDER BY id DESC")
    suspend fun getAllRouters(): List<Router>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouter(router: Router)

    @Update
    suspend fun updateRouter(router: Router)

    @Delete
    suspend fun deleteRouter(router: Router)
}

@Dao
interface ConfigDao {
    @Query("SELECT * FROM vpn_configs WHERE `key` = :key LIMIT 1")
    suspend fun getConfigByKey(key: String): VpnConfig?

    @Query("SELECT * FROM vpn_configs")
    fun getAllConfigsFlow(): Flow<List<VpnConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: VpnConfig)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY connectionTime DESC")
    fun getAllSessionsFlow(): Flow<List<Session>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session)

    @Update
    suspend fun updateSession(session: Session)

    @Delete
    suspend fun deleteSession(session: Session)

    @Query("DELETE FROM sessions WHERE voucherToken = :voucherToken")
    suspend fun disconnectVoucherSessions(voucherToken: String)

    @Query("DELETE FROM sessions")
    suspend fun clearAllSessions()
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE referralCode = :referralCode LIMIT 1")
    suspend fun getUserByReferralCode(referralCode: String): User?

    @Query("SELECT * FROM users WHERE referredByCode = :referredByCode")
    suspend fun getUsersByReferredByCode(referredByCode: String): List<User>

    @Query("SELECT * FROM users ORDER BY referredCount DESC LIMIT :limit")
    suspend fun getTopReferralUsers(limit: Int): List<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

