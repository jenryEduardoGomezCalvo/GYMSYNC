package com.AppexSolutions.gymsync.features.auth.di

import com.AppexSolutions.gymsync.core.di.appContainer
import com.AppexSolutions.gymsync.features.auth.domain.usecases.PostUserUseCase
import com.AppexSolutions.gymsync.features.auth.presentation.viewmodels.GymLoginViewModelFactory

class GymModule(private val appContainer: appContainer) {
    fun provideLoginViewModelFactory() = GymLoginViewModelFactory(
        postUserUseCase = PostUserUseCase(appContainer.gymRepositories)
    )
}
