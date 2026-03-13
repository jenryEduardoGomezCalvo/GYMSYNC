package com.AppexSolutions.gymsync.features.clients.di

import com.AppexSolutions.gymsync.core.di.appContainer
import com.AppexSolutions.gymsync.features.clients.domain.usecases.*
import com.AppexSolutions.gymsync.features.clients.presentation.viewmodels.*

class ClientsModule(private val appContainer: appContainer) {

    /* ── USE CASES ── */

    private fun provideGetClientsUsecase() = GetClientsUsecase(appContainer.clientRepository)
    private fun provideGetClientByIdUseCase() = GetClientByIdUseCase(appContainer.clientRepository)
    private fun provideUpdateClientUseCase() = UpdateClientUseCase(appContainer.clientRepository)
    private fun provideDeleteClientUseCase() = DeleteClientUseCase(appContainer.clientRepository)
    private fun provideToggleUserActiveUseCase() = ToggleUserActiveUseCase(appContainer.clientRepository)
    private fun provideCreateUserUseCase() = CreateUserUseCase(appContainer.clientRepository)
    private fun provideGetRolesUseCase() = GetRolesUseCase(appContainer.clientRepository)
    private fun provideGetGymsUseCase() = GetGymsUseCase(appContainer.clientRepository)

    // 📷 Foto de perfil
    private fun provideSaveProfilePhotoUseCase() = SaveProfilePhotoUseCase(
        profilePhotoManager = appContainer.profilePhotoManager,
        profilePhotoDao = appContainer.profilePhotoDao
    )
    private fun provideGetProfilePhotoUseCase() = GetProfilePhotoUseCase(
        profilePhotoDao = appContainer.profilePhotoDao,
        profilePhotoManager = appContainer.profilePhotoManager
    )

    /* ── FACTORIES ── */

    fun provideClientsViewModelFactory() = ClientsViewModelFactory(
        getClientsUsecase = provideGetClientsUsecase(),
        profilePhotoDao = appContainer.profilePhotoDao
    )

    fun provideEditClientViewModelFactory(clientId: Int) = EditClientViewModelFactory(
        clientId = clientId,
        getClientByIdUseCase = provideGetClientByIdUseCase(),
        updateClientUseCase = provideUpdateClientUseCase(),
        deleteClientUseCase = provideDeleteClientUseCase(),
        toggleUserActiveUseCase = provideToggleUserActiveUseCase(),
        saveProfilePhotoUseCase = provideSaveProfilePhotoUseCase(),
        getProfilePhotoUseCase = provideGetProfilePhotoUseCase()
    )

    fun provideCreateUserViewModelFactory() = CreateUserViewModelFactory(
        createUserUseCase = provideCreateUserUseCase(),
        getRolesUseCase = provideGetRolesUseCase(),
        getGymsUseCase = provideGetGymsUseCase(),
        saveProfilePhotoUseCase = provideSaveProfilePhotoUseCase()
    )
}
