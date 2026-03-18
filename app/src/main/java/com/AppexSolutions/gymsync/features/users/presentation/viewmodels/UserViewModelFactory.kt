package com.AppexSolutions.gymsync.features.users.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.AppexSolutions.gymsync.core.datastore.ProfilePhotoDao
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientByIdUseCase
import com.AppexSolutions.gymsync.features.users.data.FakeUserRepository

class UserViewModelFactory(
    private val getClientByIdUseCase: GetClientByIdUseCase,
    private val fakeRepository: FakeUserRepository,
    private val profilePhotoDao: ProfilePhotoDao,
    private val clientId: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(getClientByIdUseCase, fakeRepository, profilePhotoDao, clientId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
