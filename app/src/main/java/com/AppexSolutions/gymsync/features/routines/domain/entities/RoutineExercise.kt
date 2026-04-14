package com.AppexSolutions.gymsync.features.routines.domain.entities

data class RoutineExercise(
    val id: Int,
    val exercise: Exercise,
    val sets: Int,
    val reps: Int,
    val restSeconds: Int,
    val sortOrder: Int
)
