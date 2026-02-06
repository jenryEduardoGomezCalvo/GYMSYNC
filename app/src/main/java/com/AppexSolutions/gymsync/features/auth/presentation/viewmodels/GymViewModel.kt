package com.AppexSolutions.gymsync.features.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.auth.domain.entities.AuthSession
import com.AppexSolutions.gymsync.features.auth.domain.entities.FAuthSession
import com.AppexSolutions.gymsync.features.auth.domain.usecases.PostUserUseCase
import com.AppexSolutions.gymsync.features.auth.presentation.screens.LoginUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Login
 *
 * Maneja:
 * - Estado del formulario (email, password)
 * - Validaciones de entrada
 * - Llamada al caso de uso de login
 * - Estados de carga y error
 */
class GymViewModel(
    private val postUserUseCase: PostUserUseCase
) : ViewModel() {

    // Estado privado (mutable) - solo el ViewModel puede modificarlo
    private val _uiState = MutableStateFlow(LoginUiState())

    // Estado público (inmutable) - la UI solo puede leerlo
    val uiState = _uiState.asStateFlow()

    // ✅ CREDENCIALES HARDCODEADAS TEMPORALES
    companion object {
        private const val HARDCODED_EMAIL = "admin@gymsync.com"
        private const val HARDCODED_PASSWORD = "123456"
        private const val USE_HARDCODED_LOGIN = true  // ✅ Cambiar a false cuando tengas la API
    }

    /**
     * Actualiza el email mientras el usuario escribe
     */
    fun onEmailChange(newEmail: String) {
        _uiState.update {
            it.copy(
                email = newEmail,
                error = null  // Limpiar error al escribir
            )
        }
    }

    /**
     * Actualiza la contraseña mientras el usuario escribe
     */
    fun onPasswordChange(newPassword: String) {
        _uiState.update {
            it.copy(
                password = newPassword,
                error = null  // Limpiar error al escribir
            )
        }
    }

    /**
     * Ejecuta el login cuando el usuario presiona "Iniciar sesión"
     */
    fun login() {
        val currentEmail = _uiState.value.email.trim()
        val currentPassword = _uiState.value.password

        // Validación de campos vacíos
        if (currentEmail.isEmpty() || currentPassword.isEmpty()) {
            _uiState.update {
                it.copy(error = "Por favor completa todos los campos")
            }
            return
        }

        // Validación de formato de email
        if (!isValidEmail(currentEmail)) {
            _uiState.update {
                it.copy(error = "Email inválido")
            }
            return
        }

        // Validación de longitud de contraseña
        if (currentPassword.length < 6) {
            _uiState.update {
                it.copy(error = "La contraseña debe tener al menos 6 caracteres")
            }
            return
        }

        // Iniciar proceso de login
        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                isLoginSuccessful = false
            )
        }

        // ✅ MODO TEMPORAL: Login con credenciales hardcodeadas
        if (USE_HARDCODED_LOGIN) {
            loginWithHardcodedCredentials(currentEmail, currentPassword)
        } else {
            // 🔒 MODO REAL: Login con API (comentado temporalmente)
            loginWithApi(currentEmail, currentPassword)
        }
    }

    /**
     * ✅ LOGIN TEMPORAL CON CREDENCIALES HARDCODEADAS
     *
     * Credenciales válidas:
     * Email: admin@gymsync.com
     * Password: 123456
     */
    private fun loginWithHardcodedCredentials(email: String, password: String) {
        viewModelScope.launch {
            // Simular delay de red
            delay(1500)

            // Verificar credenciales
            if (email == HARDCODED_EMAIL && password == HARDCODED_PASSWORD) {
                // ✅ Login exitoso
                val fakeAuthSession = FAuthSession(
                    token = "fake_token_12345",
                    userId = 1,
                    email = email,
                    name = "Administrador",
                    expiresAt = System.currentTimeMillis() + 86400000 // 24 horas
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authSession = null,
                        isLoginSuccessful = true,
                        error = null
                    )
                }
            } else {
                // ❌ Credenciales incorrectas
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authSession = null,
                        isLoginSuccessful = false,
                        error = "Credenciales incorrectas. Usa:\nEmail: admin@gymsync.com\nPassword: 123456"
                    )
                }
            }
        }
    }

    /**
     * 🔒 LOGIN REAL CON API
     *
     * Este método se usará cuando tengas la API desplegada.
     * Por ahora está comentado y se usa loginWithHardcodedCredentials()
     */
    private fun loginWithApi(email: String, password: String) {
        // TODO: Descomentar cuando tengas la API lista
        /*
        viewModelScope.launch {
            val result = postUserUseCase(email, password)

            _uiState.update { currentState ->
                result.fold(
                    onSuccess = { authSession ->
                        currentState.copy(
                            isLoading = false,
                            authSession = authSession,
                            isLoginSuccessful = true,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        currentState.copy(
                            isLoading = false,
                            authSession = null,
                            isLoginSuccessful = false,
                            error = exception.message ?: "Error al iniciar sesión"
                        )
                    }
                )
            }
        }
        */

        // Mientras tanto, usar el login hardcodeado
        loginWithHardcodedCredentials(email, password)
    }

    /**
     * Limpia el error (cuando el usuario cierra el diálogo de error)
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Resetea el estado de login exitoso
     * (útil después de navegar a otra pantalla)
     */
    fun resetLoginSuccess() {
        _uiState.update { it.copy(isLoginSuccessful = false) }
    }

    /**
     * Limpia todo el formulario
     */
    fun clearForm() {
        _uiState.update {
            LoginUiState()  // Resetear a estado inicial
        }
    }

    /**
     * Valida formato de email con regex simple
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        return email.matches(emailRegex.toRegex())
    }
}