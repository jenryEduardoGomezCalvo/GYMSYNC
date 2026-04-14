package com.AppexSolutions.gymsync.features.routines.domain.repositories

import com.AppexSolutions.gymsync.features.routines.domain.entities.RoutineHistory
import kotlinx.coroutines.flow.Flow

interface RoutineHistoryRepository {
    fun getHistoryForUser(userId: Int): Flow<List<RoutineHistory>>
    suspend fun recordCompletion(routineId: Int, routineName: String, userId: Int)
}
