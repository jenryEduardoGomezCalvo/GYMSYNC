package com.AppexSolutions.gymsync.features.users.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientsUsecase
import com.AppexSolutions.gymsync.features.users.presentation.screens.UserLoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserLoginViewModel(
    private val getClientsUsecase: GetClientsUsecase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserLoginUiState())
    val uiState = _uiState.asStateFlow()

    /** Cache de clientes traídos de la API */
    private var cachedClients: List<Client> = emptyList()

    init {
        fetchClients()
    }

    /** Trae la lista real de clientes desde GET /users/ */
    private fun fetchClients() {
        viewModelScope.launch {
            val result = getClientsUsecase()
            result.fold(
                onSuccess = { clients ->
                    cachedClients = clients
                },
                onFailure = { /* silencioso: si falla, el login simplemente dirá "correo incorrecto" */ }
            )
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        if (email.isEmpty() || password.isEmpty()) {
            _uiState.update { it.copy(error = "Por favor completa todos los campos") }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        // Match por email contra la lista real de la API
        val matched = cachedClients.firstOrNull {
            it.email.equals(email, ignoreCase = true) && it.activo
        }

        if (matched != null) {
            _uiState.update { it.copy(isLoading = false, loggedClientId = matched.id) }
        } else {
            // Si la lista no se cargó aún, reintentar fetch y luego validar
            if (cachedClients.isEmpty()) {
                retryLoginAfterFetch(email)
            } else {
                _uiState.update {
                    it.copy(isLoading = false, error = "Correo o contraseña incorrectos")
                }
            }
        }
    }

    /** Si la lista estaba vacía (red lenta), reintenta tras fetch */
    private fun retryLoginAfterFetch(email: String) {
        viewModelScope.launch {
            val result = getClientsUsecase()
            result.fold(
                onSuccess = { clients ->
                    cachedClients = clients
                    val matched = clients.firstOrNull {
                        it.email.equals(email, ignoreCase = true) && it.activo
                    }
                    if (matched != null) {
                        _uiState.update { it.copy(isLoading = false, loggedClientId = matched.id) }
                    } else {
                        _uiState.update {
                            it.copy(isLoading = false, error = "Correo o contraseña incorrectos")
                        }
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Error de conexión. Intenta de nuevo.")
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetLoginSuccess() {
        _uiState.update { it.copy(loggedClientId = null) }
    }
}
