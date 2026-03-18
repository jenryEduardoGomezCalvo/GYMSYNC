package com.AppexSolutions.gymsync.features.users.domain.entities

data class MemberProfile(
    val id: Int,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val telefono: String?,
    val currentPlan: String,
    val planStatus: PlanStatus,
    val nextPaymentDate: String,
    val daysRemaining: Int,
    val currentStreak: Int,
    val monthlyVisits: Int,
    val qrCode: String
) {
    val nombreCompleto: String get() = "$nombres $apellidos"
    val inicial: String get() = nombres.firstOrNull()?.uppercase() ?: "?"
}

enum class PlanStatus(val label: String) {
    ACTIVO("Activo"),
    VENCIDO("Vencido"),
    POR_VENCER("Por Vencer")
}
