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

    // Estado privado (mutable) - solo el ViewModel puede modificarlo
    private val _uiState = MutableStateFlow(LoginUiState())

    // Estado público (inmutable) - la UI solo puede leerlo
    val uiState = _uiState.asStateFlow()

    /**
     * Actualiza el email mientras el usuario escribe
     */
    fun onEmailChange(newEmail: String) {
        _uiState.update {
            it.copy(
                email = newEmail,
                error = null  // Limpiar error al escribir
            )
        }
    }

    /**
     * Actualiza la contraseña mientras el usuario escribe
     */
    fun onPasswordChange(newPassword: String) {
        _uiState.update {
            it.copy(
                password = newPassword,
                error = null  // Limpiar error al escribir
            )
        }
    }

    /**
     * Ejecuta el login cuando el usuario presiona "Iniciar sesión"
     */
    fun login() {
        val currentEmail = _uiState.value.email.trim()
        val currentPassword = _uiState.value.password

        // Validación de campos vacíos
        if (currentEmail.isEmpty() || currentPassword.isEmpty()) {
            _uiState.update {
                it.copy(error = "Por favor completa todos los campos")
            }
            return
        }

        // Validación de formato de email
        if (!isValidEmail(currentEmail)) {
            _uiState.update {
                it.copy(error = "Email inválido")
            }
            return
        }

        // Validación de longitud de contraseña
        if (currentPassword.length < 6) {
            _uiState.update {
                it.copy(error = "La contraseña debe tener al menos 6 caracteres")
            }
            return
        }

        // Iniciar proceso de login
        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                isLoginSuccessful = false
            )
        }


        // Llamada asíncrona al caso de uso
        viewModelScope.launch {
            val result = postUserUseCase(currentEmail, currentPassword)

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

    /**
     * Limpia el error (cuando el usuario cierra el diálogo de error)
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Resetea el estado de login exitoso
     * (útil después de navegar a otra pantalla)
     */
    fun resetLoginSuccess() {
        _uiState.update { it.copy(isLoginSuccessful = false) }
    }

    /**
     * Limpia todo el formulario
     */
    fun clearForm() {
        _uiState.update {
            LoginUiState()  // Resetear a estado inicial
        }
    }

    /**
     * Valida formato de email con regex simple
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        return email.matches(emailRegex.toRegex())
    }
}