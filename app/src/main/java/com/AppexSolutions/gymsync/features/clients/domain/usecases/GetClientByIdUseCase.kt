package com.AppexSolutions.gymsync.features.clients.domain.usecases

import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository

class GetClientByIdUseCase(private val repository: ClientRepository) {
    suspend operator fun invoke(userId: Int): Result<Client> = try {
        Result.success(repository.getUserById(userId))
    } catch (e: Exception) { Result.failure(e) }
}
