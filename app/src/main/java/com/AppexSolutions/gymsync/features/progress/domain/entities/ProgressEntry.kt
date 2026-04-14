package com.AppexSolutions.gymsync.features.progress.domain.entities

import java.util.Date

data class ProgressEntry(
    val id: Int = 0,
    val userId: Int,
    val date: Date,
    val weight: Float? = null,   // kg
    val waist: Float? = null,    // cm
    val hips: Float? = null,     // cm
    val chest: Float? = null,    // cm
    val arms: Float? = null,     // cm
    val photoUri: String? = null,
    val notes: String? = null
)
