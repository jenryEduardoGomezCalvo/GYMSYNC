package com.AppexSolutions.gymsync.features.clients.domain.entities

enum class MembershipType(val displayName: String) {
    MENSUAL("Mensual"),
    TRIMESTRAL("Trimestral"),
    SEMESTRAL("Semestral"),
    ANUAL("Anual");

    // Método opcional por si necesitas buscar por nombre después
    companion object {
        fun fromDisplayName(name: String): MembershipType? {
            return entries.find { it.displayName == name }
        }
    }
}