package com.AppexSolutions.gymsync.features.routines.data.repositories

import com.AppexSolutions.gymsync.features.routines.data.local.dao.ExerciseDao
import com.AppexSolutions.gymsync.features.routines.data.local.entity.ExerciseEntity
import com.AppexSolutions.gymsync.features.routines.data.local.mapper.toDomain
import com.AppexSolutions.gymsync.features.routines.data.local.mapper.toEntity
import com.AppexSolutions.gymsync.features.routines.data.remote.ExerciseRemoteDataSource
import com.AppexSolutions.gymsync.features.routines.domain.entities.Exercise
import com.AppexSolutions.gymsync.features.routines.domain.entities.MuscleGroup
import com.AppexSolutions.gymsync.features.routines.domain.repositories.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val CACHE_TTL_MS = 7L * 24 * 60 * 60 * 1000  // 7 días

class ExerciseRepositoryImpl @Inject constructor(
    private val dao: ExerciseDao,
    private val remoteDataSource: ExerciseRemoteDataSource
) : ExerciseRepository {

    override fun getExercisesByMuscleGroup(group: MuscleGroup): Flow<List<Exercise>> =
        dao.getByMuscleGroup(group.name).map { entities -> entities.map { it.toDomain() } }

    override suspend fun refreshExercises() {
        val now = System.currentTimeMillis()

        // Ejercicios desde la API de wger.de
        val remoteDtos = remoteDataSource.fetchAll()
        val remoteEntities = remoteDtos.mapNotNull { dto ->
            val translation = remoteDataSource.getBestTranslation(dto) ?: return@mapNotNull null
            dto.toEntity(translation)
        }

        // Ejercicios de Cardio bundled (no están en wger.de)
        val cardioExercises = listOf(
            ExerciseEntity(wgerId = -1, name = "Carrera en cinta", muscleGroup = "CARDIO",
                description = "Cardio básico en cinta a ritmo moderado.", cachedAt = now),
            ExerciseEntity(wgerId = -2, name = "Bicicleta estática", muscleGroup = "CARDIO",
                description = "Cardio de bajo impacto en bicicleta estática.", cachedAt = now),
            ExerciseEntity(wgerId = -3, name = "Saltos de tijera", muscleGroup = "CARDIO",
                description = "Jumping jacks para elevar la frecuencia cardíaca.", cachedAt = now),
            ExerciseEntity(wgerId = -4, name = "Cuerda para saltar", muscleGroup = "CARDIO",
                description = "Saltar cuerda a ritmo constante durante 10 minutos.", cachedAt = now)
        )

        dao.upsertAll(remoteEntities + cardioExercises)
    }

    override suspend fun isCacheStale(): Boolean {
        val latestCachedAt = dao.getLatestCachedAt() ?: return true
        return (System.currentTimeMillis() - latestCachedAt) > CACHE_TTL_MS
    }
}
