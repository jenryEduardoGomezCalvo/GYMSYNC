package com.AppexSolutions.gymsync.features.clients.presentation.screens

import com.AppexSolutions.gymsync.features.clients.domain.entities.Client

/**
 * Estado de la pantalla de Clientes
 */
data class ClientsUiState(
    val clients: List<Client> = emptyList(),
    val filteredClients: List<Client> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)