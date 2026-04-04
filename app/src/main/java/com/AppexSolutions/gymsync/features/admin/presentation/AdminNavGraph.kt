package com.AppexSolutions.gymsync.features.admin.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.AppexSolutions.gymsync.features.admin.navigation.AdminDashboard
import com.AppexSolutions.gymsync.features.admin.navigation.AdminScanner
import com.AppexSolutions.gymsync.features.admin.navigation.AdminClients
import com.AppexSolutions.gymsync.features.admin.navigation.AdminCreateUser
import com.AppexSolutions.gymsync.features.admin.navigation.AdminEditClient
import com.AppexSolutions.gymsync.features.admin.presentation.components.AdminBottomNavBar
import com.AppexSolutions.gymsync.features.admin.presentation.components.AdminTab
import com.AppexSolutions.gymsync.features.admin.presentation.screens.DashboardScreen
import com.AppexSolutions.gymsync.features.admin.presentation.screens.ScannerScreen
import com.AppexSolutions.gymsync.features.clients.presentation.screens.ClientsScreen
import com.AppexSolutions.gymsync.features.clients.presentation.screens.CreateUserScreen
import com.AppexSolutions.gymsync.features.clients.presentation.screens.EditClientScreen

@Composable
fun AdminNavGraph(
    onLogout: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val selectedTab = when {
        currentRoute?.endsWith("AdminDashboard") == true -> AdminTab.Dashboard.index
        currentRoute?.endsWith("AdminScanner") == true   -> AdminTab.Scanner.index
        currentRoute?.endsWith("AdminClients") == true   -> AdminTab.Clients.index
        else                                             -> AdminTab.Dashboard.index
    }

    Scaffold(
        bottomBar = {
            AdminBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { tabIndex ->
                    val route = when (tabIndex) {
                        AdminTab.Dashboard.index -> AdminDashboard
                        AdminTab.Scanner.index   -> AdminScanner
                        AdminTab.Clients.index   -> AdminClients
                        else                     -> AdminDashboard
                    }
                    navController.navigate(route) {
                        popUpTo<AdminDashboard> { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = AdminDashboard,
            modifier = Modifier.padding(innerPadding)
        ) {

            // ── Dashboard ──
            composable<AdminDashboard> {
                DashboardScreen()
            }

            // ── Scanner QR ──
            composable<AdminScanner> {
                ScannerScreen(
                    onNavigateBack = {
                        navController.navigate(AdminDashboard) {
                            popUpTo<AdminDashboard> { inclusive = true }
                        }
                    },
                    onScanSuccess = {
                        navController.navigate(AdminDashboard) {
                            popUpTo<AdminDashboard> { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ── Clientes ──
            composable<AdminClients> {
                ClientsScreen(
                    onAddClient = { navController.navigate(AdminCreateUser) },
                    onClientClick = { clientId -> navController.navigate(AdminEditClient(clientId)) },
                    onTabSelected = { _ -> },
                    onLogout = onLogout,
                    showBottomBar = false
                )
            }

            // ── Crear cliente ──
            composable<AdminCreateUser> {
                CreateUserScreen(
                    onSuccess = {
                        navController.navigate(AdminClients) {
                            popUpTo<AdminClients> { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Editar cliente ──
            composable<AdminEditClient> {
                EditClientScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
