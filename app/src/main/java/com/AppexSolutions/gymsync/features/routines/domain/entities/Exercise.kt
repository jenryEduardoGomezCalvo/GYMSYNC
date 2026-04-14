package com.AppexSolutions.gymsync.features.routines.domain.entities

data class Exercise(
    val id: Int,
    val name: String,
    val muscleGroup: MuscleGroup,
    val description: String,
    val imageUrl: String? = null
)
