package com.AppexSolutions.gymsync.features.clients.presentation.viewmodels

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.core.util.FormValidator
import com.AppexSolutions.gymsync.core.util.formatToIsoTimestamp
import com.AppexSolutions.gymsync.features.clients.domain.usecases.CreateUserUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetGymsUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetRolesUseCase
import com.AppexSolutions.gymsync.features.clients.presentation.screens.CreateUserUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateUserViewModel @Inject constructor(
    private val createUserUseCase: CreateUserUseCase,
    private val getRolesUseCase: GetRolesUseCase,
    private val getGymsUseCase: GetGymsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateUserUiState())
    val uiState = _uiState.asStateFlow()

    init { loadRolesAndGyms() }

    fun loadRolesAndGyms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val rolesResult = getRolesUseCase()
            val gymsResult = getGymsUseCase()

            val errorMsg = rolesResult.exceptionOrNull()?.let { "Error cargando roles: ${it.message}" }
                ?: gymsResult.exceptionOrNull()?.let { "Error cargando gimnasios: ${it.message}" }

            _uiState.update { state ->
                state.copy(
                    roles = rolesResult.getOrDefault(emptyList()),
                    gyms = gymsResult.getOrDefault(emptyList()),
                    isLoading = false,
                    error = errorMsg
                )
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
    fun onProfileImageSelected(uri: Uri) { _uiState.update { it.copy(profileImageUri = uri) } }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createUser() {
        val s = _uiState.value
        if (s.isSaving) return

        val passwordError = FormValidator.validatePassword(s.password)

        val error = when {
            s.nombres.isBlank() || s.apellidos.isBlank() -> "Nombres y apellidos obligatorios"
            !FormValidator.isValidEmail(s.email) -> "Email inválido"
            passwordError != null -> passwordError
            s.password != s.confirmPassword -> "Las contraseñas no coinciden"
            s.selectedRolId == null -> "Selecciona un rol"
            else -> null
        }

        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }

        val isoFechaNacimiento = s.fechaNacimiento.takeIf { it.isNotBlank() }?.let {
            formatToIsoTimestamp(it)
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            createUserUseCase(
                nombres = s.nombres,
                apellidos = s.apellidos,
                email = s.email,
                password = s.password,
                telefono = s.telefono.takeIf { it.isNotBlank() },
                fechaNacimiento = isoFechaNacimiento,
                rolId = s.selectedRolId!!,
                gymId = s.selectedGymId,
                profileImageUri = s.profileImageUri
            ).onSuccess {
                _uiState.update { it.copy(isSaving = false, successMessage = "Creado con éxito") }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Error de red") }
            }
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
