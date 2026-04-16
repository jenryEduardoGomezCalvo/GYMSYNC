package com.AppexSolutions.gymsync.core.navigation

import kotlinx.serialization.Serializable

@Serializable object RoleSelection
@Serializable object Login
@Serializable object UserLogin
@Serializable object AdminHome
@Serializable data class UserHome(val clientId: Int)
@Serializable data class MembershipPlans(val clientId: Int)
@Serializable object Home
@Serializable object Profile

// Routines feature routes
@Serializable data class Routines(val userId: Int)
@Serializable data class CreateRoutine(val userId: Int)
@Serializable data class RoutineDetail(val routineId: Int, val userId: Int)
@Serializable data class ExercisePicker(val routineId: Int)
@Serializable data class RoutineHistory(val userId: Int)

// Progress feature routes
@Serializable data class ProgressDashboard(val userId: Int)
@Serializable data class AddProgressEntry(val userId: Int)
@Serializable data class ProgressHistory(val userId: Int)

// Notifications (cliente)
@Serializable data class Notifications(val clientId: Int)
