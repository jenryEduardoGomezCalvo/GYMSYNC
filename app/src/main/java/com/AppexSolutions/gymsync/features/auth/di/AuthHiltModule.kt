package com.AppexSolutions.gymsync.features.auth.di

import com.AppexSolutions.gymsync.features.auth.data.repositories.AuthRepositoryImpl
import com.AppexSolutions.gymsync.features.auth.domain.repositories.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthHiltModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
