package com.AppexSolutions.gymsync.features.routines.di

import com.AppexSolutions.gymsync.features.routines.data.repositories.ExerciseRepositoryImpl
import com.AppexSolutions.gymsync.features.routines.data.repositories.RoutineHistoryRepositoryImpl
import com.AppexSolutions.gymsync.features.routines.data.repositories.RoutineRepositoryImpl
import com.AppexSolutions.gymsync.features.routines.domain.repositories.ExerciseRepository
import com.AppexSolutions.gymsync.features.routines.domain.repositories.RoutineHistoryRepository
import com.AppexSolutions.gymsync.features.routines.domain.repositories.RoutineRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RoutinesHiltModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindRoutineRepository(impl: RoutineRepositoryImpl): RoutineRepository

    @Binds
    @Singleton
    abstract fun bindRoutineHistoryRepository(impl: RoutineHistoryRepositoryImpl): RoutineHistoryRepository
}
