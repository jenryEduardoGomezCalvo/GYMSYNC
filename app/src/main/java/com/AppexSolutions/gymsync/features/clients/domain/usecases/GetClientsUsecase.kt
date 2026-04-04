package com.AppexSolutions.gymsync.features.clients.domain.usecases

import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository
import javax.inject.Inject

class GetClientsUsecase @Inject constructor(private val repository: ClientRepository) {
    suspend operator fun invoke(): Result<List<Client>> = try {
        Result.success(repository.getClients())
    } catch (e: Exception) { Result.failure(e) }
}
