package com.AppexSolutions.gymsync.features.progress.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.AppexSolutions.gymsync.features.progress.data.local.entity.ProgressEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ProgressEntryEntity): Long

    @Query("SELECT * FROM progress_entries WHERE user_id = :userId ORDER BY recorded_at DESC")
    fun getByUserId(userId: Int): Flow<List<ProgressEntryEntity>>

    @Query("DELETE FROM progress_entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM progress_entries WHERE user_id = :userId ORDER BY recorded_at DESC LIMIT 1")
    suspend fun getLatestByUserId(userId: Int): ProgressEntryEntity?
}
