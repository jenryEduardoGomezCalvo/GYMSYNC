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

        val result = authRepository.login(email.trim(), password)

        return result.fold(
            onSuccess = { session ->
                val role = session.roleName?.lowercase()
                if (role != "cliente") {
                    Result.failure(IllegalArgumentException("Esta cuenta no es de miembro. Usa el acceso de administrador."))
                } else {
                    Result.success(session)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }
}
