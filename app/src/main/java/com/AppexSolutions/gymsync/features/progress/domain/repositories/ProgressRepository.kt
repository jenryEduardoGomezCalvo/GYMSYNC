package com.AppexSolutions.gymsync.features.progress.domain.repositories

import com.AppexSolutions.gymsync.features.progress.domain.entities.ProgressEntry
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    fun getHistory(userId: Int): Flow<List<ProgressEntry>>
    suspend fun addEntry(entry: ProgressEntry)
    suspend fun deleteEntry(id: Int)
    suspend fun getLatestEntry(userId: Int): ProgressEntry?
}
