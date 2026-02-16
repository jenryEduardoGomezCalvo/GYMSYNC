package com.AppexSolutions.gymsync.features.auth.domain.usecases

import com.AppexSolutions.gymsync.features.auth.domain.entities.RegisterUser
import com.AppexSolutions.gymsync.features.auth.domain.repositories.GymSyncRepositorie


class RegisterUserUseCase(
    private val repository: GymSyncRepositorie
) {
    suspend operator fun invoke(
        nombres: String,
        apellidos: String,
        email: String,
        password: String,
        telefono: String?,
        fechaNacimiento: String?
    ): Result<String> {
        return try {
            val registerUser = RegisterUser(
                nombres = nombres,
                apellidos = apellidos,
                email = email,
                password = password,
                telefono = telefono,
                fechaNacimiento = fechaNacimiento
            )
            val message = repository.RegisterUser(registerUser)
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
