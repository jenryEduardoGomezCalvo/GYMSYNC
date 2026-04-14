package com.AppexSolutions.gymsync.features.routines.domain.repositories

import com.AppexSolutions.gymsync.features.routines.domain.entities.Routine
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun getRoutinesForUser(userId: Int): Flow<List<Routine>>
    fun getRoutineById(routineId: Int): Flow<Routine?>
    fun getRoutinesForDay(userId: Int, dayOfWeek: Int): Flow<List<Routine>>
    suspend fun createRoutine(routine: Routine): Int
    suspend fun updateRoutine(routine: Routine)
    suspend fun deleteRoutine(routineId: Int)
    suspend fun addExerciseToRoutine(
        routineId: Int,
        exerciseId: Int,
        sets: Int,
        reps: Int,
        restSeconds: Int
    )
    suspend fun removeExerciseFromRoutine(routineExerciseId: Int)
    suspend fun updateRoutineExercise(
        routineExerciseId: Int,
        sets: Int,
        reps: Int,
        restSeconds: Int
    )
}
