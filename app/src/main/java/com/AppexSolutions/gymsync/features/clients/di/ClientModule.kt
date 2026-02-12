package com.AppexSolutions.gymsync.features.clients.di

import com.AppexSolutions.gymsync.core.di.appContainer
import com.AppexSolutions.gymsync.features.clients.data.repositories.ClientsRepoImplements
import com.AppexSolutions.gymsync.features.clients.domain.usecases.DeleteClientUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientByIdUseCase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetClientsUsecase
import com.AppexSolutions.gymsync.features.clients.domain.usecases.UpdateClientUseCase
import com.AppexSolutions.gymsync.features.clients.presentation.viewmodels.ClientsViewModelFactory
import com.AppexSolutions.gymsync.features.clients.presentation.viewmodels.EditClientViewModelFactory

/**
 * Módulo de Clientes
 * Provee todas las dependencias de la feature Clients
 */
class ClientsModule(
    private val appContainer: appContainer
) {

    /* ---------------- REPOSITORY ---------------- */

    private fun provideClientsRepository(): ClientsRepoImplements {
        return ClientsRepoImplements(appContainer.gymApis)
    }

    /* ---------------- USE CASES ---------------- */

    private fun provideGetClientsUsecase(): GetClientsUsecase {
        return GetClientsUsecase(provideClientsRepository())
    }

    private fun provideGetClientByIdUseCase(): GetClientByIdUseCase {
        return GetClientByIdUseCase(provideClientsRepository())
    }

    private fun provideUpdateClientUseCase(): UpdateClientUseCase {
        return UpdateClientUseCase(provideClientsRepository())
    }

    private fun provideDeleteClientUseCase(): DeleteClientUseCase {
        return DeleteClientUseCase(provideClientsRepository())
    }

    /* ---------------- FACTORIES ---------------- */

    // ✅ Factory para la LISTA de clientes
    fun provideClientsViewModelFactory(): ClientsViewModelFactory {
        return ClientsViewModelFactory(
            getClientsUsecase = provideGetClientsUsecase()
        )
    }

    // ✅ Factory para EDITAR cliente
    fun provideEditClientViewModelFactory(
        clientId: Int
    ): EditClientViewModelFactory {
        return EditClientViewModelFactory(
            clientId = clientId,
            getClientByIdUseCase = provideGetClientByIdUseCase(),
            updateClientUseCase = provideUpdateClientUseCase(),
            deleteClientUseCase = provideDeleteClientUseCase()
        )
    }
}
