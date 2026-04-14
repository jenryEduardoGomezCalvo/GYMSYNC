package com.AppexSolutions.gymsync.features.routines.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.routines.domain.entities.RoutineHistory
import com.AppexSolutions.gymsync.features.routines.domain.usecases.GetRoutineHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutineHistoryViewModel @Inject constructor(
    private val getRoutineHistory: GetRoutineHistoryUseCase
) : ViewModel() {

    data class UiState(
        val history: List<RoutineHistory> = emptyList(),
        val isLoading: Boolean = true
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadHistory(userId: Int) {
        viewModelScope.launch {
            getRoutineHistory(userId).collect { list ->
                _uiState.update { it.copy(history = list, isLoading = false) }
            }
        }
    }
}
