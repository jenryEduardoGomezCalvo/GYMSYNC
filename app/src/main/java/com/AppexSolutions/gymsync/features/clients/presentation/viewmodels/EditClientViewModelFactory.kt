package com.AppexSolutions.gymsync.features.clients.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.AppexSolutions.gymsync.features.clients.domain.usecases.DeleteClientUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientByIdUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.UpdateClientUseCase

/**
 * Factory para crear EditClientViewModel con dependencias
 */
class EditClientViewModelFactory(
    private val clientId: Int,
    private val getClientByIdUseCase: GetClientByIdUseCase,
    private val updateClientUseCase: UpdateClientUseCase,
    private val deleteClientUseCase: DeleteClientUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditClientViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditClientViewModel(
                clientId = clientId,
                getClientByIdUseCase = getClientByIdUseCase,
                updateClientUseCase = updateClientUseCase,
                deleteClientUseCase = deleteClientUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}