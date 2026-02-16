package com.AppexSolutions.gymsync.features.clients.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.clients.domain.usecases.CreateUserUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetGymsUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetRolesUseCase
import com.AppexSolutions.gymsync.features.clients.presentation.screens.CreateUserUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateUserViewModel(
    private val createUserUseCase: CreateUserUseCase,
    private val getRolesUseCase: GetRolesUseCase,
    private val getGymsUseCase: GetGymsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateUserUiState())
    val uiState = _uiState.asStateFlow()

    init { loadRolesAndGyms() }

    private fun loadRolesAndGyms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val rolesResult = getRolesUseCase()
                val gymsResult = getGymsUseCase()
                _uiState.update { state ->
                    state.copy(
                        roles = rolesResult.getOrDefault(emptyList()),
                        gyms = gymsResult.getOrDefault(emptyList()),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onNombresChange(v: String) { _uiState.update { it.copy(nombres = v) } }
    fun onApellidosChange(v: String) { _uiState.update { it.copy(apellidos = v) } }
    fun onEmailChange(v: String) { _uiState.update { it.copy(email = v) } }
    fun onPasswordChange(v: String) { _uiState.update { it.copy(password = v) } }
    fun onConfirmPasswordChange(v: String) { _uiState.update { it.copy(confirmPassword = v) } }
    fun onTelefonoChange(v: String) { _uiState.update { it.copy(telefono = v) } }
    fun onFechaNacimientoChange(v: String) { _uiState.update { it.copy(fechaNacimiento = v) } }
    fun onRolSelected(rolId: Int) { _uiState.update { it.copy(selectedRolId = rolId) } }
    fun onGymSelected(gymId: Int?) { _uiState.update { it.copy(selectedGymId = gymId) } }

    fun createUser() {
        val s = _uiState.value

        // Validaciones
        if (s.nombres.isBlank() || s.apellidos.isBlank()) {
            _uiState.update { it.copy(error = "Nombres y apellidos son obligatorios") }; return
        }
        if (s.email.isBlank()) {
            _uiState.update { it.copy(error = "El email es obligatorio") }; return
        }
        if (s.password.length < 6) {
            _uiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }; return
        }
        if (!s.password.matches(Regex(".*[A-Z].*")) || !s.password.matches(Regex(".*[a-z].*")) || !s.password.matches(Regex(".*\\d.*"))) {
            _uiState.update { it.copy(error = "La contraseña debe tener mayúscula, minúscula y número") }; return
        }
        if (s.password != s.confirmPassword) {
            _uiState.update { it.copy(error = "Las contraseñas no coinciden") }; return
        }
        if (s.selectedRolId == null) {
            _uiState.update { it.copy(error = "Selecciona un rol") }; return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = createUserUseCase(
                nombres = s.nombres,
                apellidos = s.apellidos,
                email = s.email,
                password = s.password,
                telefono = s.telefono.ifBlank { null },
                fechaNacimiento = s.fechaNacimiento.ifBlank { null },
                rolId = s.selectedRolId,
                gymId = s.selectedGymId
            )
            _uiState.update { state ->
                result.fold(
                    onSuccess = { state.copy(isSaving = false, successMessage = "Usuario creado exitosamente") },
                    onFailure = { e -> state.copy(isSaving = false, error = e.message ?: "Error al crear usuario") }
                )
            }
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
