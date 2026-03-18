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
import com.AppexSolutions.gymsync.core.di.appContainer
import com.AppexSolutions.gymsync.features.admin.presentation.components.AdminBottomNavBar
import com.AppexSolutions.gymsync.features.admin.presentation.components.AdminTab
import com.AppexSolutions.gymsync.features.admin.presentation.screens.DashboardScreen
import com.AppexSolutions.gymsync.features.admin.presentation.screens.ScannerScreen
import com.AppexSolutions.gymsync.features.clients.di.ClientsModule
import com.AppexSolutions.gymsync.features.clients.presentation.screens.ClientsScreen
import com.AppexSolutions.gymsync.features.clients.presentation.screens.CreateUserScreen
import com.AppexSolutions.gymsync.features.clients.presentation.screens.EditClientScreen

private object AdminRoutes {
    const val DASHBOARD   = "admin/dashboard"
    const val SCANNER     = "admin/scanner"
    const val CLIENTS     = "admin/clients"
    const val CREATE_USER = "admin/clients/create"
    const val EDIT_CLIENT = "admin/clients/edit/{clientId}"
    fun editClient(clientId: Int) = "admin/clients/edit/$clientId"
}

@Composable
fun AdminNavGraph(
    appContainer: appContainer,
    onLogout: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val clientsModule = androidx.compose.runtime.remember { ClientsModule(appContainer) }

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val selectedTab = when (currentRoute) {
        AdminRoutes.DASHBOARD -> AdminTab.Dashboard.index
        AdminRoutes.SCANNER   -> AdminTab.Scanner.index
        AdminRoutes.CLIENTS   -> AdminTab.Clients.index
        else                  -> AdminTab.Dashboard.index
    }

    Scaffold(
        bottomBar = {
            AdminBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { tabIndex ->
                    val route = when (tabIndex) {
                        AdminTab.Dashboard.index -> AdminRoutes.DASHBOARD
                        AdminTab.Scanner.index   -> AdminRoutes.SCANNER
                        AdminTab.Clients.index   -> AdminRoutes.CLIENTS
                        else                     -> AdminRoutes.DASHBOARD
                    }
                    navController.navigate(route) {
                        popUpTo(AdminRoutes.DASHBOARD) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = AdminRoutes.DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        ) {

            // ── Dashboard (hiltViewModel internamente) ──
            composable(AdminRoutes.DASHBOARD) {
                DashboardScreen()
            }

            // ── Scanner QR (hiltViewModel internamente) ──
            composable(AdminRoutes.SCANNER) {
                ScannerScreen(
                    onNavigateBack = {
                        navController.navigate(AdminRoutes.DASHBOARD) {
                            popUpTo(AdminRoutes.DASHBOARD) { inclusive = true }
                        }
                    },
                    onScanSuccess = {
                        navController.navigate(AdminRoutes.DASHBOARD) {
                            popUpTo(AdminRoutes.DASHBOARD) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ── Clientes (factory manual — cadena legacy) ──
            composable(AdminRoutes.CLIENTS) {
                ClientsScreen(
                    factory = clientsModule.provideClientsViewModelFactory(),
                    shouldRefresh = currentRoute == AdminRoutes.CLIENTS,
                    onAddClient = { navController.navigate(AdminRoutes.CREATE_USER) },
                    onClientClick = { clientId -> navController.navigate(AdminRoutes.editClient(clientId)) },
                    onTabSelected = { _ -> },
                    onLogout = onLogout,
                    showBottomBar = false
                )
            }

            // ── Crear cliente ──
            composable(AdminRoutes.CREATE_USER) {
                CreateUserScreen(
                    factory = clientsModule.provideCreateUserViewModelFactory(),
                    onSuccess = {
                        navController.navigate(AdminRoutes.CLIENTS) {
                            popUpTo(AdminRoutes.CLIENTS) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Editar cliente ──
            composable(AdminRoutes.EDIT_CLIENT) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("clientId")?.toIntOrNull() ?: return@composable
                EditClientScreen(
                    factory = clientsModule.provideEditClientViewModelFactory(clientId),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
