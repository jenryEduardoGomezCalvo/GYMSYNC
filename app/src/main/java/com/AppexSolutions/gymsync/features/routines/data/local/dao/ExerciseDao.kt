package com.AppexSolutions.gymsync.features.routines.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.AppexSolutions.gymsync.features.routines.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Upsert
    suspend fun upsertAll(exercises: List<ExerciseEntity>)

    @Query("SELECT * FROM exercises WHERE muscle_group = :group")
    fun getByMuscleGroup(group: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises")
    suspend fun getAll(): List<ExerciseEntity>

    @Query("DELETE FROM exercises")
    suspend fun deleteAll()

    @Query("SELECT MAX(cached_at) FROM exercises")
    suspend fun getLatestCachedAt(): Long?
}
