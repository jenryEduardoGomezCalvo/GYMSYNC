package com.AppexSolutions.gymsync.features.clients.domain.usecases

import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository

class GetClientsUsecase(
    private val repository: ClientRepository
) {
    suspend operator fun invoke(): Result<List<Client>> {
        return try {
            Result.success(repository.GetClients())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
