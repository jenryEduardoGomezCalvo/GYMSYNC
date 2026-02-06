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
        telefono: String,
        fechaNacimiento: String,
        rolId: Int,
        gymId: Int
    ): Result<String> {
        val registerUser = RegisterUser(
            nombres = nombres,
            apellidos = apellidos,
            email = email,
            password = password,
            telefono = telefono,
            fechaNacimiento = fechaNacimiento,
            rolId = rolId,
            gymId = gymId
        )
        val message = repository.RegisterUser(registerUser)
        return Result.success(message)
    }
}