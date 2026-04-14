package com.AppexSolutions.gymsync.features.routines.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_history")
data class RoutineHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "routine_id") val routineId: Int,
    @ColumnInfo(name = "routine_name") val routineName: String,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "completed_at") val completedAt: Long
)
