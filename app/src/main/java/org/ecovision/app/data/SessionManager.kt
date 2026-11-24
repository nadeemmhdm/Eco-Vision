package org.ecovision.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.ecovision.app.EcoVisionApplication

object SessionManager {

    private const val PREFS_NAME = "ecovision_session_prefs"
    private const val KEY_USER_MOBILE = "saved_user_mobile"
    private const val KEY_USER_ID = "saved_user_id"

    private val prefs: SharedPreferences by lazy {
        EcoVisionApplication.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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

    suspend fun switchDemoRole(role: UserRole) {
        val mobile = when (role) {
            UserRole.USER -> "+919123456789"
            UserRole.AGENT -> "+919876543210"
            UserRole.COMPANY -> "+919888877776"
            UserRole.ADMIN -> "+919999999999"
        }
        val found = SupabaseClient.findProfileByMobile(mobile)
        if (found != null) {
            setProfile(found, persist = true)
        }
    }

    suspend fun refreshProfile() {
        val curr = _currentProfile.value ?: return
        val fresh = SupabaseClient.findProfileByMobile(curr.mobile)
        if (fresh != null) {
            setProfile(fresh, persist = true)
        }
    }

    fun logout() {
        clearSession()
    }
}
