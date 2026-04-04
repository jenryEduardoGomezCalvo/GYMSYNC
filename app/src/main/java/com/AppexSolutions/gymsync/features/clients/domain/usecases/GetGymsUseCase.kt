package com.AppexSolutions.gymsync.features.clients.domain.usecases

import com.AppexSolutions.gymsync.features.clients.domain.entities.Gym
import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository
import javax.inject.Inject

class GetGymsUseCase @Inject constructor(private val repository: ClientRepository) {
    suspend operator fun invoke(): Result<List<Gym>> = try {
        Result.success(repository.getGyms())
    } catch (e: Exception) { Result.failure(e) }
}
