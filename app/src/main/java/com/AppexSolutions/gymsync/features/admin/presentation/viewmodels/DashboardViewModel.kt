package com.AppexSolutions.gymsync.features.admin.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.core.datastore.AttendanceByDate
import com.AppexSolutions.gymsync.core.datastore.AttendanceEntity
import com.AppexSolutions.gymsync.features.admin.data.repositories.AttendanceRepository
import com.AppexSolutions.gymsync.features.admin.domain.usecases.GetAttendanceSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
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
    private val repository: AttendanceRepository,
    private val getAttendanceSummaryUseCase: GetAttendanceSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            combine(
                repository.getAllAttendances(),
                repository.getAttendancesGroupedByDate()
            ) { todas, porDia ->
                val summary = getAttendanceSummaryUseCase(todas, porDia)
                DashboardUiState(
                    totalHoy = summary.totalHoy,
                    totalSemana = summary.totalSemana,
                    totalMes = summary.totalMes,
                    asistenciasPorDia = summary.asistenciasPorDia,
                    asistenciasRecientes = summary.asistenciasRecientes,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
