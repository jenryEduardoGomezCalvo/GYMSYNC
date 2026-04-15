package com.AppexSolutions.gymsync.features.clients.di

import com.AppexSolutions.gymsync.core.datastore.ProfilePhotoDao
import com.AppexSolutions.gymsync.core.datastore.camera.CameraDataSource
import com.AppexSolutions.gymsync.features.clients.data.datasource.hardware.ProfilePhotoManager
import com.AppexSolutions.gymsync.features.clients.data.repositories.ClientsRepoImplements
import com.AppexSolutions.gymsync.features.clients.domain.repositories.ClientRepository
import com.AppexSolutions.gymsync.features.clients.domain.usecases.GetAllClientPhotosUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ClientsHiltModule {

    @Binds
    @Singleton
    abstract fun bindClientRepository(impl: ClientsRepoImplements): ClientRepository

    companion object {

        @Provides
        @Singleton
        fun provideProfilePhotoManager(cameraDataSource: CameraDataSource): ProfilePhotoManager =
            ProfilePhotoManager(cameraDataSource)

        @Provides
        @Singleton
        fun provideGetAllClientPhotosUseCase(
            profilePhotoDao: ProfilePhotoDao
        ): GetAllClientPhotosUseCase = GetAllClientPhotosUseCase(profilePhotoDao)
    }
}
