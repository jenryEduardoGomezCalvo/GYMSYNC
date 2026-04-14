package com.AppexSolutions.gymsync.features.routines.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.AppexSolutions.gymsync.features.routines.domain.repositories.ExerciseRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Worker periódico (cada 24 h) que refresca el caché local de ejercicios.
 *
 * No usa @HiltWorker porque androidx.hilt:hilt-compiler 1.x no soporta
 * metadata de Kotlin 2.x (genera "unsupported metadata kind: null" en kapt).
 * En su lugar usa @EntryPoint para obtener el repositorio desde el
 * SingletonComponent de Hilt sin necesitar kapt adicional.
 */
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncWorkerEntryPoint {
        fun exerciseRepository(): ExerciseRepository
    }

    private val exerciseRepository: ExerciseRepository by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            SyncWorkerEntryPoint::class.java
        ).exerciseRepository()
    }

    override suspend fun doWork(): Result {
        return try {
            if (exerciseRepository.isCacheStale()) {
                exerciseRepository.refreshExercises()
            }
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "gymsync_daily_exercise_sync"
        private const val MAX_RETRIES = 3
    }
}
