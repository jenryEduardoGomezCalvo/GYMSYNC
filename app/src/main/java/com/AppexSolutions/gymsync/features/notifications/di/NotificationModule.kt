package com.AppexSolutions.gymsync.features.notifications.di

import com.AppexSolutions.gymsync.features.notifications.data.repository.AnnouncementRepositoryImpl
import com.AppexSolutions.gymsync.features.notifications.data.repository.FcmRepositoryImpl
import com.AppexSolutions.gymsync.features.notifications.domain.repository.AnnouncementRepository
import com.AppexSolutions.gymsync.features.notifications.domain.repository.FcmRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    abstract fun bindFcmRepository(
        impl: FcmRepositoryImpl
    ): FcmRepository

    @Binds
    abstract fun bindAnnouncementRepository(
        impl: AnnouncementRepositoryImpl
    ): AnnouncementRepository
}
