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
import com.AppexSolutions.gymsync.features.auth.presentation.screens.RoleSelectionScreen
import com.AppexSolutions.gymsync.features.auth.presentation.screens.UserLoginScreen
import com.AppexSolutions.gymsync.features.clients.di.ClientsModule
import com.AppexSolutions.gymsync.features.clients.presentation.screens.ClientsScreen
import com.AppexSolutions.gymsync.features.clients.presentation.screens.CreateUserScreen
import com.AppexSolutions.gymsync.features.clients.presentation.screens.EditClientScreen
import com.AppexSolutions.gymsync.features.users.di.UserModule
import com.AppexSolutions.gymsync.features.users.presentation.screens.MembershipPlansScreen
import com.AppexSolutions.gymsync.features.users.presentation.screens.UserHomeScreen
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class Screen(val route: String) {
    object RoleSelection : Screen("role_selection")
    object Login : Screen("login")
    object UserLogin : Screen("user_login")
    object Clients : Screen("clients")
    object UserHome : Screen("user_home/{clientId}") {
        fun createRoute(clientId: Int) = "user_home/$clientId"
    }
    object MembershipPlans : Screen("membership_plans/{clientId}") {
        fun createRoute(clientId: Int) = "membership_plans/$clientId"
    }
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
    val userModule = remember { UserModule(appContainer) }

    NavHost(navController = navController, startDestination = Screen.RoleSelection.route) {

        // ── Selección de Rol ──
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onAdminSelected = {
                    navController.navigate(Screen.Login.route)
                },
                onUserSelected = {
                    navController.navigate(Screen.UserLogin.route)
                }
            )
        }

        // ── Login Admin ──
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Clients.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = false }
                    }
                }
            )
        }

        // ── Login Cliente (Miembro) ──
        composable(Screen.UserLogin.route) {
            UserLoginScreen(
                factory = userModule.provideUserLoginViewModelFactory(),
                onUserSelected = { clientId ->
                    navController.navigate(Screen.UserHome.createRoute(clientId)) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = false }
                    }
                }
            )
        }

        // ── Home Cliente con QR ──
        composable(
            route = Screen.UserHome.route,
            arguments = listOf(navArgument("clientId") { type = NavType.IntType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getInt("clientId") ?: 0
            UserHomeScreen(
                factory = userModule.provideUserViewModelFactory(clientId),
                onNavigateToPlans = {
                    navController.navigate(Screen.MembershipPlans.createRoute(clientId))
                },
                onNavigateToProfile = { },
                onTabSelected = { tabIndex ->
                    when (tabIndex) {
                        0 -> { /* ya estamos en home */ }
                        1 -> navController.navigate(Screen.MembershipPlans.createRoute(clientId))
                        2 -> { /* perfil */ }
                    }
                },
                onLogout = {
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Planes de Membresía ──
        composable(
            route = Screen.MembershipPlans.route,
            arguments = listOf(navArgument("clientId") { type = NavType.IntType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getInt("clientId") ?: 0
            MembershipPlansScreen(
                factory = userModule.provideUserViewModelFactory(clientId),
                onTabSelected = { tabIndex ->
                    when (tabIndex) {
                        0 -> navController.navigate(Screen.UserHome.createRoute(clientId)) {
                            popUpTo(Screen.MembershipPlans.route) { inclusive = true }
                        }
                        1 -> { /* ya estamos */ }
                        2 -> { /* perfil */ }
                    }
                }
            )
        }

        // ── Admin: Lista de Clientes ──
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
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Admin: Crear Usuario ──
        composable(Screen.CreateUser.route) {
            CreateUserScreen(
                factory = clientsModule.provideCreateUserViewModelFactory(),
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Admin: Editar Cliente ──
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
            PlaceholderScreen("Inicio Admin", navController::popBackStack)
        }
        composable(Screen.Profile.route) {
            PlaceholderScreen("Perfil Admin", navController::popBackStack)
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
