package com.AppexSolutions.gymsync.features.users.presentation.screens

data class UserLoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedClientId: Int? = null
)
