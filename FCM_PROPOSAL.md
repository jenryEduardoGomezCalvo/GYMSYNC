# Propuesta: Implementación de FCM en GYMSYNC

## Resumen Ejecutivo

Esta propuesta presenta la implementación de **Firebase Cloud Messaging (FCM)** para GYMSYNC, utilizando tanto **Notification Messages** como **Data Messages**, integrados mediante un `FirebaseMessagingService` que sigue los principios de Clean Architecture.

---

## 1. Diferencias: Notification Message vs Data Message

### Notification Message
| Aspecto | Descripción |
|---------|-------------|
| **Definición** | Mensaje que FCM muestra automáticamente en la bandeja de notificaciones |
| **Manejo en foreground** | La app recibe el payload en `onMessageReceived()` |
| **Manejo en background/killed** | FCM muestra la notificación directamente sin ejecutar código de la app |
| **Uso en GYMSYNC** | Recordatorios de membresía, promociones, anuncios generales |
| **Payload típico** | `title`, `body`, `image`, `click_action` |

### Data Message
| Aspecto | Descripción |
|---------|-------------|
| **Definición** | Mensaje con datos personalizados que SIEMPRE ejecuta código de la app |
| **Manejo en foreground** | `onMessageReceived()` recibe los datos |
| **Manejo en background/killed** | `onMessageReceived()` también se ejecuta (a menos que el SO mate el proceso por recursos) |
| **Uso en GYMSYNC** | Sincronización silenciosa de asistencia, actualización de QR, acciones del admin |
| **Payload típico** | `{"action": "sync_attendance", "clientId": "123", "timestamp": "..."}` |

---

## 2. Arquitectura de Servicios en Android

### ¿Por qué un Service para FCM?

En Android, los **Services** son componentes que ejecutan operaciones de larga duración en segundo plano. Para FCM necesitamos:

1. **`FirebaseMessagingService`** - Extiende `Service` y maneja la recepción de mensajes
2. **`JobIntentService`/`WorkManager`** - Para procesamiento pesado sin bloquear el hilo principal
3. **Hilt para inyección** - El service necesita acceso a repositorios y use cases

### Flujo de Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                         FCM CLOUD                                │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐     │
│  │ Notification │    │   Data       │    │   Both       │     │
│  │   Message    │    │   Message    │    │  (Mixed)     │     │
│  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘     │
└─────────┼───────────────────┼───────────────────┼───────────────┘
          │                   │                   │
          ▼                   ▼                   ▼
┌─────────────────────────────────────────────────────────────────┐
│              FirebaseMessagingService                           │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  onMessageReceived(remoteMessage: RemoteMessage)          │   │
│  │  ├── Si tiene notification → mostrar notificación local  │   │
│  │  └── Si tiene data → procesar según acción              │   │
│  └─────────────────────────────────────────────────────────┘   │
│                          │                                      │
│  ┌───────────────────────┴─────────────────────────┐            │
│  │  onNewToken(token: String)                     │            │
│  │  └── Enviar token al backend para asociarlo    │            │
│  │      con el usuario autenticado                │            │
│  └────────────────────────────────────────────────┘            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      USE CASES (Domain Layer)                   │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐ │
│  │ SyncAttendance   │  │ ShowNotification │  │ RefreshQR    │ │
│  │ UseCase          │  │ UseCase          │  │ UseCase      │ │
│  └──────────────────┘  └──────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. Casos de Uso para GYMSYNC

### Escenario 1: Notificación Simple (Notification Message)

**Caso**: Recordatorio de membresía por vencer

```json
// Payload desde backend
{
  "notification": {
    "title": "Tu membresía vence pronto",
    "body": "Quedan 3 días. Renueva ahora y obtén 10% de descuento.",
    "image": "https://gymsync.com/promo.png"
  },
  "token": "<device_fcm_token>"
}
```

**Comportamiento**:
- **App abierta**: Se muestra un banner en la UI con la notificación
- **App cerrada**: Android muestra la notificación automáticamente
- **Acción al tocar**: Abre la app en la pantalla de membresías

