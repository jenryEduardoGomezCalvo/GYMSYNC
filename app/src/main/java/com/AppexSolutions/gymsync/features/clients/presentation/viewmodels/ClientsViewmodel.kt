package com.AppexSolutions.gymsync.features.clients.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientsUsecase
import com.AppexSolutions.gymsync.features.clients.presentation.screens.ClientsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Clientes
 */
class ClientsViewModel(
    private val getClientsUsecase: GetClientsUsecase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadClients()
    }

    /**
     * Carga los clientes desde la API
     */
    private fun loadClients() {
        viewModelScope.launch {
            // Activar estado de carga
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Llamar al caso de uso
            val result = getClientsUsecase()

            // Actualizar el estado según el resultado
            _uiState.update { currentState ->
                result.fold(
                    onSuccess = { clients ->
                        currentState.copy(
                            clients = clients,
                            filteredClients = clients,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        currentState.copy(
                            clients = emptyList(),
                            filteredClients = emptyList(),
                            isLoading = false,
                            error = exception.message ?: "Error al cargar clientes"
                        )
                    }
                )
            }
        }
    }

    /**
     * Actualiza la búsqueda mientras el usuario escribe
     */
    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                state.clients
            } else {
                state.clients.filter { client ->
                    client.name.contains(query, ignoreCase = true)
                }
            }

            state.copy(
                searchQuery = query,
                filteredClients = filtered
            )
        }
    }

    /**
     * Limpia la búsqueda
     */
    fun clearSearch() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                filteredClients = it.clients
            )
        }
    }

    /**
     * Recarga los clientes (útil para pull-to-refresh)
     */
    fun refresh() {
        loadClients()
    }
}