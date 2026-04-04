# MVP: Implementación de Firebase Cloud Messaging (FCM) en GYMSYNC

## Resumen Ejecutivo

Este MVP propone la implementación completa de **Firebase Cloud Messaging (FCM)** para GYMSYNC, permitiendo:
- **Notificaciones push** para membresías por vencer, pagos confirmados y anuncios del gimnasio
- **Sincronización silenciosa** de asistencia y datos de membresía vía Data Messages
- **Seguimiento de tokens FCM** por usuario para envío segmentado

**Tiempo estimado de implementación**: 2-3 días
**Complejidad**: Media
**Dependencias**: Firebase Console, Backend integration (opcional para MVP)

---

## Estado Actual del Proyecto

| Componente | Estado | Ubicación |
|------------|--------|-----------|
| Dependencias Gradle | ✅ Configurado | `libs.versions.toml`, `app/build.gradle.kts` |
| Servicio FCM | ✅ Implementado | `features/notifications/service/GymSyncMessagingService.kt` |
| AndroidManifest | ✅ Configurado | Permisos y servicio registrados |
| Use Cases | ✅ Implementados | ProcessFcmMessageUseCase, UpdateFcmTokenUseCase, InitializeFcmUseCase |
| Repositorio FCM | ✅ Implementado | FcmRepositoryImpl con Room integration |
| Módulo DI | ✅ Configurado | NotificationModule.kt |
| Permisos UI | ✅ Implementado | NotificationPermissionHandler.kt |
| google-services.json | ❌ **FALTA** | Descargar de Firebase Console |
| Integración Login | ❌ **FALTA** | Llamar InitializeFcmUseCase post-login |
| Tabla de asistencias | ❌ **FALTA** | Crear si se quiere persistir asistencia local |

---

## Alcance del MVP

### Funcionalidades Incluidas

1. **Recepción de Notificaciones (Notification Messages)**
   - Recordatorio de membresía por vencer (3 días antes)
   - Confirmación de pago exitoso
   - Anuncios generales del gimnasio

2. **Procesamiento de Data Messages**
   - Sincronización de asistencia (sin notificación UI)
   - Invalidación de QR para regeneración
   - Actualización de datos de membresía

3. **Gestión de Tokens FCM**
   - Obtención del token al iniciar sesión
   - Almacenamiento local en Room asociado al usuario
   - Actualización del token cuando FCM lo renueva

### Funcionalidades Fuera del MVP (Futuras)

- Envío de token al backend (requiere endpoint API)
- Suscripción a tópicos (gimnasios específicos)
- Historial de notificaciones en la app
- Preferencias de notificación por usuario

---

## Diagrama de Flujo del MVP

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           FLUJO DE IMPLEMENTACIÓN                          │
└─────────────────────────────────────────────────────────────────────────────┘

