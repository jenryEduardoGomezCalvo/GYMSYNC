package com.AppexSolutions.gymsync.features.notifications.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import com.AppexSolutions.gymsync.features.notifications.domain.usecases.RequestNotificationPermissionUseCase

/**
 * Composable que maneja la solicitud de permisos de notificación (Android 13+)
 * Debe usarse en la pantalla principal o después del login
 */
@Composable
fun NotificationPermissionHandler(
    context: Context,
    requestNotificationPermissionUseCase: RequestNotificationPermissionUseCase,
    onPermissionGranted: () -> Unit = {},
    onPermissionDenied: () -> Unit = {}
) {
    // Launcher para solicitar permiso (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    // Verificar si necesitamos solicitar permiso
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            when {
                ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED -> {
                    onPermissionGranted()
                }
                else -> {
                    permissionLauncher.launch(permission)
                }
            }
        } else {
            // Android < 13, permiso implícito
            onPermissionGranted()
        }
    }
}
