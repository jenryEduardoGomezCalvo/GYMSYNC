package com.AppexSolutions.gymsync.core.di

import android.content.Context
import androidx.room.Room
import com.AppexSolutions.gymsync.core.datastore.AppDatabase
import com.AppexSolutions.gymsync.core.datastore.AttendanceDao
import com.AppexSolutions.gymsync.core.datastore.ProfilePhotoDao
import com.AppexSolutions.gymsync.core.datastore.UserDao
import com.AppexSolutions.gymsync.core.datastore.camera.CameraDataSource
import com.AppexSolutions.gymsync.features.routines.data.local.dao.ExerciseDao
import com.AppexSolutions.gymsync.features.routines.data.local.dao.RoutineDao
import com.AppexSolutions.gymsync.features.routines.data.local.dao.RoutineDayDao
import com.AppexSolutions.gymsync.features.routines.data.local.dao.RoutineExerciseDao
import com.AppexSolutions.gymsync.features.progress.data.local.dao.ProgressDao
import com.AppexSolutions.gymsync.features.routines.data.local.dao.RoutineHistoryDao
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
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8
            )
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

    @Provides
    @Singleton
    fun provideCameraDataSource(@ApplicationContext context: Context): CameraDataSource {
        return CameraDataSource(context)
    }

    // DAOs de Rutinas
    @Provides @Singleton
    fun provideExerciseDao(db: AppDatabase): ExerciseDao = db.exerciseDao()

    @Provides @Singleton
    fun provideRoutineDao(db: AppDatabase): RoutineDao = db.routineDao()

    @Provides @Singleton
    fun provideRoutineDayDao(db: AppDatabase): RoutineDayDao = db.routineDayDao()

    @Provides @Singleton
    fun provideRoutineExerciseDao(db: AppDatabase): RoutineExerciseDao = db.routineExerciseDao()

    @Provides @Singleton
    fun provideRoutineHistoryDao(db: AppDatabase): RoutineHistoryDao = db.routineHistoryDao()

    // DAO de Progreso físico
    @Provides @Singleton
    fun provideProgressDao(db: AppDatabase): ProgressDao = db.progressDao()
}
