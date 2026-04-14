package com.AppexSolutions.gymsync.features.routines.data.remote

import com.AppexSolutions.gymsync.features.routines.data.remote.dto.WgerExerciseInfoDto
import com.AppexSolutions.gymsync.features.routines.data.remote.dto.WgerTranslationDto
import javax.inject.Inject

class ExerciseRemoteDataSource @Inject constructor(
    private val api: WgerApiService
) {
    /**
     * Obtiene todos los ejercicios de wger.de paginando hasta que `next == null`.
     * Filtra por idioma español (4); si un ejercicio no tiene traducción ES usa inglés (2).
     */
    suspend fun fetchAll(): List<WgerExerciseInfoDto> {
        val result = mutableListOf<WgerExerciseInfoDto>()
        var offset = 0
        do {
            val page = api.getExercises(offset = offset)
            result.addAll(page.results)
            offset += 100
            if (page.next == null) break
        } while (true)

        return result.filter { dto ->
            dto.translations.any { it.language == 4 || it.language == 2 }
        }
    }

    fun getBestTranslation(dto: WgerExerciseInfoDto): WgerTranslationDto? =
        dto.translations.firstOrNull { it.language == 4 }
            ?: dto.translations.firstOrNull { it.language == 2 }
}
