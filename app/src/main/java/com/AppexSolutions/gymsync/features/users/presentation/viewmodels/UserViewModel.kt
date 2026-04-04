package com.AppexSolutions.gymsync.features.users.presentation.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AppexSolutions.gymsync.core.util.QrGenerator
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientByIdUseCase
import com.AppexSolutions.gymsync.features.users.data.mapper.toMemberProfile
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

    private fun loadData() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = getClientByIdUseCase(clientId)
            result.fold(
                onSuccess = { client ->
                    val profile = client.toMemberProfile()
                    val plans = userDataRepository.getMembershipPlans()
                    _uiState.update {
                        it.copy(
                            profile = profile,
                            plans = plans,
                            isLoading = false
                        )
                    }
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
