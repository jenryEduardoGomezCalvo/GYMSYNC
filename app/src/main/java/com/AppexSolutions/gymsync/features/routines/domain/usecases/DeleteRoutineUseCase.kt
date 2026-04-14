package com.AppexSolutions.gymsync.features.routines.domain.usecases

import com.AppexSolutions.gymsync.features.routines.domain.repositories.RoutineRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class DeleteRoutineUseCase @Inject constructor(
    private val repository: RoutineRepository,
    private val cancelAlarms: CancelRoutineAlarmsUseCase
) {
    suspend operator fun invoke(routineId: Int) {
        val routine = repository.getRoutineById(routineId).firstOrNull()
        routine?.let { cancelAlarms(routineId, it.days) }
        repository.deleteRoutine(routineId)
    }
}
