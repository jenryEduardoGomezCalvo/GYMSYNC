package com.AppexSolutions.gymsync.features.routines.domain.usecases

import com.AppexSolutions.gymsync.features.routines.domain.repositories.RoutineRepository
import javax.inject.Inject

class AddExerciseToRoutineUseCase @Inject constructor(
    private val repository: RoutineRepository
) {
    suspend operator fun invoke(
        routineId: Int,
        exerciseId: Int,
        sets: Int = 3,
        reps: Int = 10,
        restSeconds: Int = 60
    ) = repository.addExerciseToRoutine(routineId, exerciseId, sets, reps, restSeconds)
}
