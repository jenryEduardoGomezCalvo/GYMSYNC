package com.AppexSolutions.gymsync.features.admin.data.repositories

import com.AppexSolutions.gymsync.core.datastore.AttendanceByDate
import com.AppexSolutions.gymsync.core.datastore.AttendanceDao
import com.AppexSolutions.gymsync.core.datastore.AttendanceEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val attendanceDao: AttendanceDao
) {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun insertAttendance(clienteId: String, nombreCliente: String, timestamp: Long) {
        val fecha = dateFormatter.format(Date(timestamp))
        val entity = AttendanceEntity(
            clienteId = clienteId,
            nombreCliente = nombreCliente,
            timestamp = timestamp,
            fecha = fecha
        )
        attendanceDao.insertAttendance(entity)
    }

    fun getAttendancesGroupedByDate(): Flow<List<AttendanceByDate>> =
        attendanceDao.getAttendancesGroupedByDate()

    fun getAllAttendances(): Flow<List<AttendanceEntity>> =
        attendanceDao.getAllAttendances()

    fun getAttendancesByClienteId(clienteId: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendancesByClienteId(clienteId)
}
