package com.AppexSolutions.gymsync.features.clients.presentation.screens

import com.AppexSolutions.gymsync.features.clients.domain.entities.Gym
import com.AppexSolutions.gymsync.features.clients.domain.entities.Rol

data class CreateUserUiState(
    val nombres: String = "",
    val apellidos: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = "",
    val selectedRolId: Int? = null,
    val selectedGymId: Int? = null,
    val roles: List<Rol> = emptyList(),
    val gyms: List<Gym> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
