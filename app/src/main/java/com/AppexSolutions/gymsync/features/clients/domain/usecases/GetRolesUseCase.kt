package com.AppexSolutions.gymsync.features.clients.domain.usecases

import com.AppexSolutions.gymsync.features.clients.domain.entities.Rol
import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository
import javax.inject.Inject

class GetRolesUseCase @Inject constructor(private val repository: ClientRepository) {
    suspend operator fun invoke(): Result<List<Rol>> = try {
        Result.success(repository.getRoles())
    } catch (e: Exception) { Result.failure(e) }
}
