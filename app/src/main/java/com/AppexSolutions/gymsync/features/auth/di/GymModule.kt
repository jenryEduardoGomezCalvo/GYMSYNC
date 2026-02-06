package com.AppexSolutions.gymsync.features.auth.di

import com.AppexSolutions.gymsync.core.di.appContainer
import com.AppexSolutions.gymsync.features.auth.domain.usecases.PostUserUseCase
import com.AppexSolutions.gymsync.features.auth.presentation.viewmodels.GymLoginViewModelFactory

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
}