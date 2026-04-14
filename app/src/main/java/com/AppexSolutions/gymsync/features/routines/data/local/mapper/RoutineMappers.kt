package com.AppexSolutions.gymsync.features.routines.data.local.mapper

import com.AppexSolutions.gymsync.features.routines.data.local.entity.RoutineExerciseEntity
import com.AppexSolutions.gymsync.features.routines.data.local.entity.RoutineHistoryEntity
import com.AppexSolutions.gymsync.features.routines.data.local.entity.RoutineEntity
import com.AppexSolutions.gymsync.features.routines.domain.entities.Exercise
import com.AppexSolutions.gymsync.features.routines.domain.entities.Routine
import com.AppexSolutions.gymsync.features.routines.domain.entities.RoutineExercise
import com.AppexSolutions.gymsync.features.routines.domain.entities.RoutineHistory

fun RoutineEntity.toDomain(
    days: List<Int>,
    exercises: List<RoutineExercise>
) = Routine(
    id = id,
    userId = userId,
    name = name,
    days = days,
    notificationHour = notificationHour,
    notificationMinute = notificationMinute,
    exercises = exercises,
    createdAt = createdAt
)

fun RoutineExerciseEntity.toDomain(exercise: Exercise) = RoutineExercise(
    id = id,
    exercise = exercise,
    sets = sets,
    reps = reps,
    restSeconds = restSeconds,
    sortOrder = sortOrder
)

fun RoutineHistoryEntity.toDomain() = RoutineHistory(
    id = id,
    routineId = routineId,
    routineName = routineName,
    userId = userId,
    completedAt = completedAt
)
