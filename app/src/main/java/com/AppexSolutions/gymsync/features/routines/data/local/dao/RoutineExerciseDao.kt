package com.AppexSolutions.gymsync.features.routines.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.AppexSolutions.gymsync.features.routines.data.local.entity.RoutineExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineExerciseDao {
    @Insert
    suspend fun insert(re: RoutineExerciseEntity): Long

    @Query("DELETE FROM routine_exercises WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Update
    suspend fun update(re: RoutineExerciseEntity)

    @Query("SELECT * FROM routine_exercises WHERE routine_id = :routineId ORDER BY sort_order")
    fun getByRoutineId(routineId: Int): Flow<List<RoutineExerciseEntity>>

    @Query("SELECT * FROM routine_exercises WHERE routine_id = :routineId ORDER BY sort_order")
    suspend fun getByRoutineIdOnce(routineId: Int): List<RoutineExerciseEntity>

    @Query("SELECT * FROM routine_exercises WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): RoutineExerciseEntity?

    @Query("UPDATE routine_exercises SET sets = :sets, reps = :reps, rest_secs = :restSeconds WHERE id = :id")
    suspend fun updateSetsReps(id: Int, sets: Int, reps: Int, restSeconds: Int)
}
