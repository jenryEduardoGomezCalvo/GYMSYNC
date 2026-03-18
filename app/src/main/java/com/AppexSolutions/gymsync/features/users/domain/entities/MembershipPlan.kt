package com.AppexSolutions.gymsync.features.users.domain.entities

data class MembershipPlan(
    val id: Int,
    val name: String,
    val description: String,
    val pricePerMonth: Int,
    val features: List<String>,
    val isPopular: Boolean = false,
    val iconType: PlanIcon = PlanIcon.BOLT
)

enum class PlanIcon {
    BOLT, CROWN
}
