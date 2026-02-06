package com.AppexSolutions.gymsync.features.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.auth.domain.usecases.PostUserUseCase
import com.AppexSolutions.gymsync.features.auth.presentation.screens.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Login
 *
 * Maneja:
 * - Estado del formulario (email, password)
 * - Validaciones de entrada
 * - Llamada al caso de uso de login
 * - Estados de carga y error
 */

class GymViewModel(
    private val postUserUseCase: PostUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _uiState.update {
            it.copy(
                email = newEmail,
                error = null
            )
        }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update {
            it.copy(
                password = newPassword,
                error = null
            )
        }
    }

    fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isEmpty() || password.isEmpty()) {
            _uiState.update {
                it.copy(error = "Por favor completa todos los campos")
            }
            return
        }

        if (!isValidEmail(email)) {
            _uiState.update {
                it.copy(error = "Email inválido")
            }
            return
        }

        if (password.length < 6) {
            _uiState.update {
                it.copy(error = "La contraseña debe tener al menos 6 caracteres")
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                isLoginSuccessful = false
            )
        }

        loginWithApi(email, password)
    }

    private fun loginWithApi(email: String, password: String) {
        viewModelScope.launch {
            val result = postUserUseCase(email, password)

            _uiState.update { currentState ->
                result.fold(
                    onSuccess = { authSession ->
                        currentState.copy(
                            isLoading = false,
                            authSession = authSession,
                            isLoginSuccessful = true,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        currentState.copy(
                            isLoading = false,
                            authSession = null,
                            isLoginSuccessful = false,
                            error = exception.message ?: "Error al iniciar sesión"
                        )
                    }
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetLoginSuccess() {
        _uiState.update { it.copy(isLoginSuccessful = false) }
    }

    fun clearForm() {
        _uiState.update { LoginUiState() }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(emailRegex.toRegex())
    }
}
