package com.AppexSolutions.gymsync.features.auth.domain.usecases

import com.AppexSolutions.gymsync.features.auth.data.datasource.hardware.BiometricAuthManager
import com.AppexSolutions.gymsync.features.auth.domain.repositories.AuthRepository
import javax.inject.Inject

class HasBiometricSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val biometricAuthManager: BiometricAuthManager
) {
    /** Retorna true solo si: hay sesión cacheada con biometría habilitada Y el hardware está disponible. */
    suspend operator fun invoke(): Boolean {
        val hasSession = authRepository.hasBiometricSession()
        val hardwareAvailable = biometricAuthManager.isHardwareAvailable()
        return hasSession && hardwareAvailable
    }
}
