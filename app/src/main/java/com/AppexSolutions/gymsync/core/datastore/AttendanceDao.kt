package com.AppexSolutions.gymsync.core.datastore

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class AttendanceByDate(
    val fecha: String,
    val total: Int
)

@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    @Query("SELECT * FROM attendances ORDER BY timestamp DESC")
    fun getAllAttendances(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendances WHERE fecha = :fecha ORDER BY timestamp DESC")
    fun getAttendancesByDate(fecha: String): Flow<List<AttendanceEntity>>

    @Query("SELECT fecha, COUNT(*) AS total FROM attendances GROUP BY fecha ORDER BY fecha DESC")
    fun getAttendancesGroupedByDate(): Flow<List<AttendanceByDate>>

    @Query("SELECT * FROM attendances WHERE cliente_id = :clienteId ORDER BY timestamp DESC")
    fun getAttendancesByClienteId(clienteId: String): Flow<List<AttendanceEntity>>
}
