package com.AppexSolutions.gymsync.features.clients.presentation.screens

import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.entities.ClientStatus
import com.AppexSolutions.gymsync.features.clients.domain.entities.MembershipType

/**
 * Estado de la pantalla de Editar Cliente
 */
data class EditClientUiState(
    val client: Client? = null,
    val name: String = "",
    val last_name: String="",
    val phone: String = "",
    val membershipType: MembershipType = MembershipType.BASICA,
    val registrationDate: String = "",
    val status: ClientStatus = ClientStatus.ACTIVO,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showDeleteDialog: Boolean = false
)