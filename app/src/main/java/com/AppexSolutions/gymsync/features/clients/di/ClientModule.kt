package com.AppexSolutions.gymsync.features.clients.di

import com.AppexSolutions.gymsync.core.di.appContainer
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientsUsecase

class ClientModule (
    private val appContainer: appContainer
){
    private fun providerGetClientsUseCase(): GetClientsUsecase{
        return GetClientsUsecase(appContainer.ClientRepositorie)
    }
}