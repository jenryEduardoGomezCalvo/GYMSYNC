package com.AppexSolutions.gymsync.features.clients.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.AppexSolutions.gymsync.features.clients.domain.usecases.CreateUserUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetGymsUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetRolesUseCase

class CreateUserViewModelFactory(
    private val createUserUseCase: CreateUserUseCase,
    private val getRolesUseCase: GetRolesUseCase,
    private val getGymsUseCase: GetGymsUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CreateUserViewModel(createUserUseCase, getRolesUseCase, getGymsUseCase) as T
    }
}
