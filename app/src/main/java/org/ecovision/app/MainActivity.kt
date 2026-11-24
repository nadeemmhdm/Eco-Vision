package org.ecovision.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import kotlinx.coroutines.launch
import org.ecovision.app.data.SessionManager
import org.ecovision.app.theme.*
import org.ecovision.app.ui.components.EcoHeader
import org.ecovision.app.ui.navigation.Screen
import org.ecovision.app.ui.screens.admin.AdminDashboardScreen
import org.ecovision.app.ui.screens.agent.AgentDashboardScreen
import org.ecovision.app.ui.screens.auth.AuthScreen
import org.ecovision.app.ui.screens.company.CompanyDashboardScreen
import org.ecovision.app.ui.screens.maintenance.MaintenanceScreen
import org.ecovision.app.ui.screens.profile.ProfileScreen
import org.ecovision.app.ui.screens.splash.SplashScreen
import org.ecovision.app.ui.screens.user.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcoVisionTheme {
                MainAppHost()
            }
        }
    }
}

@Composable
fun MainAppHost() {
    val navController = rememberNavController()
    val profile by SessionManager.currentProfile.collectAsState()
    val isMaintenanceBlocked by SessionManager.maintenanceBlocked.collectAsState()
    val scope = rememberCoroutineScope()

    if (isMaintenanceBlocked && profile?.role != "admin") {
        MaintenanceScreen(
            onRecovery = {
                scope.launch { SessionManager.refreshProfile() }
            }
        )
        return
    }

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val isTopLevelScreen = currentRoute in listOf(
        Screen.UserHome.route,
        Screen.AgentDashboard.route,
        Screen.CompanyDashboard.route,
        Screen.AdminDashboard.route
    )

    val isNavBarVisible = currentRoute in listOf(
        Screen.UserHome.route,
        Screen.AgentDashboard.route,
        Screen.CompanyDashboard.route,
        Screen.AdminDashboard.route,
        Screen.Marketplace.route,
        Screen.VirtualBin.route
    )

    Scaffold(
        containerColor = Slate900,
        topBar = {
            if (isTopLevelScreen) {
                EcoHeader(
                    title = "EcoVision",
                    subtitle = when (profile?.role) {
                        "agent" -> "Pickup Agent Desk"
                        "company" -> "B2B Waste Marketplace"
                        "admin" -> "Admin Governance"
                        else -> "Circular Economy Loop"
                    },
                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                    onWalletClick = { navController.navigate(Screen.LeafCoins.route) }
                )
            }
        },
        bottomBar = {
            if (isNavBarVisible) {
                NavigationBar(
                    containerColor = Slate950,
                    contentColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    val items = when (profile?.role) {
                        "agent" -> listOf(
                            BottomNavItem("Collection", Screen.AgentDashboard.route, Icons.Default.LocalShipping),
                            BottomNavItem("Profile", Screen.Profile.route, Icons.Default.Person)
                        )
                        "company" -> listOf(
                            BottomNavItem("Godown Feed", Screen.CompanyDashboard.route, Icons.Default.Inventory),
                            BottomNavItem("Profile", Screen.Profile.route, Icons.Default.Person)
                        )
                        "admin" -> listOf(
                            BottomNavItem("Governance", Screen.AdminDashboard.route, Icons.Default.AdminPanelSettings),
                            BottomNavItem("Profile", Screen.Profile.route, Icons.Default.Person)
                        )
                        else -> listOf(
                            BottomNavItem("Home", Screen.UserHome.route, Icons.Default.Home),
                            BottomNavItem("Scan", Screen.AiScan.route, Icons.Default.CameraAlt),
                            BottomNavItem("Bin", Screen.VirtualBin.route, Icons.Default.DeleteOutline),
                            BottomNavItem("Store", Screen.Marketplace.route, Icons.Default.Storefront),
                            BottomNavItem("Wallet", Screen.LeafCoins.route, Icons.Default.Spa)
                        )
                    }

                    for (item in items) {
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Slate950,
                                selectedTextColor = EcoEmerald400,
                                indicatorColor = EcoEmerald500,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400
                            ),
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Splash Screen
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToAuth = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToHome = { userProfile ->
                        val targetRoute = when (userProfile.role) {
                            "agent" -> Screen.AgentDashboard.route
                            "company" -> Screen.CompanyDashboard.route
                            "admin" -> Screen.AdminDashboard.route
                            else -> Screen.UserHome.route
                        }
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Auth Screen
            composable(Screen.Auth.route) {
                AuthScreen(
                    onAuthSuccess = { authenticatedProfile ->
                        val targetRoute = when (authenticatedProfile.role) {
                            "agent" -> Screen.AgentDashboard.route
                            "company" -> Screen.CompanyDashboard.route
                            "admin" -> Screen.AdminDashboard.route
                            else -> Screen.UserHome.route
                        }
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }

            // User Routes
            composable(Screen.UserHome.route) {
                UserHomeScreen(
                    onNavigateToScan = { navController.navigate(Screen.AiScan.route) },
                    onNavigateToBin = { navController.navigate(Screen.VirtualBin.route) },
                    onNavigateToMarketplace = { navController.navigate(Screen.Marketplace.route) },
                    onNavigateToWallet = { navController.navigate(Screen.LeafCoins.route) },
                    onNavigateToDoorstep = { id -> navController.navigate(Screen.DoorstepVerification.createRoute(id)) },
                    onNavigateToRate = { id -> navController.navigate(Screen.RateAgent.createRoute(id)) }
                )
            }
            composable(Screen.AiScan.route) {
                AiScanScreen(
                    onAddedToBin = { navController.navigate(Screen.VirtualBin.route) },
                    onNavigateToMarketplace = { navController.navigate(Screen.Marketplace.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.VirtualBin.route) {
                VirtualBinScreen(
                    onPickupRequested = { navController.navigate(Screen.UserHome.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Marketplace.route) {
                MarketplaceScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.LeafCoins.route) {
                LeafCoinWalletScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.DoorstepVerification.route) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("requestId") ?: ""
                DoorstepVerificationScreen(
                    requestId = id,
                    onComplete = { navController.navigate(Screen.RateAgent.createRoute(id)) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.RateAgent.route) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("requestId") ?: ""
                RateAgentScreen(
                    requestId = id,
                    onSubmitted = { navController.navigate(Screen.UserHome.route) },
                    onBack = { navController.popBackStack() }
                )
            }

            // Role Dashboards
            composable(Screen.AgentDashboard.route) {
                AgentDashboardScreen()
            }
            composable(Screen.CompanyDashboard.route) {
                CompanyDashboardScreen()
            }
            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen()
            }

            // Profile
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)
