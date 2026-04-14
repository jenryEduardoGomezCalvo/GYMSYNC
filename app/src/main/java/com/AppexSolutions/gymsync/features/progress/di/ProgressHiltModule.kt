package com.AppexSolutions.gymsync.features.progress.di

import com.AppexSolutions.gymsync.features.progress.data.repositories.ProgressRepositoryImpl
import com.AppexSolutions.gymsync.features.progress.domain.repositories.ProgressRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProgressHiltModule {

    @Binds
    @Singleton
    abstract fun bindProgressRepository(
        impl: ProgressRepositoryImpl
    ): ProgressRepository
}
