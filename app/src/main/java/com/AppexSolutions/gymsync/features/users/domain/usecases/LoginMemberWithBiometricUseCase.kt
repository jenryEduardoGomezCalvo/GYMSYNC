package com.AppexSolutions.gymsync.features.users.domain.usecases

import androidx.fragment.app.FragmentActivity
import com.AppexSolutions.gymsync.features.auth.data.datasource.hardware.BiometricAuthManager
import com.AppexSolutions.gymsync.features.auth.data.datasource.hardware.BiometricResult
import com.AppexSolutions.gymsync.features.auth.domain.usecases.BiometricLoginResult
import com.AppexSolutions.gymsync.features.auth.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LoginMemberWithBiometricUseCase @Inject constructor(
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
