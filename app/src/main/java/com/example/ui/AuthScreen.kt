package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: VpnViewModel) {
    var isLoginMode by remember { mutableStateOf(true) }
    
    // Form Inputs
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var referralCodeInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("CLIENT") } // CLIENT or ADMIN
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeDarkBg)
    ) {
        // Decorative cyber background gradient glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ThemeDarkSurface.copy(alpha = 0.8f),
                            ThemeDarkBg
                        ),
                        radius = 1200f
                    )
                )
        )

        // Language Selector
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .safeDrawingPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val currentLang by viewModel.appLanguage.collectAsState()
            
            Text(
                text = "EN",
                color = if (currentLang == "en") ThemeNeonTeal else ThemeMutedText,
                fontWeight = if (currentLang == "en") FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp,
                modifier = Modifier
                    .clickable { viewModel.setAppLanguage("en") }
                    .padding(8.dp)
            )
            
            Text(
                text = "|",
                color = ThemeMutedText,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            
            Text(
                text = "SW",
                color = if (currentLang == "sw") ThemeNeonTeal else ThemeMutedText,
                fontWeight = if (currentLang == "sw") FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp,
                modifier = Modifier
                    .clickable { viewModel.setAppLanguage("sw") }
                    .padding(8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Brand Logo Header
            Icon(
                imageVector = Icons.Filled.VpnLock,
                contentDescription = "Zero-Data Security Lock",
                tint = ThemeNeonTeal,
                modifier = Modifier
                    .size(72.dp)
                    .shadow(16.dp, RoundedCornerShape(36.dp), ambientColor = ThemeNeonTeal, spotColor = ThemeNeonTeal)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ZERO-DATA VPN",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 2.sp
            )

            Text(
                text = stringResource("kasi_thabiti", viewModel),
                color = ThemeNeonBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Glassmorphic Cyber Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ThemeDarkCard, RoundedCornerShape(16.dp))
                    .shadow(12.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = ThemeDarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // MODE SWITCHER (LOGIN vs SIGNUP)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ThemeDarkCard)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isLoginMode) ThemeNeonTeal else Color.Transparent)
                                .clickable { isLoginMode = true }
                                .padding(vertical = 10.dp)
                                .testTag("auth_mode_login"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource("ingia", viewModel),
                                color = if (isLoginMode) ThemeDarkBg else Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!isLoginMode) ThemeNeonTeal else Color.Transparent)
                                .clickable { isLoginMode = false }
                                .padding(vertical = 10.dp)
                                .testTag("auth_mode_signup"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource("jisajili", viewModel),
                                color = if (!isLoginMode) ThemeDarkBg else Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = if (isLoginMode) stringResource("karibu_tena", viewModel) else stringResource("fungua_akaunti", viewModel),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Username/Email Field
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_username_input"),
                        label = { Text(stringResource("username_label", viewModel), color = ThemeMutedText) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Username Lock Icon",
                                tint = ThemeNeonTeal
                            )
                        },
                        trailingIcon = {
                            if (usernameInput.isNotBlank()) {
                                IconButton(onClick = { usernameInput = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Safi", tint = ThemeMutedText)
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ThemeNeonTeal,
                            unfocusedBorderColor = ThemeDarkCard,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = ThemeNeonTeal,
                            focusedContainerColor = ThemeDarkBg,
                            unfocusedContainerColor = ThemeDarkBg
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input"),
                        label = { Text(stringResource("password_label", viewModel), color = ThemeMutedText) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Password Lock Icon",
                                tint = ThemeNeonTeal
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Onyesha nenosiri",
                                    tint = ThemeMutedText
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ThemeNeonTeal,
                            unfocusedBorderColor = ThemeDarkCard,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = ThemeNeonTeal,
                            focusedContainerColor = ThemeDarkBg,
                            unfocusedContainerColor = ThemeDarkBg
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Optional Referral Code Input in SIGNUP Mode
                    AnimatedVisibility(
                        visible = !isLoginMode,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = referralCodeInput,
                                onValueChange = { referralCodeInput = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_referral_input"),
                                label = { Text(stringResource("namba_ya_rufaa_label", viewModel), color = ThemeMutedText) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.CardGiftcard,
                                        contentDescription = "Referral Code Icon",
                                        tint = ThemeNeonTeal
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ThemeNeonTeal,
                                    unfocusedBorderColor = ThemeDarkCard,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = ThemeNeonTeal,
                                    focusedContainerColor = ThemeDarkBg,
                                    unfocusedContainerColor = ThemeDarkBg
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (isLoginMode) {
                                viewModel.attemptLogin(usernameInput, passwordInput)
                            } else {
                                viewModel.registerNewUser(usernameInput, passwordInput, "CLIENT", referralCodeInput)
                                // Pre-fill and switch to Login
                                isLoginMode = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLoginMode) ThemeNeonTeal else ThemeNeonBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isLoginMode) stringResource("button_ingia", viewModel) else stringResource("button_jisajili", viewModel),
                            color = ThemeDarkBg,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Tips / Demo accounts helper info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ThemeDarkSurface)
                    .border(1.dp, ThemeDarkCard, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Taarifa Muhimu",
                            tint = ThemeNeonBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Akaunti za Majaribio (Demo Accounts):",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Admin: richardmakasi200@gmail.com / Rich.012\n• Client: client@test.com / client123",
                        color = ThemeMutedText,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Jisajili hapa kupata akaunti mpya ya mteja (Client) papo hapo na uingie moja kwa moja kwenye dashboard yako!",
                        color = ThemeNeonTeal,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
