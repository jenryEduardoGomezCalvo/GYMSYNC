package com.AppexSolutions.gymsync.features.routines.data.repositories

import com.AppexSolutions.gymsync.features.routines.data.local.dao.ExerciseDao
import com.AppexSolutions.gymsync.features.routines.data.local.dao.RoutineDao
import com.AppexSolutions.gymsync.features.routines.data.local.dao.RoutineDayDao
import com.AppexSolutions.gymsync.features.routines.data.local.dao.RoutineExerciseDao
import com.AppexSolutions.gymsync.features.routines.data.local.entity.RoutineDayEntity
import com.AppexSolutions.gymsync.features.routines.data.local.entity.RoutineEntity
import com.AppexSolutions.gymsync.features.routines.data.local.entity.RoutineExerciseEntity
import com.AppexSolutions.gymsync.features.routines.data.local.mapper.toDomain
import com.AppexSolutions.gymsync.features.routines.domain.entities.Routine
import com.AppexSolutions.gymsync.features.routines.domain.repositories.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoutineRepositoryImpl @Inject constructor(
    private val routineDao: RoutineDao,
    private val dayDao: RoutineDayDao,
    private val exerciseDao: RoutineExerciseDao,
    private val exDao: ExerciseDao
) : RoutineRepository {

    override fun getRoutinesForUser(userId: Int): Flow<List<Routine>> =
        routineDao.getByUserId(userId).flatMapLatest { routineEntities ->
            val flows = routineEntities.map { entity ->
                buildRoutineFlow(entity)
            }
            if (flows.isEmpty()) flowOf(emptyList())
            else combine(flows) { it.toList() }
        }

    override fun getRoutineById(routineId: Int): Flow<Routine?> =
        routineDao.getById(routineId).flatMapLatest { entity ->
            if (entity == null) flowOf(null)
            else buildRoutineFlow(entity).map { it }
        }

    override fun getRoutinesForDay(userId: Int, dayOfWeek: Int): Flow<List<Routine>> =
        routineDao.getByUserId(userId).flatMapLatest { routineEntities ->
            val flows = routineEntities.map { entity -> buildRoutineFlow(entity) }
            if (flows.isEmpty()) flowOf(emptyList())
            else combine(flows) { routines ->
                routines.filter { dayOfWeek in it.days }
            }
        }

    override suspend fun createRoutine(routine: Routine): Int {
        val entity = RoutineEntity(
            userId = routine.userId,
            name = routine.name,
            notificationHour = routine.notificationHour,
            notificationMinute = routine.notificationMinute,
            createdAt = routine.createdAt
        )
        val id = routineDao.insert(entity).toInt()
        val days = routine.days.map { RoutineDayEntity(routineId = id, dayOfWeek = it) }
        dayDao.insertAll(days)
        return id
    }

    override suspend fun updateRoutine(routine: Routine) {
        val entity = RoutineEntity(
            id = routine.id,
            userId = routine.userId,
            name = routine.name,
            notificationHour = routine.notificationHour,
            notificationMinute = routine.notificationMinute,
            createdAt = routine.createdAt
        )
        routineDao.update(entity)
        dayDao.deleteByRoutineId(routine.id)
        dayDao.insertAll(routine.days.map { RoutineDayEntity(routineId = routine.id, dayOfWeek = it) })
    }

    override suspend fun deleteRoutine(routineId: Int) {
        routineDao.deleteById(routineId)
        // routine_days y routine_exercises se borran por CASCADE
    }

    override suspend fun addExerciseToRoutine(
        routineId: Int, exerciseId: Int, sets: Int, reps: Int, restSeconds: Int
    ) {
        val currentCount = exerciseDao.getByRoutineIdOnce(routineId).size
        exerciseDao.insert(
            RoutineExerciseEntity(
                routineId = routineId,
                exerciseId = exerciseId,
                sets = sets,
                reps = reps,
                restSeconds = restSeconds,
                sortOrder = currentCount
            )
        )
    }

    override suspend fun removeExerciseFromRoutine(routineExerciseId: Int) {
        exerciseDao.deleteById(routineExerciseId)
    }

    override suspend fun updateRoutineExercise(
        routineExerciseId: Int, sets: Int, reps: Int, restSeconds: Int
    ) {
        exerciseDao.updateSetsReps(routineExerciseId, sets, reps, restSeconds)
    }

    private fun buildRoutineFlow(entity: RoutineEntity): Flow<Routine> =
        exerciseDao.getByRoutineId(entity.id).map { reEntities ->
            val allExercises = exDao.getAll().associateBy { it.id }
            val exercises = reEntities.mapNotNull { re ->
                val exEntity = allExercises[re.exerciseId] ?: return@mapNotNull null
                re.toDomain(exEntity.toDomain())
            }
            val days = dayDao.getDaysByRoutineId(entity.id)
            entity.toDomain(days, exercises)
        }
}
