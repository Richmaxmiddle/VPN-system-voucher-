package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.data.model.User
import com.example.data.model.Voucher
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// High-fidelity Neon Palette
val ThemeDarkBg = Color(0xFF0F111A)
val ThemeDarkSurface = Color(0xFF171926)
val ThemeDarkCard = Color(0xFF1F2235)
val ThemeNeonTeal = Color(0xFF00FFC4)
val ThemeNeonBlue = Color(0xFF00BFFF)
val ThemeNeonOrange = Color(0xFFFF5E00)
val ThemeMutedText = Color(0xFF8F93B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientScreen(
    viewModel: VpnViewModel,
    modifier: Modifier = Modifier
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val activeVoucher by viewModel.activeVoucher.collectAsState()
    val sniHost by viewModel.sniHost.collectAsState()
    val vlessEndpoint by viewModel.vlessEndpoint.collectAsState()
    val vlessProtocol by viewModel.vlessProtocol.collectAsState()
    val uploadSpeed by viewModel.uploadSpeedKbps.collectAsState()
    val downloadSpeed by viewModel.downloadSpeedKbps.collectAsState()
    val dataUsed by viewModel.dataUsedInCurrentSessionMb.collectAsState()
    val pingMs by viewModel.pingMs.collectAsState()

    var showConfigEditor by remember { mutableStateOf(false) }
    var voucherInput by remember { mutableStateOf("") }
    
    // Ticking for countdown duration of remaining voucher time
    var remainingTimeText by remember { mutableStateOf("Muda haujaanzishwa") }

    LaunchedEffect(connectionState) {
        if (connectionState == "CONNECTED") {
            while (true) {
                remainingTimeText = viewModel.formatTimeRemaining(viewModel.activeVoucher.value?.expiresAt)
                delay(1000)
            }
        } else {
            remainingTimeText = "Muda haujaanzishwa"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeDarkBg)
            .padding(horizontal = 16.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Large Cybermatic Connection Hub Glow Header
        ConnectionStatusCard(
            connectionState = connectionState,
            remainingTime = remainingTimeText,
            pingMs = pingMs,
            dataUsed = dataUsed,
            activeVoucher = activeVoucher
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Interactive Tachometer Gauge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
            contentAlignment = Alignment.Center
        ) {
            TachometerSpeedometer(
                speedKbps = downloadSpeed,
                maxSpeedKbps = 3200.0,
                color = ThemeNeonTeal
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (downloadSpeed > 1024) String.format("%.2f", downloadSpeed/1024) else String.format("%.0f", downloadSpeed),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (downloadSpeed > 1024) "MB/s" else "KB/s",
                    color = ThemeNeonTeal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dual Speed Widget Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SpeedMiniCard(
                title = "UP SPEED",
                value = if (uploadSpeed > 1024) String.format("%.1f MB/s", uploadSpeed/1024) else String.format("%.0f KB/s", uploadSpeed),
                icon = Icons.Filled.CloudUpload,
                color = ThemeNeonBlue,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            SpeedMiniCard(
                title = "DATA USED",
                value = String.format("%.2f MB", dataUsed),
                icon = Icons.Filled.DataUsage,
                color = ThemeNeonOrange,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Single Entry Ticket Window (Connection controls area)
        AnimatedVisibility(
            visible = connectionState == "DISCONNECTED",
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ThemeDarkSurface)
                        .border(1.dp, ThemeDarkCard, RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ConfirmationNumber,
                        contentDescription = "Voucher",
                        tint = ThemeNeonTeal,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    
                    TextField(
                        value = voucherInput,
                        onValueChange = { voucherInput = it },
                        placeholder = { Text(stringResource("ingiza_vocha_token", viewModel), color = ThemeMutedText) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("voucher_input_field"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                    )

                    // Autopaste convenience
                    var clipboard = LocalClipboardManager.current
                    IconButton(
                        onClick = {
                            val text = clipboard.getText()?.text
                            if (!text.isNullOrBlank()) {
                                voucherInput = text
                            }
                        }
                    ) {
                        Icon(Icons.Filled.ContentPaste, contentDescription = "Paste", tint = ThemeNeonBlue)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.connectVpn(voucherInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeNeonTeal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("connect_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PowerSettingsNew, contentDescription = "Connect", tint = ThemeDarkBg)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource("anza_muunganisho", viewModel),
                            color = ThemeDarkBg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = connectionState != "DISCONNECTED",
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Button(
                onClick = { viewModel.disconnectVpn() },
                colors = ButtonDefaults.buttonColors(containerColor = ThemeNeonOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("disconnect_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Stop, contentDescription = "Disconnect")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource("vunja_muunganisho", viewModel),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        PurchaseVoucherCard(
            viewModel = viewModel,
            onApplyToken = { voucherInput = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        ReferralSystemCard(
            viewModel = viewModel,
            onApplyToken = { voucherInput = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Hidden / Dropdown Developer Bug Host/SNI Configuration Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ThemeDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ThemeDarkCard, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showConfigEditor = !showConfigEditor },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.BugReport, contentDescription = "Engine Settings", tint = ThemeNeonTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mazingira ya Siri (SNI & Bug Host)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Icon(
                        imageVector = if (showConfigEditor) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "Toggle",
                        tint = ThemeMutedText
                    )
                }

                AnimatedVisibility(visible = showConfigEditor) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            text = "Hapa ni maalum kwa ajili ya kuweka Bug Host/SNI ya mtandao wako wa simu ili VPN ipite bila kukata bando la simu (Zero-Rating Bypass).",
                            color = ThemeMutedText,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        var currentSni by remember(sniHost) { mutableStateOf(sniHost) }
                        var currentEndpoint by remember(vlessEndpoint) { mutableStateOf(vlessEndpoint) }
                        var currentProto by remember(vlessProtocol) { mutableStateOf(vlessProtocol) }

                        OutlinedTextField(
                            value = currentSni,
                            onValueChange = { currentSni = it },
                            label = { Text("SNI/Bug Host (Pita Bila Bando)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ThemeNeonTeal,
                                unfocusedBorderColor = ThemeDarkCard
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("sni_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentEndpoint,
                            onValueChange = { currentEndpoint = it },
                            label = { Text("Tunnel Server Endpoint") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ThemeNeonTeal,
                                unfocusedBorderColor = ThemeDarkCard
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        var expandedProto by remember { mutableStateOf(false) }
                        Box {
                            OutlinedTextField(
                                value = currentProto,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Itifaki Ya Mtandao") },
                                trailingIcon = {
                                    IconButton(onClick = { expandedProto = true }) {
                                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown")
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = ThemeNeonTeal,
                                    unfocusedBorderColor = ThemeDarkCard
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = expandedProto,
                                onDismissRequest = { expandedProto = false },
                                modifier = Modifier.background(ThemeDarkSurface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("VLESS-WebSocket-TLS", color = Color.White) },
                                    onClick = { currentProto = "VLESS-WebSocket-TLS"; expandedProto = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("VLESS-gRPC-TLS", color = Color.White) },
                                    onClick = { currentProto = "VLESS-gRPC-TLS"; expandedProto = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("WireGuard Tunnel (Local Mesh)", color = Color.White) },
                                    onClick = { currentProto = "WireGuard Tunnel (Local Mesh)"; expandedProto = false }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.updateBackgroundConfigs(currentSni, currentEndpoint, currentProto)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeNeonBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Hifadhi Config", color = ThemeDarkBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Footnote indicating standard protection and isolation rules
        Text(
            text = "Kifaa chako kimeunganishwa kwenye mtandao uliotengwa na LAN ya Nyumbani (Traffic Isolated). Inayotumiwa: Mtandao Salama wa Router.",
            color = ThemeMutedText,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
        )
    }
}

@Composable
fun ConnectionStatusCard(
    connectionState: String,
    remainingTime: String,
    pingMs: Int,
    dataUsed: Double,
    activeVoucher: Voucher? = null
) {
    val transition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val statusColor = when (connectionState) {
        "CONNECTED" -> ThemeNeonTeal
        "CONNECTING" -> ThemeNeonBlue
        else -> ThemeNeonOrange
    }

    val statusText = when (connectionState) {
        "CONNECTED" -> "IMEUNGANISHWA (CONNECTED)"
        "CONNECTING" -> "INAUNGANISHA... (CONNECTING)"
        else -> "HAIJAUNGANISHWA (DISCONNECTED)"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeDarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(statusColor.copy(alpha = 0.5f), ThemeDarkCard)),
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Pulsating status dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = if (connectionState != "DISCONNECTED") pulseAlpha else 1f))
                )
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = statusText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = ThemeDarkCard, thickness = 1.dp)

            Spacer(modifier = Modifier.height(12.dp))

            // Time ticker display
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccessTime, contentDescription = "Time Left", tint = ThemeNeonTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Muda wa Vocha:", color = ThemeMutedText, fontSize = 13.sp)
                }
                Text(
                    text = remainingTime,
                    color = if (connectionState == "CONNECTED") ThemeNeonTeal else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.testTag("remaining_time_label")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ping display
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.NetworkCheck, contentDescription = "Signal Ping", tint = ThemeNeonBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ping (Ucheleweshaji):", color = ThemeMutedText, fontSize = 13.sp)
                }
                Text(
                    text = if (connectionState == "CONNECTED") "$pingMs ms" else "--- ms",
                    color = if (connectionState == "CONNECTED") ThemeNeonBlue else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Device Lock security row
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, contentDescription = "Device Lock Protection", tint = ThemeNeonTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ulinzi wa Kifaa (Device-Lock):", color = ThemeMutedText, fontSize = 13.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (activeVoucher != null) ThemeNeonTeal.copy(alpha = 0.15f)
                            else ThemeMutedText.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (activeVoucher != null) "IMEFUNGWA (TIED)" else "IMELINDWA",
                        color = if (activeVoucher != null) ThemeNeonTeal else ThemeMutedText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SpeedMiniCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeDarkSurface),
        modifier = modifier.border(1.dp, ThemeDarkCard, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(text = title, color = ThemeMutedText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun TachometerSpeedometer(
    speedKbps: Double,
    maxSpeedKbps: Double,
    color: Color
) {
    val animatedProgress = animateFloatAsState(
        targetValue = (speedKbps / maxSpeedKbps).coerceIn(0.0, 1.0).toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "SpeedDial"
    )

    Box(
        modifier = Modifier
            .size(150.dp)
            .drawBehind {
                if (size.width > 0f) {
                    val strokeWidth = 10.dp.toPx()
                    val radius = ((size.width - strokeWidth) / 2).coerceAtLeast(0f)
                    
                    // Draw muted background circle arc
                    drawArc(
                        color = ThemeDarkCard,
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Draw glowing speed arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            0.0f to color.copy(alpha = 0.3f),
                            0.5f to color,
                            1.0f to ThemeNeonBlue
                        ),
                        startAngle = 140f,
                        sweepAngle = 260f * animatedProgress.value,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    
                    // Draw tick lines
                    val numTicks = 6
                    val center = size.width / 2
                    for (i in 0 until numTicks) {
                        val angleDeg = 140f + (260f * i / (numTicks - 1))
                        val angleRad = angleDeg * PI / 180f
                        val innerR = (radius - 12.dp.toPx()).coerceAtLeast(0f)
                        val outerR = (radius - 4.dp.toPx()).coerceAtLeast(0f)
                        
                        val innerX = center + innerR * cos(angleRad).toFloat()
                        val innerY = center + innerR * sin(angleRad).toFloat()
                        val outerX = center + outerR * cos(angleRad).toFloat()
                        val outerY = center + outerR * sin(angleRad).toFloat()
                        
                        drawLine(
                            color = if (animatedProgress.value >= (i.toFloat() / (numTicks - 1))) color else ThemeMutedText,
                            start = androidx.compose.ui.geometry.Offset(innerX, innerY),
                            end = androidx.compose.ui.geometry.Offset(outerX, outerY),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }
    )
}

@Composable
fun PurchaseVoucherCard(
    viewModel: VpnViewModel,
    onApplyToken: (String) -> Unit
) {
    val purchaseState by viewModel.purchaseState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Form fields
    var phoneInput by remember { mutableStateOf("") }
    var selectedNetwork by remember { mutableStateOf("Vodacom") }
    
    val isEn = viewModel.appLanguage.value == "en"
    val packages = listOf(
        Triple(if (isEn) "3 Hours" else "Saa 3", 3, 500.0),
        Triple(if (isEn) "1 Day" else "Siku 1", 24, 1000.0),
        Triple(if (isEn) "1 Week" else "Wiki 1", 168, 5000.0),
        Triple(if (isEn) "30 Days" else "Siku 30", 720, 15000.0)
    )
    var selectedPackageIndex by remember { mutableStateOf(1) } // Default daily package
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeDarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(ThemeNeonTeal.copy(alpha = 0.5f), ThemeDarkCard)),
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "Buy Voucher",
                    tint = ThemeNeonTeal,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEn) "BUY INTERNET VOUCHER" else "NUNUA VOCHA (BUY VOUCHER)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            
            Text(
                text = if (isEn) "Pay instantly with all mobile networks in Tanzania (M-Pesa, Airtel Money, Tigo Pesa, Halopesa) to get your high-speed internet voucher." else "Lipia papo hapo kwa mitandao yote ya simu Tanzania (M-Pesa, Airtel Money, Tigo Pesa, Halopesa) kupata Vocha.",
                color = ThemeMutedText,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            
            HorizontalDivider(color = ThemeDarkCard, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))
            
            when (val state = purchaseState) {
                is PurchaseState.Idle -> {
                    // 1. SELECT PACKAGE
                    Text(
                        text = if (isEn) "1. Choose Package:" else "1. Chagua Kifurushi:",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        packages.forEachIndexed { index, (label, hours, price) ->
                            val isSelected = selectedPackageIndex == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ThemeNeonTeal.copy(alpha = 0.15f) else ThemeDarkCard)
                                    .border(
                                        1.dp,
                                        if (isSelected) ThemeNeonTeal else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedPackageIndex = index }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) ThemeNeonTeal else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${price.toInt()} TZS",
                                        color = if (isSelected) Color.White else ThemeMutedText,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 2. SELECT NETWORK
                    Text(
                        text = if (isEn) "2. Select Network:" else "2. Chagua Mtandao:",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    val networks = listOf(
                        "Vodacom" to Color(0xFFE6211E),
                        "Tigo" to Color(0xFF00BFFF),
                        "Airtel" to Color(0xFFFF5E00),
                        "Halotel" to Color(0xFFFFBB00)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        networks.forEach { (name, brandColor) ->
                            val isSelected = selectedNetwork == name
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) brandColor.copy(alpha = 0.2f) else ThemeDarkCard)
                                    .border(
                                        1.dp,
                                        if (isSelected) brandColor else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedNetwork = name }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name,
                                    color = if (isSelected) Color.White else ThemeMutedText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 3. PHONE NUMBER
                    Text(
                        text = if (isEn) "3. Enter Phone Number for Push Payment:" else "3. Weka namba ya Simu upokee Lipia Push:",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ThemeDarkCard)
                            .border(1.dp, ThemeDarkCard, RoundedCornerShape(8.dp))
                    ) {
                        TextField(
                            value = phoneInput,
                            onValueChange = { input ->
                                    if (input.all { it.isDigit() } && input.length <= 12) {
                                        phoneInput = input
                                    }
                            },
                            placeholder = { Text(if (isEn) "Example: 0712345678" else "Mfano: 0712345678", color = ThemeMutedText) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val currentPkg = packages[selectedPackageIndex]
                    Button(
                        onClick = {
                            viewModel.initiateMobilePurchase(
                                phone = phoneInput,
                                network = selectedNetwork,
                                durationHours = currentPkg.second,
                                priceTzs = currentPkg.third
                            )
                        },
                        enabled = phoneInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ThemeNeonTeal,
                            disabledContainerColor = ThemeDarkCard
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "LIPIA TZS ${currentPkg.third.toInt()} VIA ${selectedNetwork.uppercase()}",
                            color = if (phoneInput.isNotBlank()) ThemeDarkBg else ThemeMutedText,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                is PurchaseState.Processing -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = ThemeNeonTeal,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.statusMsg,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tafadhali usiondoke kwenye skrini hii wakati malipo yakiprosesika...",
                            color = ThemeMutedText,
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp
                        )
                    }
                }
                is PurchaseState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(ThemeNeonTeal.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Succeeded",
                                tint = ThemeNeonTeal,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "MALIPO YAMEFANIKIWA!",
                            color = ThemeNeonTeal,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        
                        Text(
                            text = "Vocha yako imetengenezwa. Nakili hapa chini:",
                            color = ThemeMutedText,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )
                        
                        // Display Box with Copy action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ThemeDarkCard)
                                .border(1.dp, ThemeNeonTeal.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.voucherToken,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                            
                            val clipboard = LocalClipboardManager.current
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(state.voucherToken))
                                    android.widget.Toast.makeText(context, "Vocha imeiga chapa kwenye clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copy Voucher",
                                    tint = ThemeNeonBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                onApplyToken(state.voucherToken)
                                viewModel.resetPurchaseState()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeNeonBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text(
                                text = "KOPYA & WEKA KWENYE UVIEW",
                                color = ThemeDarkBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                is PurchaseState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Error",
                            tint = ThemeNeonOrange,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.errorMsg,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.resetPurchaseState() },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeNeonOrange),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("JARIBU TENA", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReferralSystemCard(
    viewModel: VpnViewModel,
    onApplyToken: (String) -> Unit
) {
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val vouchers by viewModel.vouchers.collectAsState()
    val referredUsers by viewModel.referredUsers.collectAsState()
    val referralRewardHours by viewModel.referralRewardHours.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Trigger user refresh to keep referred counter updated
    LaunchedEffect(Unit) {
        viewModel.refreshLoggedInUser()
    }

    val user = loggedInUser ?: return
    val cleanUsername = user.username.take(5).uppercase().filter { it.isLetterOrDigit() }
    val myBonusVouchers = vouchers.filter {
        it.status == "UNUSED" && (it.token.startsWith("WELCOME-$cleanUsername") || it.token.startsWith("REF-BONUS-$cleanUsername"))
    }

    val rewardLabel = if (referralRewardHours % 24 == 0) {
        "Siku ${referralRewardHours / 24}"
    } else {
        "Masaa $referralRewardHours"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeDarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, ThemeDarkCard, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CardGiftcard,
                    contentDescription = "Referral Gift",
                    tint = ThemeNeonTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Alika Marafiki & Pata Zawadi",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Shiriki namba yako ya rufaa na rafiki yako. Mkijiunga nyote wawili, kila mmoja wenu atapokea vocha ya bure ya $rewardLabel ya internet!",
                color = ThemeMutedText,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Code Display Card Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ThemeDarkBg)
                    .border(1.dp, ThemeDarkCard, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NAMBA YAKO YA RUFAA:",
                            color = ThemeMutedText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.referralCode.ifEmpty { "REF-${cleanUsername}-GENERATING" },
                            color = ThemeNeonTeal,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val refCode = user.referralCode.ifEmpty { "REF-${cleanUsername}" }
                            clipboardManager.setText(AnnotatedString(refCode))
                            android.widget.Toast.makeText(context, "Namba yako ya rufaa imenakiliwa!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeDarkCard),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("copy_referral_code")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy Code",
                            tint = ThemeNeonTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val refCode = user.referralCode.ifEmpty { "REF-${cleanUsername}" }
                            val shareMessage = "Jiunge na Zero-Data VPN ukitumia namba yangu ya rufaa: $refCode ili kupata intaneti ya bure ya $rewardLabel ya internet pale unapojisajili! Pakua sasa sote tufurahie intaneti ya kupaa!"
                            
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                                }
                                val chooser = android.content.Intent.createChooser(intent, "Shiriki Namba ya Rufaa").apply {
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(chooser)
                            } catch (e: Exception) {
                                // Fallback: copy entire invite message to clipboard so user can paste manually
                                clipboardManager.setText(AnnotatedString(shareMessage))
                                android.widget.Toast.makeText(
                                    context, 
                                    "Hakuna App ya kushiriki iliyopatikana. Ujumbe wa mwaliko umenakiliwa kwenye clipboard!", 
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeNeonTeal),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(36.dp)
                            .testTag("fast_share_referral")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Fast Share",
                            tint = ThemeDarkBg,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kushiriki Haraka", color = ThemeDarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stat Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Marafiki Waliojisajili: ",
                    color = ThemeMutedText,
                    fontSize = 13.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.People,
                        contentDescription = "Referred Friends Count",
                        tint = ThemeNeonBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${user.referredCount} marafiki",
                        color = ThemeNeonBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Registry list of friends
            Text(
                text = "Orodha ya Marafiki Ulioalika",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (referredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ThemeDarkBg.copy(alpha = 0.5f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hakuna rafiki aliyejiunga bado kupitia namba yako. Waalike sote tufurahie bando!",
                        color = ThemeMutedText,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ThemeDarkBg)
                        .border(1.dp, ThemeDarkCard, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    referredUsers.forEach { refUser ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "New Friend",
                                    tint = ThemeNeonTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = refUser.username,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ThemeNeonTeal.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Sahihi • Zawadi $rewardLabel",
                                    color = ThemeNeonTeal,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Earned vouchers list
            Text(
                text = "Zawadi Zako za Bure (${myBonusVouchers.size})",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (myBonusVouchers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ThemeDarkBg.copy(alpha = 0.5f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Huna zawadi bado. Mualike rafiki kwa kutumia namba yako kupokea vocha za bure ya $rewardLabel!",
                        color = ThemeMutedText,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    myBonusVouchers.forEach { voucher ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ThemeDarkBg)
                                .border(1.dp, ThemeDarkCard, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = voucher.token,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = if (voucher.durationHours % 24 == 0) "Siku ${voucher.durationHours / 24} Bure" else "${voucher.durationHours} Masaa Bure",
                                    color = ThemeNeonTeal,
                                    fontSize = 10.sp
                                )
                            }

                            Button(
                                onClick = { onApplyToken(voucher.token) },
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeNeonTeal),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(28.dp).testTag("activate_bonus_${voucher.token}")
                            ) {
                                Text(
                                    text = "AMILISHA",
                                    color = ThemeDarkBg,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
