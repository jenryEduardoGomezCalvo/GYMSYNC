package com.AppexSolutions.gymsync.features.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.auth.domain.usecases.RegisterUserUseCase
import com.AppexSolutions.gymsync.features.auth.presentation.screens.RegisterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Registro
 *
 * Maneja:
 * - Estado del formulario (nombres, apellidos, email, password, etc.)
 * - Validaciones de entrada
 * - Llamada al caso de uso de registro
 * - Estados de carga y error
 */
class RegisterViewModel(
    private val registerUserUseCase: RegisterUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    // ── Funciones para actualizar campos ──

    fun onNombresChange(value: String) {
        _uiState.update { it.copy(nombres = value, error = null) }
    }

    fun onApellidosChange(value: String) {
        _uiState.update { it.copy(apellidos = value, error = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, error = null) }
    }

    fun onTelefonoChange(value: String) {
        _uiState.update { it.copy(telefono = value, error = null) }
    }

    fun onFechaNacimientoChange(value: String) {
        _uiState.update { it.copy(fechaNacimiento = value, error = null) }
    }

    fun onRolChange(id: Int, nombre: String) {
        _uiState.update { it.copy(rolId = id, rolNombre = nombre, error = null) }
    }

    fun onGymChange(id: Int, nombre: String) {
        _uiState.update { it.copy(gymId = id, gymNombre = nombre, error = null) }
    }

    // ── Registro ──

    fun register() {
        val state = _uiState.value

        // Validaciones
        if (state.nombres.isBlank() || state.apellidos.isBlank() ||
            state.email.isBlank() || state.password.isBlank() ||
            state.confirmPassword.isBlank() || state.telefono.isBlank() ||
            state.fechaNacimiento.isBlank()
        ) {
            _uiState.update { it.copy(error = "Por favor completa todos los campos") }
            return
        }

        if (!isValidEmail(state.email.trim())) {
            _uiState.update { it.copy(error = "Email inválido") }
            return
        }

        if (state.password.length < 6) {
            _uiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
            return
        }

        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(error = "Las contraseñas no coinciden") }
            return
        }

        if (state.rolId == 0) {
            _uiState.update { it.copy(error = "Selecciona un rol") }
            return
        }

        if (state.gymId == 0) {
            _uiState.update { it.copy(error = "Selecciona un gimnasio") }
            return
        }

        // Iniciar registro
        _uiState.update {
            it.copy(isLoading = true, error = null, isRegisterSuccessful = false)
        }

        viewModelScope.launch {
            val result = registerUserUseCase(
                nombres = state.nombres.trim(),
                apellidos = state.apellidos.trim(),
                email = state.email.trim(),
                password = state.password,
                telefono = state.telefono.trim(),
                fechaNacimiento = state.fechaNacimiento,
                rolId = state.rolId,
                gymId = state.gymId
            )

            _uiState.update { currentState ->
                result.fold(
                    onSuccess = {
                        currentState.copy(
                            isLoading = false,
                            isRegisterSuccessful = true,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        currentState.copy(
                            isLoading = false,
                            isRegisterSuccessful = false,
                            error = exception.message ?: "Error al registrar usuario"
                        )
                    }
                )
            }
        }
    }

    // ── Utilidades ──

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetRegisterSuccess() {
        _uiState.update { it.copy(isRegisterSuccessful = false) }
    }

    fun clearForm() {
        _uiState.update { RegisterUiState() }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        return email.matches(emailRegex.toRegex())
    }
}
