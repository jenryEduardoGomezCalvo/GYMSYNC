package com.AppexSolutions.gymsync.features.routines.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_exercises",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routine_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"]
        )
    ]
)
data class RoutineExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "routine_id") val routineId: Int,
    @ColumnInfo(name = "exercise_id") val exerciseId: Int,
    val sets: Int = 3,
    val reps: Int = 10,
    @ColumnInfo(name = "rest_secs") val restSeconds: Int = 60,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0
)
