package com.AppexSolutions.gymsync.features.routines.domain.usecases

import com.AppexSolutions.gymsync.features.routines.domain.entities.Exercise
import com.AppexSolutions.gymsync.features.routines.domain.entities.MuscleGroup
import com.AppexSolutions.gymsync.features.routines.domain.repositories.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExercisesByMuscleGroupUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    operator fun invoke(group: MuscleGroup): Flow<List<Exercise>> =
        repository.getExercisesByMuscleGroup(group)
}
