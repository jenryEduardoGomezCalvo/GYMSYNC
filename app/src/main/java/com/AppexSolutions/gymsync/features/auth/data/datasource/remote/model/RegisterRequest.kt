package com.AppexSolutions.gymsync.features.auth.data.datasource.remote.model

data class RegisterRequest(
    val nombres: String,
    val apellidos: String,
    val email: String,
    val password: String,
    val telefono: String,
    val fechaNacimiento: String,  // Formato: "YYYY-MM-DD"
    val rolId: Int,
    val gymId: Int
)
