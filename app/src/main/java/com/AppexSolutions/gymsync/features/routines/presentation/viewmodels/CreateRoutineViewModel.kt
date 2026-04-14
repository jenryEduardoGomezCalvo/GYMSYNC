package com.AppexSolutions.gymsync.features.routines.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.routines.domain.usecases.CreateRoutineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateRoutineViewModel @Inject constructor(
    private val createRoutineUseCase: CreateRoutineUseCase
) : ViewModel() {

    data class UiState(
        val name: String = "",
        val selectedDays: Set<Int> = emptySet(),
        val notificationHour: Int = 8,
        val notificationMinute: Int = 0,
        val isSaving: Boolean = false,
        val savedRoutineId: Int? = null,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun setName(name: String) = _uiState.update { it.copy(name = name) }

    fun toggleDay(day: Int) = _uiState.update { s ->
        s.copy(
            selectedDays = if (day in s.selectedDays) s.selectedDays - day
            else s.selectedDays + day
        )
    }

    fun setNotificationTime(hour: Int, minute: Int) =
        _uiState.update { it.copy(notificationHour = hour, notificationMinute = minute) }

    fun save(userId: Int) {
        val state = _uiState.value
        if (state.name.isBlank() || state.selectedDays.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val id = createRoutineUseCase(
                    name = state.name,
                    userId = userId,
                    days = state.selectedDays.sorted(),
                    notificationHour = state.notificationHour,
                    notificationMinute = state.notificationMinute
                )
                _uiState.update { it.copy(isSaving = false, savedRoutineId = id) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
