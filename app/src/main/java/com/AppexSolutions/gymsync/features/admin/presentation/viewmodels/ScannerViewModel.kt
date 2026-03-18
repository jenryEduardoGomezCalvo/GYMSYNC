package com.AppexSolutions.gymsync.features.admin.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.features.admin.data.repositories.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

sealed class ScannerUiState {
    object Idle : ScannerUiState()
    object Scanning : ScannerUiState()
    data class Success(val nombreCliente: String) : ScannerUiState()
    data class AlreadyScanned(val nombreCliente: String) : ScannerUiState()
    data class Error(val mensaje: String) : ScannerUiState()
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState = _uiState.asStateFlow()

    // Guarda los últimos escaneos: clienteId -> timestamp del escaneo
    private val recentScans = mutableMapOf<String, Long>()

    private companion object {
        const val DOUBLE_SCAN_WINDOW_MS = 5 * 60 * 1000L // 5 minutos
    }

    fun onQrDetected(rawValue: String) {
        if (_uiState.value is ScannerUiState.Success ||
            _uiState.value is ScannerUiState.AlreadyScanned
        ) return

        _uiState.value = ScannerUiState.Scanning

        viewModelScope.launch {
            try {
                val json = JSONObject(rawValue)
                val clienteId = json.getString("id")
                val nombreCliente = json.getString("nombre")
                val timestamp = System.currentTimeMillis()

                val lastScan = recentScans[clienteId]
                if (lastScan != null && timestamp - lastScan < DOUBLE_SCAN_WINDOW_MS) {
                    _uiState.value = ScannerUiState.AlreadyScanned(nombreCliente)
                    return@launch
                }

                repository.insertAttendance(
                    clienteId = clienteId,
                    nombreCliente = nombreCliente,
                    timestamp = timestamp
                )
                recentScans[clienteId] = timestamp
                _uiState.value = ScannerUiState.Success(nombreCliente)

            } catch (e: Exception) {
                _uiState.value = ScannerUiState.Error("QR inválido o no reconocido")
            }
        }
    }

    fun resetState() {
        _uiState.value = ScannerUiState.Idle
    }
}
