# MVP FCM - Resumen de Implementación

## TL;DR

La implementación de **Firebase Cloud Messaging (FCM)** para GYMSYNC está **95% completa**. Solo falta descargar el archivo de configuración de Firebase Console.

---

## ¿Qué está implementado?

### 1. Infraestructura ✅

- **Dependencias**: Firebase BOM, Messaging y Analytics agregados
- **AndroidManifest**: Permisos y servicio configurados
- **Servicio FCM**: `GymSyncMessagingService` maneja 7 tipos de mensajes

### 2. Capa de Dominio ✅

```kotlin
// Use Cases implementados
ProcessFcmMessageUseCase      // Procesa data messages
UpdateFcmTokenUseCase         // Actualiza token en Room
InitializeFcmUseCase          // Inicializa FCM post-login
RequestNotificationPermissionUseCase  // Maneja permisos Android 13+
```

### 3. Integración con Login ✅

El `LoginViewModel` ya llama a `initializeFcmUseCase()` después de:
- Login normal exitoso
- Login biométrico exitoso

```kotlin
// En LoginViewModel.kt (ya implementado)
loginUseCase(email, password).fold(
    onSuccess = { session ->
        // Inicializar FCM después del login
        initializeFcmUseCase()
        // ...
    }
)
```

### 4. Manejo de Mensajes ✅

El servicio soporta estos tipos de mensajes:

| Action | Tipo | Descripción |
|--------|------|-------------|
| `membership_expiring` | Notification + Data | Membresía por vencer (3 días) |
| `membership_expired` | Notification | Membresía expirada |
| `payment_confirmed` | Notification + Data | Pago procesado exitosamente |
| `attendance_confirmed` | Data | Asistencia registrada (silencioso) |
| `qr_refresh` | Data | Invalidar QR (silencioso) |
| `membership_updated` | Data | Datos de membresía actualizados |
| `gym_announcement` | Notification + Data | Anuncio del gimnasio |

---

## ¿Qué falta?

### Paso Único: Descargar `google-services.json`

Este es el **único paso pendiente** para que FCM funcione.

#### Instrucciones:

1. **Ir a Firebase Console**
   - Abrir: https://console.firebase.google.com

2. **Crear o seleccionar proyecto**
   - Crear nuevo proyecto (recomendado)
   - O seleccionar uno existente

3. **Agregar app Android**
   - Hacer clic en el icono de Android
   - **Package name**: `com.AppexSolutions.gymsync`
   - **App nickname**: `GYMSYNC`
   - **SHA-1** (opcional para debug): Obtener con:
     ```bash
     ./gradlew signingReport
     ```

4. **Descargar archivo de configuración**
   - Descargar `google-services.json`
   - Copiar a: `app/google-services.json`

5. **Sincronizar proyecto**
   - En Android Studio: `File → Sync Project with Gradle Files`
   - O ejecutar: `./gradlew :app:sync`

---

## Estructura de Payloads para Testing

### Desde Firebase Console

Ve a: **Firebase Console → Cloud Messaging → Send your first message**

#### Ejemplo 1: Notificación Simple

```json
{
  "notification": {
    "title": "Tu membresía vence pronto",
    "body": "Quedan 3 días. Renueva ahora!"
  },
  "data": {
    "action": "membership_expiring",
    "daysLeft": "3",
    "userId": "123"
  }
}
```

#### Ejemplo 2: Data Message (Silencioso)

```json
{
  "data": {
    "action": "attendance_confirmed",
    "clientId": "123",
    "gymId": "456",
    "timestamp": "2026-03-26T14:30:00Z"
  }
}
```

---

## Testing Checklist

Una vez configurado `google-services.json`:

- [ ] Compilar proyecto sin errores: `./gradlew :app:build`
- [ ] Iniciar sesión y verificar que se obtiene token FCM
- [ ] Enviar mensaje de prueba desde Firebase Console
- [ ] Verificar recepción en foreground, background y app cerrada
- [ ] Verificar que los data messages se procesan silenciosamente

---

## Arquitectura del Flujo FCM

```
┌─────────────────────────────────────────────────────────────────┐
│                        FIREBASE CONSOLE                         │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐     │
│  │   Send Msg   │───▶│  FCM Cloud   │───▶│   Device     │     │
│  └──────────────┘    └──────────────┘    └──────────────┘     │
└─────────────────────────────────────────────────────────────────┘
                                                      │
                                                      ▼
┌─────────────────────────────────────────────────────────────────┐
│              GymSyncMessagingService                            │
│                         │                                       │
│           ┌─────────────┼─────────────┐                        │
│           │             │             │                        │
│           ▼             ▼             ▼                        │
│     ┌─────────┐  ┌─────────┐  ┌─────────┐                    │
│     │Membership│  │Payment  │  │Attendance│                    │
│     │Expiring │  │Confirmed│  │Confirmed │                    │
│     └────┬────┘  └────┬────┘  └────┬────┘                    │
│          │            │            │                            │
│     ┌────┴────┐  ┌────┴────┐  ┌────┴────┐                    │
│     │Show     │  │Show     │  │Process  │                    │
│     │Notif.   │  │Notif.   │  │Silently │                    │
│     └─────────┘  └─────────┘  └─────────┘                    │
└─────────────────────────────────────────────────────────────────┘
```

---

## Recursos Adicionales

- **MVP Proposal**: `MVP_FCM_PROPOSAL.md`
- **Checklist**: `MVP_FCM_CHECKLIST.md`
- **Propuesta Original**: `FCM_PROPOSAL.md`

---

## Notas Importantes

1. **Android 13+**: El permiso `POST_NOTIFICATIONS` se solicita automáticamente. Sin él, los data messages siguen funcionando pero no se muestran notificaciones UI.

2. **Token Management**: El token FCM se almacena en Room asociado al usuario. Si cambia, `onNewToken()` lo actualiza automáticamente.

3. **Background Execution**: Los data messages funcionan incluso con la app cerrada (hasta que el SO la mate por recursos).

4. **Testing**: Usa la Firebase Console para enviar mensajes de prueba antes de integrar el backend.

---

**Fecha**: 26 de Marzo 2026
**Estado**: Listo para usar (solo falta `google-services.json`)
