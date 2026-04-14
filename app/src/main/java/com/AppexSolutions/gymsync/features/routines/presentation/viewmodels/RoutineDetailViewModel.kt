package com.AppexSolutions.gymsync.features.routines.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.routines.domain.entities.Routine
import com.AppexSolutions.gymsync.features.routines.domain.usecases.CompleteRoutineUseCase
import com.AppexSolutions.gymsync.features.routines.domain.usecases.GetRoutineDetailUseCase
import com.AppexSolutions.gymsync.features.routines.domain.usecases.RemoveExerciseFromRoutineUseCase
import com.AppexSolutions.gymsync.features.routines.domain.usecases.UpdateRoutineExerciseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutineDetailViewModel @Inject constructor(
    private val getRoutineDetail: GetRoutineDetailUseCase,
    private val removeExercise: RemoveExerciseFromRoutineUseCase,
    private val updateExercise: UpdateRoutineExerciseUseCase,
    private val completeRoutine: CompleteRoutineUseCase
) : ViewModel() {

    data class UiState(
        val routine: Routine? = null,
        val isLoading: Boolean = true,
        val isCompleting: Boolean = false,
        val completedSuccessfully: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadRoutine(routineId: Int) {
        viewModelScope.launch {
            getRoutineDetail(routineId).collect { routine ->
                _uiState.update { it.copy(routine = routine, isLoading = false) }
            }
        }
    }

    fun removeExercise(routineExerciseId: Int) {
        viewModelScope.launch { removeExercise.invoke(routineExerciseId) }
    }

    fun updateExercise(id: Int, sets: Int, reps: Int, restSeconds: Int) {
        viewModelScope.launch { updateExercise.invoke(id, sets, reps, restSeconds) }
    }

    fun completeRoutine(userId: Int) {
        val routine = _uiState.value.routine ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCompleting = true) }
            try {
                completeRoutine.invoke(routine.id, routine.name, userId)
                _uiState.update { it.copy(isCompleting = false, completedSuccessfully = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isCompleting = false, error = e.message) }
            }
        }
    }
}
