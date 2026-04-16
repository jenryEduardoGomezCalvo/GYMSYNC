package com.AppexSolutions.gymsync.features.clients.di

import com.AppexSolutions.gymsync.features.clients.data.repositories.ClientsRepoImplements
import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ClientsHiltModule {

    @Binds
    @Singleton
    abstract fun bindClientRepository(impl: ClientsRepoImplements): ClientRepository
}
