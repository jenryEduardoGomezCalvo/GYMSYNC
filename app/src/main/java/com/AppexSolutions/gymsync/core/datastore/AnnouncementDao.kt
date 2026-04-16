package com.AppexSolutions.gymsync.core.datastore

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(announcement: AnnouncementEntity): Long

    @Query("SELECT * FROM announcements ORDER BY sent_at DESC")
    fun observeAll(): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements ORDER BY sent_at DESC")
    suspend fun getAll(): List<AnnouncementEntity>

    @Query("SELECT * FROM announcements WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): AnnouncementEntity?

    @Query("SELECT COUNT(*) FROM announcements WHERE is_read = 0")
    fun observeUnreadCount(): Flow<Int>

    @Query("UPDATE announcements SET is_read = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("UPDATE announcements SET is_read = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM announcements")
    suspend fun clearAll()
}
