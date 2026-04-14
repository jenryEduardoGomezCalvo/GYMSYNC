package com.AppexSolutions.gymsync.features.routines.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int,
    val name: String,
    @ColumnInfo(name = "notification_hour") val notificationHour: Int = 8,
    @ColumnInfo(name = "notification_minute") val notificationMinute: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
