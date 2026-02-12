package com.AppexSolutions.gymsync.features.clients.domain.usecases

import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository

/**
 * Caso de uso para eliminar un cliente
 */
class DeleteClientUseCase(
    private val repository: ClientRepository
) {
    suspend operator fun invoke(clientId: Int): Result<Unit> {
        return try {

             repository.deleteClient(clientId)
             Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}