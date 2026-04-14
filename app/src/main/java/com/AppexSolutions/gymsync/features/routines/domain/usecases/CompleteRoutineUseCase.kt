package com.AppexSolutions.gymsync.features.routines.domain.usecases

import com.AppexSolutions.gymsync.features.routines.domain.repositories.RoutineHistoryRepository
import javax.inject.Inject

class CompleteRoutineUseCase @Inject constructor(
    private val historyRepository: RoutineHistoryRepository
) {
    suspend operator fun invoke(routineId: Int, routineName: String, userId: Int) =
        historyRepository.recordCompletion(routineId, routineName, userId)
}
