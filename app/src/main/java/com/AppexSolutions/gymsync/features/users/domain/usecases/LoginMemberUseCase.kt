package com.AppexSolutions.gymsync.features.users.domain.usecases

import com.AppexSolutions.gymsync.features.auth.domain.entities.AuthSession
import com.AppexSolutions.gymsync.features.auth.domain.repositories.AuthRepository
import javax.inject.Inject

class LoginMemberUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthSession> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Completa todos los campos"))
        }
        return authRepository.login(email.trim(), password)
    }
}