FASE 1: CONFIGURACIÓN FIREBASE CONSOLE (30 min)
┌─────────────────────────────────────────────────────────┐
│ 1. Crear proyecto en Firebase Console                   │
│ 2. Agregar app Android (package: com.AppexSolutions.   │
│    gymsync)                                             │
│ 3. Descargar google-services.json                       │
│ 4. Colocar en app/                                      │
│ 5. Habilitar Cloud Messaging API                        │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
FASE 2: INTEGRACIÓN EN LOGIN (1 hora)
┌─────────────────────────────────────────────────────────┐
│ LoginViewModel                                          │
│                                                         │
│ ├── login() exitoso                                     │
│ │   └── InitializeFcmUseCase()                         │
│ │       ├── Verifica permisos POST_NOTIFICATIONS       │
│ │       ├── Obtiene token FCM                          │
│ │       └── Guarda en Room (userDao.updateFcmToken)    │
│ │                                                      │
│ └── loginWithBiometric() exitoso                      │
│     └── InitializeFcmUseCase() (mismo flujo)          │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
FASE 3: MANEJO DE MENSAJES (ya implementado)
┌─────────────────────────────────────────────────────────┐
│ GymSyncMessagingService                                 │
│                                                         │
│ onMessageReceived(message)                              │
│ ├── Si tiene notification payload                      │
│ │   └── showNotification() (solo en foreground)        │
│ │                                                      │
│ └── Si tiene data payload                              │
│     └── processDataMessage()                           │
│         ├── "membership_expiring" → notificación UI    │
│         ├── "payment_confirmed" → notificación UI      │
│         ├── "attendance_confirmed" → sync silencioso   │
│         └── "qr_refresh" → invalidar QR local         │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
FASE 4: TESTING (1 hora)
┌─────────────────────────────────────────────────────────┐
│ Firebase Console → Cloud Messaging → Send test message │
│                                                         │
│ Escenarios:                                             │
│ 1. App en foreground → recibe y muestra notificación  │
│ 2. App en background → FCM muestra automáticamente    │
│ 3. App cerrada → FCM muestra al abrir                  │
│ 4. Token rotation → se actualiza en Room correctamente │
└─────────────────────────────────────────────────────────┘
```

---

## Estructura de Payloads para el MVP

### 1. Membresía por Vencer (Notification + Data)

```json
{
  "notification": {
    "title": "Tu membresía vence pronto",
    "body": "Hola {userName}, te quedan {daysLeft} días. Renueva ahora!"
  },
  "data": {
    "action": "membership_expiring",
    "userId": "123",
    "daysLeft": "3",
    "navigateTo": "membership"
  },
  "token": "<device_fcm_token>"
}
```

### 2. Pago Confirmado (Notification + Data)

```json
{
  "notification": {
    "title": "Pago confirmado",
    "body": "Tu pago ha sido procesado exitosamente."
  },
  "data": {
    "action": "payment_confirmed",
    "userId": "123",
    "membershipType": "premium",
    "navigateTo": "payment_history"
  },
  "token": "<device_fcm_token>"
}
```

### 3. Asistencia Confirmada (Data Message solo)

```json
{
  "data": {
    "action": "attendance_confirmed",
    "clientId": "123",
    "gymId": "456",
    "timestamp": "2026-03-26T14:30:00Z",
    "sessionType": "strength_training"
  },
  "token": "<device_fcm_token>",
  "android": { "priority": "high" }
}
```

---

## Cambios Requeridos

### 1. Integrar FCM en LoginViewModel

**Ubicación**: `features/auth/presentation/viewmodels/LoginViewModel.kt`

Agregar inyección:
```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    // ... dependencias existentes ...
    private val initializeFcmUseCase: InitializeFcmUseCase
) : ViewModel() {
    // ...
}
```

Llamar después de login exitoso:
```kotlin
// En login() y loginWithBiometric() cuando es exitoso:
viewModelScope.launch {
    initializeFcmUseCase()
}
```

### 2. Descargar google-services.json

**Pasos**:
1. Ir a [Firebase Console](https://console.firebase.google.com)
2. Crear proyecto o seleccionar existente
3. Agregar app Android
4. Ingresar package: `com.AppexSolutions.gymsync`
5. Descargar `google-services.json`
6. Copiar a `app/google-services.json`
7. Sync Project with Gradle Files

---

## Testing del MVP

### Escenario 1: Token Registration

```gherkin
Dado que el usuario inicia sesión exitosamente
Cuando se completa el login
Entonces se obtiene el token FCM
Y se guarda en Room asociado al usuario
```

### Escenario 2: Notificación Foreground

```gherkin
Dado que la app está abierta
Cuando llega un mensaje con notification payload
Entonces se muestra una notificación local personalizada
```

### Escenario 3: Notificación Background

```gherkin
Dado que la app está en segundo plano
Cuando llega un mensaje con notification payload
Entonces Android muestra la notificación automáticamente
```

### Escenario 4: Data Message Silencioso

```gherkin
Dado que la app está en cualquier estado
Cuando llega un data message con action "attendance_confirmed"
Entonces se procesa silenciosamente sin mostrar notificación
```

---

## Próximos Pasos Post-MVP

| Prioridad | Feature | Descripción |
|-----------|---------|-------------|
| P1 | Backend integration | Endpoint para recibir tokens y enviar mensajes |
| P2 | Topics | Suscripción automática al gimnasio del usuario |
| P3 | Analytics | Tracking de apertura de notificaciones |
| P4 | Rich notifications | Imágenes y acciones en notificaciones |
| P5 | Scheduled messages | Notificaciones programadas (cron jobs) |

---

## Recursos

- **Firebase Console**: https://console.firebase.google.com
- **FCM Documentation**: https://firebase.google.com/docs/cloud-messaging
- **FCM HTTP API**: https://firebase.google.com/docs/cloud-messaging/send-message

---

## Notas de Implementación

1. **Permisos Android 13+**: La app solicita `POST_NOTIFICATIONS` automáticamente. Sin este permiso, los data messages siguen funcionando pero no se muestran notificaciones UI.

2. **Token Management**: FCM puede regenerar el token en cualquier momento. El `GymSyncMessagingService` maneja esto vía `onNewToken()`.

3. **Background Execution**: Los data messages funcionan en background incluso si la app está cerrada (a menos que el SO mate el proceso por recursos).

4. **Testing**: Usar la consola de Firebase para enviar mensajes de prueba antes de integrar el backend.

---

**Documento preparado para**: Implementación de FCM MVP
**Fecha**: 26 de Marzo 2026
**Autor**: Claude Code
