package com.AppexSolutions.gymsync.features.routines.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "routine_days",
    primaryKeys = ["routine_id", "day_of_week"],
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routine_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RoutineDayEntity(
    @ColumnInfo(name = "routine_id") val routineId: Int,
    @ColumnInfo(name = "day_of_week") val dayOfWeek: Int  // 1=Lun … 7=Dom (ISO 8601)
)
