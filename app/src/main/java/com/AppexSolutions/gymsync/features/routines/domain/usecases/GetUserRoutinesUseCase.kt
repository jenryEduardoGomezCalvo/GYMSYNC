package com.AppexSolutions.gymsync.features.routines.domain.usecases

import com.AppexSolutions.gymsync.features.routines.domain.entities.Routine
import com.AppexSolutions.gymsync.features.routines.domain.repositories.RoutineRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserRoutinesUseCase @Inject constructor(
    private val repository: RoutineRepository
) {
    operator fun invoke(userId: Int): Flow<List<Routine>> =
        repository.getRoutinesForUser(userId)
}
