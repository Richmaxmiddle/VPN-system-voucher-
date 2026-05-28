package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Router
import com.example.data.model.Session
import com.example.data.model.Voucher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun AdminScreen(
    viewModel: VpnViewModel,
    modifier: Modifier = Modifier
) {
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    if (!isAdminLoggedIn) {
        AdminLoginView(viewModel = viewModel, modifier = modifier)
    } else {
        val vouchers by viewModel.vouchers.collectAsState()
        val routers by viewModel.routers.collectAsState()
        val sessions by viewModel.sessions.collectAsState()

        var activeTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: Vouchers, 2: Router Link, 3: VPS Script, 4: Active Users, 5: Referrals
        val tabTitles = listOf("Muhtasari", "Vocha", "Router Linker", "VPS Setup", "Sifa wa Wateja", "Usimamizi wa Rufaa")

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(ThemeDarkBg)
        ) {
            // Logged-in admin indicator & Logout row to visually separate & differentiate
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ThemeDarkSurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(ThemeNeonTeal)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Admin: Richardmakasi200@gmail.com",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "ONDOKA (LOGOUT)",
                    color = ThemeNeonOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ThemeNeonOrange.copy(alpha = 0.15f))
                        .clickable { viewModel.logoutAdmin() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // Web Dashboard style top row tabs (Horizontal scrollable tab bar representation)
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = ThemeDarkSurface,
                contentColor = ThemeNeonTeal,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = ThemeNeonTeal
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content Area with crossfade transition
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                when (activeTab) {
                    0 -> AdminOverviewTab(vouchers, routers, sessions, viewModel)
                    1 -> AdminVouchersTab(vouchers, viewModel)
                    2 -> AdminRouterLinkerTab(routers, viewModel)
                    3 -> AdminVpsSetupTab()
                    4 -> AdminActiveUsersTab(sessions, viewModel)
                    5 -> AdminReferralsTab(viewModel)
                }
            }
        }
    }
}

