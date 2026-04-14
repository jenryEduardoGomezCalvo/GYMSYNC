package com.AppexSolutions.gymsync.features.routines.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.AppexSolutions.gymsync.features.routines.data.local.entity.RoutineDayEntity

@Dao
interface RoutineDayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(days: List<RoutineDayEntity>)

    @Query("DELETE FROM routine_days WHERE routine_id = :routineId")
    suspend fun deleteByRoutineId(routineId: Int)

    @Query("SELECT routine_id FROM routine_days WHERE day_of_week = :day")
    suspend fun getRoutineIdsByDay(day: Int): List<Int>

    @Query("SELECT day_of_week FROM routine_days WHERE routine_id = :routineId")
    suspend fun getDaysByRoutineId(routineId: Int): List<Int>
}
