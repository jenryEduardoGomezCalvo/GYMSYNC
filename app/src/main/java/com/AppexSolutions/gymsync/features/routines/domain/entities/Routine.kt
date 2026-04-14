package com.AppexSolutions.gymsync.features.routines.domain.entities

data class Routine(
    val id: Int,
    val userId: Int,
    val name: String,
    val days: List<Int>,               // 1=Lun … 7=Dom (ISO 8601)
    val notificationHour: Int,
    val notificationMinute: Int,
    val exercises: List<RoutineExercise>,
    val createdAt: Long
)
