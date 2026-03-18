package com.AppexSolutions.gymsync.features.users.di

import com.AppexSolutions.gymsync.core.di.appContainer
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientByIdUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientsUsecase
import com.AppexSolutions.gymsync.features.users.data.FakeUserRepository
import com.AppexSolutions.gymsync.features.users.presentation.viewmodels.UserLoginViewModelFactory
import com.AppexSolutions.gymsync.features.users.presentation.viewmodels.UserViewModelFactory

class UserModule(private val appContainer: appContainer) {

    val fakeUserRepository: FakeUserRepository by lazy { FakeUserRepository() }

    private fun provideGetClientsUsecase() = GetClientsUsecase(appContainer.clientRepository)
    private fun provideGetClientByIdUseCase() = GetClientByIdUseCase(appContainer.clientRepository)

    fun provideUserLoginViewModelFactory() = UserLoginViewModelFactory(
        getClientsUsecase = provideGetClientsUsecase()
    )

    fun provideUserViewModelFactory(clientId: Int) = UserViewModelFactory(
        getClientByIdUseCase = provideGetClientByIdUseCase(),
        fakeRepository = fakeUserRepository,
        profilePhotoDao = appContainer.profilePhotoDao,
        clientId = clientId
    )
}
