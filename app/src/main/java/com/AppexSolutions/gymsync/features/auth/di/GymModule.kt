package com.AppexSolutions.gymsync.features.auth.di

import com.AppexSolutions.gymsync.core.di.appContainer
import com.AppexSolutions.gymsync.features.auth.domain.usecases.PostUserUseCase
import com.AppexSolutions.gymsync.features.auth.domain.usecases.RegisterUserUseCase          // ← NUEVO import
import com.AppexSolutions.gymsync.features.auth.presentation.viewmodels.GymLoginViewModelFactory
import com.AppexSolutions.gymsync.features.auth.presentation.viewmodels.RegisterViewModelFactory  // ← NUEVO import

class GymModule(
    private val appContainer: appContainer
){
    private fun providePostUserUseCase(): PostUserUseCase{
        return PostUserUseCase(appContainer.GymRepositories)
    }

    fun providerLoginviewModelFactory(): GymLoginViewModelFactory{
        return GymLoginViewModelFactory(
            postUserUseCase = providePostUserUseCase()
        )
    }

    private fun provideRegisterUserUseCase(): RegisterUserUseCase {
        return RegisterUserUseCase(appContainer.GymRepositories)
    }

    fun provideRegisterViewModelFactory(): RegisterViewModelFactory {
        return RegisterViewModelFactory(
            registerUserUseCase = provideRegisterUserUseCase()
        )
    }
}