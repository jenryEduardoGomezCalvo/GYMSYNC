package com.AppexSolutions.gymsync.features.users.data.mapper

import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.users.domain.entities.MemberProfile
import com.AppexSolutions.gymsync.features.users.domain.entities.PlanStatus

fun Client.toMemberProfile(): MemberProfile {
    val plans = listOf("Pro", "Premium", "Ultimate")
    val assignedPlan = plans[id % plans.size]
    val isActive = activo
    val status = if (isActive) PlanStatus.ACTIVO else PlanStatus.VENCIDO

    return MemberProfile(
        id = id,
        nombres = nombres,
        apellidos = apellidos,
        email = email,
        telefono = telefono,
        currentPlan = assignedPlan,
        planStatus = status,
        nextPaymentDate = if (isActive) "10 Abril 2026" else "—",
        daysRemaining = if (isActive) (15 + (id * 7) % 30) else 0,
        currentStreak = if (isActive) (3 + (id * 5) % 25) else 0,
        monthlyVisits = if (isActive) (5 + (id * 3) % 20) else 0,
        qrCode = "GYMSYNC-USR-${id.toString().padStart(3, '0')}-2026"
    )
}
