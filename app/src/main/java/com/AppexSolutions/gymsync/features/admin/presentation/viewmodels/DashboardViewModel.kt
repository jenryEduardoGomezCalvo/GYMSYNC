package com.AppexSolutions.gymsync.features.admin.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.core.datastore.AttendanceByDate
import com.AppexSolutions.gymsync.core.datastore.AttendanceEntity
import com.AppexSolutions.gymsync.features.admin.data.repositories.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class DashboardUiState(
    val totalHoy: Int = 0,
    val totalSemana: Int = 0,
    val totalMes: Int = 0,
    val asistenciasPorDia: List<AttendanceByDate> = emptyList(),
    val asistenciasRecientes: List<AttendanceEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            combine(
                repository.getAllAttendances(),
                repository.getAttendancesGroupedByDate()
            ) { todas, porDia ->
                val hoy = dateFormatter.format(Calendar.getInstance().time)
                val inicioSemana = getStartOfWeek()
                val inicioMes = getStartOfMonth()

                val totalHoy = todas.count { it.fecha == hoy }

                val totalSemana = todas.count {
                    it.fecha >= inicioSemana && it.fecha <= hoy
                }

                val totalMes = todas.count {
                    it.fecha >= inicioMes && it.fecha <= hoy
                }

                val ultimos30Dias = getLast30Days()
                val asistenciasPorDia = porDia.filter { it.fecha >= ultimos30Dias }

                val asistenciasRecientes = todas.take(10)

                DashboardUiState(
                    totalHoy = totalHoy,
                    totalSemana = totalSemana,
                    totalMes = totalMes,
                    asistenciasPorDia = asistenciasPorDia,
                    asistenciasRecientes = asistenciasRecientes,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun getStartOfWeek(): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }
        return dateFormatter.format(cal.time)
    }

    private fun getStartOfMonth(): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return dateFormatter.format(cal.time)
    }

    private fun getLast30Days(): String {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -30)
        }
        return dateFormatter.format(cal.time)
    }
}
