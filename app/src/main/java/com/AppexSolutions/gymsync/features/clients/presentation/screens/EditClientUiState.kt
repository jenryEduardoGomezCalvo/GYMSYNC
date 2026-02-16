package com.AppexSolutions.gymsync.features.clients.presentation.screens

import com.AppexSolutions.gymsync.features.clients.domain.entities.Client

data class EditClientUiState(
    val client: Client? = null,
    val nombres: String = "",
    val apellidos: String = "",
    val email: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = "",
    val activo: Boolean = true,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showDeleteDialog: Boolean = false
)
