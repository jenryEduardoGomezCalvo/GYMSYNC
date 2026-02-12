package com.AppexSolutions.gymsync.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.AppexSolutions.gymsync.core.di.appContainer
import com.AppexSolutions.gymsync.features.auth.di.AuthModule
import com.AppexSolutions.gymsync.features.auth.presentation.screens.LoginScreen
import com.AppexSolutions.gymsync.features.clients.di.ClientsModule
import com.AppexSolutions.gymsync.features.clients.presentation.screens.ClientsScreen
import com.AppexSolutions.gymsync.features.clients.presentation.screens.EditClientScreen

/**
 * Rutas de navegación de la app
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Clients : Screen("clients")
    object Home : Screen("home")
    object Profile : Screen("profile")

    // ✅ Nueva ruta para Editar Cliente
    object EditClient : Screen("edit_client/{clientId}") {
        fun createRoute(clientId: Int) = "edit_client/$clientId"
    }
}

/**
 * Sistema de navegación de la app
 */
@Composable
fun AppNavigation(
    appContainer: appContainer,
    navController: NavHostController = rememberNavController()
) {
    val authModule = AuthModule(appContainer)
    val clientsModule = ClientsModule(appContainer)

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // ✅ Pantalla de Login
        composable(Screen.Login.route) {
            LoginScreen(
                factory = authModule.provideLoginViewModelFactory(),
                onLoginSuccess = {
                    navController.navigate(Screen.Clients.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ✅ Pantalla de Lista de Clientes
        composable(Screen.Clients.route) {
            ClientsScreen(
                factory = clientsModule.provideClientsViewModelFactory(),
                onAddClient = {
                    // TODO: Navegar a pantalla de agregar cliente
                    println("➕ Agregar cliente")
                },
                onClientClick = { clientId ->
                    // ✅ Navegar a editar cliente
                    navController.navigate(Screen.EditClient.createRoute(clientId))
                },
                onTabSelected = { tabIndex ->
                    when (tabIndex) {
                        0 -> {
                            // Navegar a Home
                            navController.navigate(Screen.Home.route)
                        }
                        1 -> {
                            // Ya estamos en Clientes
                        }
                        2 -> {
                            // Navegar a Perfil
                            navController.navigate(Screen.Profile.route)
                        }
                    }
                }
            )
        }

        // ✅ Pantalla de Editar Cliente
        composable(
            route = Screen.EditClient.route,
            arguments = listOf(
                navArgument("clientId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getInt("clientId") ?: 0

            EditClientScreen(
                factory = clientsModule.provideEditClientViewModelFactory(clientId),
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ✅ Pantalla de Home (placeholder)
        composable(Screen.Home.route) {
            PlaceholderScreen(
                title = "🏠 Home",
                onBack = { navController.popBackStack() }
            )
        }

        // ✅ Pantalla de Perfil (placeholder)
        composable(Screen.Profile.route) {
            PlaceholderScreen(
                title = "👤 Perfil",
                onBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Pantalla placeholder para rutas no implementadas
 */
@Composable
fun PlaceholderScreen(
    title: String,
    onBack: () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color(0xFF0A1628)),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text(
                text = title,
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 32.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            androidx.compose.foundation.layout.Spacer(
                modifier = androidx.compose.ui.Modifier.height(24.dp)
            )
            androidx.compose.material3.Button(onClick = onBack) {
                androidx.compose.material3.Text("Volver")
            }
        }
    }
}