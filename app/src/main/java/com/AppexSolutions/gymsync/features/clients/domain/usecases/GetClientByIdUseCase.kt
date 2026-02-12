package com.AppexSolutions.gymsync.features.clients.domain.usecases

import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository

/**
 * Caso de uso para obtener un cliente por ID
 */
class GetClientByIdUseCase(
    private val repository: ClientRepository
) {
    suspend operator fun invoke(clientId: Int): Result<Client> {
        return try {
            val client = repository.GetClientById(clientId)

            if (client != null) {
                Result.success(client)
            } else {
                Result.failure(Exception("Cliente no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}