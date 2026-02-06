package com.AppexSolutions.gymsync.features.clients.data.datasource.remote.mapper

import com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model.ClientResponse
import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.entities.ClientStatus
import com.AppexSolutions.gymsync.features.clients.domain.entities.MembershipType

/**
 * Convierte ClientResponse (DTO de la API) → Client (Entidad de dominio)
 */
fun ClientResponse.toDomain(): Client {
    return Client(
        id = this.id ?: 0,  // Si la API no devuelve id, usar 0 por defecto
        name = this.nombre,
        membershipType = mapMembershipType(this.plan),
        status = mapClientStatus(this.status),
        avatarUrl = this.avatarUrl ?: ""  // Si no hay avatar, usar string vacío
    )
}

/**
 * Mapea el string del plan a MembershipType enum
 */
private fun mapMembershipType(plan: String?): MembershipType {
    return when (plan?.lowercase()) {
        "premium" -> MembershipType.PREMIUM
        "basica", "básica" -> MembershipType.BASICA
        "estandar", "estándar" -> MembershipType.ESTANDAR
        else -> MembershipType.BASICA  // Valor por defecto
    }
}

/**
 * Mapea el string del status a ClientStatus enum
 */
private fun mapClientStatus(status: String?): ClientStatus {
    return when (status?.lowercase()) {
        "activo", "active" -> ClientStatus.ACTIVO
        "inactivo", "inactive" -> ClientStatus.INACTIVO
        else -> ClientStatus.INACTIVO  // Valor por defecto
    }
}