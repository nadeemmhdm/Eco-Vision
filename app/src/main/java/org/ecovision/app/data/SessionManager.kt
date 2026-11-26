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

    private val _agentTab = MutableStateFlow<Int>(0)
    val agentTab: StateFlow<Int> = _agentTab.asStateFlow()
    fun setAgentTab(tab: Int) { _agentTab.value = tab }

    private val _companyTab = MutableStateFlow<Int>(0)
    val companyTab: StateFlow<Int> = _companyTab.asStateFlow()
    fun setCompanyTab(tab: Int) { _companyTab.value = tab }

    private val _adminTab = MutableStateFlow<Int>(0)
    val adminTab: StateFlow<Int> = _adminTab.asStateFlow()
    fun setAdminTab(tab: Int) { _adminTab.value = tab }

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    fun addToCart(product: MarketplaceProduct, customPrice: Double? = null) {
        val targetProduct = if (customPrice != null) product.copy(askingPrice = customPrice) else product
        val current = _cartItems.value
        val existing = current.find { it.product.id == targetProduct.id }
        _cartItems.value = if (existing != null) {
            current.map {
                if (it.product.id == targetProduct.id) {
                    it.copy(product = targetProduct, quantity = it.quantity + 1)
                } else it
            }
        } else {
            current + CartItem(targetProduct, 1)
        }
    }

    fun updateCartQuantity(productId: String, delta: Int) {
        _cartItems.value = _cartItems.value.mapNotNull { item ->
            if (item.product.id == productId) {
                val nextQty = item.quantity + delta
                if (nextQty > 0) item.copy(quantity = nextQty) else null
            } else item
        }
    }

    fun removeFromCart(productId: String) {
        _cartItems.value = _cartItems.value.filterNot { it.product.id == productId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

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
