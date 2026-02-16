package com.AppexSolutions.gymsync.features.clients.domain.usecases

import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository

class CreateUserUseCase(private val repository: ClientRepository) {
    suspend operator fun invoke(
        nombres: String, apellidos: String, email: String, password: String,
        telefono: String?, fechaNacimiento: String?, rolId: Int, gymId: Int?
    ): Result<Client> = try {
        Result.success(repository.createUser(nombres, apellidos, email, password, telefono, fechaNacimiento, rolId, gymId))
    } catch (e: Exception) { Result.failure(e) }
}
