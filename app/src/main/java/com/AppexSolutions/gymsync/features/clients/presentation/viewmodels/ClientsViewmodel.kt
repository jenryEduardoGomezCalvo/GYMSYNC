package com.AppexSolutions.gymsync.features.clients.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetAllClientPhotosUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientsUsecase
import com.AppexSolutions.gymsync.features.clients.presentation.screens.ClientsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientsViewModel @Inject constructor(
    private val getClientsUsecase: GetClientsUsecase,
    private val getAllClientPhotosUseCase: GetAllClientPhotosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientsUiState())
    val uiState = _uiState.asStateFlow()

    init { loadClients() }

    private fun loadClients() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = getClientsUsecase()
            _uiState.update { state ->
                result.fold(
                    onSuccess = { clients ->
                        state.copy(
                            clients = clients,
                            filteredClients = filterList(clients, state.searchQuery),
                            isLoading = false, error = null
                        )
                    },
                    onFailure = { e ->
                        state.copy(
                            clients = emptyList(), filteredClients = emptyList(),
                            isLoading = false, error = e.message ?: "Error al cargar clientes"
                        )
                    }
                )
            }
            loadClientPhotos()
        }
    }

    private suspend fun loadClientPhotos() {
        val clients = _uiState.value.clients
        if (clients.isEmpty()) return
        val photoMap = getAllClientPhotosUseCase(clients.map { it.id })
        _uiState.update { it.copy(clientPhotoUris = photoMap) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredClients = filterList(state.clients, query)
            )
        }
    }

    private fun filterList(
        clients: List<com.AppexSolutions.gymsync.features.clients.domain.entities.Client>,
        query: String
    ) = if (query.isBlank()) clients
        else clients.filter { it.nombreCompleto.contains(query, ignoreCase = true) ||
                it.email.contains(query, ignoreCase = true) }

    fun refresh() { loadClients() }
}
