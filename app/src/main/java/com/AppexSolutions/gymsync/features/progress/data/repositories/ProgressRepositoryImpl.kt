package com.AppexSolutions.gymsync.features.progress.data.repositories

import com.AppexSolutions.gymsync.features.progress.data.local.dao.ProgressDao
import com.AppexSolutions.gymsync.features.progress.data.local.entity.ProgressEntryEntity
import com.AppexSolutions.gymsync.features.progress.domain.entities.ProgressEntry
import com.AppexSolutions.gymsync.features.progress.domain.repositories.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class ProgressRepositoryImpl @Inject constructor(
    private val dao: ProgressDao
) : ProgressRepository {

    override fun getHistory(userId: Int): Flow<List<ProgressEntry>> =
        dao.getByUserId(userId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun addEntry(entry: ProgressEntry) {
        dao.insert(entry.toEntity())
    }

    override suspend fun deleteEntry(id: Int) {
        dao.deleteById(id)
    }

    override suspend fun getLatestEntry(userId: Int): ProgressEntry? =
        dao.getLatestByUserId(userId)?.toDomain()

    private fun ProgressEntryEntity.toDomain() = ProgressEntry(
        id = id,
        userId = userId,
        date = Date(recordedAt),
        weight = weight,
        waist = waist,
        hips = hips,
        chest = chest,
        arms = arms,
        photoUri = photoUri,
        notes = notes
    )

    private fun ProgressEntry.toEntity() = ProgressEntryEntity(
        id = id,
        userId = userId,
        recordedAt = date.time,
        weight = weight,
        waist = waist,
        hips = hips,
        chest = chest,
        arms = arms,
        photoUri = photoUri,
        notes = notes
    )
}
