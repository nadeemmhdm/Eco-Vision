package org.ecovision.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.ecovision.app.EcoVisionApplication
import org.ecovision.app.theme.ThemeMode

object SessionManager {

    private const val PREFS_NAME = "ecovision_session_prefs"
    private const val KEY_USER_MOBILE = "saved_user_mobile"
    private const val KEY_USER_ID = "saved_user_id"
    private const val KEY_THEME_MODE = "saved_theme_mode"

    private val prefs: SharedPreferences by lazy {
        EcoVisionApplication.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _themeMode = MutableStateFlow<ThemeMode>(
        try {
            when (prefs.getString(KEY_THEME_MODE, "SYSTEM")) {
                "LIGHT" -> ThemeMode.LIGHT
                "DARK" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        try {
            prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        } catch (e: Exception) { }
    }

    fun toggleThemeMode() {
        val next = when (_themeMode.value) {
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
        }
        setThemeMode(next)
    }

    private val _currentProfile = MutableStateFlow<Profile?>(null)
    val currentProfile: StateFlow<Profile?> = _currentProfile.asStateFlow()

    private val _agentDetails = MutableStateFlow<AgentDetails?>(null)
    val agentDetails: StateFlow<AgentDetails?> = _agentDetails.asStateFlow()

    private val _companyDetails = MutableStateFlow<CompanyDetails?>(null)
    val companyDetails: StateFlow<CompanyDetails?> = _companyDetails.asStateFlow()

    private val _platformSettings = MutableStateFlow<PlatformSettings>(PlatformSettings())
    val platformSettings: StateFlow<PlatformSettings> = _platformSettings.asStateFlow()

    private val _maintenanceBlocked = MutableStateFlow<Boolean>(false)
    val maintenanceBlocked: StateFlow<Boolean> = _maintenanceBlocked.asStateFlow()

    private val _isInitialized = MutableStateFlow<Boolean>(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    suspend fun init() {
        val settings = SupabaseClient.getPlatformSettings() ?: PlatformSettings()
        _platformSettings.value = settings

        // Check if there is a saved user session
        val savedMobile = prefs.getString(KEY_USER_MOBILE, null)

        if (savedMobile != null) {
            val user = SupabaseClient.findProfileByMobile(savedMobile)
            if (user != null) {
                setProfile(user, persist = false)
            } else {
                clearSession()
            }
        }
        checkMaintenance()
        _isInitialized.value = true
    }

    suspend fun setProfile(profile: Profile?, persist: Boolean = true) {
        _currentProfile.value = profile
        if (profile != null) {
            if (persist) {
                prefs.edit()
                    .putString(KEY_USER_MOBILE, profile.mobile)
                    .putString(KEY_USER_ID, profile.id)
                    .apply()
            }
            if (profile.role == "agent") {
                _agentDetails.value = SupabaseClient.getAgentDetails(profile.id)
            } else {
                _agentDetails.value = null
            }
            if (profile.role == "company") {
                _companyDetails.value = SupabaseClient.getCompanyDetails(profile.id)
            } else {
                _companyDetails.value = null
            }
        } else {
            if (persist) {
                clearSession()
            }
            _agentDetails.value = null
            _companyDetails.value = null
        }
        checkMaintenance()
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_USER_MOBILE)
            .remove(KEY_USER_ID)
            .apply()
        _currentProfile.value = null
        _agentDetails.value = null
        _companyDetails.value = null
    }

    fun checkMaintenance() {
        val settings = _platformSettings.value
        val profile = _currentProfile.value
        if (!settings.maintenanceMode) {
            _maintenanceBlocked.value = false
            return
        }
        // Admin always bypasses maintenance mode
        if (profile?.role == "admin") {
            _maintenanceBlocked.value = false
            return
        }
        if (settings.maintenanceRoles.isEmpty()) {
            _maintenanceBlocked.value = true
        } else {
            _maintenanceBlocked.value = profile?.role in settings.maintenanceRoles
        }
    }

    fun updateSettings(settings: PlatformSettings) {
        _platformSettings.value = settings
        checkMaintenance()
    }

    fun getFallbackDemoProfile(role: UserRole): Profile {
        return when (role) {
            UserRole.USER -> Profile(
                id = "00000000-0000-0000-0000-000000000002",
                username = "adhil",
                fullName = "Adhil Shahad",
                mobile = "+919123456789",
                email = "adhil@ecovision.org",
                role = "user",
                kycStatus = "verified",
                leafCoins = 240,
                defaultAddress = "Kakkanad, Kochi, Kerala",
                pincode = "682030"
            )
            UserRole.AGENT -> Profile(
                id = "00000000-0000-0000-0000-000000000003",
                username = "rahul_agent",
                fullName = "Rahul Kumar (Eco Agent)",
                mobile = "+919876543210",
                email = "rahul.agent@ecovision.org",
                role = "agent",
                kycStatus = "verified",
                leafCoins = 50,
                defaultAddress = "Vyttila Hub, Kochi",
                pincode = "682019"
            )
            UserRole.COMPANY -> Profile(
                id = "00000000-0000-0000-0000-000000000004",
                username = "greenloop_co",
                fullName = "GreenLoop Clean Recyclers",
                mobile = "+919888877776",
                email = "procurement@greenloop.in",
                role = "company",
                kycStatus = "verified",
                leafCoins = 50,
                defaultAddress = "Industrial Area, Eloor, Kochi",
                pincode = "683501"
            )
            UserRole.ADMIN -> Profile(
                id = "00000000-0000-0000-0000-000000000001",
                username = "admin",
                fullName = "EcoVision Super Admin",
                mobile = "+919999999999",
                email = "admin@ecovision.org",
                role = "admin",
                kycStatus = "verified",
                leafCoins = 500,
                defaultAddress = "EcoVision Command Center, Kochi",
                pincode = "682001"
            )
        }
    }

    fun getFallbackProfileForIdentifier(identifier: String): Profile? {
        val clean = identifier.trim().lowercase().removePrefix("+91")
        return when {
            clean == "admin" || clean == "9999999999" -> getFallbackDemoProfile(UserRole.ADMIN)
            clean == "adhil" || clean == "9123456789" -> getFallbackDemoProfile(UserRole.USER)
            clean == "rahul_agent" || clean == "9876543210" -> getFallbackDemoProfile(UserRole.AGENT)
            clean == "greenloop_co" || clean == "9888877776" -> getFallbackDemoProfile(UserRole.COMPANY)
            else -> null
        }
    }

    suspend fun switchDemoRole(role: UserRole) {
        val mobile = when (role) {
            UserRole.USER -> "+919123456789"
            UserRole.AGENT -> "+919876543210"
            UserRole.COMPANY -> "+919888877776"
            UserRole.ADMIN -> "+919999999999"
        }
        val found = try {
            SupabaseClient.findProfileByMobile(mobile) ?: getFallbackDemoProfile(role)
        } catch (e: Exception) {
            getFallbackDemoProfile(role)
        }
        setProfile(found, persist = true)
    }

    fun updateCurrentProfile(updated: Profile) {
        _currentProfile.value = updated
    }

    suspend fun refreshProfile() {
        val curr = _currentProfile.value ?: return
        val fresh = SupabaseClient.findProfileByMobile(curr.mobile)
        if (fresh != null) {
            // Respect locally deducted coins if more recent
            val resolvedCoins = if (fresh.leafCoins < curr.leafCoins) fresh.leafCoins else curr.leafCoins
            setProfile(fresh.copy(leafCoins = resolvedCoins), persist = true)
        }
    }

    fun logout() {
        clearSession()
    }
}
