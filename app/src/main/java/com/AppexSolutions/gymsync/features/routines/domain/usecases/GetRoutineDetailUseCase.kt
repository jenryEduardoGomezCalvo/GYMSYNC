package com.AppexSolutions.gymsync.features.routines.domain.usecases

import com.AppexSolutions.gymsync.features.routines.domain.entities.Routine
import com.AppexSolutions.gymsync.features.routines.domain.repositories.RoutineRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRoutineDetailUseCase @Inject constructor(
    private val repository: RoutineRepository
) {
    operator fun invoke(routineId: Int): Flow<Routine?> =
        repository.getRoutineById(routineId)
}
