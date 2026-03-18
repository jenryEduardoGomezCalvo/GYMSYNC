package com.AppexSolutions.gymsync.features.users.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.core.datastore.ProfilePhotoDao
import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientByIdUseCase
import com.AppexSolutions.gymsync.features.users.data.FakeUserRepository
import com.AppexSolutions.gymsync.features.users.domain.entities.MemberProfile
import com.AppexSolutions.gymsync.features.users.domain.entities.PlanStatus
import com.AppexSolutions.gymsync.features.users.presentation.screens.UserUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserViewModel(
    private val getClientByIdUseCase: GetClientByIdUseCase,
    private val fakeRepository: FakeUserRepository,
    private val profilePhotoDao: ProfilePhotoDao,
    private val clientId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = getClientByIdUseCase(clientId)
            result.fold(
                onSuccess = { client ->
                    val profile = client.toMemberProfile()
                    _uiState.update {
                        it.copy(
                            profile = profile,
                            plans = fakeRepository.membershipPlans,
                            isLoading = false
                        )
                    }
                    // Cargar foto de perfil desde Room
                    loadProfilePhoto()
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar perfil"
                        )
                    }
                }
            )
        }
    }

    private suspend fun loadProfilePhoto() {
        val uri = profilePhotoDao.getPhotoUri(clientId)
        if (uri != null && java.io.File(uri).exists()) {
            _uiState.update { it.copy(profilePhotoUri = uri) }
        }
    }

    fun onTabSelected(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    private fun Client.toMemberProfile(): MemberProfile {
        val plans = listOf("Pro", "Premium", "Ultimate")
        val assignedPlan = plans[id % plans.size]

        val isActive = activo
        val status = if (isActive) PlanStatus.ACTIVO else PlanStatus.VENCIDO

        return MemberProfile(
            id = id,
            nombres = nombres,
            apellidos = apellidos,
            email = email,
            telefono = telefono,
            currentPlan = assignedPlan,
            planStatus = status,
            nextPaymentDate = if (isActive) "10 Abril 2026" else "—",
            daysRemaining = if (isActive) (15 + (id * 7) % 30) else 0,
            currentStreak = if (isActive) (3 + (id * 5) % 25) else 0,
            monthlyVisits = if (isActive) (5 + (id * 3) % 20) else 0,
            qrCode = "GYMSYNC-USR-${id.toString().padStart(3, '0')}-2026"
        )
    }
}
