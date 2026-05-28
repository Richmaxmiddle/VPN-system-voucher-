package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.db.AppDatabase
import com.example.data.repository.VpnRepository
import com.example.data.model.User
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge to Edge compatibility
        enableEdgeToEdge()

        // Room and MVVM core initialization
        val database = AppDatabase.getDatabase(this)
        val repository = VpnRepository(
            voucherDao = database.voucherDao(),
            routerDao = database.routerDao(),
            configDao = database.configDao(),
            sessionDao = database.sessionDao(),
            userDao = database.userDao()
        )
        val viewModelFactory = VpnViewModelFactory(applicationContext, repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[VpnViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val currentScreen by viewModel.currentScreen.collectAsState()
                val loggedInUser by viewModel.loggedInUser.collectAsState()

                val currentContext by rememberUpdatedState(context)
                // Register observer safely using standard LaunchedEffect keyed on viewModel
                LaunchedEffect(viewModel) {
                    viewModel.toastMessage.collect { msg ->
                        Toast.makeText(currentContext, msg, Toast.LENGTH_LONG).show()
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ThemeDarkBg),
                    topBar = {
                        if (loggedInUser != null) {
                            VpnCoreTopAppBar(
                                viewModel = viewModel,
                                currentScreen = currentScreen,
                                loggedInUser = loggedInUser,
                                onScreenSelected = { viewModel.setScreen(it) },
                                onLogout = { viewModel.logout() }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Crossfade(
                            targetState = currentScreen,
                            animationSpec = tween(400),
                            label = "MainScreenFade"
                        ) { screen ->
                            when (screen) {
                                "AUTH" -> AuthScreen(viewModel = viewModel)
                                "USER_PORTAL" -> ClientScreen(viewModel = viewModel)
                                "ADMIN_DASHBOARD" -> AdminScreen(viewModel = viewModel)
                                else -> AuthScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnCoreTopAppBar(
    viewModel: VpnViewModel,
    currentScreen: String,
    loggedInUser: User?,
    onScreenSelected: (String) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ThemeDarkSurface)
            .shadow(4.dp)
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Main visual logo/header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.VpnLock,
                        contentDescription = "VPN Lock",
                        tint = ThemeNeonTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ZERO-DATA VPN",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 1.sp
                    )
                }

                if (loggedInUser != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (loggedInUser.role == "ADMIN") {
                            // Admin has switcher
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ThemeDarkCard)
                                    .padding(2.dp)
                            ) {
                                // Client portal button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (currentScreen == "USER_PORTAL") ThemeNeonTeal else Color.Transparent)
                                        .clickable { onScreenSelected("USER_PORTAL") }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("nav_user_portal"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.PowerSettingsNew,
                                            contentDescription = "User",
                                            tint = if (currentScreen == "USER_PORTAL") ThemeDarkBg else ThemeMutedText,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "CLIENT",
                                            color = if (currentScreen == "USER_PORTAL") ThemeDarkBg else ThemeMutedText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                // Admin panel button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (currentScreen == "ADMIN_DASHBOARD") ThemeNeonTeal else Color.Transparent)
                                        .clickable { onScreenSelected("ADMIN_DASHBOARD") }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("nav_admin_dashboard"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.SupervisorAccount,
                                            contentDescription = "Admin",
                                            tint = if (currentScreen == "ADMIN_DASHBOARD") ThemeDarkBg else ThemeMutedText,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "ADMIN",
                                            color = if (currentScreen == "ADMIN_DASHBOARD") ThemeDarkBg else ThemeMutedText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            // Client only has role badge status
                            Text(
                                text = loggedInUser.username.uppercase(),
                                color = ThemeNeonBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ThemeDarkCard)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Language selector
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ThemeDarkCard)
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val currentLang by viewModel.appLanguage.collectAsState()
                            Text(
                                text = "EN",
                                color = if (currentLang == "en") ThemeNeonTeal else ThemeMutedText,
                                fontWeight = if (currentLang == "en") FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .clickable { viewModel.setAppLanguage("en") }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                            Text(
                                text = "|",
                                color = ThemeMutedText,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "SW",
                                color = if (currentLang == "sw") ThemeNeonTeal else ThemeMutedText,
                                fontWeight = if (currentLang == "sw") FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .clickable { viewModel.setAppLanguage("sw") }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Unified logout button at top right
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ThemeDarkCard)
                                .testTag("btn_logout")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PowerSettingsNew,
                                contentDescription = "Toka Akaunti",
                                tint = ThemeNeonOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = ThemeDarkCard, thickness = 1.dp)
    }
}
