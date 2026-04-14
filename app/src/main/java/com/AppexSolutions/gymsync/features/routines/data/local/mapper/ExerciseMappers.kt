package com.AppexSolutions.gymsync.features.routines.data.local.mapper

import com.AppexSolutions.gymsync.features.routines.data.local.entity.ExerciseEntity
import com.AppexSolutions.gymsync.features.routines.data.remote.WgerMuscleGroupMapper
import com.AppexSolutions.gymsync.features.routines.data.remote.dto.WgerExerciseInfoDto
import com.AppexSolutions.gymsync.features.routines.data.remote.dto.WgerTranslationDto
import com.AppexSolutions.gymsync.features.routines.domain.entities.Exercise
import com.AppexSolutions.gymsync.features.routines.domain.entities.MuscleGroup

fun ExerciseEntity.toDomain() = Exercise(
    id = id,
    name = name,
    muscleGroup = try {
        MuscleGroup.valueOf(muscleGroup)
    } catch (e: IllegalArgumentException) {
        MuscleGroup.CORE
    },
    description = description,
    imageUrl = imageUrl
)

fun WgerExerciseInfoDto.toEntity(translation: WgerTranslationDto): ExerciseEntity {
    val muscleGroup = WgerMuscleGroupMapper.map(this)
    val imageUrl = images.firstOrNull { it.isMain }?.image
        ?: images.firstOrNull()?.image
    return ExerciseEntity(
        wgerId = id,
        name = translation.name.ifBlank { "Exercise $id" },
        muscleGroup = muscleGroup.name,
        description = translation.description,
        imageUrl = imageUrl,
        cachedAt = System.currentTimeMillis()
    )
}
