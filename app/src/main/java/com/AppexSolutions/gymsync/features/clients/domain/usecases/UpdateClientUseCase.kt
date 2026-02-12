package com.AppexSolutions.gymsync.features.clients.domain.usecases

import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository

/**
 * Caso de uso para actualizar un cliente
 */
class UpdateClientUseCase(
    private val repository: ClientRepository
) {
    suspend operator fun invoke(client: Client): Result<Unit> {
        return try {
            // Validaciones de negocio
            if (client.name.isBlank()) {
                return Result.failure(Exception("El nombre es obligatorio"))
            }


            repository.updateClient(client)

                Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}