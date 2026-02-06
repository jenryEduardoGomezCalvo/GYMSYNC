package com.AppexSolutions.gymsync.features.auth.presentation.screens

import com.AppexSolutions.gymsync.features.auth.domain.entities.AuthSession


data class LoginUiState(
    val isLoading: Boolean = false,
    val authSession: AuthSession? = null,
    val error: String? = null,
    val email: String = "",
    val password: String = "",
    val isLoginSuccessful: Boolean = false
)