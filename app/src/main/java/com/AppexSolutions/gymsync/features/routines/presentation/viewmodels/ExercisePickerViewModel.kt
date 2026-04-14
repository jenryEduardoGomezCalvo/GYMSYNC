package com.AppexSolutions.gymsync.features.routines.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.routines.domain.entities.Exercise
import com.AppexSolutions.gymsync.features.routines.domain.entities.MuscleGroup
import com.AppexSolutions.gymsync.features.routines.domain.usecases.AddExerciseToRoutineUseCase
import com.AppexSolutions.gymsync.features.routines.domain.usecases.GetExercisesByMuscleGroupUseCase
import com.AppexSolutions.gymsync.features.routines.domain.usecases.RefreshExercisesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExercisePickerViewModel @Inject constructor(
    private val getExercises: GetExercisesByMuscleGroupUseCase,
    private val refreshExercises: RefreshExercisesUseCase,
    private val addExerciseToRoutine: AddExerciseToRoutineUseCase
) : ViewModel() {

    data class ExerciseConfig(val sets: Int = 3, val reps: Int = 10, val restSeconds: Int = 60)

    data class UiState(
        val selectedGroup: MuscleGroup = MuscleGroup.PECHO,
        val exercises: List<Exercise> = emptyList(),
        val selectedIds: Set<Int> = emptySet(),
        val exerciseConfigs: Map<Int, ExerciseConfig> = emptyMap(),
        val isRefreshing: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        refreshIfStale()
        loadGroup(MuscleGroup.PECHO)
    }

    private fun refreshIfStale() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isRefreshing = true) }
                refreshExercises()
                _uiState.update { it.copy(isRefreshing = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRefreshing = false, error = e.message) }
            }
        }
    }

    fun selectGroup(group: MuscleGroup) {
        _uiState.update { it.copy(selectedGroup = group) }
        loadGroup(group)
    }

    private var currentGroupJob: kotlinx.coroutines.Job? = null

    private fun loadGroup(group: MuscleGroup) {
        currentGroupJob?.cancel()
        currentGroupJob = viewModelScope.launch {
            getExercises(group).collect { list ->
                _uiState.update { it.copy(exercises = list) }
            }
        }
    }

    fun toggleExercise(exerciseId: Int) = _uiState.update { s ->
        val newSelected = if (exerciseId in s.selectedIds) s.selectedIds - exerciseId
        else s.selectedIds + exerciseId
        s.copy(selectedIds = newSelected)
    }

    fun updateConfig(exerciseId: Int, sets: Int, reps: Int, restSeconds: Int) =
        _uiState.update { s ->
            s.copy(
                exerciseConfigs = s.exerciseConfigs + (exerciseId to ExerciseConfig(sets, reps, restSeconds))
            )
        }

    fun confirmSelection(routineId: Int) {
        val state = _uiState.value
        if (state.selectedIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                state.selectedIds.forEach { exerciseId ->
                    val config = state.exerciseConfigs[exerciseId] ?: ExerciseConfig()
                    addExerciseToRoutine(routineId, exerciseId, config.sets, config.reps, config.restSeconds)
                }
                _uiState.update { it.copy(isSaving = false, selectedIds = emptySet()) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun refresh() = refreshIfStale()
}
