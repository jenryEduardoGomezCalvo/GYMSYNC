package com.AppexSolutions.gymsync.features.progress.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress_entries")
data class ProgressEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "recorded_at") val recordedAt: Long,
    val weight: Float? = null,
    val waist: Float? = null,
    val hips: Float? = null,
    val chest: Float? = null,
    val arms: Float? = null,
    @ColumnInfo(name = "photo_uri") val photoUri: String? = null,
    val notes: String? = null
)
