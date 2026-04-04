package com.AppexSolutions.gymsync.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.AppexSolutions.gymsync.features.admin.presentation.AdminNavGraph
import com.AppexSolutions.gymsync.features.auth.presentation.screens.LoginScreen
import com.AppexSolutions.gymsync.features.auth.presentation.screens.RoleSelectionScreen
import com.AppexSolutions.gymsync.features.auth.presentation.screens.UserLoginScreen
import com.AppexSolutions.gymsync.features.progress.presentation.screens.AddProgressEntryScreen
import com.AppexSolutions.gymsync.features.progress.presentation.screens.ProgressDashboardScreen
import com.AppexSolutions.gymsync.features.progress.presentation.screens.ProgressHistoryScreen
import com.AppexSolutions.gymsync.features.routines.navigation.routinesNavGraph
import com.AppexSolutions.gymsync.features.users.presentation.screens.MembershipPlansScreen
import com.AppexSolutions.gymsync.features.users.presentation.screens.UserHomeScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = RoleSelection) {

        // ── Selección de Rol ──
        composable<RoleSelection> {
            RoleSelectionScreen(
                onAdminSelected = {
                    navController.navigate(Login)
                },
                onUserSelected = {
                    navController.navigate(UserLogin)
                }
            )
        }

        // ── Login Admin ──
        composable<Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AdminHome) {
                        popUpTo<RoleSelection> { inclusive = false }
                    }
                }
            )
        }

        // ── Login Cliente (Miembro) ──
        composable<UserLogin> {
            UserLoginScreen(
                onUserSelected = { clientId ->
                    navController.navigate(UserHome(clientId)) {
                        popUpTo<RoleSelection> { inclusive = false }
                    }
                }
            )
        }

        // ── Admin NavGraph ──
        composable<AdminHome> {
            AdminNavGraph(
                onLogout = {
                    navController.navigate(RoleSelection) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Home Cliente con QR ──
        composable<UserHome> { backStackEntry ->
            val clientId = backStackEntry.toRoute<UserHome>().clientId
            UserHomeScreen(
                onNavigateToPlans = {
                    navController.navigate(MembershipPlans(clientId))
                },
                onNavigateToProfile = { },
                onTabSelected = { tabIndex ->
                    when (tabIndex) {
                        0 -> { /* ya estamos en home */ }
                        1 -> navController.navigate(MembershipPlans(clientId))
                        2 -> { /* perfil */ }
                        3 -> navController.navigate(Routines(clientId))
                        4 -> navController.navigate(ProgressDashboard(clientId))
                    }
                },
                onLogout = {
                    navController.navigate(RoleSelection) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Planes de Membresía ──
        composable<MembershipPlans> { backStackEntry ->
            val clientId = backStackEntry.toRoute<MembershipPlans>().clientId
            MembershipPlansScreen(
                onTabSelected = { tabIndex ->
                    when (tabIndex) {
                        0 -> navController.navigate(UserHome(clientId)) {
                            popUpTo<MembershipPlans> { inclusive = true }
                        }
                        1 -> { /* ya estamos */ }
                        2 -> { /* perfil */ }
                        3 -> navController.navigate(Routines(clientId))
                        4 -> navController.navigate(ProgressDashboard(clientId))
                    }
                }
            )
        }

        // ── Rutinas ──
        routinesNavGraph(navController)

        // ── Progreso Físico ──
        composable<ProgressDashboard> { backStackEntry ->
            val userId = backStackEntry.toRoute<ProgressDashboard>().userId
            ProgressDashboardScreen(
                userId = userId,
                onNavigateToAdd = { navController.navigate(AddProgressEntry(userId)) },
                onNavigateToHistory = { navController.navigate(ProgressHistory(userId)) },
                onTabSelected = { tabIndex ->
                    when (tabIndex) {
                        0 -> navController.navigate(UserHome(userId)) {
                            popUpTo<ProgressDashboard> { inclusive = true }
                        }
                        1 -> navController.navigate(MembershipPlans(userId))
                        3 -> navController.navigate(Routines(userId))
                        4 -> { /* ya estamos */ }
                    }
                }
            )
        }

        composable<AddProgressEntry> { backStackEntry ->
            val userId = backStackEntry.toRoute<AddProgressEntry>().userId
            AddProgressEntryScreen(
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<ProgressHistory> { backStackEntry ->
            val userId = backStackEntry.toRoute<ProgressHistory>().userId
            ProgressHistoryScreen(
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
