package com.AppexSolutions.gymsync.features.progress.presentation.viewmodels

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.progress.domain.entities.ProgressEntry
import com.AppexSolutions.gymsync.features.progress.domain.usecases.AddProgressEntryUseCase
import com.AppexSolutions.gymsync.features.progress.domain.usecases.DeleteProgressEntryUseCase
import com.AppexSolutions.gymsync.features.progress.domain.usecases.GetProgressHistoryUseCase
import com.AppexSolutions.gymsync.features.routines.notifications.RoutineNotificationReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class ProgressUiState(
    val entries: List<ProgressEntry> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedSuccessfully: Boolean = false
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getProgressHistory: GetProgressHistoryUseCase,
    private val addProgressEntry: AddProgressEntryUseCase,
    private val deleteProgressEntry: DeleteProgressEntryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState = _uiState.asStateFlow()

    fun loadHistory(userId: Int) {
        viewModelScope.launch {
            getProgressHistory(userId).collect { entries ->
                _uiState.update { it.copy(entries = entries, isLoading = false) }
            }
        }
    }

    fun addEntry(
        userId: Int,
        weight: Float?,
        waist: Float?,
        hips: Float?,
        chest: Float?,
        arms: Float?,
        photoUri: String?,
        notes: String?
    ) {
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                addProgressEntry(
                    ProgressEntry(
                        userId = userId,
                        date = Date(),
                        weight = weight,
                        waist = waist,
                        hips = hips,
                        chest = chest,
                        arms = arms,
                        photoUri = photoUri,
                        notes = notes.takeIf { !it.isNullOrBlank() }
                    )
                )
                _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
                scheduleProgressReminder()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch {
            try {
                deleteProgressEntry(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearSavedFlag() = _uiState.update { it.copy(savedSuccessfully = false) }

    /**
     * Programa un recordatorio en 7 días usando el BroadcastReceiver existente
     * de rutinas (mismo canal de notificaciones).
     */
    private fun scheduleProgressReminder() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, RoutineNotificationReceiver::class.java).apply {
            putExtra("routineName", "Registro de progreso físico")
            putExtra("routineId", -99)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_PROGRESS,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } catch (_: SecurityException) {
            // SCHEDULE_EXACT_ALARM no concedido — alarm no crítica, se ignora
        }
    }

    companion object {
        private const val REQUEST_CODE_PROGRESS = 9001
    }
}
