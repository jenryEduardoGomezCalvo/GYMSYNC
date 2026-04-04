# Configuración FCM para GYMSYNC

## Pasos para completar la implementación

### 1. Crear proyecto en Firebase Console

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Crea un nuevo proyecto llamado "GYMSYNC"
3. Agrega una app Android con el package: `com.AppexSolutions.gymsync`
4. Descarga el archivo `google-services.json`
5. Coloca el archivo en: `app/google-services.json`

### 2. Estructura del archivo google-services.json

El archivo debe verse similar a esto:

```json
{
  "project_info": {
    "project_number": "123456789",
    "project_id": "gymsync-abc123",
    "storage_bucket": "gymsync-abc123.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:123456789:android:abc123def456",
        "android_client_info": {
          "package_name": "com.AppexSolutions.gymsync"
        }
      },
      "api_key": [
        {
          "current_key": "AIza..."
        }
      ]
    }
  ]
}
```

### 3. Sincronizar proyecto

Después de agregar el archivo, sincroniza el proyecto en Android Studio:

```bash
./gradlew clean build
```

O en Android Studio: **File > Sync Project with Gradle Files**

### 4. Tipos de notificaciones implementadas

El sistema soporta los siguientes tipos de mensajes:

#### Notification Messages (se muestran automáticamente)
- `membership_expiring` - Membresía por vencer (ej: 3 días restantes)
- `membership_expired` - Membresía expirada
- `payment_confirmed` - Pago procesado exitosamente
- `gym_announcement` - Anuncios del gimnasio

#### Data Messages (procesamiento silencioso)
- `attendance_confirmed` - Confirmación de asistencia
- `qr_refresh` - Regenerar código QR
- `membership_updated` - Actualizar datos de membresía

### 5. Payload de ejemplo para membresía por vencer

```json
{
  "notification": {
    "title": "Tu membresía vence pronto",
    "body": "Hola Juan, te quedan 3 días. Renueva ahora y mantén tu acceso al gimnasio."
  },
  "data": {
    "action": "membership_expiring",
    "userId": "123",
    "userName": "Juan",
    "daysLeft": "3",
    "navigateTo": "membership"
  },
  "token": "<fcm_token_del_dispositivo>"
}
```

### 6. Canales de notificación creados

- **gymsync_general** - Notificaciones generales y anuncios
- **gymsync_membership** - Recordatorios de vencimiento (ALTA PRIORIDAD)
- **gymsync_promotions** - Promociones y ofertas

### 7. Permisos necesarios

El sistema ya solicita automáticamente:
- `POST_NOTIFICATIONS` (Android 13+)
- Inicialización del token FCM después del login

### 8. Prueba de notificaciones

Desde Firebase Console:
1. Ve a **Cloud Messaging**
2. Click en **Send your first message**
3. Selecciona el app GYMSYNC
4. Escribe el título y cuerpo del mensaje
5. En **Custom data** agrega:
   - Key: `action`
   - Value: `membership_expiring`
6. Envía la notificación

### 9. Backend - Ejemplo de envío (Node.js)

```javascript
const admin = require('firebase-admin');

// Inicializar con service account
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// Enviar recordatorio de membresía por vencer
async function sendMembershipReminder(userToken, userName, daysLeft) {
  const message = {
    notification: {
      title: 'Tu membresía vence pronto',
      body: `Hola ${userName}, te quedan ${daysLeft} días. Renueva ahora!`,
    },
    data: {
      action: 'membership_expiring',
      userName: userName,
      daysLeft: daysLeft.toString(),
      navigateTo: 'membership'
    },
    token: userToken,
    android: {
      priority: 'high',
      notification: {
        channelId: 'gymsync_membership',
        sound: 'default'
      }
    }
  };

  const response = await admin.messaging().send(message);
  console.log('Mensaje enviado:', response);
}

// Enviar a todos los usuarios de un gimnasio usando topic
async function announceToGym(gymId, title, body) {
  const message = {
    notification: { title, body },
    data: {
      action: 'gym_announcement',
      announcementTitle: title,
      announcementBody: body
    },
    topic: `gym_${gymId}`,
  };

  await admin.messaging().send(message);
}
```

### 10. Verificación del token FCM

El token se obtiene automáticamente después del login y se guarda:
- Localmente en Room (campo `fcm_token` en tabla `users`)
- Debería enviarse al backend (implementar endpoint)

Para ver el token en logs (debug):
```kotlin
// Busca en logcat: "Nuevo FCM Token" o "FCM inicializado"
```

### 11. Troubleshooting

| Problema | Solución |
|----------|----------|
| No llegan notificaciones | Verificar que `google-services.json` está en app/ |
| Error de compilación | Sync Project with Gradle Files |
| Permiso denegado (Android 13+) | La app solicita automáticamente el permiso |
| Token null | Verificar conexión a internet |
| App crashea | Revisar que el servicio está declarado en AndroidManifest |

### 12. Estructura de archivos creados

```
features/notifications/
├── service/
│   └── GymSyncMessagingService.kt
├── domain/
│   ├── usecases/
│   │   ├── ProcessFcmMessageUseCase.kt
│   │   ├── UpdateFcmTokenUseCase.kt
│   │   ├── RequestNotificationPermissionUseCase.kt
│   │   └── InitializeFcmUseCase.kt
│   └── repository/
│       └── FcmRepository.kt
├── data/
│   └── repository/
│       └── FcmRepositoryImpl.kt
├── di/
│   └── NotificationModule.kt
└── presentation/
    └── NotificationPermissionHandler.kt
```

---

**Nota**: No subas el archivo `google-services.json` al repositorio git. Agregarlo a `.gitignore` si es necesario.