---

### Escenario 2: Acción Silenciosa (Data Message)

**Caso**: Registrar asistencia vía QR escaneado por el admin

```json
// Payload desde backend
{
  "data": {
    "action": "attendance_confirmed",
    "clientId": "client_12345",
    "gymId": "gym_678",
    "timestamp": "2025-03-25T14:30:00Z",
    "sessionType": "strength_training"
  },
  "token": "<device_fcm_token>"
}
```

**Comportamiento**:
- La app recibe los datos en segundo plano
- Guarda la asistencia en Room local
- Actualiza el contador de sesiones restantes
- Si la app está abierta, muestra un toast de confirmación
- No muestra notificación en la bandeja (acción silenciosa)

---

### Escenario 3: Combinado (Notification + Data)

**Caso**: Nuevo cliente asignado al entrenador

```json
// Payload desde backend
{
  "notification": {
    "title": "Nuevo cliente asignado",
    "body": "Juan Pérez te ha seleccionado como entrenador personal."
  },
  "data": {
    "action": "new_client_assigned",
    "clientId": "client_12345",
    "clientName": "Juan Pérez",
    "planType": "personal_training",
    "navigateTo": "client_detail"
  },
  "token": "<device_fcm_token>"
}
```

**Comportamiento**:
- **App cerrada**: Muestra la notificación. Al tocar, abre el perfil del cliente
- **App abierta**: Recibe ambos payloads y puede mostrar un diálogo o banner interno

---
12
## 4. Propuesta de Implementación Técnica

### Paso 1: Dependencias Gradle

```toml
# En libs.versions.toml
[versions]
firebase-bom = "33.10.0"

[libraries]
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebase-bom" }
firebase-messaging = { group = "com.google.firebase", name = "firebase-messaging-ktx" }
firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics-ktx" }

[plugins]
gms-google-services = { id = "com.google.gms.google-services", version = "4.4.2" }
```

```kotlin
// En app/build.gradle.kts
plugins {
    // ... plugins existentes
    alias(libs.plugins.gms.google.services)
}

dependencies {
    // ... dependencias existentes

    // Firebase BOM (maneja versiones compatibles)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics) // Opcional para tracking
}
```

### Paso 2: Configuración del Service

