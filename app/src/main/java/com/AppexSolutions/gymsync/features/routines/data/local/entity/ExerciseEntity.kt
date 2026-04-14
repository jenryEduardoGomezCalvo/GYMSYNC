package com.AppexSolutions.gymsync.features.routines.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    indices = [Index(value = ["wger_id"], unique = true)]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "wger_id") val wgerId: Int,
    val name: String,
    @ColumnInfo(name = "muscle_group") val muscleGroup: String,
    val description: String = "",
    @ColumnInfo(name = "image_url") val imageUrl: String? = null,
    @ColumnInfo(name = "cached_at") val cachedAt: Long
)
