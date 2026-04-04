package com.AppexSolutions.gymsync.features.users.presentation.screens

data class UserLoginUiState(
    val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val error: String? = null,
    val loggedClientId: Int? = null,
    val showBiometricButton: Boolean = false,
    val biometricLoginInProgress: Boolean = false,
    val showEnableBiometricDialog: Boolean = false,
    val lastLoggedEmail: String = ""
)
