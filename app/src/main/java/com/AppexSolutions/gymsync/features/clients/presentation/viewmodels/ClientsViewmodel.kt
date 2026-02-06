package com.AppexSolutions.gymsync.features.clients.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.entities.ClientStatus
import com.AppexSolutions.gymsync.features.clients.domain.entities.MembershipType
import com.AppexSolutions.gymsync.features.clients.presentation.screens.ClientsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel para la pantalla de Clientes
 */
class ClientsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ClientsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadClients()
    }

    /**
     * Carga los clientes (hardcodeados por ahora)
     */
    private fun loadClients() {
        val hardcodedClients = listOf(
            Client(
                id = 1,
                name = "Sofía Martínez",
                membershipType = MembershipType.PREMIUM,
                status = ClientStatus.ACTIVO
            ),
            Client(
                id = 2,
                name = "Carlos Ruiz",
                membershipType = MembershipType.BASICA,
                status = ClientStatus.ACTIVO
            ),
            Client(
                id = 3,
                name = "Ana Gómez",
                membershipType = MembershipType.PREMIUM,
                status = ClientStatus.INACTIVO
            ),
            Client(
                id = 4,
                name = "Miguel Ángel",
                membershipType = MembershipType.ESTANDAR,
                status = ClientStatus.ACTIVO
            ),
            Client(
                id = 5,
                name = "Lucía Fernández",
                membershipType = MembershipType.PREMIUM,
                status = ClientStatus.ACTIVO
            )
        )

        _uiState.update {
            it.copy(
                clients = hardcodedClients,
                filteredClients = hardcodedClients
            )
        }
    }

    /**
     * Actualiza la búsqueda
     */
    fun onSearchQueryChange(query: String) {
        _uiState.update { currentState ->
            val filtered = if (query.isBlank()) {
                currentState.clients
            } else {
                currentState.clients.filter { client ->
                    client.name.contains(query, ignoreCase = true)
                }
            }

            currentState.copy(
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
}