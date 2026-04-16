package com.AppexSolutions.gymsync.features.users.presentation.viewmodels

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.auth.data.datasource.hardware.BiometricAuthManager
import com.AppexSolutions.gymsync.features.auth.domain.usecases.BiometricLoginResult
import com.AppexSolutions.gymsync.features.users.domain.usecases.EnableMemberBiometricUseCase
import com.AppexSolutions.gymsync.features.users.domain.usecases.HasMemberBiometricSessionUseCase
import com.AppexSolutions.gymsync.features.users.domain.usecases.LoginMemberUseCase
import com.AppexSolutions.gymsync.features.users.domain.usecases.LoginMemberWithBiometricUseCase
import com.AppexSolutions.gymsync.features.users.presentation.screens.UserLoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserLoginViewModel @Inject constructor(
    private val loginMemberUseCase: LoginMemberUseCase,
    private val loginMemberWithBiometricUseCase: LoginMemberWithBiometricUseCase,
    private val enableMemberBiometricUseCase: EnableMemberBiometricUseCase,
    private val hasMemberBiometricSessionUseCase: HasMemberBiometricSessionUseCase,
    private val biometricAuthManager: BiometricAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserLoginUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkBiometricAvailability()
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Completa todos los campos") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            loginMemberUseCase(email, password).fold(
                onSuccess = { session ->
                    val canOfferBiometric = biometricAuthManager.isHardwareAvailable() &&
                            biometricAuthManager.isBiometricEnrolled() &&
                            !hasMemberBiometricSessionUseCase()

                    val clientId = when (val raw = session.id_user) {
                        is Int -> raw
                        is Long -> raw.toInt()
                        is Double -> raw.toInt()
                        is Number -> raw.toInt()
                        else -> raw.toString().toIntOrNull() ?: 0
                    }
                    android.util.Log.d("UserLoginVM", "Login OK → clientId=$clientId roleName=${session.roleName}")

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lastLoggedEmail = email,
                            showEnableBiometricDialog = canOfferBiometric,
                            loggedClientId = if (!canOfferBiometric) clientId else null,
                            // Store clientId in lastLoggedEmail context for after biometric dialog
                        )
                    }
                    if (!canOfferBiometric) checkBiometricAvailability()
                    // Store clientId temporarily for after biometric dialog
                    if (canOfferBiometric) {
                        _pendingClientId = clientId
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Error de autenticación")
                    }
                }
            )
        }
    }

    fun loginWithBiometric(activity: FragmentActivity) {
        _uiState.update { it.copy(biometricLoginInProgress = true, error = null) }
        viewModelScope.launch {
            loginMemberWithBiometricUseCase(activity).collect { result ->
                when (result) {
                    is BiometricLoginResult.Success -> {
                        _uiState.update {
                            it.copy(
                                biometricLoginInProgress = false,
                                loggedClientId = result.user.id
                            )
                        }
                    }
                    is BiometricLoginResult.UserCancelled -> _uiState.update {
                        it.copy(biometricLoginInProgress = false)
                    }
                    is BiometricLoginResult.HardwareUnavailable -> _uiState.update {
                        it.copy(
                            biometricLoginInProgress = false,
                            showBiometricButton = false,
                            error = "Hardware biométrico no disponible"
                        )
                    }
                    is BiometricLoginResult.NoBiometricEnrolled -> _uiState.update {
                        it.copy(
                            biometricLoginInProgress = false,
                            error = "No hay huella o Face ID registrado en el dispositivo"
                        )
                    }
                    is BiometricLoginResult.NoSessionFound -> _uiState.update {
                        it.copy(
                            biometricLoginInProgress = false,
                            showBiometricButton = false,
                            error = "No se encontró sesión guardada. Inicia sesión normalmente primero."
                        )
                    }
                    is BiometricLoginResult.Error -> _uiState.update {
                        it.copy(biometricLoginInProgress = false, error = result.message)
                    }
                }
            }
        }
    }

    fun confirmEnableBiometric() {
        val email = _uiState.value.lastLoggedEmail
        viewModelScope.launch {
            enableMemberBiometricUseCase(email, true)
            _uiState.update {
                it.copy(
                    showEnableBiometricDialog = false,
                    loggedClientId = _pendingClientId
                )
            }
            checkBiometricAvailability()
        }
    }

    fun skipEnableBiometric() {
        _uiState.update {
            it.copy(
                showEnableBiometricDialog = false,
                loggedClientId = _pendingClientId
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetLoginSuccess() {
        _uiState.update { it.copy(loggedClientId = null) }
    }

    private fun checkBiometricAvailability() {
        viewModelScope.launch {
            val show = hasMemberBiometricSessionUseCase()
            _uiState.update { it.copy(showBiometricButton = show) }
        }
    }

    private var _pendingClientId: Int = 0
}
