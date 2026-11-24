package org.ecovision.app.ui.navigation

sealed class Screen(val route: String, val title: String) {
    data object Auth : Screen("auth", "Sign In / Register")
    data object Maintenance : Screen("maintenance", "System Maintenance")

    // Household User
    data object UserHome : Screen("user_home", "Dashboard")
    data object AiScan : Screen("ai_scan", "AI Waste Scanner")
    data object VirtualBin : Screen("virtual_bin", "Virtual Bin")
    data object Marketplace : Screen("marketplace", "Upcycled Store")
    data object LeafCoins : Screen("leaf_coins", "Leaf Coin Wallet")
    data object DoorstepVerification : Screen("doorstep_verify/{requestId}", "Doorstep Verification") {
        fun createRoute(requestId: String) = "doorstep_verify/$requestId"
    }
    data object RateAgent : Screen("rate_agent/{requestId}", "Rate & Review Agent") {
        fun createRoute(requestId: String) = "rate_agent/$requestId"
    }

    // Role Dashboards
    data object AgentDashboard : Screen("agent_dashboard", "Agent Collection Desk")
    data object CompanyDashboard : Screen("company_dashboard", "B2B Waste Marketplace")
    data object AdminDashboard : Screen("admin_dashboard", "Admin Governance")

    // Profile & Settings
    data object Profile : Screen("profile", "Profile & Security")
}
