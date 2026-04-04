package com.AppexSolutions.gymsync.features.clients.domain.usecases

import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository
import javax.inject.Inject

class UpdateClientUseCase @Inject constructor(private val repository: ClientRepository) {
    suspend operator fun invoke(
        userId: Int, nombres: String?, apellidos: String?,
        email: String?, telefono: String?, fechaNacimiento: String?
    ): Result<Client> = try {
        Result.success(repository.updateUser(userId, nombres, apellidos, email, telefono, fechaNacimiento))
    } catch (e: Exception) { Result.failure(e) }
}
