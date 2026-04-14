package com.AppexSolutions.gymsync.features.routines.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.AppexSolutions.gymsync.core.datastore.UserDao
import com.AppexSolutions.gymsync.features.routines.domain.repositories.RoutineRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject lateinit var userDao: UserDao
    @Inject lateinit var routineRepository: RoutineRepository
    @Inject lateinit var scheduler: RoutineAlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val lastUser = userDao.getLastBiometricUser()
                if (lastUser != null) {
                    val routines = routineRepository
                        .getRoutinesForUser(lastUser.id)
                        .firstOrNull() ?: emptyList()
                    routines.forEach { scheduler.schedule(it) }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
