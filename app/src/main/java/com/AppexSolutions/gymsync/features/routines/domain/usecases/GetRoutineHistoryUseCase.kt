package com.AppexSolutions.gymsync.features.routines.domain.usecases

import com.AppexSolutions.gymsync.features.routines.domain.entities.RoutineHistory
import com.AppexSolutions.gymsync.features.routines.domain.repositories.RoutineHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRoutineHistoryUseCase @Inject constructor(
    private val repository: RoutineHistoryRepository
) {
    operator fun invoke(userId: Int): Flow<List<RoutineHistory>> =
        repository.getHistoryForUser(userId)
}
