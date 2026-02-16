package com.AppexSolutions.gymsync.features.clients.domain.entities

/**
 * Entidad de dominio — usuario del sistema
 */
data class Client(
    val id: Int,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val telefono: String?,
    val fechaNacimiento: String?,
    val activo: Boolean,
    val rolId: Int,
    val rolNombre: String,
    val gymId: Int?,
    val gymNombre: String?
) {
    val nombreCompleto: String get() = "$nombres $apellidos"
    val inicial: String get() = nombres.firstOrNull()?.uppercase() ?: "?"
}

/** Rol para los dropdowns */
data class Rol(val id: Int, val nombre: String, val descripcion: String?)

/** Gym para los dropdowns */
data class Gym(val id: Int, val nombre: String)
