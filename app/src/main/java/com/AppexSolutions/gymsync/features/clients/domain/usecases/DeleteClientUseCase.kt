package com.AppexSolutions.gymsync.features.clients.domain.usecases

import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository

class DeleteClientUseCase(private val repository: ClientRepository) {
    suspend operator fun invoke(userId: Int): Result<Unit> = try {
        repository.deleteUser(userId)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}
