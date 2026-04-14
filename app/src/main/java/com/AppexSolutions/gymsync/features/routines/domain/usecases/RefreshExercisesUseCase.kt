package com.AppexSolutions.gymsync.features.routines.domain.usecases

import com.AppexSolutions.gymsync.features.routines.domain.repositories.ExerciseRepository
import javax.inject.Inject

class RefreshExercisesUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    suspend operator fun invoke() {
        if (repository.isCacheStale()) {
            repository.refreshExercises()
        }
    }
}
