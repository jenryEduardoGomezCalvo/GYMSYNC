package com.AppexSolutions.gymsync.features.auth.domain.usecases

import com.AppexSolutions.gymsync.features.auth.domain.repositories.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
