package com.AppexSolutions.gymsync.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.AppexSolutions.gymsync.core.di.appContainer
import com.AppexSolutions.gymsync.features.auth.di.AuthModule
import com.AppexSolutions.gymsync.features.auth.presentation.screens.LoginScreen
import com.AppexSolutions.gymsync.features.clients.di.ClientsModule
import com.AppexSolutions.gymsync.features.clients.presentation.screens.ClientsScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Clients : Screen("clients")
    object Home : Screen("home")
    object Profile : Screen("profile")
}

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

        composable(Screen.Clients.route) {
            ClientsScreen(
                factory = clientsModule.provideClientsViewModelFactory()
            )
        }
    }
}
