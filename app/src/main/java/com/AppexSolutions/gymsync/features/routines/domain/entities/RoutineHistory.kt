package com.AppexSolutions.gymsync.features.routines.domain.entities

data class RoutineHistory(
    val id: Int,
    val routineId: Int,
    val routineName: String,
    val userId: Int,
    val completedAt: Long
)
