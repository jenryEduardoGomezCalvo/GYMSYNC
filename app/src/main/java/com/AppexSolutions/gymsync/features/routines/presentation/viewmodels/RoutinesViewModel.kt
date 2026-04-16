package com.AppexSolutions.gymsync.features.routines.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.routines.domain.entities.Routine
import com.AppexSolutions.gymsync.features.routines.domain.usecases.DeleteRoutineUseCase
import com.AppexSolutions.gymsync.features.routines.domain.usecases.GetUserRoutinesUseCase
import com.AppexSolutions.gymsync.features.routines.notifications.RoutineAlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val getUserRoutines: GetUserRoutinesUseCase,
    private val deleteRoutineUseCase: DeleteRoutineUseCase,
    private val alarmScheduler: RoutineAlarmScheduler
) : ViewModel() {

    data class UiState(
        val routines: List<Routine> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadRoutines(userId: Int) {
        viewModelScope.launch {
            getUserRoutines(userId).collect { list ->
                _uiState.update { it.copy(routines = list, isLoading = false) }
            }
        }
    }

    fun deleteRoutine(routineId: Int) {
        viewModelScope.launch {
            try {
                deleteRoutineUseCase(routineId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    /** Programa una alarma de prueba para la rutina indicada, dispara en 15 segundos. */
    fun scheduleTestNotification(routineId: Int, routineName: String) {
        alarmScheduler.scheduleTest(routineId, routineName, delaySeconds = 15)
        _uiState.update { it.copy(error = "Prueba: notificación en ~15 s para \"$routineName\"") }
    }
}
