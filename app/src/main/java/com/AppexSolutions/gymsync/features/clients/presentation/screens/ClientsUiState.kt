package com.AppexSolutions.gymsync.features.clients.presentation.screens

import com.AppexSolutions.gymsync.features.clients.domain.entities.Client

data class ClientsUiState(
    val clients: List<Client> = emptyList(),
    val filteredClients: List<Client> = emptyList(),
    val searchQuery: String = "",
    val clientPhotoUris: Map<Int, String> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)
