package com.AppexSolutions.gymsync.features.clients.di

import com.AppexSolutions.gymsync.core.di.appContainer
import com.AppexSolutions.gymsync.features.clients.data.repositories.ClientsRepoImplements
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientsUsecase
import com.AppexSolutions.gymsync.features.clients.presentation.viewmodels.ClientsViewModelFactory

/**
 * Módulo de Clientes
 *
 * Provee todas las dependencias necesarias para la feature de clientes
 */
class ClientsModule(
    private val appContainer: appContainer
) {

    /**
     * Provee el repositorio de clientes
     */
    private fun provideClientsRepository(): ClientsRepoImplements {
        return ClientsRepoImplements(appContainer.gymApis)
    }

    /**
     * Provee el caso de uso para obtener clientes
     */
    private fun provideGetClientsUsecase(): GetClientsUsecase {
        return GetClientsUsecase(provideClientsRepository())
    }

    /**
     * Provee el Factory del ViewModel
     */
    fun provideClientsViewModelFactory(): ClientsViewModelFactory {
        return ClientsViewModelFactory(
            getClientsUsecase = provideGetClientsUsecase()
        )
    }
}