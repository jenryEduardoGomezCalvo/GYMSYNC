# MVP FCM Implementation Checklist

## Status de Implementación

### ✅ Completado

| Componente | Estado | Detalles |
|------------|--------|----------|
| Dependencias Gradle | ✅ | `firebase-bom`, `firebase-messaging`, `firebase-analytics` configurados |
| Plugin Google Services | ✅ | `gms-google-services` aplicado en `app/build.gradle.kts` |
| Permisos AndroidManifest | ✅ | `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `c2dm.permission.RECEIVE` |
| Servicio FCM | ✅ | `GymSyncMessagingService.kt` implementado con 7 actions soportados |
| Canales de Notificación | ✅ | `gymsync_general`, `gymsync_membership`, `gymsync_promotions` |
| Use Cases | ✅ | 4 use cases implementados y funcionales |
| Repositorio FCM | ✅ | `FcmRepositoryImpl` con Room integration |
| Módulo Hilt | ✅ | `NotificationModule.kt` para inyección de dependencias |
| Permisos UI | ✅ | `NotificationPermissionHandler.kt` para Android 13+ |
| UserEntity | ✅ | Campo `fcmToken` agregado |
| UserDao | ✅ | Métodos `updateFcmToken()` y `getUserWithFcmToken()` agregados |
| Integración Login | ✅ | `InitializeFcmUseCase` llamado en login normal y biométrico |

### ❌ Pendiente

| Componente | Prioridad | Acción Requerida |
|------------|-----------|------------------|
| `google-services.json` | **ALTA** | Descargar de Firebase Console y colocar en `app/` |
| Backend Endpoint | Media | Crear endpoint para recibir tokens FCM del usuario |
| Tabla Asistencias | Baja | Crear entidad Room para persistir asistencia local |
| Tabla Anuncios | Baja | Crear entidad Room para historial de notificaciones |
| Suscripción Topics | Baja | Implementar suscripción a tópicos de gimnasio |

---

## Próximos Pasos

### Paso 1: Configurar Firebase Console (Obligatorio)

1. Ir a [Firebase Console](https://console.firebase.google.com)
2. Crear proyecto nuevo o seleccionar existente
3. Agregar aplicación Android:
   - **Package name**: `com.AppexSolutions.gymsync`
   - **App nickname**: GYMSYNC
4. Descargar `google-services.json`
5. Copiar archivo a: `app/google-services.json`
6. Sync project with Gradle files en Android Studio

### Paso 2: Verificar Compilación

```bash
./gradlew :app:build
```

### Paso 3: Testing Manual

1. **Enviar mensaje de prueba desde Firebase Console:**
   - Ir a Firebase Console → Cloud Messaging
   - "Send your first message"
   - Seleccionar dispositivo de prueba (token FCM)
   - Enviar notificación de prueba

2. **Escenarios a probar:**
   - App en foreground: recibe y muestra notificación
   - App en background: FCM muestra notificación automática
   - App cerrada: FCM muestra notificación al abrir
   - Token rotation: reinstall app y verificar que se actualiza en Room

---

## Estructura de Archivos

```
app/src/main/java/com/AppexSolutions/gymsync/features/notifications/
├── service/
│   └── GymSyncMessagingService.kt          # ✅ Service principal FCM
├── domain/
│   ├── repository/
│   │   └── FcmRepository.kt                 # ✅ Interface del repo
│   └── usecases/
│       ├── ProcessFcmMessageUseCase.kt      # ✅ Procesa data messages
│       ├── UpdateFcmTokenUseCase.kt         # ✅ Actualiza token
│       ├── InitializeFcmUseCase.kt        # ✅ Inicializa FCM
│       └── RequestNotificationPermissionUseCase.kt  # ✅ Maneja permisos
├── data/
│   └── repository/
│       └── FcmRepositoryImpl.kt             # ✅ Implementación del repo
├── di/
│   └── NotificationModule.kt                # ✅ Módulo Hilt
└── presentation/
    └── NotificationPermissionHandler.kt     # ✅ Composable para permisos

app/src/main/
├── AndroidManifest.xml                      # ✅ Permisos y servicio
└── google-services.json                     # ❌ PENDIENTE: descargar de Firebase

app/
└── build.gradle.kts                         # ✅ Plugin y dependencias FCM

gradle/
└── libs.versions.toml                       # ✅ Versiones de Firebase
```

---

## Payloads Soportados

### Notification + Data Messages:
- `membership_expiring` - Recordatorio de vencimiento
- `membership_expired` - Membresía expirada
- `payment_confirmed` - Pago exitoso

### Data Messages (Silenciosos):
- `attendance_confirmed` - Sincronización de asistencia
- `qr_refresh` - Invalidación de QR
- `membership_updated` - Actualización de membresía
- `gym_announcement` - Anuncio del gimnasio

---

## Notas Técnicas

- **Android 13+**: Se requiere permiso `POST_NOTIFICATIONS`. El handler lo solicita automáticamente.
- **Data Messages**: Funcionan incluso con app cerrada (hasta que el SO mate el proceso por recursos).
- **Token Management**: FCM puede regenerar el token. `onNewToken()` se encarga de actualizarlo en Room.
- **Foreground vs Background**: En foreground, `onMessageReceived()` maneja todo. En background, el sistema maneja notification payload automáticamente.

---

**Última actualización**: 26 de Marzo 2026
**Implementado por**: Claude Code
