package com.AppexSolutions.gymsync.features.clients.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.AppexSolutions.gymsync.features.clients.domain.usecases.DeleteClientUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientByIdUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.ToggleUserActiveUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.UpdateClientUseCase

class EditClientViewModelFactory(
    private val clientId: Int,
    private val getClientByIdUseCase: GetClientByIdUseCase,
    private val updateClientUseCase: UpdateClientUseCase,
    private val deleteClientUseCase: DeleteClientUseCase,
    private val toggleUserActiveUseCase: ToggleUserActiveUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EditClientViewModel(clientId, getClientByIdUseCase, updateClientUseCase, deleteClientUseCase, toggleUserActiveUseCase) as T
    }
}
