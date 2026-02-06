package com.AppexSolutions.gymsync.features.clients.domain.entities

/**
 * Entidad de Cliente
 */
data class Client(
    val id: Int,
    val name: String,
    val membershipType: MembershipType,
    val status: ClientStatus,
    val avatarUrl: String = ""  // URL del avatar (vacío por ahora)
)

/**
 * Tipos de membresía
 */
enum class MembershipType(val displayName: String) {
    PREMIUM("Premium"),
    BASICA("Básica"),
    ESTANDAR("Estándar")
}

/**
 * Estados del cliente
 */
enum class ClientStatus(val displayName: String) {
    ACTIVO("Activo"),
    INACTIVO("Inactivo")
}