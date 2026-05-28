package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vouchers")
data class Voucher(
    @PrimaryKey
    val token: String,
    val durationHours: Int,
    val priceTzs: Double,
    val status: String, // "UNUSED", "ACTIVE", "EXPIRED"
    val createdAt: Long = System.currentTimeMillis(),
    val activatedAt: Long? = null,
    val expiresAt: Long? = null,
    val deviceId: String? = null,
    val totalDataMb: Long = 0L // cumulative usage
)

@Entity(tableName = "routers")
data class Router(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val status: String, // "CONNECTED", "DISCONNECTED"
    val wireguardIp: String,
    val routerIp: String,
    val totalDataTransferMb: Double,
    val activeUsers: Int,
    val connectedSince: Long = System.currentTimeMillis(),
    val privateKey: String = "",
    val publicKey: String = ""
)

@Entity(tableName = "vpn_configs")
data class VpnConfig(
    @PrimaryKey
    val key: String,
    val value: String
)

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userIp: String,
    val voucherToken: String,
    val connectionTime: Long = System.currentTimeMillis(),
    val currentUploadSpeedKb: Double,
    val currentDownloadSpeedKb: Double,
    val dataTransferredMb: Double,
    val deviceModel: String,
    val status: String // "ACTIVE", "DISCONNECTED"
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val username: String,
    val passwordHash: String,
    val role: String, // "CLIENT" or "ADMIN"
    val referralCode: String = "",
    val referredByCode: String? = null,
    val referredCount: Int = 0
)

