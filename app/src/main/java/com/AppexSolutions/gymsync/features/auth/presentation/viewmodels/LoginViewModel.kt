package com.AppexSolutions.gymsync.features.auth.presentation.viewmodels

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.auth.domain.entities.AuthSession
import com.AppexSolutions.gymsync.features.auth.domain.usecases.BiometricLoginResult
import com.AppexSolutions.gymsync.features.auth.data.datasource.hardware.BiometricAuthManager
import com.AppexSolutions.gymsync.features.auth.domain.usecases.EnableBiometricUseCase
import com.AppexSolutions.gymsync.features.auth.domain.usecases.HasBiometricSessionUseCase
import com.AppexSolutions.gymsync.features.auth.domain.usecases.LoginUseCase
import com.AppexSolutions.gymsync.features.auth.domain.usecases.LoginWithBiometricUseCase
import com.AppexSolutions.gymsync.features.notifications.domain.usecases.InitializeFcmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginBiometricUiState(
    val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val error: String? = null,
    val authSession: AuthSession? = null,
    val isLoginSuccessful: Boolean = false,
    val showBiometricButton: Boolean = false,
    val biometricLoginInProgress: Boolean = false,
    val showEnableBiometricDialog: Boolean = false,
    val lastLoggedEmail: String = ""
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val loginWithBiometricUseCase: LoginWithBiometricUseCase,
    private val enableBiometricUseCase: EnableBiometricUseCase,
    private val hasBiometricSessionUseCase: HasBiometricSessionUseCase,
    private val biometricAuthManager: BiometricAuthManager,
    private val initializeFcmUseCase: InitializeFcmUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginBiometricUiState())
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

    /** Login normal contra servidor. Si es exitoso, guarda la sesión en Room. */
    fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Completa todos los campos") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            loginUseCase(email, password).fold(
                onSuccess = { session ->
                    val canOfferBiometric = biometricAuthManager.isHardwareAvailable() &&
                            biometricAuthManager.isBiometricEnrolled() &&
                            !hasBiometricSessionUseCase()

                    initializeFcmUseCase()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            authSession = session,
                            lastLoggedEmail = email,
                            showEnableBiometricDialog = canOfferBiometric,
                            isLoginSuccessful = !canOfferBiometric
                        )
                    }
                    if (!canOfferBiometric) checkBiometricAvailability()
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Error de autenticación")
                    }
                }
            )
        }
    }

    /** Login con huella/Face ID usando la sesión cacheada en Room. Sin llamada al servidor. */
    fun loginWithBiometric(activity: FragmentActivity) {
        _uiState.update { it.copy(biometricLoginInProgress = true, error = null) }
        viewModelScope.launch {
            loginWithBiometricUseCase(activity).collect { result ->
                when (result) {
                    is BiometricLoginResult.Success -> {
                        initializeFcmUseCase()

                        _uiState.update {
                            it.copy(
                                biometricLoginInProgress = false,
                                authSession = AuthSession(
                                    token = result.user.token,
                                    id_user = result.user.id
                                ),
                                isLoginSuccessful = true
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

    /** Activa o desactiva el acceso biométrico para el usuario actual. */
    fun enableBiometricForUser(email: String, enable: Boolean = true) {
        viewModelScope.launch {
            enableBiometricUseCase(email, enable)
            checkBiometricAvailability()
        }
    }

    /** El usuario acepta activar biometría → guarda en Room y navega. */
    fun confirmEnableBiometric() {
        val email = _uiState.value.lastLoggedEmail
        viewModelScope.launch {
            enableBiometricUseCase(email, true)
            _uiState.update {
                it.copy(showEnableBiometricDialog = false, isLoginSuccessful = true)
            }
            checkBiometricAvailability()
        }
    }

    /** El usuario rechaza activar biometría → navega sin guardar biometría. */
    fun skipEnableBiometric() {
        _uiState.update {
            it.copy(showEnableBiometricDialog = false, isLoginSuccessful = true)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetLoginSuccess() {
        _uiState.update { it.copy(isLoginSuccessful = false) }
    }

    private fun checkBiometricAvailability() {
        viewModelScope.launch {
            val show = hasBiometricSessionUseCase()
            _uiState.update { it.copy(showBiometricButton = show) }
        }
    }
}
