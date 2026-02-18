package com.AppexSolutions.gymsync.features.auth.data.datasource.hardware

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class BiometricResult {
    data object Success : BiometricResult()
    data object UserCancelled : BiometricResult()
    data object HardwareUnavailable : BiometricResult()
    data object NoBiometricEnrolled : BiometricResult()
    data class Error(val message: String) : BiometricResult()
}

@Singleton
class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val biometricManager = BiometricManager.from(context)

    fun isHardwareAvailable(): Boolean {
        return when (biometricManager.canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun isBiometricEnrolled(): Boolean {
        return when (biometricManager.canAuthenticate(BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Acceso con biometría",
        subtitle: String = "Usa tu huella, Face ID o PIN para entrar",
        negativeButtonText: String = "Cancelar"
    ): Flow<BiometricResult> = callbackFlow {

        val canAuthenticate = biometricManager.canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)

        when (canAuthenticate) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                trySend(BiometricResult.HardwareUnavailable)
                close()
                return@callbackFlow
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                trySend(BiometricResult.NoBiometricEnrolled)
                close()
                return@callbackFlow
            }
        }

        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                trySend(BiometricResult.Success)
                close()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                        trySend(BiometricResult.UserCancelled)
                    }
                    else -> trySend(BiometricResult.Error(errString.toString()))
                }
                close()
            }

            override fun onAuthenticationFailed() {
                // El sistema ya muestra feedback al usuario; no cerramos el flow
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        biometricPrompt.authenticate(promptInfo)

        awaitClose { /* BiometricPrompt no necesita cancelación manual */ }
    }
}
