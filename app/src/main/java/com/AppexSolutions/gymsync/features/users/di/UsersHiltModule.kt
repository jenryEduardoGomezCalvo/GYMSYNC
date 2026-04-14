package com.AppexSolutions.gymsync.features.users.di

import com.AppexSolutions.gymsync.core.util.QrGenerator
import com.AppexSolutions.gymsync.features.users.data.FakeUserDataRepository
import com.AppexSolutions.gymsync.features.users.domain.repositories.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UsersHiltModule {

    @Binds
    @Singleton
    abstract fun bindUserDataRepository(impl: FakeUserDataRepository): UserDataRepository

    companion object {
        @Provides
        @Singleton
        fun provideQrGenerator(): QrGenerator = QrGenerator()
    }
}
