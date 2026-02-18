package com.AppexSolutions.gymsync.features.auth.domain.usecases

import androidx.fragment.app.FragmentActivity
import com.AppexSolutions.gymsync.core.datastore.UserEntity
import com.AppexSolutions.gymsync.features.auth.data.datasource.hardware.BiometricAuthManager
import com.AppexSolutions.gymsync.features.auth.data.datasource.hardware.BiometricResult
import com.AppexSolutions.gymsync.features.auth.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

sealed class BiometricLoginResult {
    data class Success(val user: UserEntity) : BiometricLoginResult()
    data object UserCancelled : BiometricLoginResult()
    data object HardwareUnavailable : BiometricLoginResult()
    data object NoBiometricEnrolled : BiometricLoginResult()
    data object NoSessionFound : BiometricLoginResult()
    data class Error(val message: String) : BiometricLoginResult()
}

class LoginWithBiometricUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val biometricAuthManager: BiometricAuthManager
) {
    operator fun invoke(activity: FragmentActivity): Flow<BiometricLoginResult> = flow {
        val cachedUser = authRepository.getLastBiometricUser()
        if (cachedUser == null) {
            emit(BiometricLoginResult.NoSessionFound)
            return@flow
        }

        biometricAuthManager.authenticate(activity).collect { result ->
            when (result) {
                BiometricResult.Success -> {
                    // Actualizar last_login en Room
                    authRepository.saveUserSession(
                        cachedUser.copy(lastLogin = java.util.Date())
                    )
                    emit(BiometricLoginResult.Success(cachedUser))
                }
                BiometricResult.UserCancelled -> emit(BiometricLoginResult.UserCancelled)
                BiometricResult.HardwareUnavailable -> emit(BiometricLoginResult.HardwareUnavailable)
                BiometricResult.NoBiometricEnrolled -> emit(BiometricLoginResult.NoBiometricEnrolled)
                is BiometricResult.Error -> emit(BiometricLoginResult.Error(result.message))
            }
        }
    }
}
