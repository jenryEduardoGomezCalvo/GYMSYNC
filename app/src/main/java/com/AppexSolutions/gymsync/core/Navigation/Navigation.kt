package com.AppexSolutions.gymsync.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.AppexSolutions.gymsync.core.di.appContainer
import com.AppexSolutions.gymsync.features.auth.presentation.screens.LoginScreen
import com.AppexSolutions.gymsync.features.clients.di.ClientsModule
import com.AppexSolutions.gymsync.features.clients.presentation.screens.ClientsScreen
import com.AppexSolutions.gymsync.features.clients.presentation.screens.CreateUserScreen
import com.AppexSolutions.gymsync.features.clients.presentation.screens.EditClientScreen
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Clients : Screen("clients")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object CreateUser : Screen("create_user")
    object EditClient : Screen("edit_client/{clientId}") {
        fun createRoute(clientId: Int) = "edit_client/$clientId"
    }
}

@Composable
fun AppNavigation(
    appContainer: appContainer,
    navController: NavHostController = rememberNavController()
) {
    val clientsModule = remember { ClientsModule(appContainer) }

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        // ── Login ──
        // Usa LoginViewModel (Hilt): guarda sesión en Room y soporta biometría.
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Clients.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Lista de Clientes ──
        composable(Screen.Clients.route) {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            ClientsScreen(
                factory = clientsModule.provideClientsViewModelFactory(),
                shouldRefresh = currentRoute == Screen.Clients.route,
                onAddClient = {
                    navController.navigate(Screen.CreateUser.route)
                },
                onClientClick = { clientId ->
                    navController.navigate(Screen.EditClient.createRoute(clientId))
                },
                onTabSelected = { tabIndex ->
                    when (tabIndex) {
                        0 -> navController.navigate(Screen.Home.route)
                        1 -> { /* ya estamos */ }
                        2 -> navController.navigate(Screen.Profile.route)
                    }
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Crear Usuario (super_admin crea cuentas) ──
        composable(Screen.CreateUser.route) {
            CreateUserScreen(
                factory = clientsModule.provideCreateUserViewModelFactory(),
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Editar Cliente/Usuario ──
        composable(
            route = Screen.EditClient.route,
            arguments = listOf(navArgument("clientId") { type = NavType.IntType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getInt("clientId") ?: 0
            EditClientScreen(
                factory = clientsModule.provideEditClientViewModelFactory(clientId),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Placeholders ──
        composable(Screen.Home.route) {
            PlaceholderScreen("Inicio", navController::popBackStack)
        }
        composable(Screen.Profile.route) {
            PlaceholderScreen("Perfil", navController::popBackStack)
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, onBack: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onBack) {
                Text(
                    "Volver",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
