package com.AppexSolutions.gymsync.features.clients.presentation.viewmodels

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.clients.domain.usecases.DeleteClientUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientByIdUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.ToggleUserActiveUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.UpdateClientUseCase
import com.AppexSolutions.gymsync.features.clients.presentation.screens.EditClientUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditClientViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getClientByIdUseCase: GetClientByIdUseCase,
    private val updateClientUseCase: UpdateClientUseCase,
    private val deleteClientUseCase: DeleteClientUseCase,
    private val toggleUserActiveUseCase: ToggleUserActiveUseCase
) : ViewModel() {

    private val clientId: Int = savedStateHandle["clientId"] ?: 0

    private val _uiState = MutableStateFlow(EditClientUiState())
    val uiState = _uiState.asStateFlow()

    init { loadClient() }

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
                            profileImageUri = client.profileImage?.let { Uri.parse(it) },
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
