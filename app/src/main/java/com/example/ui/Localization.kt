package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.example.ui.VpnViewModel

object Localization {
    private val translations = mapOf(
        "kasi_thabiti" to mapOf("sw" to "Kasi thabiti • Usiri usio na kikomo", "en" to "Solid speed • Unlimited privacy"),
        "ingia" to mapOf("sw" to "INGIA", "en" to "LOGIN"),
        "jisajili" to mapOf("sw" to "JISAJILI", "en" to "SIGN UP"),
        "karibu_tena" to mapOf("sw" to "Karibu Tena! Ingiza akaunti yako", "en" to "Welcome Back! Log in to your account"),
        "fungua_akaunti" to mapOf("sw" to "Fungua Akaunti Mpya ya VPN", "en" to "Create a New VPN Account"),
        "username_label" to mapOf("sw" to "Jina la mtumiaji (username/email)", "en" to "Username or email"),
        "password_label" to mapOf("sw" to "Nenosiri", "en" to "Password"),
        "onyesha_nenosiri" to mapOf("sw" to "Onyesha nenosiri", "en" to "Show password"),
        "namba_ya_rufaa_label" to mapOf("sw" to "Namba ya Rufaa (Sio lazima)", "en" to "Referral Code (Optional)"),
        "namba_ya_rufaa_hint" to mapOf("sw" to "Ingiza namba ya rufaa ya aliyekupa simu", "en" to "Enter referral code of the person who referred you"),
        "chagua_jukumu" to mapOf("sw" to "Chagua Jukumu la Akaunti:", "en" to "Choose Account Role:"),
        "mteja" to mapOf("sw" to "MTEJA (Client)", "en" to "CLIENT"),
        "mtawala" to mapOf("sw" to "MTAWALA (Admin)", "en" to "ADMIN"),
        "button_ingia" to mapOf("sw" to "INGIA KWENYE MFUMO", "en" to "LOG IN"),
        "button_jisajili" to mapOf("sw" to "JISAJILI SASA", "en" to "REGISTER NOW"),
        "tayari_akaunti" to mapOf("sw" to "Tayari una akaunti? Ingia", "en" to "Already have an account? Log in"),
        "bado_akaunti" to mapOf("sw" to "Je, huna akaunti bado? Jisajili", "en" to "Don't have an account yet? Register"),
        
        // Client screen strings:
        "kujiondoa" to mapOf("sw" to "Kujiondoa", "en" to "Log Out"),
        "karibu" to mapOf("sw" to "Karibu, ", "en" to "Welcome, "),
        "kiwango_cha_rufaa" to mapOf("sw" to "Kiwango cha Rufaa: watu ", "en" to "Referral Count: "),
        "mchezaji_bora" to mapOf("sw" to "mchezaji bora aliyeleta rufaa nyingi!", "en" to "top referrers!"),
        "rufaa_status" to mapOf("sw" to "Umefanikiwa kualika watu", "en" to "Successfully invited people"),
        "haijaunganishwa" to mapOf("sw" to "HAIJAUNGANISHWA", "en" to "DISCONNECTED"),
        "inaunganisha" to mapOf("sw" to "INAUNGANISHA...", "en" to "CONNECTING..."),
        "imeunganishwa" to mapOf("sw" to "IMEUNGANISHWA", "en" to "CONNECTED"),
        "ingiza_vocha_token" to mapOf("sw" to "Ingiza Token ya Vocha Kuvinjari bwerere", "en" to "Enter Voucher Token to Browse Free"),
        "anza_muunganisho" to mapOf("sw" to "ANZA MUUNGANISHO", "en" to "START CONNECTION"),
        "vunja_muunganisho" to mapOf("sw" to "VUNJA MUUNGANISHO", "en" to "DISCONNECT"),
        "hali_ya_akaunti" to mapOf("sw" to "HALI YA MUUNGANISHO", "en" to "CONNECTION STATUS"),
        "muunganisho_salama" to mapOf("sw" to "Muunganisho wako ni salama kabisa na data yako inalindwa.", "en" to "Your connection is completely secure and your data is protected."),
        "muda_uliobakia" to mapOf("sw" to "Muda Uliobakia", "en" to "Remaining Time"),
        "mwisho_wa_muda" to mapOf("sw" to "Mwisho wa Muda", "en" to "Expiry Date"),
        "pakua" to mapOf("sw" to "Pakua (Download)", "en" to "Download"),
        "tuma" to mapOf("sw" to "Tuma (Upload)", "en" to "Upload"),
        "jumla_ya_data" to mapOf("sw" to "Data Zilizotumika", "en" to "Data Used"),
        "server_handshake" to mapOf("sw" to "Taarifa za Server", "en" to "Server Information"),
        "itifaki" to mapOf("sw" to "Itifaki", "en" to "Protocol"),
        
        "nunua_vocha" to mapOf("sw" to "Nunua Vocha ya Internet", "en" to "Buy Internet Voucher"),
        "jaza_fomu_nunua" to mapOf("sw" to "Lipia upate internet ya kasi bila kikomo cha bando", "en" to "Pay to get high-speed internet with no data limit"),
        "namba_ya_simu" to mapOf("sw" to "Nambari ya Simu (M-Pesa, TigoPesa, AirtelMoney)", "en" to "Phone Number (M-Pesa, TigoPesa, AirtelMoney)"),
        "chagua_bando" to mapOf("sw" to "Chagua Bando la Kasi", "en" to "Select High-Speed Package"),
        "saa_24" to mapOf("sw" to "Saa 24 (Siku 1) - TZS 2,000", "en" to "24 Hours (1 Day) - TZS 2,000"),
        "saa_12" to mapOf("sw" to "Saa 12 - TZS 1,000", "en" to "12 Hours - TZS 1,000"),
        "saa_168" to mapOf("sw" to "Siku 7 (Wiki 1) - TZS 5,000", "en" to "7 Days (1 Week) - TZS 5,000"),
        "lipia_sasa" to mapOf("sw" to "LIPIA SASA", "en" to "PAY NOW"),
        
        "rufaa_na_bwerere" to mapOf("sw" to "Programu ya Rufaa & Internet ya Bure", "en" to "Referral Program & Free Internet"),
        "sheria_ya_rufaa" to mapOf("sw" to "Mwalike rafiki yako akijiunga na kuweka vocha yoyote, akaunti yako itaongezewa masaa %d ya internet ya bure kama zawadi na yeye atafurahia pia!", "en" to "Invite your friend! When they join and activate any voucher, your account will be rewarded with %d free hours of internet!"),
        "nambari_ya_rufaa_yako" to mapOf("sw" to "Nambari yako ya rufaa:", "en" to "Your referral code:"),
        "nakili_namba" to mapOf("sw" to "NAKILI NAMBA", "en" to "COPY CODE"),
        "shiriki_kwenye_whatsapp" to mapOf("sw" to "SHIRIKI KWENYE WHATSAPP", "en" to "SHARE ON WHATSAPP"),
        
        "historia_ya_vocha" to mapOf("sw" to "Historia ya Vocha Zako", "en" to "Your Voucher History"),
        "hakuna_vocha" to mapOf("sw" to "Bado haujanunua vocha yoyote. Nunua hapo juu kuanza kuvinjari bwerere!", "en" to "You haven't purchased any vouchers yet. Buy one above to start browsing safely!"),
        "alama_ya_rufaa" to mapOf("sw" to "Jedwali la Wanaorufaa Bora (Leaderboard)", "en" to "Top Referrers Leaderboard"),
        "vocha_imeigizwa" to mapOf("sw" to "Vocha imeiga chapa kwenye clipboard!", "en" to "Voucher copied to clipboard!"),
        
        // Admin screen strings:
        "kasi_dashibodi" to mapOf("sw" to "DASHIBODI YA USIMAMIZI YA VPN", "en" to "VPN ADMINISTRATION DASHBOARD"),
        "mambo_mapya" to mapOf("sw" to "Hali ya Mtandao sasa hivi", "en" to "Network Status Overview"),
        "watumiaji_hai" to mapOf("sw" to "Watumiaji Hai", "en" to "Active Users"),
        "jumla_ya_mishahara" to mapOf("sw" to "Jumla ya Vocha Ulizouza", "en" to "Total Paid Vouchers"),
        "muda_wasifu" to mapOf("sw" to "Vipimo vya Vifaa", "en" to "Device Specs"),
        "bendi_ya_mtandao" to mapOf("sw" to "Bandwidth Iliyotumika", "en" to "Bandwidth Transferred"),
        "simamia_vifaa" to mapOf("sw" to "Simamia Router (Wireguard / VLESS Endpoints)", "en" to "Manage Routers (Wireguard / VLESS Endpoints)"),
        "ongeza_router" to mapOf("sw" to "ONGEZA ROUTER MPYA", "en" to "ADD NEW ROUTER"),
        "jina_la_router" to mapOf("sw" to "Jina la Router", "en" to "Router Name"),
        "wireguard_ip" to mapOf("sw" to "Wireguard Node IP (e.g. 10.0.0.1)", "en" to "Wireguard Node IP (e.g. 10.0.0.1)"),
        "router_ip" to mapOf("sw" to "Router Public Key IP/Gateway", "en" to "Router Public Key IP/Gateway"),
        "vifaa_vilivyosajiliwa" to mapOf("sw" to "Routers & Servers Zilizosajiliwa", "en" to "Registered Routers & Servers"),
        "tengeneza_vocha_zawadi" to mapOf("sw" to "Tengeneza Vocha Mpya (Bulk Admin Panel)", "en" to "Create New Voucher (Bulk Admin Panel)"),
        "bando" to mapOf("sw" to "Bando", "en" to "Plan"),
        "bei_tzs" to mapOf("sw" to "Bei (TZS)", "en" to "Price (TZS)"),
        "tengeneza_vocha_btn" to mapOf("sw" to "TENGENEZA VOCHA", "en" to "GENERATE VOUCHER"),
        "orodha_ya_vocha_zote" to mapOf("sw" to "Mifumo yote ya Vocha (Zilizopo & Zilizotumika)", "en" to "All Purchased/Generated Vouchers"),
        "muda_wa_zawadi_rufaa" to mapOf("sw" to "Mpangilio wa Zawadi za Rufaa (Hours)", "en" to "Referral Reward Configuration (Hours)"),
        "bofya_mabadiliko" to mapOf("sw" to "HIFADHI MABADILIKO YA ZAWADI", "en" to "SAVE CONFIGURATION"),
        "orodha_ya_watumiaji" to mapOf("sw" to "Orodha ya Watumiaji wa Mfumo", "en" to "Registered Users"),
        "mabadiliko_ya_vless" to mapOf("sw" to "Marekebisho ya VPN Configurations (VLESS & SNI Bug Host)", "en" to "VPN Settings Override (VLESS & SNI Bug Host)"),
        "hifadhi_vless" to mapOf("sw" to "HIFADHI MAREKEBISHO", "en" to "SAVE SETTINGS"),
        "ongeza" to mapOf("sw" to "Ongeza", "en" to "Add"),
        "muda" to mapOf("sw" to "Muda", "en" to "Duration")
    )

    fun translate(key: String, lang: String): String {
        val entry = translations[key] ?: return key
        return entry[lang] ?: entry["sw"] ?: key
    }
}

@Composable
fun stringResource(key: String, viewModel: VpnViewModel): String {
    val langState = viewModel.appLanguage.collectAsState()
    return Localization.translate(key, langState.value)
}
