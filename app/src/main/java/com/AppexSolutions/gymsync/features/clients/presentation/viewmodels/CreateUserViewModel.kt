package com.AppexSolutions.gymsync.features.clients.presentation.viewmodels

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.core.util.FormValidator
import com.AppexSolutions.gymsync.features.clients.domain.usecases.CreateUserUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetGymsUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetRolesUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.SaveProfilePhotoUseCase
import com.AppexSolutions.gymsync.features.clients.presentation.screens.CreateUserUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentIsoTimestamp(): String {
    // Genera: 2026-02-16T05:07:53.196Z
    return ZonedDateTime.now(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatToIsoTimestamp(dateString: String): String? {
    if (dateString.isBlank()) return null
    return try {
        // Asumiendo que dateString viene como "yyyy-MM-dd" desde el input del usuario
        val localDate = java.time.LocalDate.parse(dateString)
        localDate.atStartOfDay(ZoneOffset.UTC)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
    } catch (e: Exception) {
        null // O manejar error de formato
    }
}
class CreateUserViewModel(
    private val createUserUseCase: CreateUserUseCase,
    private val getRolesUseCase: GetRolesUseCase,
    private val getGymsUseCase: GetGymsUseCase,
    private val saveProfilePhotoUseCase: SaveProfilePhotoUseCase? = null
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
                telefono = s.telefono.takeIf { it.isNotBlank() }, // ✅ Más limpio que ifBlank
                fechaNacimiento = isoFechaNacimiento,
                rolId = s.selectedRolId!!,
                gymId = s.selectedGymId
            ).onSuccess { createdClient ->
                // Guardar foto de perfil localmente si se seleccionó una
                val photoUri = _uiState.value.profileImageUri
                if (photoUri != null && saveProfilePhotoUseCase != null) {
                    saveProfilePhotoUseCase.invoke(createdClient.id, photoUri)
                }
                _uiState.update { it.copy(isSaving = false, successMessage = "Creado con éxito") }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Error de red") }
            }
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
