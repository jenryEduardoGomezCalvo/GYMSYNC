package com.AppexSolutions.gymsync.features.routines.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.AppexSolutions.gymsync.features.routines.data.local.entity.RoutineHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineHistoryDao {
    @Insert
    suspend fun insert(history: RoutineHistoryEntity): Long

    @Query("SELECT * FROM routine_history WHERE user_id = :userId ORDER BY completed_at DESC")
    fun getByUserId(userId: Int): Flow<List<RoutineHistoryEntity>>
}
