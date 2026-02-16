package com.AppexSolutions.gymsync.features.clients.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientsUsecase

class ClientsViewModelFactory(
    private val getClientsUsecase: GetClientsUsecase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ClientsViewModel(getClientsUsecase) as T
    }
}
