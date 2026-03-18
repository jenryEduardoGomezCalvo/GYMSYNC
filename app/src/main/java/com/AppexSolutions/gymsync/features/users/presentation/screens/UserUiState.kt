package com.AppexSolutions.gymsync.features.users.presentation.screens

import com.AppexSolutions.gymsync.features.users.domain.entities.MemberProfile
import com.AppexSolutions.gymsync.features.users.domain.entities.MembershipPlan

data class UserUiState(
    val profile: MemberProfile? = null,
    val plans: List<MembershipPlan> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: Int = 0, // 0=Home, 1=Plans, 2=Profile
    val profilePhotoUri: String? = null
)
