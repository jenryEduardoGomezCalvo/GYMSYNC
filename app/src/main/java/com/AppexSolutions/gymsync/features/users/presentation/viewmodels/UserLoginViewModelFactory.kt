package com.AppexSolutions.gymsync.features.users.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientsUsecase

class UserLoginViewModelFactory(
    private val getClientsUsecase: GetClientsUsecase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserLoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserLoginViewModel(getClientsUsecase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
