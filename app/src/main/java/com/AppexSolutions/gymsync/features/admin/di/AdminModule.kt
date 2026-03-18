package com.AppexSolutions.gymsync.features.admin.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Módulo Hilt del feature admin.
 * AttendanceRepository se inyecta directamente vía @Inject constructor —
 * no requiere @Provides aquí.
 * Reservado para bindings futuros del módulo admin.
 */
@Module
@InstallIn(SingletonComponent::class)
object AdminModule
