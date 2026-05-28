package com.example.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Router
import com.example.data.model.Session
import com.example.data.model.Voucher
import com.example.data.model.User
import com.example.data.repository.VpnRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class VpnViewModel(
    private val context: Context,
    private val repository: VpnRepository
) : ViewModel() {

    private val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
        android.util.Log.e("VpnViewModel", "Unhandled exception in background coroutine", throwable)
    }

    // Unique Device ID for Anti-Share check (1 Voucher = 1 Device ID)
    val deviceId: String by lazy {
        try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "FALLBACK_DEV_ID_99"
        } catch (e: Exception) {
            "DEVICE_" + UUID.randomUUID().toString().take(12)
        }
    }

    // Default configuration settings
    private val _sniHost = MutableStateFlow("zero.data-tunnel.co.tz")
    val sniHost: StateFlow<String> = _sniHost.asStateFlow()

    private val _vlessEndpoint = MutableStateFlow("vps-central.lengo-tunnel.net:443")
    val vlessEndpoint: StateFlow<String> = _vlessEndpoint.asStateFlow()

    private val _vlessProtocol = MutableStateFlow("VLESS-WebSocket-TLS")
    val vlessProtocol: StateFlow<String> = _vlessProtocol.asStateFlow()

    // Screen State
    private val _currentScreen = MutableStateFlow("AUTH") // AUTH, USER_PORTAL, ADMIN_DASHBOARD
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _loggedInUser = MutableStateFlow<User?>(null)
    val loggedInUser: StateFlow<User?> = _loggedInUser.asStateFlow()

    private val _referredUsers = MutableStateFlow<List<User>>(emptyList())
    val referredUsers: StateFlow<List<User>> = _referredUsers.asStateFlow()

    private val _referralRewardHours = MutableStateFlow(24)
    val referralRewardHours: StateFlow<Int> = _referralRewardHours.asStateFlow()

    private val _topReferrers = MutableStateFlow<List<User>>(emptyList())
    val topReferrers: StateFlow<List<User>> = _topReferrers.asStateFlow()

    private val _appLanguage = MutableStateFlow("sw")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    fun setAppLanguage(lang: String) {
        _appLanguage.value = lang
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
            repository.setConfigValue("APP_LANGUAGE", lang)
        }
    }

    fun registerNewUser(username: String, passwordRaw: String, role: String, referralCodeUsed: String = "") {
        if (username.isBlank() || passwordRaw.isBlank()) {
            sendToast("Tafadhali jaza jina la mtumiaji na nenosiri!")
            return
        }
        val trimmedUsername = username.trim().lowercase()
        val trimmedPassword = passwordRaw.trim()

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
            val existing = repository.getUserByUsername(trimmedUsername)
            if (existing != null) {
                sendToast("Jina hili la mtumiaji tayari limechukuliwa!")
                return@launch
            }

            var referredBy: String? = null
            var referrerUser: User? = null

            if (referralCodeUsed.isNotBlank()) {
                val code = referralCodeUsed.trim().uppercase()
                val match = repository.getUserByReferralCode(code)
                if (match == null) {
                    sendToast("Namba ya rufaa haipo! Jisajili bila kuweka namba au ingiza iliyo sahihi.")
                    return@launch
                }
                referredBy = match.referralCode
                referrerUser = match
            }

            val cleanName = trimmedUsername.take(5).uppercase().filter { it.isLetterOrDigit() }
            val randomNumber = (1000..9999).random()
            val userReferralCode = "REF-$cleanName-$randomNumber"

            val newUser = User(
                username = trimmedUsername,
                passwordHash = trimmedPassword,
                role = role,
                referralCode = userReferralCode,
                referredByCode = referredBy,
                referredCount = 0
            )
            repository.insertUser(newUser)

            if (referrerUser != null) {
                // Increment referrer's referral count
                val updatedReferrer = referrerUser.copy(referredCount = referrerUser.referredCount + 1)
                repository.insertUser(updatedReferrer)

                // Get dynamic configured reward hours
                val rewardHours = _referralRewardHours.value

                // Create referral bonus voucher for referrer
                val refSuffix = (100..999).random()
                val referrerClean = referrerUser.username.take(5).uppercase().filter { it.isLetterOrDigit() }
                val referrerVoucher = Voucher(
                    token = "REF-BONUS-$referrerClean-$refSuffix",
                    durationHours = rewardHours,
                    priceTzs = 0.0,
                    status = "UNUSED",
                    createdAt = System.currentTimeMillis()
                )
                repository.insertVoucher(referrerVoucher)

                // Create reward welcome voucher for new user
                val wlSuffix = (100..999).random()
                val newUserVoucher = Voucher(
                    token = "WELCOME-$cleanName-$wlSuffix",
                    durationHours = rewardHours,
                    priceTzs = 0.0,
                    status = "UNUSED",
                    createdAt = System.currentTimeMillis()
                )
                repository.insertVoucher(newUserVoucher)

                val label = if (rewardHours % 24 == 0) {
                    val days = rewardHours / 24
                    "Siku $days"
                } else {
                    "Masaa $rewardHours"
                }
                sendToast("Jisajili limekamilika! Umepokea vocha ya bure ya $label kwa kutumia namba ya rufaa!")
                
                // Refresh leaderboard
                refreshLeaderboard()
            } else {
                sendToast("Akaunti ya $role $trimmedUsername imefunguliwa! Sasa unaweza kuingia.")
            }
        }
    }

    fun refreshLoggedInUser() {
        val current = _loggedInUser.value ?: return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
            val updated = repository.getUserByUsername(current.username)
            if (updated != null) {
                _loggedInUser.value = updated
                if (updated.referralCode.isNotBlank()) {
                    val list = repository.getUsersByReferredByCode(updated.referralCode)
                    _referredUsers.value = list
                }
            }
        }
    }

    fun attemptLogin(username: String, passwordRaw: String) {
        if (username.isBlank() || passwordRaw.isBlank()) {
            sendToast("Tafadhali jaza jina la mtumiaji na nenosiri!")
            return
        }
        val trimmedUsername = username.trim().lowercase()
        val trimmedPassword = passwordRaw.trim()

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
            val user = repository.getUserByUsername(trimmedUsername)
            if (user == null || user.passwordHash != trimmedPassword) {
                sendToast("Jina la mtumiaji au nenosiri si sahihi!")
                return@launch
            }
            _loggedInUser.value = user
            if (user.role == "ADMIN") {
                _isAdminLoggedIn.value = true
                _currentScreen.value = "ADMIN_DASHBOARD"
                sendToast("Ingizo la Admin $trimmedUsername limefanikiwa!")
            } else {
                _isAdminLoggedIn.value = false
                _currentScreen.value = "USER_PORTAL"
                refreshLoggedInUser()
                sendToast("Ingizo la Client $trimmedUsername limefanikiwa!")
            }
        }
    }

    fun logout() {
        _loggedInUser.value = null
        _isAdminLoggedIn.value = false
        _currentScreen.value = "AUTH"
        disconnectVpn()
        sendToast("Umetoka kwenye akaunti kwa usalama!")
    }

    fun attemptAdminLogin(email: String, password: String): Boolean {
        val emailTrimmed = email.trim()
        val pwdTrimmed = password.trim()
        if (emailTrimmed.equals("Richardmakasi200@gmail.com", ignoreCase = true) && pwdTrimmed == "Rich.012") {
            _isAdminLoggedIn.value = true
            val dummyAdmin = User(emailTrimmed.lowercase(), pwdTrimmed, "ADMIN")
            _loggedInUser.value = dummyAdmin
            _currentScreen.value = "ADMIN_DASHBOARD"
            sendToast("Ingizo la Admin limefanikiwa!")
            return true
        } else {
            sendToast("Barua pepe au Nenosiri si sahihi!")
            return false
        }
    }

    fun logoutAdmin() {
        logout()
    }

    // VPN Connection State
    private val _connectionState = MutableStateFlow("DISCONNECTED") // DISCONNECTED, CONNECTING, CONNECTED
    val connectionState: StateFlow<String> = _connectionState.asStateFlow()

    // Authenticated Voucher key in current session
    private val _activeVoucher = MutableStateFlow<Voucher?>(null)
    val activeVoucher: StateFlow<Voucher?> = _activeVoucher.asStateFlow()

    // Realtime speed meters
    private val _uploadSpeedKbps = MutableStateFlow(0.0)
    val uploadSpeedKbps: StateFlow<Double> = _uploadSpeedKbps.asStateFlow()

    private val _downloadSpeedKbps = MutableStateFlow(0.0)
    val downloadSpeedKbps: StateFlow<Double> = _downloadSpeedKbps.asStateFlow()

    private val _dataUsedInCurrentSessionMb = MutableStateFlow(0.0)
    val dataUsedInCurrentSessionMb: StateFlow<Double> = _dataUsedInCurrentSessionMb.asStateFlow()

    private val _pingMs = MutableStateFlow(0)
    val pingMs: StateFlow<Int> = _pingMs.asStateFlow()

    // Toast/Notification feedback state
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    // Flow listings
    val vouchers: StateFlow<List<Voucher>> = repository.allVouchers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routers: StateFlow<List<Router>> = repository.allRouters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<Session>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var initialVoucherDataMb = 0L
    private val dateTimeFormatter by lazy { SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()) }

    // Simulated worker handles connection speeds, timeouts, etc.
    private var connectionMonitorJob: Job? = null

    init {
        // Safely perform sequential database checks and seeding on Dispatchers.IO to prevent race conditions
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
            try {
                // 1. Retrieve config values from store; if missing insert default safely once
                val savedLang = repository.getConfigValue("APP_LANGUAGE", "sw")
                _appLanguage.value = savedLang
                repository.setConfigValue("APP_LANGUAGE", savedLang)

                val currentSni = repository.getConfigValue("SNI_BUG_HOST", _sniHost.value)
                _sniHost.value = currentSni
                repository.setConfigValue("SNI_BUG_HOST", currentSni)

                val currentEndpoint = repository.getConfigValue("VLESS_ENDPOINT", _vlessEndpoint.value)
                _vlessEndpoint.value = currentEndpoint
                repository.setConfigValue("VLESS_ENDPOINT", currentEndpoint)

                val currentProto = repository.getConfigValue("VLESS_PROTOCOL", _vlessProtocol.value)
                _vlessProtocol.value = currentProto
                repository.setConfigValue("VLESS_PROTOCOL", currentProto)

                val currentRewardHours = repository.getConfigValue("REFERRAL_REWARD_HOURS", "24").toIntOrNull() ?: 24
                _referralRewardHours.value = currentRewardHours
                repository.setConfigValue("REFERRAL_REWARD_HOURS", currentRewardHours.toString())

                refreshLeaderboard()

                // 2. Check and seed Routers sequentially
                val currentRouters = repository.getAllRoutersDirect()
                if (currentRouters.isEmpty()) {
                    repository.registerRouter("Dar Es Salaam Main Router", "192.168.100.1", "10.0.0.2")
                    repository.registerRouter("Arusha Branch Office", "192.168.1.1", "10.0.0.3")
                    repository.registerRouter("Mwanza Home Fiber Link", "192.168.88.1", "10.0.0.4")
                }

                // 3. Check and seed Vouchers sequentially
                val currentVouchers = repository.getAllVouchersDirect()
                if (currentVouchers.isEmpty()) {
                    repository.insertVoucher(
                        Voucher("LGO-DEMO-4433", 24, 2000.0, "UNUSED", System.currentTimeMillis() - 7200000)
                    )
                    repository.insertVoucher(
                        Voucher("LGO-TEST-9900", 12, 1000.0, "UNUSED", System.currentTimeMillis())
                    )
                    repository.insertVoucher(
                        Voucher("LGO-FREE-ZERO", 168, 5000.0, "UNUSED", System.currentTimeMillis())
                    )
                }

                // 4. Prepopulate users table with default accounts
                val countUsers = repository.getUserCount()
                if (countUsers == 0) {
                    repository.insertUser(User("richardmakasi200@gmail.com", "Rich.012", "ADMIN", "REF-RICHARD-4412"))
                    repository.insertUser(User("client@test.com", "client123", "CLIENT", "REF-CLIENT-9000"))
                }

                // Database is seeded correctly, no continuous collection needed as all writes occur internally through ViewModel
            } catch (e: Exception) {
                android.util.Log.e("VpnViewModel", "Error in sequential ViewModel/DB initialization: ${e.message}", e)
            }
        }
    }

    fun setScreen(screen: String) {
        _currentScreen.value = screen
    }

    // Toggle configuration updates from VPN Admin Panel
    fun updateBackgroundConfigs(sni: String, endpoint: String, protocol: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
            repository.setConfigValue("SNI_BUG_HOST", sni)
            repository.setConfigValue("VLESS_ENDPOINT", endpoint)
            repository.setConfigValue("VLESS_PROTOCOL", protocol)
            _sniHost.value = sni
            _vlessEndpoint.value = endpoint
            _vlessProtocol.value = protocol
            _toastMessage.emit("Cungulio mpya imesawazishwa upande wa Server!")
        }
    }

    // Connect to Zero-Data VPN Tunnel
    fun connectVpn(tokenInput: String) {
        if (tokenInput.isBlank()) {
            sendToast("Tafadhali ingiza namba ya vocha!")
            return
        }

        val token = tokenInput.trim().uppercase()
        _connectionState.value = "CONNECTING"
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
            delay(1500) // Simulate connection server handshake
            
            val voucher = repository.getVoucherByToken(token)
            if (voucher == null) {
                _connectionState.value = "DISCONNECTED"
                sendToast("Vocha uliyoingiza haipo! Thibitisha namba ya vocha.")
                return@launch
            }

            if (voucher.status == "EXPIRED") {
                _connectionState.value = "DISCONNECTED"
                sendToast("Vocha hii tayari imeisha muda wake wa matumizi!")
                return@launch
            }

            // Anti-Share Lock validation (1 voucher = 1 device)
            if (voucher.status == "ACTIVE" && voucher.deviceId != null && voucher.deviceId != deviceId) {
                _connectionState.value = "DISCONNECTED"
                val truncatedDevice = if (voucher.deviceId.length > 8) voucher.deviceId.take(8) + "..." else voucher.deviceId
                sendToast("Vocha hii inatumika kwenye kifaa kingine: device account tied ($truncatedDevice).")
                return@launch
            }

            // Activation logic
            val now = System.currentTimeMillis()
            var updatedVoucher = voucher

            if (voucher.status == "UNUSED") {
                val expiresAt = now + (voucher.durationHours * 3600 * 1000L)
                updatedVoucher = voucher.copy(
                    status = "ACTIVE",
                    activatedAt = now,
                    expiresAt = expiresAt,
                    deviceId = deviceId
                )
                repository.updateVoucher(updatedVoucher)
                sendToast("Vocha yako imeamilishwa sasa hivi kwa mafanikio!")
            }

            _activeVoucher.value = updatedVoucher
            _connectionState.value = "CONNECTED"
            _dataUsedInCurrentSessionMb.value = 0.0
            initialVoucherDataMb = updatedVoucher.totalDataMb
            
            // Build custom UI Session
            val session = Session(
                userIp = "10.0.0.${Random.nextInt(10, 250)}",
                voucherToken = token,
                connectionTime = System.currentTimeMillis(),
                currentUploadSpeedKb = 0.0,
                currentDownloadSpeedKb = 0.0,
                dataTransferredMb = 0.0,
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                status = "ACTIVE"
            )
            repository.insertSession(session)

            startSimulatedConnectionTicks()
        }
    }

    // Start periodic simulator task
    private fun startSimulatedConnectionTicks() {
        connectionMonitorJob?.cancel()
        connectionMonitorJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
            _pingMs.value = Random.nextInt(25, 60)
            var tickCount = 0
            while (_connectionState.value == "CONNECTED") {
                delay(1000)
                tickCount++
                
                // Fetch dynamic active voucher status to check limit
                val currentToken = _activeVoucher.value?.token ?: break
                val fetched = repository.getVoucherByToken(currentToken)
                
                if (fetched == null || fetched.status == "EXPIRED") {
                    disconnectVpn()
                    sendToast("Muda wa vocha yako umekwisha!")
                    break
                }

                val now = System.currentTimeMillis()
                if (fetched.expiresAt != null && now >= fetched.expiresAt) {
                    val expiredVoucher = fetched.copy(status = "EXPIRED")
                    repository.updateVoucher(expiredVoucher)
                    _activeVoucher.value = expiredVoucher
                    disconnectVpn()
                    sendToast("Matumizi yameisha! Tafadhali ingiza vocha nyingine.")
                    break
                }

                // Simulate fluctuation of speeds
                val upSpeed = Random.nextDouble(15.0, 310.0)
                val downSpeed = Random.nextDouble(180.0, 3200.0)
                _uploadSpeedKbps.value = upSpeed
                _downloadSpeedKbps.value = downSpeed

                val addedSessionDataMb = ((upSpeed + downSpeed) / 1024.0) / 8.0 // Approx MB in 1s
                _dataUsedInCurrentSessionMb.value += addedSessionDataMb

                // Update total usage on voucher database only every 10 seconds to optimize DB writes
                val updatedWithData = fetched.copy(
                    totalDataMb = initialVoucherDataMb + _dataUsedInCurrentSessionMb.value.toLong()
                )
                
                if (tickCount % 10 == 0) {
                    repository.updateVoucher(updatedWithData)
                }
                
                _activeVoucher.value = updatedWithData

                // Fluctuating ping
                _pingMs.value = (_pingMs.value + Random.nextInt(-3, 4)).coerceIn(12, 120)
            }
        }
    }

    // Disconnect manual or safe termination
    fun disconnectVpn() {
        connectionMonitorJob?.cancel()
        _connectionState.value = "DISCONNECTED"
        _uploadSpeedKbps.value = 0.0
        _downloadSpeedKbps.value = 0.0
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
            val activeToken = _activeVoucher.value?.token
            if (activeToken != null) {
                // Save final data usage on disconnect
                val fetched = repository.getVoucherByToken(activeToken)
                if (fetched != null) {
                    val finalVoucher = fetched.copy(
                        totalDataMb = initialVoucherDataMb + _dataUsedInCurrentSessionMb.value.toLong()
                    )
                    repository.updateVoucher(finalVoucher)
                }
                repository.disconnectSessionsForVoucher(activeToken)
            }
            _activeVoucher.value = null
        }
    }

    // Admin Dashboard Operations
    fun generateVouchers(count: Int, durationHours: Int, priceTzs: Double) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
            repository.generateBulkVouchers(count, durationHours, priceTzs)
            _toastMessage.emit("Imepiga chapa Vocha $count mpya kwa mafanikio!")
        }
    }

    fun deleteVoucherAdmin(voucher: Voucher) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
            // Disconnect if user is currently connected to it
            if (_activeVoucher.value?.token == voucher.token) {
                disconnectVpn()
            }
            repository.deleteVoucher(voucher)
            _toastMessage.emit("Vocha imefutwa!")
        }
    }

    fun clearAllVouchersAdmin() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
            disconnectVpn()
            repository.clearAllVouchers()
            _toastMessage.emit("Vocha zote zimesafishwa!")
        }
    }

    fun blockUserSession(session: Session) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
            repository.deleteSession(session)
            // Expire voucher
            val voucher = repository.getVoucherByToken(session.voucherToken)
            if (voucher != null) {
                val blockedVoucher = voucher.copy(status = "EXPIRED")
                repository.updateVoucher(blockedVoucher)
                if (_activeVoucher.value?.token == voucher.token) {
                    disconnectVpn()
                }
            }
            _toastMessage.emit("Mtumishi amezuiwa na kutolewa mtandaoni!")
        }
    }

    fun linkNewRouter(name: String, routerIp: String, wireguardIp: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
            repository.registerRouter(name, routerIp, wireguardIp)
            _toastMessage.emit("Router mpya imeunganishwa upande wa WireGuard Tunnel!")
        }
    }

    fun deleteRouterAdmin(router: Router) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
            repository.deleteRouter(router)
            _toastMessage.emit("Router imefutwa kwenye muunganisho wa Mesh!")
        }
    }

    fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }

    fun initiateMobilePurchase(phone: String, network: String, durationHours: Int, priceTzs: Double) {
        val sanitizedPhone = phone.replace(" ", "").trim()
        if (sanitizedPhone.length < 9) {
            sendToast("Nambari ya simu haijakamilika! Hakikisha umeiandika vizuri.")
            return
        }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
            _purchaseState.value = PurchaseState.Processing("Inaanzisha ombi la malipo (Push SDK) kwenda nambari ya simu '$sanitizedPhone' ($network)...")
            delay(2500)
            
            _purchaseState.value = PurchaseState.Processing("Tafadhali angalia screen ya simu yako na uingize PIN ya siri ya $network ili kukamilisha malipo ya TZS ${String.format(Locale.US, "%,.0f", priceTzs)}...")
            delay(4000)
            
            val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            val suffix = (1..4).map { chars[Random.nextInt(chars.length)] }.joinToString("")
            val netCode = when(network.lowercase()) {
                "vodacom" -> "MPESA"
                "tigo" -> "TPESA"
                "airtel" -> "AMONEY"
                "halotel" -> "HPESA"
                else -> "MOBI"
            }
            val generatedToken = "LGO-$netCode-$suffix"
            
            val newVoucher = Voucher(
                token = generatedToken,
                durationHours = durationHours,
                priceTzs = priceTzs,
                status = "UNUSED",
                createdAt = System.currentTimeMillis()
            )
            repository.insertVoucher(newVoucher)
            
            _purchaseState.value = PurchaseState.Success(generatedToken, priceTzs)
            sendToast("Malipo yamethibitishwa vizuri! Vocha yako ni $generatedToken")
        }
    }

    private fun sendToast(msg: String) {
        viewModelScope.launch(exceptionHandler) {
            _toastMessage.emit(msg)
        }
    }

    // Helper formatter for time remaining
    fun formatTimeRemaining(expiresAt: Long?): String {
        if (expiresAt == null) return "Muda haujaanzishwa"
        val remainingMs = expiresAt - System.currentTimeMillis()
        if (remainingMs <= 0) return "Imekwisha"
        
        val totalSecs = remainingMs / 1000
        val hours = (totalSecs / 3600).toInt()
        val mins = ((totalSecs % 3600) / 60).toInt()
        val secs = (totalSecs % 60).toInt()
        
        return String.format(Locale.US, "%02d:%02d:%02d zimebakia", hours, mins, secs)
    }

    fun formatPrice(value: Double): String {
        return "TZS " + String.format(Locale.US, "%,.0f", value)
    }

    fun formatDateTime(timestamp: Long): String {
        return try {
            dateTimeFormatter.format(Date(timestamp))
        } catch (e: Exception) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }

    fun refreshLeaderboard() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
            try {
                val list = repository.getTopReferralUsers(10)
                _topReferrers.value = list
            } catch (e: Exception) {
                android.util.Log.e("VpnViewModel", "Failed to load leaderboard: ${e.message}")
            }
        }
    }

    fun updateReferralRewardHours(hours: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + exceptionHandler) {
            try {
                repository.setConfigValue("REFERRAL_REWARD_HOURS", hours.toString())
                _referralRewardHours.value = hours
                sendToast("Muda wa zawadi umehifadhiwa: Masaa $hours kulingana na promo!")
            } catch (e: Exception) {
                sendToast("Hitilafu imetokea wakati wa kuhifadhi!")
            }
        }
    }
}

class VpnViewModelFactory(
    private val context: Context,
    private val repository: VpnRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VpnViewModel::class.java)) {
            return VpnViewModel(context, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed interface PurchaseState {
    object Idle : PurchaseState
    data class Processing(val statusMsg: String) : PurchaseState
    data class Success(val voucherToken: String, val amount: Double) : PurchaseState
    data class Error(val errorMsg: String) : PurchaseState
}
