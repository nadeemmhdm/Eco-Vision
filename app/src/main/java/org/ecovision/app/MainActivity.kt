package org.ecovision.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import org.ecovision.app.ui.components.EcoFloatingBottomBar
import org.ecovision.app.ui.navigation.Screen
import org.ecovision.app.ui.screens.admin.AdminDashboardScreen
import org.ecovision.app.ui.screens.agent.AgentDashboardScreen
import org.ecovision.app.ui.screens.auth.AuthScreen
import org.ecovision.app.ui.screens.company.CompanyDashboardScreen
import org.ecovision.app.ui.screens.maintenance.MaintenanceScreen
import org.ecovision.app.ui.screens.profile.ProfileScreen
import org.ecovision.app.ui.screens.settings.*
import org.ecovision.app.ui.screens.splash.SplashScreen
import org.ecovision.app.ui.screens.user.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by SessionManager.themeMode.collectAsState()
            EcoVisionTheme(themeMode = themeMode) {
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

    val isTopLevelScreen = false // Screens have dedicated hero headers (UserHome, Agent, Company, Admin)

    val isNavBarVisible = currentRoute in listOf(
        Screen.UserHome.route,
        Screen.AgentDashboard.route,
        Screen.CompanyDashboard.route,
        Screen.AdminDashboard.route,
        Screen.Marketplace.route,
        Screen.VirtualBin.route,
        Screen.LeafCoins.route,
        Screen.Settings.route,
        "wallet",
        "leaf_coins"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = isNavBarVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(220)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(180))
            ) {
                Box(modifier = Modifier.navigationBarsPadding()) {
                    EcoFloatingBottomBar(
                        currentRoute = currentRoute,
                        role = profile?.role,
                        onNavigate = { targetRoute ->
                            if (currentRoute != targetRoute) {
                                navController.navigate(targetRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        onCenterScanClick = {
                            if (currentRoute != Screen.AiScan.route) {
                                navController.navigate(Screen.AiScan.route)
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                fadeIn(animationSpec = tween(260, easing = FastOutSlowInEasing)) +
                    slideInHorizontally(
                        initialOffsetX = { it / 16 },
                        animationSpec = tween(260, easing = FastOutSlowInEasing)
                    )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(260, easing = FastOutSlowInEasing)) +
                    slideInHorizontally(
                        initialOffsetX = { -it / 16 },
                        animationSpec = tween(260, easing = FastOutSlowInEasing)
                    )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                    slideOutHorizontally(
                        targetOffsetX = { it / 16 },
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    )
            }
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
                LaunchedEffect(profile) {
                    val r = profile?.role
                    if (r == "admin") {
                        navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.UserHome.route) { inclusive = true }
                        }
                    } else if (r == "agent") {
                        navController.navigate(Screen.AgentDashboard.route) {
                            popUpTo(Screen.UserHome.route) { inclusive = true }
                        }
                    } else if (r == "company") {
                        navController.navigate(Screen.CompanyDashboard.route) {
                            popUpTo(Screen.UserHome.route) { inclusive = true }
                        }
                    }
                }
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
            composable("leaf_coins") {
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
                LaunchedEffect(profile) {
                    if (profile != null && profile?.role != "agent") {
                        val route = when (profile?.role) {
                            "admin" -> Screen.AdminDashboard.route
                            "company" -> Screen.CompanyDashboard.route
                            else -> Screen.UserHome.route
                        }
                        navController.navigate(route) {
                            popUpTo(Screen.AgentDashboard.route) { inclusive = true }
                        }
                    }
                }
                AgentDashboardScreen()
            }
            composable(Screen.CompanyDashboard.route) {
                LaunchedEffect(profile) {
                    if (profile != null && profile?.role != "company") {
                        val route = when (profile?.role) {
                            "admin" -> Screen.AdminDashboard.route
                            "agent" -> Screen.AgentDashboard.route
                            else -> Screen.UserHome.route
                        }
                        navController.navigate(route) {
                            popUpTo(Screen.CompanyDashboard.route) { inclusive = true }
                        }
                    }
                }
                CompanyDashboardScreen()
            }
            composable(Screen.AdminDashboard.route) {
                LaunchedEffect(profile) {
                    if (profile != null && profile?.role != "admin") {
                        val route = when (profile?.role) {
                            "agent" -> Screen.AgentDashboard.route
                            "company" -> Screen.CompanyDashboard.route
                            else -> Screen.UserHome.route
                        }
                        navController.navigate(route) {
                            popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                        }
                    }
                }
                AdminDashboardScreen()
            }

            // Settings & Dedicated Info Pages
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToMyOrders = { navController.navigate(Screen.MyOrders.route) },
                    onNavigateToSecurity = { navController.navigate(Screen.Security.route) },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) },
                    onNavigateToTerms = { navController.navigate(Screen.Terms.route) },
                    onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) },
                    onNavigateToFaq = { navController.navigate(Screen.Faq.route) },
                    onLogout = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.MyOrders.route) {
                MyOrdersScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Security.route) {
                SecurityScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.About.route) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Terms.route) {
                TermsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.PrivacyPolicy.route) {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Faq.route) {
                FaqScreen(onBack = { navController.popBackStack() })
            }

            // Profile
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                    onNavigateToWallet = { navController.navigate(Screen.LeafCoins.route) }
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
