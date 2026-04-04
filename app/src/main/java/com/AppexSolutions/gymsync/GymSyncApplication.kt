package com.AppexSolutions.gymsync

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.AppexSolutions.gymsync.features.routines.data.workers.SyncWorker
import com.AppexSolutions.gymsync.features.routines.notifications.RoutineNotificationReceiver
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class GymSyncApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        schedulePeriodicSync()
    }

    private fun schedulePeriodicSync() {
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                RoutineNotificationReceiver.CHANNEL_ID,
                "Rutinas de ejercicio",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios de tus rutinas de ejercicio"
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}
