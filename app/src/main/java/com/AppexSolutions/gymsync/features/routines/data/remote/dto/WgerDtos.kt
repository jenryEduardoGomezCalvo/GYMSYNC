package com.AppexSolutions.gymsync.features.routines.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WgerPaginatedResponse<T>(
    val count: Int,
    val next: String?,
    val results: List<T>
)

data class WgerExerciseInfoDto(
    val id: Int,
    val category: WgerCategoryDto,
    val muscles: List<WgerMuscleDto>,
    @SerializedName("muscles_secondary") val musclesSecondary: List<WgerMuscleDto> = emptyList(),
    val translations: List<WgerTranslationDto>,
    val images: List<WgerExerciseImageDto> = emptyList()
)

data class WgerExerciseImageDto(
    val image: String,
    @SerializedName("is_main") val isMain: Boolean = false
)

data class WgerCategoryDto(
    val id: Int,
    val name: String
)

data class WgerMuscleDto(
    val id: Int,
    @SerializedName("name_en") val nameEn: String
)

data class WgerTranslationDto(
    val language: Int,   // 4 = español, 2 = inglés
    val name: String,
    val description: String
)
