package com.AppexSolutions.gymsync.features.routines.domain.usecases

import com.AppexSolutions.gymsync.features.routines.domain.repositories.RoutineRepository
import javax.inject.Inject

class UpdateRoutineExerciseUseCase @Inject constructor(
    private val repository: RoutineRepository
) {
    suspend operator fun invoke(
        routineExerciseId: Int,
        sets: Int,
        reps: Int,
        restSeconds: Int
    ) = repository.updateRoutineExercise(routineExerciseId, sets, reps, restSeconds)
}
