package com.AppexSolutions.gymsync.features.notifications.di

import com.AppexSolutions.gymsync.features.notifications.data.repository.FcmRepositoryImpl
import com.AppexSolutions.gymsync.features.notifications.domain.repository.FcmRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    // TODO: Descomentar cuando actives Firebase y tengas google-services.json
    @Binds
    abstract fun bindFcmRepository(
        impl: FcmRepositoryImpl
    ): FcmRepository
}
