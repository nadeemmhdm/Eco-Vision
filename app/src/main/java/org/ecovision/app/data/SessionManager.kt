package org.ecovision.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {

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

    suspend fun init() {
        val settings = SupabaseClient.getPlatformSettings() ?: PlatformSettings()
        _platformSettings.value = settings
        checkMaintenance()

        // Set default demo user initially if null
        if (_currentProfile.value == null) {
            val user = SupabaseClient.findProfileByMobile("+919123456789")
            if (user != null) {
                setProfile(user)
            }
        }
    }

    suspend fun setProfile(profile: Profile?) {
        _currentProfile.value = profile
        if (profile?.role == "agent") {
            _agentDetails.value = SupabaseClient.getAgentDetails(profile.id)
        } else {
            _agentDetails.value = null
        }
        if (profile?.role == "company") {
            _companyDetails.value = SupabaseClient.getCompanyDetails(profile.id)
        } else {
            _companyDetails.value = null
        }
        checkMaintenance()
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
            setProfile(found)
        }
    }

    suspend fun refreshProfile() {
        val curr = _currentProfile.value ?: return
        val fresh = SupabaseClient.findProfileByMobile(curr.mobile)
        if (fresh != null) {
            setProfile(fresh)
        }
    }

    fun logout() {
        _currentProfile.value = null
        _agentDetails.value = null
        _companyDetails.value = null
    }
}
