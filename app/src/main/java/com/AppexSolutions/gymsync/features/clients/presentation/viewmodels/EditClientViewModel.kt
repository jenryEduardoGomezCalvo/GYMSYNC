package com.AppexSolutions.gymsync.features.clients.presentation.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.clients.domain.usecases.DeleteClientUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientByIdUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetProfilePhotoUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.SaveProfilePhotoUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.ToggleUserActiveUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.UpdateClientUseCase
import com.AppexSolutions.gymsync.features.clients.presentation.screens.EditClientUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditClientViewModel(
    private val clientId: Int,
    private val getClientByIdUseCase: GetClientByIdUseCase,
    private val updateClientUseCase: UpdateClientUseCase,
    private val deleteClientUseCase: DeleteClientUseCase,
    private val toggleUserActiveUseCase: ToggleUserActiveUseCase,
    private val saveProfilePhotoUseCase: SaveProfilePhotoUseCase? = null,
    private val getProfilePhotoUseCase: GetProfilePhotoUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditClientUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadClient()
        loadProfilePhoto()
    }

    private fun loadClient() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = getClientByIdUseCase(clientId)
            _uiState.update { state ->
                result.fold(
                    onSuccess = { client ->
                        state.copy(
                            client = client,
                            nombres = client.nombres,
                            apellidos = client.apellidos,
                            email = client.email,
                            telefono = client.telefono ?: "",
                            fechaNacimiento = client.fechaNacimiento ?: "",
                            activo = client.activo,
                            isLoading = false
                        )
                    },
                    onFailure = { e ->
                        state.copy(isLoading = false, error = e.message ?: "Error al cargar usuario")
                    }
                )
            }
        }
    }

    fun onNombresChange(v: String) { _uiState.update { it.copy(nombres = v) } }
    fun onApellidosChange(v: String) { _uiState.update { it.copy(apellidos = v) } }
    fun onEmailChange(v: String) { _uiState.update { it.copy(email = v) } }
    fun onTelefonoChange(v: String) { _uiState.update { it.copy(telefono = v) } }
    fun onFechaNacimientoChange(v: String) { _uiState.update { it.copy(fechaNacimiento = v) } }

    fun onProfileImageSelected(uri: Uri) {
        _uiState.update { it.copy(profileImageUri = uri) }
        // Persistir la foto localmente
        if (saveProfilePhotoUseCase != null) {
            viewModelScope.launch {
                saveProfilePhotoUseCase.invoke(clientId, uri)
            }
        }
    }

    private fun loadProfilePhoto() {
        if (getProfilePhotoUseCase == null) return
        viewModelScope.launch {
            getProfilePhotoUseCase.invoke(clientId).onSuccess { localPath ->
                if (localPath != null) {
                    _uiState.update {
                        it.copy(profileImageUri = Uri.fromFile(java.io.File(localPath)))
                    }
                }
            }
        }
    }

    fun saveChanges() {
        val s = _uiState.value
        if (s.nombres.isBlank() || s.apellidos.isBlank()) {
            _uiState.update { it.copy(error = "Nombres y apellidos son obligatorios") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = updateClientUseCase(
                userId = clientId,
                nombres = s.nombres,
                apellidos = s.apellidos,
                email = s.email.ifBlank { null },
                telefono = s.telefono.ifBlank { null },
                fechaNacimiento = s.fechaNacimiento.ifBlank { null }
            )
            _uiState.update { state ->
                result.fold(
                    onSuccess = { state.copy(isSaving = false, successMessage = "Usuario actualizado", navigateBack = true) },
                    onFailure = { e -> state.copy(isSaving = false, error = e.message ?: "Error al actualizar") }
                )
            }
        }
    }

    fun toggleActive() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = toggleUserActiveUseCase(clientId)
            _uiState.update { state ->
                result.fold(
                    onSuccess = { updated ->
                        state.copy(
                            isSaving = false, activo = updated.activo,
                            successMessage = if (updated.activo) "Usuario activado" else "Usuario desactivado"
                        )
                    },
                    onFailure = { e -> state.copy(isSaving = false, error = e.message ?: "Error") }
                )
            }
        }
    }

    fun showDeleteDialog() { _uiState.update { it.copy(showDeleteDialog = true) } }
    fun hideDeleteDialog() { _uiState.update { it.copy(showDeleteDialog = false) } }

    fun deleteClient() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, showDeleteDialog = false) }
            val result = deleteClientUseCase(clientId)
            _uiState.update { state ->
                result.fold(
                    onSuccess = { state.copy(isSaving = false, successMessage = "Usuario eliminado", navigateBack = true) },
                    onFailure = { e -> state.copy(isSaving = false, error = e.message ?: "Error al eliminar") }
                )
            }
        }
    }

    fun clearMessages() { _uiState.update { it.copy(error = null, successMessage = null) } }
    fun refresh() { loadClient() }
}
