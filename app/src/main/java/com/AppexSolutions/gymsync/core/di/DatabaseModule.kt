package com.AppexSolutions.gymsync.core.di

import android.content.Context
import androidx.room.Room
import com.AppexSolutions.gymsync.core.datastore.AppDatabase
import com.AppexSolutions.gymsync.core.datastore.AttendanceDao
import com.AppexSolutions.gymsync.core.datastore.ProfilePhotoDao
import com.AppexSolutions.gymsync.core.datastore.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gymsync_database"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideProfilePhotoDao(database: AppDatabase): ProfilePhotoDao {
        return database.profilePhotoDao()
    }

    @Provides
    @Singleton
    fun provideAttendanceDao(database: AppDatabase): AttendanceDao {
        return database.attendanceDao()
    }
}
