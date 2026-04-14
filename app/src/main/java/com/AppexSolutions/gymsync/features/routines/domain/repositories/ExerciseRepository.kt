package com.AppexSolutions.gymsync.features.routines.domain.repositories

import com.AppexSolutions.gymsync.features.routines.domain.entities.Exercise
import com.AppexSolutions.gymsync.features.routines.domain.entities.MuscleGroup
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getExercisesByMuscleGroup(group: MuscleGroup): Flow<List<Exercise>>
    suspend fun refreshExercises()
    suspend fun isCacheStale(): Boolean
}
