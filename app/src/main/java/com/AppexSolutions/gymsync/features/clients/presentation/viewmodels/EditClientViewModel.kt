package com.AppexSolutions.gymsync.features.clients.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.clients.domain.entities.ClientStatus
import com.AppexSolutions.gymsync.features.clients.domain.entities.MembershipType
import com.AppexSolutions.gymsync.features.clients.domain.usecases.DeleteClientUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientByIdUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.UpdateClientUseCase
import com.AppexSolutions.gymsync.features.clients.presentation.screens.EditClientUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para Editar Cliente
 */
class EditClientViewModel(
    private val clientId: Int,
    private val getClientByIdUseCase: GetClientByIdUseCase,
    private val updateClientUseCase: UpdateClientUseCase,
    private val deleteClientUseCase: DeleteClientUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditClientUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadClient()
    }

    /**
     * Carga los datos del cliente desde la API
     */
    private fun loadClient() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = getClientByIdUseCase(clientId)

            _uiState.update { currentState ->
                result.fold(
                    onSuccess = { client ->

                        val safeName = client.name.orEmpty()

                        val parts = safeName
                            .replace("null", "", ignoreCase = true)
                            .trim()
                            .split(" ", limit = 2)

                        currentState.copy(
                            client = client,
                            name = parts.getOrNull(0).orEmpty(),
                            last_name = parts.getOrNull(1).orEmpty(),
                            membershipType = client.membershipType,
                            status = client.status,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        currentState.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al cargar el cliente"
                        )
                    }
                )
            }
        }
    }

    /**
     * Actualiza el nombre
     */
    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    /**
     * Actualiza el teléfono
     */
    fun onPhoneChange(newPhone: String) {
        _uiState.update { it.copy(phone = newPhone) }
    }

    /**
     * Actualiza el tipo de membresía
     */
    fun onMembershipTypeChange(newType: MembershipType) {
        _uiState.update { it.copy(membershipType = newType) }
    }

    /**
     * Actualiza la fecha de inscripción
     */
    fun onRegistrationDateChange(newDate: String) {
        _uiState.update { it.copy(registrationDate = newDate) }
    }

    /**
     * Actualiza el estado
     */
    fun onStatusChange(newStatus: ClientStatus) {
        _uiState.update { it.copy(status = newStatus) }
    }

    /**
     * Guarda los cambios en la API
     */
    fun saveChanges() {
        val currentState = _uiState.value

        // Validaciones
        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            // Crear el objeto cliente actualizado
            val updatedClient = currentState.client?.copy(
                name = currentState.name,
                membershipType = currentState.membershipType,
                status = currentState.status
            )

            if (updatedClient == null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Error: No se pudo obtener los datos del cliente"
                    )
                }
                return@launch
            }

            // Llamar al UseCase
            val result = updateClientUseCase(updatedClient)

            _uiState.update { currentState ->
                result.fold(
                    onSuccess = {
                        currentState.copy(
                            isSaving = false,
                            successMessage = "Cliente actualizado correctamente",
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        currentState.copy(
                            isSaving = false,
                            error = exception.message ?: "Error al actualizar el cliente",
                            successMessage = null
                        )
                    }
                )
            }
        }
    }

    /**
     * Muestra el diálogo de confirmación para eliminar
     */
    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    /**
     * Oculta el diálogo de confirmación
     */
    fun hideDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    /**
     * Elimina el cliente de la API
     */
    fun deleteClient() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    showDeleteDialog = false,
                    error = null
                )
            }

            val result = deleteClientUseCase(clientId)

            _uiState.update { currentState ->
                result.fold(
                    onSuccess = {
                        currentState.copy(
                            isSaving = false,
                            successMessage = "Cliente eliminado correctamente",
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        currentState.copy(
                            isSaving = false,
                            error = exception.message ?: "Error al eliminar el cliente",
                            successMessage = null
                        )
                    }
                )
            }
        }
    }

    /**
     * Limpia los mensajes de error y éxito
     */
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    /**
     * Recarga los datos del cliente
     */
    fun refresh() {
        loadClient()
    }
}