package com.AppexSolutions.gymsync.features.admin.domain.usecases

import com.AppexSolutions.gymsync.core.datastore.AttendanceByDate
import com.AppexSolutions.gymsync.core.datastore.AttendanceEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class GetAttendanceSummaryUseCase @Inject constructor() {

    data class AttendanceSummary(
        val totalHoy: Int,
        val totalSemana: Int,
        val totalMes: Int,
        val asistenciasPorDia: List<AttendanceByDate>,
        val asistenciasRecientes: List<AttendanceEntity>
    )

    operator fun invoke(
        todas: List<AttendanceEntity>,
        porDia: List<AttendanceByDate>
    ): AttendanceSummary {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hoy = formatter.format(Calendar.getInstance().time)
        val inicioSemana = getStartOfWeek(formatter)
        val inicioMes = getStartOfMonth(formatter)
        val ultimos30Dias = getLast30Days(formatter)

        return AttendanceSummary(
            totalHoy = todas.count { it.fecha == hoy },
            totalSemana = todas.count { it.fecha >= inicioSemana && it.fecha <= hoy },
            totalMes = todas.count { it.fecha >= inicioMes && it.fecha <= hoy },
            asistenciasPorDia = porDia.filter { it.fecha >= ultimos30Dias },
            asistenciasRecientes = todas.take(10)
        )
    }

    private fun getStartOfWeek(formatter: SimpleDateFormat): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }
        return formatter.format(cal.time)
    }

    private fun getStartOfMonth(formatter: SimpleDateFormat): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return formatter.format(cal.time)
    }

    private fun getLast30Days(formatter: SimpleDateFormat): String {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -30)
        }
        return formatter.format(cal.time)
    }
}