```kotlin
// features/notifications/service/GymSyncMessagingService.kt
package com.AppexSolutions.gymsync.features.notifications.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.AppexSolutions.gymsync.MainActivity
import com.AppexSolutions.gymsync.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GymSyncMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var processFcmMessageUseCase: ProcessFcmMessageUseCase

    @Inject
    lateinit var updateFcmTokenUseCase: UpdateFcmTokenUseCase

    companion object {
        const val CHANNEL_GENERAL = "gymsync_general"
        const val CHANNEL_ATTENDANCE = "gymsync_attendance"
        const val CHANNEL_PROMOTIONS = "gymsync_promotions"
        const val TAG = "GymSyncFCM"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    /**
     * Se ejecuta cuando llega un mensaje FCM.
     * Se ejecuta tanto en foreground como background (para data messages).
     */
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "Mensaje recibido de: ${message.from}")
        Log.d(TAG, "Tipo de mensaje: ${message.messageType}")
        Log.d(TAG, "Data payload: ${message.data}")

        // 1. Extraer datos del payload
        val action = message.data["action"]
        val clientId = message.data["clientId"]
        val navigateTo = message.data["navigateTo"]

        // 2. Si el mensaje tiene "notification" payload, FCM ya lo mostró automáticamente
        //    cuando la app está en background. Pero en foreground, debemos manejarlo.
        message.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "GYMSYNC",
                body = notification.body ?: "",
                channelId = getChannelForAction(action),
                data = message.data
            )
        }

        // 3. Procesar data payload (siempre, independientemente del estado de la app)
        if (message.data.isNotEmpty()) {
            processDataMessage(message.data)
        }
    }

    /**
     * Se ejecuta cuando FCM genera un nuevo token (instalación, reinstall, etc.)
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Nuevo FCM Token: $token")

        // Enviar al backend para asociarlo con el usuario actual
        updateFcmTokenUseCase(token)
    }

    private fun processDataMessage(data: Map<String, String>) {
        val action = data["action"] ?: return

        when (action) {
            "attendance_confirmed" -> {
                // Guardar asistencia local silenciosamente
                processFcmMessageUseCase.syncAttendance(data)
            }
            "qr_refresh" -> {
                // Forzar regeneración del QR del usuario
                processFcmMessageUseCase.refreshQrCode(data)
            }
            "membership_updated" -> {
                // Actualizar datos de membresía
                processFcmMessageUseCase.updateMembership(data)
            }
            "new_client_assigned" -> {
                // Notificar al entrenador (ya se mostró la notificación,
                // aquí podríamos precargar datos)
                processFcmMessageUseCase.prefetchClientData(data)
            }
            "gym_announcement" -> {
                // Anuncio del gimnasio
                processFcmMessageUseCase.handleAnnouncement(data)
            }
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        channelId: String,
        data: Map<String, String>
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent para abrir la app al tocar la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // Pasar datos extras para navegación
            data["navigateTo"]?.let { putExtra("navigate_to", it) }
            data["clientId"]?.let { putExtra("client_id", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Necesitas crear este ícono
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun getChannelForAction(action: String?): String {
        return when (action) {
            "attendance_confirmed" -> CHANNEL_ATTENDANCE
            "qr_refresh" -> CHANNEL_ATTENDANCE
            "membership_updated" -> CHANNEL_PROMOTIONS
            "new_client_assigned" -> CHANNEL_GENERAL
            else -> CHANNEL_GENERAL
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_GENERAL,
                    "Notificaciones Generales",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Anuncios y notificaciones importantes" },

                NotificationChannel(
                    CHANNEL_ATTENDANCE,
                    "Asistencia",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Confirmaciones de asistencia y QR" },

                NotificationChannel(
                    CHANNEL_PROMOTIONS,
                    "Promociones",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Ofertas y promociones de membresía" }
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannels(channels)
        }
    }
}
```

### Paso 3: Use Cases (Domain Layer)

```kotlin
// features/notifications/domain/usecases/ProcessFcmMessageUseCase.kt
package com.AppexSolutions.gymsync.features.notifications.domain.usecases

import android.util.Log
import com.AppexSolutions.gymsync.core.datastore.AppDatabase
import com.AppexSolutions.gymsync.features.attendance.domain.repository.AttendanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessFcmMessageUseCase @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val userRepository: UserRepository,
    private val database: AppDatabase
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Sincroniza asistencia recibida por FCM.
     * Se ejecuta en background sin mostrar notificación.
     */
    fun syncAttendance(data: Map<String, String>) {
        val clientId = data["clientId"] ?: return
        val timestamp = data["timestamp"]
        val gymId = data["gymId"]

        scope.launch {
            try {
                // Guardar en base local
                attendanceRepository.recordAttendance(
                    clientId = clientId,
                    gymId = gymId ?: "",
                    timestamp = parseTimestamp(timestamp)
                )

                // Actualizar contador de sesiones restantes
                userRepository.decrementRemainingSessions(clientId)

                Log.d("FCM", "Asistencia sincronizada para cliente: $clientId")
            } catch (e: Exception) {
                Log.e("FCM", "Error sincronizando asistencia", e)
            }
        }
    }

    /**
     * Fuerza regeneración del QR del usuario.
     * Útil si el admin invalida QRs antiguos por seguridad.
     */
    fun refreshQrCode(data: Map<String, String>) {
        val userId = data["userId"] ?: return

        scope.launch {
            userRepository.invalidateQrCode(userId)
            Log.d("FCM", "QR invalidado para usuario: $userId")
        }
    }

    /**
     * Actualiza datos de membresía desde el servidor.
     */
    fun updateMembership(data: Map<String, String>) {
        val userId = data["userId"] ?: return

        scope.launch {
            userRepository.refreshMembershipData(userId)
            Log.d("FCM", "Membresía actualizada para: $userId")
        }
    }

    /**
     * Precarga datos del cliente para navegación rápida.
     */
    fun prefetchClientData(data: Map<String, String>) {
        val clientId = data["clientId"] ?: return

        scope.launch {
            userRepository.prefetchClientProfile(clientId)
        }
    }

    /**
     * Maneja anuncios del gimnasio (ya se muestra notificación,
     * aquí podríamos guardar en historial).
     */
    fun handleAnnouncement(data: Map<String, String>) {
        val announcementId = data["announcementId"] ?: return

        scope.launch {
            // Guardar en tabla de anuncios para historial
            // announcementRepository.saveAnnouncement(...)
        }
    }

    private fun parseTimestamp(timestamp: String?): Long {
        // Implementar parsing según formato
        return System.currentTimeMillis()
    }
}
```

