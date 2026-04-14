package com.AppexSolutions.gymsync.features.routines.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.AppexSolutions.gymsync.core.navigation.CreateRoutine
import com.AppexSolutions.gymsync.core.navigation.ExercisePicker
import com.AppexSolutions.gymsync.core.navigation.RoutineDetail
import com.AppexSolutions.gymsync.core.navigation.RoutineHistory
import com.AppexSolutions.gymsync.core.navigation.Routines
import com.AppexSolutions.gymsync.features.routines.presentation.screens.CreateRoutineScreen
import com.AppexSolutions.gymsync.features.routines.presentation.screens.ExercisePickerScreen
import com.AppexSolutions.gymsync.features.routines.presentation.screens.RoutineDetailScreen
import com.AppexSolutions.gymsync.features.routines.presentation.screens.RoutineHistoryScreen
import com.AppexSolutions.gymsync.features.routines.presentation.screens.RoutinesScreen

fun NavGraphBuilder.routinesNavGraph(navController: NavController) {
    composable<Routines> { backStackEntry ->
        val route: Routines = backStackEntry.toRoute()
        RoutinesScreen(
            userId = route.userId,
            onNavigateToCreate = { navController.navigate(CreateRoutine(route.userId)) },
            onNavigateToDetail = { id -> navController.navigate(RoutineDetail(routineId = id, userId = route.userId)) },
            onNavigateToHistory = { navController.navigate(RoutineHistory(route.userId)) }
        )
    }
    composable<CreateRoutine> { backStackEntry ->
        val route: CreateRoutine = backStackEntry.toRoute()
        CreateRoutineScreen(
            userId = route.userId,
            onRoutineCreated = { routineId ->
                navController.navigate(ExercisePicker(routineId)) {
                    popUpTo<CreateRoutine> { inclusive = true }
                }
            },
            onBack = { navController.popBackStack() }
        )
    }
    composable<RoutineDetail> { backStackEntry ->
        val route: RoutineDetail = backStackEntry.toRoute()
        RoutineDetailScreen(
            routineId = route.routineId,
            userId = route.userId,
            onBack = { navController.popBackStack() },
            onAddExercises = { id -> navController.navigate(ExercisePicker(id)) }
        )
    }
    composable<ExercisePicker> { backStackEntry ->
        val route: ExercisePicker = backStackEntry.toRoute()
        ExercisePickerScreen(
            routineId = route.routineId,
            onBack = { navController.popBackStack() }
        )
    }
    composable<RoutineHistory> { backStackEntry ->
        val route: RoutineHistory = backStackEntry.toRoute()
        RoutineHistoryScreen(
            userId = route.userId,
            onBack = { navController.popBackStack() }
        )
    }
}
