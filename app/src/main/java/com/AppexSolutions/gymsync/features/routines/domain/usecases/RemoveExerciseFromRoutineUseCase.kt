package com.AppexSolutions.gymsync.features.routines.domain.usecases

import com.AppexSolutions.gymsync.features.routines.domain.repositories.RoutineRepository
import javax.inject.Inject

class RemoveExerciseFromRoutineUseCase @Inject constructor(
    private val repository: RoutineRepository
) {
    suspend operator fun invoke(routineExerciseId: Int) =
        repository.removeExerciseFromRoutine(routineExerciseId)
}