```kotlin
// features/notifications/domain/usecases/UpdateFcmTokenUseCase.kt
package com.AppexSolutions.gymsync.features.notifications.domain.usecases

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateFcmTokenUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Envía el token FCM al backend para asociarlo con el usuario.
     * Se ejecuta cada vez que FCM genera un nuevo token.
     */
    operator fun invoke(token: String) {
        scope.launch {
            try {
                userRepository.updateFcmToken(token)
            } catch (e: Exception) {
                // Guardar localmente para retry posterior
                // tokenQueue.savePendingToken(token)
            }
        }
    }
}
```

```kotlin
// features/notifications/domain/usecases/RequestNotificationPermissionUseCase.kt
package com.AppexSolutions.gymsync.features.notifications.domain.usecases

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
nimport android.os.Build
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import javax.inject.Inject

class RequestNotificationPermissionUseCase @Inject constructor(
    private val context: Context
) {
    /**
     * Verifica si tenemos permiso de notificaciones (Android 13+)
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Antes de Android 13 no se necesita permiso explícito
        }
    }

    /**
     * Obtiene el token FCM actual.
     */
    suspend fun getCurrentToken(): String? = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                continuation.resume(token)
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }
}
```

### Paso 4: Repository (Data Layer)

```kotlin
// features/notifications/data/repository/FcmRepositoryImpl.kt
package com.AppexSolutions.gymsync.features.notifications.data.repository

import com.AppexSolutions.gymsync.core.network.GymSyncAPI
import com.AppexSolutions.gymsync.features.notifications.domain.repository.FcmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmRepositoryImpl @Inject constructor(
    private val api: GymSyncAPI,
    private val userDao: UserDao
) : FcmRepository {

    override suspend fun updateFcmToken(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Obtener usuario actual
            val currentUser = userDao.getCurrentUser() ?: return@withContext Result.failure(
                IllegalStateException("No hay usuario autenticado")
            )

            // Enviar al backend
            val response = api.updateFcmToken(
                userId = currentUser.id,
                token = token
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Paso 5: Di Module (Hilt)

```kotlin
// features/notifications/di/NotificationModule.kt
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

    @Binds
    abstract fun bindFcmRepository(
        impl: FcmRepositoryImpl
    ): FcmRepository
}
```

### Paso 6: Actualizar AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Permisos existentes -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />
    <uses-permission android:name="android.permission.USE_FINGERPRINT" />
    <uses-permission android:name="android.permission.CAMERA" />

    <!-- NUEVOS: Permisos para FCM -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />

    <application
        android:name=".GymSyncApplication"
        android:usesCleartextTraffic="true"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.GymSync">

        <!-- ... Provider y Activity existentes ... -->

        <!-- NUEVO: Servicio de Firebase Messaging -->
        <service
            android:name=".features.notifications.service.GymSyncMessagingService"
            android:exported="false"
            android:directBootAware="true">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>

        <!-- Metadata de Firebase -->
        <meta-data
            android:name="com.google.firebase.messaging.default_notification_icon"
            android:resource="@drawable/ic_notification" />
        <meta-data
            android:name="com.google.firebase.messaging.default_notification_channel_id"
            android:value="gymsync_general" />
        <meta-data
            android:name="firebase_analytics_collection_enabled"
            android:value="true" />

    </application>
</manifest>
```

