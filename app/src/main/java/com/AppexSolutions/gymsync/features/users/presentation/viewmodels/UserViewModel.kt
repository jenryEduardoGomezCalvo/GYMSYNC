package com.AppexSolutions.gymsync.features.users.presentation.viewmodels

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.core.datastore.UserDao
import com.AppexSolutions.gymsync.core.datastore.UserEntity
import com.AppexSolutions.gymsync.core.util.QrGenerator
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientByIdUseCase
import com.AppexSolutions.gymsync.features.users.data.mapper.toMemberProfile
import com.AppexSolutions.gymsync.features.users.domain.entities.MemberProfile
import com.AppexSolutions.gymsync.features.users.domain.entities.PlanStatus
import com.AppexSolutions.gymsync.features.users.domain.repositories.UserDataRepository
import com.AppexSolutions.gymsync.features.users.presentation.screens.UserUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getClientByIdUseCase: GetClientByIdUseCase,
    private val userDataRepository: UserDataRepository,
    private val userDao: UserDao,
    private val qrGenerator: QrGenerator
) : ViewModel() {

    private val clientId: Int = savedStateHandle["clientId"] ?: 0

    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap = _qrBitmap.asStateFlow()

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun reload() { loadData() }

    fun loadData() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            Log.d("UserViewModel", "loadData() clientId=$clientId")

            val result = getClientByIdUseCase(clientId)
            result.fold(
                onSuccess = { client ->
                    val profile = client.toMemberProfile()
                    val plans = userDataRepository.getMembershipPlans()
                    _uiState.update {
                        it.copy(profile = profile, plans = plans, isLoading = false, error = null)
                    }
                },
                onFailure = { e ->
                    Log.e("UserViewModel", "API falló (clientId=$clientId): ${e.message}", e)
                    // Fallback: cargar datos básicos desde Room (guardados en login)
                    val fallback = buildFallbackProfile()
                    if (fallback != null) {
                        Log.d("UserViewModel", "Usando fallback de Room para clientId=$clientId")
                        val plans = userDataRepository.getMembershipPlans()
                        _uiState.update {
                            it.copy(profile = fallback, plans = plans, isLoading = false, error = null)
                        }
                    } else {
                        val errorMsg = when {
                            e.message?.contains("401") == true -> "Sin autorización (401). Inicia sesión nuevamente."
                            e.message?.contains("403") == true -> "Acceso denegado (403)."
                            e.message?.contains("404") == true -> "Usuario no encontrado (404)."
                            e.message?.contains("Unable to resolve host") == true -> "Sin conexión a internet."
                            else -> "Error al cargar perfil: ${e.message}"
                        }
                        _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                    }
                }
            )
        }
    }

    private suspend fun buildFallbackProfile(): MemberProfile? {
        return try {
            val entity = userDao.getActiveUser() ?: return null
            entity.toFallbackProfile()
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error leyendo Room: ${e.message}", e)
            null
        }
    }

    fun onTabSelected(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun generateQr() {
        val profile = _uiState.value.profile ?: return
        viewModelScope.launch {
            _qrBitmap.value = qrGenerator.generarQr(
                userId = profile.id.toString(),
                nombre = profile.nombreCompleto
            )
        }
    }
}

/** Construye un MemberProfile básico desde los datos de sesión guardados en Room. */
private fun UserEntity.toFallbackProfile(): MemberProfile {
    val parts = name.trim().split(" ", limit = 2)
    val nombres = parts.getOrElse(0) { email.substringBefore("@") }
    val apellidos = parts.getOrElse(1) { "" }
    val id = if (backendId != 0) backendId else this.id
    return MemberProfile(
        id = id,
        nombres = nombres,
        apellidos = apellidos,
        email = email,
        telefono = null,
        profileImage = null,
        currentPlan = "—",
        planStatus = PlanStatus.ACTIVO,
        nextPaymentDate = "—",
        daysRemaining = 0,
        currentStreak = 0,
        monthlyVisits = 0,
        qrCode = "GYMSYNC-USR-${id.toString().padStart(3, '0')}-2026"
    )
}
