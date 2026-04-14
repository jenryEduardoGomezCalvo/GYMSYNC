package com.AppexSolutions.gymsync.features.routines.data.repositories

import com.AppexSolutions.gymsync.features.routines.data.local.dao.RoutineHistoryDao
import com.AppexSolutions.gymsync.features.routines.data.local.entity.RoutineHistoryEntity
import com.AppexSolutions.gymsync.features.routines.data.local.mapper.toDomain
import com.AppexSolutions.gymsync.features.routines.domain.entities.RoutineHistory
import com.AppexSolutions.gymsync.features.routines.domain.repositories.RoutineHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoutineHistoryRepositoryImpl @Inject constructor(
    private val dao: RoutineHistoryDao
) : RoutineHistoryRepository {

    override fun getHistoryForUser(userId: Int): Flow<List<RoutineHistory>> =
        dao.getByUserId(userId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun recordCompletion(routineId: Int, routineName: String, userId: Int) {
        dao.insert(
            RoutineHistoryEntity(
                routineId = routineId,
                routineName = routineName,
                userId = userId,
                completedAt = System.currentTimeMillis()
            )
        )
    }
}