### Paso 7: UI - Solicitar Permisos (Android 13+)

```kotlin
// features/notifications/presentation/NotificationPermissionHandler.kt
@Composable
fun NotificationPermissionHandler(
    viewModel: NotificationViewModel = hiltViewModel(),
    onPermissionGranted: () -> Unit = {}
) {
    val context = LocalContext.current

    // Launcher para solicitar permiso (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionGranted()
            onPermissionGranted()
        }
    }

    // Verificar si necesitamos solicitar permiso
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            when {
                ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED -> {
                    viewModel.onPermissionGranted()
                }
                shouldShowRequestPermissionRationale(context, permission) -> {
                    // Mostrar UI explicando por qué necesitamos notificaciones
                    viewModel.showPermissionRationale()
                }
                else -> {
                    permissionLauncher.launch(permission)
                }
            }
        } else {
            // Android < 13, permiso implícito
            viewModel.onPermissionGranted()
        }
    }
}
```

---

## 5. Comparativa: Cuándo usar cada tipo

| Situación | Tipo Recomendado | Razón |
|-----------|------------------|-------|
| Recordatorio de membresía por vencer | **Notification** | El usuario necesita verlo en bandeja |
| Promoción del gimnasio | **Notification** | Mensaje visible para marketing |
| Confirmación de asistencia (usuario) | **Notification + Data** | Notificación visible + guardar en BD |
| Registro silencioso de asistencia (admin) | **Data** | Solo sincronización, no molestar |
| Actualización de QR | **Data** | Acción técnica, sin UI |
| Membresía pagada exitosamente | **Notification + Data** | Confirmación + actualizar estado |
| Anuncio urgente del gimnasio | **Notification** | Alta prioridad, visible inmediato |
| Sync de datos offline | **Data** | Mantener datos actualizados silenciosamente |

---

## 6. Flujo de Integración con el Backend

```
┌────────────────────────────────────────────────────────────────────┐
│                           BACKEND (Node/Python)                      │
│                                                                      │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────────────┐     │
│  │  Usuario    │────▶│  Token FCM  │────▶│  Firebase Admin SDK │     │
│  │  paga plan  │     │  asociado   │     │  (servidor)         │     │
│  └─────────────┘     └─────────────┘     └─────────────────────┘     │
│                                                    │                 │
│                                                    ▼                 │
│                                           ┌─────────────┐            │
│                                           │  POST /fcm  │            │
│                                           │  send       │            │
│                                           └──────┬──────┘            │
└──────────────────────────────────────────────────┼───────────────────┘
                                                  │
                                                  ▼
┌────────────────────────────────────────────────────────────────────┐
│                      FIREBASE CLOUD MESSAGING                       │
│                           (Servicio FCM)                           │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  Encuentra el dispositivo por token                         │   │
│  │  Entrega el mensaje (Notification, Data, o Mixed)            │   │
│  └─────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────┘
                                                  │
                                                  ▼
┌────────────────────────────────────────────────────────────────────┐
│                         DISPOSITIVO ANDROID                         │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  FirebaseMessagingService.onMessageReceived()                  │   │
│  │  └── GymSyncMessagingService procesa el mensaje              │   │
│  └─────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────┘
```

---

## 7. Costos y Límites de FCM

| Aspecto | Límite/Costo |
|---------|--------------|
| Mensajes por dispositivo | 500 mensajes/hora/aplicación (burst) |
| Tópicos | Sin límite de suscriptores, hasta 2000 tópicos por app |
| Precio | Gratuito (hasta ciertos límites de Google Cloud) |
| Latencia | ~200-500ms promedio |

