package com.AppexSolutions.gymsync.features.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.AppexSolutions.gymsync.features.auth.domain.usecases.RegisterUserUseCase

/**
 * Factory para crear RegisterViewModel con dependencias
 */
class RegisterViewModelFactory(
    private val registerUserUseCase: RegisterUserUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(registerUserUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