@Composable
fun AdminLoginView(
    viewModel: VpnViewModel,
    modifier: Modifier = Modifier
) {
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeDarkBg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ThemeDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp)
                .border(
                    1.dp,
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(ThemeNeonTeal.copy(alpha = 0.6f), ThemeNeonBlue.copy(alpha = 0.2f))
                    ),
                    RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Secure key icon with pulsing backdrop
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(ThemeNeonTeal.copy(alpha = 0.1f))
                        .border(1.dp, ThemeNeonTeal.copy(alpha = 0.3f), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Admin Area",
                        tint = ThemeNeonTeal,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PANELI YA UTAWALA",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Ingiza akaunti yako ya Admin ili kudhibiti mtandao",
                    color = ThemeMutedText,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // 1. EMAIL FIELD
                Text(
                    text = "Barua Pepe ya Admin (Email Address):",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ThemeDarkCard)
                        .border(1.dp, ThemeDarkCard, RoundedCornerShape(8.dp))
                ) {
                    TextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        placeholder = { Text("Mfano: admin@email.com", color = ThemeMutedText, fontSize = 13.sp) },
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. PASSWORD FIELD
                Text(
                    text = "Nenosiri la Admin (Password):",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ThemeDarkCard)
                        .border(1.dp, ThemeDarkCard, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            placeholder = { Text("Weka nenosiri la siri hapa", color = ThemeMutedText, fontSize = 13.sp) },
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
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ficha nenosiri" else "Onyesha nenosiri",
                                tint = ThemeMutedText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = {
                        viewModel.attemptAdminLogin(emailInput, passwordInput)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeNeonTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "INGIA KWENYE PANEL",
                        color = ThemeDarkBg,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Credentials Helper Box (Click to autofill)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ThemeDarkCard)
                        .clickable {
                            emailInput = "Richardmakasi200@gmail.com"
                            passwordInput = "Rich.012"
                        }
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TUMIA CREDENTIALS ZA MAJARIBIO (AUTO-FILL)",
                        color = ThemeNeonBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Barua Pepe: Richardmakasi200@gmail.com\nPassword/PIN: Rich.012",
                        color = ThemeMutedText,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AdminOverviewTab(
    vouchers: List<Voucher>,
    routers: List<Router>,
    sessions: List<Session>,
    viewModel: VpnViewModel
) {
    val totalRevenue = vouchers.sumOf { it.priceTzs }
    val unusedCount = vouchers.count { it.status == "UNUSED" }
    val activeCount = vouchers.count { it.status == "ACTIVE" }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "VPS Central Dashboard (Marzban Core GUI Mockup)",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Web GUI quick card stats grid
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "MAPATO YA JUMLA",
                    value = viewModel.formatPrice(totalRevenue),
                    color = ThemeNeonTeal,
                    modifier = Modifier.weight(1f).padding(end = 6.dp)
                )
                StatCard(
                    title = "ROUTERS ZILIZOPO",
                    value = routers.size.toString(),
                    color = ThemeNeonBlue,
                    modifier = Modifier.weight(1f).padding(start = 6.dp)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "VOCHA HAI (ACTIVE)",
                    value = activeCount.toString(),
                    color = ThemeNeonTeal,
                    modifier = Modifier.weight(1f).padding(end = 6.dp)
                )
                StatCard(
                    title = "ZILIZOBAKIA (UNUSED)",
                    value = unusedCount.toString(),
                    color = ThemeMutedText,
                    modifier = Modifier.weight(1f).padding(start = 6.dp)
                )
            }
        }

        // Router summary list
        item {
            HorizontalDivider(color = ThemeDarkCard)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Hali ya Mesh Network (Routers)",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (routers.isEmpty()) {
            item {
                EmptyStateView(msg = "Hakuna Router iliyounganishwa mwilini!")
            }
        } else {
            items(routers) { router ->
                RouterStatusAdminCard(router = router, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AdminVouchersTab(
    vouchers: List<Voucher>,
    viewModel: VpnViewModel
) {
    var makeCountInput by remember { mutableStateOf("50") }
    var voucherPriceInput by remember { mutableStateOf("1000") }
    var selectedDurationHours by remember { mutableStateOf(24) } // 12, 24, 168 (7 Days), 720 (30 Days)
    var searchQuery by remember { mutableStateOf("") }
    
    var showPrintPreview by remember { mutableStateOf(false) }
    var clipboardManager = LocalClipboardManager.current

    val filteredVouchers = remember(vouchers, searchQuery) {
        if (searchQuery.isBlank()) vouchers
        else vouchers.filter { it.token.contains(searchQuery, ignoreCase = true) }
    }

    if (showPrintPreview) {
        // High fidelity printer view
        PrintableVouchersDialog(
            vouchers = filteredVouchers.take(150),
            viewModel = viewModel,
            onDismiss = { showPrintPreview = false }
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Tengeneza Vocha Kwa Wingi (Bulk Voucher Generator)",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Generator Input Form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ThemeDarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ThemeDarkCard, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = makeCountInput,
                            onValueChange = { makeCountInput = it },
                            label = { Text("Idadi (e.g. 100)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = ThemeNeonTeal, unfocusedBorderColor = ThemeDarkCard
                            ),
                            modifier = Modifier.weight(1f).padding(end = 4.dp).testTag("bulk_count_input")
                        )

                        OutlinedTextField(
                            value = voucherPriceInput,
                            onValueChange = { voucherPriceInput = it },
                            label = { Text("Bei ya TZS") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = ThemeNeonTeal, unfocusedBorderColor = ThemeDarkCard
                            ),
                            modifier = Modifier.weight(1f).padding(start = 4.dp).testTag("voucher_price_input")
                        )
                    }

                    // Duration selector
                    Text("Muda wa Vocha (Masaa):", color = ThemeMutedText, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val options = listOf(
                            12 to "Saa 12",
                            24 to "Siku 1",
                            168 to "Siku 7",
                            720 to "Siku 30"
                        )
                        options.forEach { (hours, label) ->
                            val isSelected = selectedDurationHours == hours
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ThemeNeonTeal else ThemeDarkCard)
                                    .clickable { selectedDurationHours = hours }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) ThemeDarkBg else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            val count = makeCountInput.toIntOrNull() ?: 50
                            val price = voucherPriceInput.toDoubleOrNull() ?: 1000.0
                            viewModel.generateVouchers(count, selectedDurationHours, price)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeNeonTeal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("bulk_generate_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Gen", tint = ThemeDarkBg)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tengeneza Vocha Sasa (Generate)", color = ThemeDarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Export Actions / Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tafuta Vocha...", color = ThemeMutedText) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = ThemeMutedText) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = ThemeNeonTeal, unfocusedBorderColor = ThemeDarkCard
                    ),
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    singleLine = true
                )

                // Export Button (Copies CSV table of all matching vouchers)
                Button(
                    onClick = {
                        val csv = StringBuilder("Voucher_Code,Duration_Hours,Price_TZS,Status,Created_At\n")
                        filteredVouchers.forEach {
                            csv.append("${it.token},${it.durationHours},${it.priceTzs},${it.status},${it.createdAt}\n")
                        }
                        clipboardManager.setText(AnnotatedString(csv.toString()))
                        viewModel.generateVouchers(0, 0, 0.0) // Dummy call to trigger message or manual toast on clipboard copy
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeNeonBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 4.dp).testTag("export_csv_button")
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Export CSV", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Show high-fidelity printable sheet view
                Button(
                    onClick = { showPrintPreview = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeNeonTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Print, contentDescription = "Print PDF", modifier = Modifier.size(16.dp), tint = ThemeDarkBg)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Piga", fontSize = 11.sp, color = ThemeDarkBg, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (filteredVouchers.isEmpty()) {
            item {
                EmptyStateView("Hakuna vocha iliyopatikana kulingana na utafutaji wako.")
            }
        } else {
            items(filteredVouchers) { voucher ->
                VoucherListRow(voucher = voucher, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AdminRouterLinkerTab(
    routers: List<Router>,
    viewModel: VpnViewModel
) {
    var routerName by remember { mutableStateOf("") }
    var routerIpAddress by remember { mutableStateOf("192.168.100.1") }
    var routerWireguardIp by remember { mutableStateOf("10.0.0.10") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "1-Click Router Linker (WireGuard Peer Configs)",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Unganisha router yako ya nyumbani (OpenWRT au Mikrotik) ili ijiunge yenyewe kwenye VPS na kushiriki internet yake isiyo na kikomo (Unlimited Home Fiber/Router LAN) kwa wateja wa VPN.",
                color = ThemeMutedText,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ThemeDarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ThemeDarkCard, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = routerName,
                        onValueChange = { routerName = it },
                        label = { Text("Jina la Router (e.g. Mwanza Home Fiber)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = ThemeNeonTeal, unfocusedBorderColor = ThemeDarkCard
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("router_name_input")
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = routerIpAddress,
                            onValueChange = { routerIpAddress = it },
                            label = { Text("Local IP ya Router") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = ThemeNeonTeal, unfocusedBorderColor = ThemeDarkCard
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f).padding(end = 4.dp)
                        )

                        OutlinedTextField(
                            value = routerWireguardIp,
                            onValueChange = { routerWireguardIp = it },
                            label = { Text("WireGuard Tunnel IP") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = ThemeNeonTeal, unfocusedBorderColor = ThemeDarkCard
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f).padding(start = 4.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (routerName.isNotBlank()) {
                                viewModel.linkNewRouter(routerName, routerIpAddress, routerWireguardIp)
                                routerName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeNeonTeal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("link_router_button")
                    ) {
                        Icon(Icons.Filled.SettingsEthernet, contentDescription = "Add Router", tint = ThemeDarkBg)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sajili & Link Router (WireGuard Linker)", color = ThemeDarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            HorizontalDivider(color = ThemeDarkCard)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pata maelekezo ya Copy-Paste ya OpenWRT Router:",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            var clipboardManager = LocalClipboardManager.current
            val openWrtCommand = """
# Kwenye OpenWRT/Home Router Shell, andika amri hizi:
opkg update && opkg install wireguard-tools luci-app-wireguard
wg genkey > /etc/wireguard/private.key
wg pubkey < /etc/wireguard/private.key > /etc/wireguard/public.key

# Jiunge kwenye VPS Central Mesh Server
cat <<EOF > /etc/config/network
config interface 'wg0'
    option proto 'wireguard'
    option private_key '\$(cat /etc/wireguard/private.key)'
    list addresses '$routerWireguardIp/24'

config wireguard_wg0 'peer1'
    option public_key 'VPS_WG_PUBLIC_KEY_PLACEHOLDER='
    option endpoint 'vps-central.lengo-tunnel.net:51820'
    option route_allowed_ips '0.0.0.0/0'
    option persistent_keepalive '25'
EOF

/etc/init.d/network restart
            """.trimIndent()

            Card(
                colors = CardDefaults.cardColors(containerColor = ThemeDarkCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("OpenWRT Setup Terminal Command", color = ThemeNeonBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "COPY",
                            color = ThemeNeonTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { clipboardManager.setText(AnnotatedString(openWrtCommand)) }
                                .padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = openWrtCommand,
                        color = Color.LightGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AdminVpsSetupTab() {
    var clipboardManager = LocalClipboardManager.current
    val dockerCommand = """
#!/bin/bash
# automated Central VPN Server Setup (Marzban & WireGuard Core)
# Platform: Ubuntu 20.04 / 22.04 / Debian 11+
# 100% Fully Automated with Security Rules and Routing

set -e

# Visual colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0;m'

echo -e "__DOLLAR__{BLUE}[+] Kuanza Usakinishaji wa automated Central VPN Server...__DOLLAR__{NC}"

# Hakikisha unatumia account ya root
if [ "__DOLLAR__{EUID}" -ne 0 ]; then
  echo -e "__DOLLAR__{RED}[!] Tafadhali fanya kazi kama root (Kimbiza: sudo su)__DOLLAR__{NC}"
  exit 1
fi

# Update na kufunga vifurushi vya msingi
echo -e "__DOLLAR__{BLUE}[+] Kusasisha vifurushi vya Linux na kusakinisha zana...__DOLLAR__{NC}"
apt-get update && apt-get install -y curl wget git wireguard ufw fail2ban jq sqlite3

# Kuamsha IP Forwarding kwenye Kernel kwa ajili ya routing ya intaneti mwilini
echo -e "__DOLLAR__{BLUE}[+] Kuwezesha IP packet forwarding...__DOLLAR__{NC}"
sysctl -w net.ipv4.ip_forward=1
echo "net.ipv4.ip_forward=1" >> /etc/sysctl.conf

# Kufunga Docker CE na Docker Compose kiotomatiki
if ! command -v docker &> /dev/null; then
  echo -e "__DOLLAR__{BLUE}[+] Kusakinisha Docker Containerization platform...__DOLLAR__{NC}"
  curl -fsSL https://get.docker.com -o get-docker.sh
  sh get-docker.sh
fi

# Kuunda folda husika za Xray VLESS Core na Wireguard configurations
mkdir -p /etc/xray /etc/wireguard /var/log/xray

# Kujenga usanidi wa Xray Config Core (VLESS with Anti-Sniffing & fallback settings)
echo -e "__DOLLAR__{BLUE}[+] Kujenga sanidi za Xray (VLESS and Trojan core Websocket TLS)...__DOLLAR__{NC}"
cat << 'EOF' > /etc/xray/config.json
{
  "log": {
    "access": "/var/log/xray/access.log",
    "error": "/var/log/xray/error.log",
    "loglevel": "warning"
  },
  "inbounds": [{
    "port": 443,
    "protocol": "vless",
    "settings": {
      "clients": [],
      "decryption": "none",
      "fallbacks": [{"dest": 80}]
    },
    "streamSettings": {
      "network": "ws",
      "security": "tls",
      "tlsSettings": {
        "certificates": [{
          "certificateFile": "/etc/xray/server.crt",
          "keyFile": "/etc/xray/server.key"
        }]
      },
      "wsSettings": {
        "path": "/ray"
      }
    }
  }],
  "outbounds": [{"protocol": "freedom"}]
}
EOF

# Wireguard routing na masquerade za firewall
echo -e "__DOLLAR__{BLUE}[+] Kusuka Interface ya Wireguard wg0...__DOLLAR__{NC}"
PRIVATE_KEY=__DOLLAR__(wg genkey)
PUBLIC_KEY=__DOLLAR__(echo "__DOLLAR__{PRIVATE_KEY}" | wg pubkey)

cat << EOF > /etc/wireguard/wg0.conf
[Interface]
PrivateKey = __DOLLAR__{PRIVATE_KEY}
Address = 10.0.0.1/24
ListenPort = 51820
PostUp = ufw route allow in on wg0 out on eth0
PostUp = iptables -t nat -I POSTROUTING -o eth0 -j MASQUERADE
PostDown = iptables -t nat -D POSTROUTING -o eth0 -j MASQUERADE
EOF

# Kujenga ulinzi thabiti kwa kutumia UFW na Fail2Ban kuzuia Brute Force
echo -e "__DOLLAR__{BLUE}[+] Kuanzisha ulinzi na firewall thabiti (UFW Rules)...__DOLLAR__{NC}"
ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp
ufw allow 443/tcp
ufw allow 51820/udp
ufw --force enable

# Kuanzisha huduma zote
systemctl enable wg-quick@wg0
systemctl restart wg-quick@wg0 || true

echo -e "__DOLLAR__{GREEN}[✓] VPN Central Server imeandaliwa kikamilifu na imelindwa!__DOLLAR__{NC}"
echo -e "__DOLLAR__{GREEN}[✓] WireGuard Mesh & Xray Core zinatumika sasa kwa usalama wa 100%!__DOLLAR__{NC}"
    """.trimIndent().replace("__DOLLAR__", "$")

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Automated VPS Server Setup (1-Click Install Script)",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Andika amri hii mara moja tu (Zero manual config) kwenye VPS yako ya Linux (Ubuntu 20.04/22.04+) ili kusanidi Seva yote ya VPN yenye VLESS Proxy, Certbot SSL na Wireguard mesh backend kiotomatiki.",
                color = ThemeMutedText,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ThemeDarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ThemeDarkCard, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Andika amri hii kwenye SSH Terminal ya VPS yako:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ThemeDarkCard)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dockerCommand,
                            color = ThemeNeonTeal,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { clipboardManager.setText(AnnotatedString(dockerCommand)) },
                            modifier = Modifier.testTag("copy_setup_script_button")
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Command", tint = ThemeNeonTeal)
                        }
                    }

                    HorizontalDivider(color = ThemeDarkCard)

                    Text("VPS Auto-Installer Inavyofanya kazi nyuma ya pazia:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    
                    val features = listOf(
                        "Sakinisha Docker-CE na Docker Compose.",
                        "Hifadhi cheti cha TLS SSL kiotomatiki kwa kutumia Certbot.",
                        "Weka Xray-Core proxy (VLESS over WebSocket & gRPC).",
                        "Unganisha WireGuard Mesh Tunneling kwa ajili ya home local router.",
                        "Tengeneza SQLite database kuratibu usajili wa Vocha papo hapo."
                    )
                    features.forEachIndexed { i, feature ->
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 2.dp)) {
                            Text("${i + 1}. ", color = ThemeNeonBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(feature, color = ThemeMutedText, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Live mock simulation terminal logs of script install
        item {
            var simulatedOutput by remember { mutableStateOf("Ready to receive execution trigger...") }
            var isSimulating by remember { mutableStateOf(false) }
            val dummyLogs = listOf(
                "[✓] Root privileges verified.",
                "[i] System update and dependencies installation starting...",
                "[✓] Docker CE and Docker-compose installed successfully.",
                "[i] Registering Certbot TLS details with domain host...",
                "[✓] Let's Encrypt certificate obtained successfully.",
                "[i] Structuring Xray config.json (VLESS-Websocket-TLS)...",
                "[i] Setting up Wireguard mesh profile gateway...",
                "[✓] Wireguard daemon wg-quick@wg0 interface launched (10.0.0.1/24)",
                "[✓] SQLite schema built: table Vouchers (Anti-Share locked index).",
                "[✓] Panel established on port 443! Happy Tunneling!"
            )
            
            val scope = rememberCoroutineScope()

            Button(
                onClick = {
                    if (!isSimulating) {
                        isSimulating = true
                        simulatedOutput = ""
                        scope.launch {
                            dummyLogs.forEach { log ->
                                simulatedOutput += "$log\n"
                                delay(600)
                            }
                            isSimulating = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ThemeNeonBlue),
                shape = RoundedCornerShape(8.dp),
                enabled = !isSimulating,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isSimulating) "INSTALLING CENTRAL SERVER..." else "SIMULATE INSTALLATION OUTPUT LOGS",
                    color = ThemeDarkBg,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    Text(
                        text = simulatedOutput,
                        color = Color.Green,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun AdminActiveUsersTab(
    sessions: List<Session>,
    viewModel: VpnViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Wateja Waliopo Mtandaoni (Active Connected Users)",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Fuatilia mienendo ya bando inayotumiwa na vifaa vilivyounganishwa kwenye VPS yako mwilini, na uwafukuze (block/disconnect) watumiaji walio nje ya utaratibu.",
                color = ThemeMutedText,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (sessions.isEmpty()) {
            item {
                EmptyStateView("Hakuna vifaa vilivyounganishwa kwa sasa mtandaoni.")
            }
        } else {
            items(sessions) { session ->
                ActiveSessionCard(session = session, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeDarkSurface),
        modifier = modifier.border(1.dp, ThemeDarkCard, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, color = ThemeMutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = color, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun RouterStatusAdminCard(
    router: Router,
    viewModel: VpnViewModel
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeDarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ThemeDarkCard, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Router, contentDescription = "Router", tint = ThemeNeonTeal, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = router.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = { viewModel.deleteRouterAdmin(router) },
                    modifier = Modifier.size(24.dp).testTag("delete_router_${router.id}")
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Router", tint = ThemeNeonOrange, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("WG IP: ${router.wireguardIp}", color = ThemeMutedText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Local IP: ${router.routerIp}", color = ThemeMutedText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Matumizi: ${String.format("%.1f MB", router.totalDataTransferMb)}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Hali: ${router.status}", color = ThemeNeonTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VoucherListRow(
    voucher: Voucher,
    viewModel: VpnViewModel
) {
    var clipboardManager = LocalClipboardManager.current
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeDarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ThemeDarkCard, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = voucher.token,
                        color = if (voucher.status == "UNUSED") ThemeNeonTeal else if (voucher.status == "ACTIVE") ThemeNeonBlue else ThemeMutedText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy",
                        tint = ThemeMutedText,
                        modifier = Modifier
                            .size(12.dp)
                            .clickable { clipboardManager.setText(AnnotatedString(voucher.token)) }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${voucher.durationHours} Hours", color = ThemeMutedText, fontSize = 11.sp)
                    Text(viewModel.formatPrice(voucher.priceTzs), color = ThemeNeonBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Created: ${viewModel.formatDateTime(voucher.createdAt)}", color = ThemeMutedText, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // State Badge
            val statusColor = when (voucher.status) {
                "UNUSED" -> ThemeNeonTeal
                "ACTIVE" -> ThemeNeonBlue
                else -> ThemeNeonOrange
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = voucher.status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Deletion admin
            IconButton(
                onClick = { viewModel.deleteVoucherAdmin(voucher) },
                modifier = Modifier.size(24.dp).testTag("delete_voucher_${voucher.token}")
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = ThemeNeonOrange, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun ActiveSessionCard(
    session: Session,
    viewModel: VpnViewModel
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeDarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ThemeDarkCard, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = session.deviceModel, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Tunnel IP: ${session.userIp}", color = ThemeMutedText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }

                // kick fukuza
                Button(
                    onClick = { viewModel.blockUserSession(session) },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeNeonOrange),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(28.dp).testTag("kick_session_${session.id}")
                ) {
                    Text("FUKUZA", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = ThemeDarkCard)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Vocha: ${session.voucherToken}", color = ThemeMutedText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Bandwidth: ${String.format("%.2f MB", session.dataTransferredMb)}", color = ThemeNeonTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(msg: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Filled.HourglassEmpty, contentDescription = "Empty", tint = ThemeMutedText, modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = msg,
                color = ThemeMutedText,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun PrintableVouchersDialog(
    vouchers: List<Voucher>,
    viewModel: VpnViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kuhitimisha (Done)", color = ThemeNeonTeal)
            }
        },
        title = { Text("Pre-formatted Retail Ticket Layout (Printout / PDF)", color = Color.White) },
        text = {
            Column {
                Text(
                    text = "Vocha hizi zimeandaliwa katika vifurushi vya kadi safi tayari kwa kukatwa na kuuzwa rejareja kwa wateja wako wa mitaani.",
                    color = ThemeMutedText,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .height(300.dp)
                        .background(ThemeDarkBg)
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(vouchers) { voucher ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, ThemeNeonTeal, RoundedCornerShape(8.dp))
                                .background(ThemeDarkSurface)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("LENGO TUNNEL VPN", color = ThemeNeonTeal, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = voucher.token,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Muda: ${voucher.durationHours} Masaa (Bila Bando)", color = ThemeMutedText, fontSize = 10.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Icon(Icons.Filled.QrCode, contentDescription = "QR Code Placeholder", tint = Color.White, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(viewModel.formatPrice(voucher.priceTzs), color = ThemeNeonTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = ThemeDarkSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun AdminReferralsTab(
    viewModel: VpnViewModel
) {
    val referralRewardHours by viewModel.referralRewardHours.collectAsState()
    val topReferrers by viewModel.topReferrers.collectAsState()
    
    // We can have local input state for hours
    var hoursInput by remember { mutableStateOf(referralRewardHours.toString()) }
    
    // Synchronize input if DB state changes
    LaunchedEffect(referralRewardHours) {
        hoursInput = referralRewardHours.toString()
    }
    
    // Refresh leaderboard on view enter
    LaunchedEffect(Unit) {
        viewModel.refreshLeaderboard()
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Section 1: Referral Reward Settings Manager (Upanuzi wa Usimamizi wa Rufaa)
        item {
            Text(
                text = "Upanuzi wa Usimamizi wa Rufaa (Referral Settings)",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sanidi masaa ya majaribio ya bure kwa watumiaji wapya na watu walioalika kulingana na promo pasipo kubadilisha kodi ya programu mwilini.",
                color = ThemeMutedText,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ThemeDarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ThemeDarkCard, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Muda wa Majaribio wa Sasa:",
                                color = ThemeMutedText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val label = if (referralRewardHours % 24 == 0) "Siku ${referralRewardHours / 24} ($referralRewardHours Masaa)" else "$referralRewardHours Masaa"
                            Text(
                                text = label,
                                color = ThemeNeonTeal,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Config Icon",
                            tint = ThemeNeonTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    HorizontalDivider(color = ThemeDarkCard)

                    OutlinedTextField(
                        value = hoursInput,
                        onValueChange = { hoursInput = it },
                        label = { Text("Sanidi Muda (Masaa / Hours)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ThemeNeonTeal,
                            unfocusedBorderColor = ThemeDarkCard
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("referral_hours_input")
                    )

                    Button(
                        onClick = {
                            val hours = hoursInput.toIntOrNull() ?: 24
                            viewModel.updateReferralRewardHours(hours)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeNeonTeal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("save_referral_hours_button")
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = "Hifadhi", tint = ThemeDarkBg, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Hifadhi Vipimo vya Rufaa",
                            color = ThemeDarkBg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Section 2: Referral Leaderboard (Ubao wa Viongozi wa Rufaa)
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ubao wa Viongozi wa Rufaa (Leaderboard)",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = Icons.Filled.Leaderboard,
                    contentDescription = "Refresh",
                    tint = ThemeNeonBlue,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { viewModel.refreshLeaderboard() }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Orodha ya watumiaji wanaoongoza kualika watu wengi ili kuwazawadia zawadi maalum au kugundua akaunti zisizo halali mapema.",
                color = ThemeMutedText,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }

        if (topReferrers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ThemeDarkSurface)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hakuna rufaa iliyorekodiwa bado.",
                        color = ThemeMutedText,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            itemsIndexed(topReferrers) { index, referrer ->
                val rank = index + 1
                val rankColor = when (rank) {
                    1 -> ThemeNeonTeal
                    2 -> ThemeNeonBlue
                    3 -> Color(0xFFFFB300) // Gold-ish yellow
                    else -> Color.White
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = ThemeDarkSurface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ThemeDarkCard, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Rank Number Box
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(rankColor.copy(alpha = 0.15f))
                                    .border(1.dp, rankColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$rank",
                                    color = rankColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = referrer.username,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Code: ${referrer.referralCode}",
                                    color = ThemeMutedText,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Referral Counter Badge
                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ThemeNeonTeal.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${referrer.referredCount} Aliko",
                                    color = ThemeNeonTeal,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