---

## 8. Conclusión y Recomendación

### Para GYMSYNC recomiendo:

1. **Usar ambos tipos de mensajes** según el caso de uso:
   - **Notification Messages**: Para comunicación con el usuario (membresías, promociones)
   - **Data Messages**: Para operaciones silenciosas (sync, QR refresh)

2. **Arquitectura**:
   - `GymSyncMessagingService` extiende `FirebaseMessagingService`
   - Use Cases en domain layer para cada acción (principio SRP)
   - Repository para enviar tokens al backend

3. **Ventajas para el negocio**:
   - Re-engagement de usuarios con membresías por vencer
   - Comunicación directa admin-cliente
   - Sync silencioso sin interrumpir al usuario
   - Mejor experiencia con datos siempre actualizados

4. **Próximos pasos**:
   - Crear proyecto en Firebase Console
   - Descargar `google-services.json`
   - Implementar los archivos propuestos
   - Configurar backend con Firebase Admin SDK

---

## Anexos

### A. Ejemplo de envío desde backend (Node.js)

```javascript
const admin = require('firebase-admin');

// Inicializar con service account
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// Enviar Notification Message
async function sendMembershipReminder(userToken, userName, daysLeft) {
  const message = {
    notification: {
      title: 'Tu membresía vence pronto',
      body: `Hola ${userName}, te quedan ${daysLeft} días. Renueva ahora!`,
    },
    token: userToken,
    // Opcional: prioridad alta para que llegue rápido
    android: {
      priority: 'high',
      notification: {
        channelId: 'gymsync_promotions',
        sound: 'default'
      }
    }
  };

  const response = await admin.messaging().send(message);
  console.log('Mensaje enviado:', response);
}

// Enviar Data Message
async function syncAttendance(userToken, attendanceData) {
  const message = {
    data: {
      action: 'attendance_confirmed',
      clientId: attendanceData.clientId,
      timestamp: new Date().toISOString(),
      gymId: attendanceData.gymId
    },
    token: userToken,
    // Para data messages, prioridad alta ayuda en doze mode
    android: { priority: 'high' }
  };

  await admin.messaging().send(message);
}

// Enviar mensaje a un tópico (todos los usuarios de un gimnasio)
async function announceToGym(gymId, announcement) {
  const message = {
    notification: {
      title: announcement.title,
      body: announcement.body,
    },
    topic: `gym_${gymId}`,
  };

  await admin.messaging().send(message);
}
```

### B. Diagrama de estados del Service

```
┌─────────────────────────────────────────────────────────────────┐
│                      GYMSYNC MESSAGING SERVICE                  │
│                                                                  │
│   ┌────────────┐    onMessageReceived()    ┌────────────┐       │
│   │   IDLE     │ ────────────────────────▶ │ PROCESSING │       │
│   │            │                           │            │       │
│   └────────────┘                           └────────────┘       │
│         ▲                                        │                │
│         │                              ┌───────┴───────┐        │
│   onNewToken()                         │               │        │
│   (regenerar token)              notification      data payload   │
│                                        │               │        │
│                                  ┌─────┴─────┐    ┌───┴────┐    │
│                                  ▼           │    ▼        │    │
│                           ┌──────────┐       │ ┌────────┐  │    │
│                           │  Mostrar │       │ │Ejecutar│  │    │
│                           │Notificación    │ │ │Acción  │  │    │
│                           └──────────┘       │ └────────┘  │    │
│                                              │             │    │
│                                              │  ┌──────────┘    │
│                                              │  ▼               │
│                                              │ ┌──────────────┐ │
│                                              └▶│   GUARDAR    │ │
│                                                │   EN ROOM    │ │
│                                                └──────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

**Documento preparado para revisión del profesor**
**Proyecto**: GYMSYNC - Sistema de Gestión de Gimnasios
**Fecha**: 25 de Marzo 2026
