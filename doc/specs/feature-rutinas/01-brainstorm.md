# Sistema de Rutinas para GYMSYNC

**Slug:** feature-rutinas
**Author:** Claude Code
**Date:** 2026-04-12
**Branch:** preflight/feature-rutinas
**Related:** doc/specs/refactor-mvvm-clean-arch-nav3/ (arquitectura base)

---

## 1) Intent & Assumptions

- **Task brief:** Agregar a GYMSYNC un sistema de rutinas de ejercicio para miembros: visualización de ejercicios por grupo muscular, composición de rutinas, asignación de días de la semana y notificaciones locales en esos días.
- **Assumptions:**
  - La feature es para el **rol miembro** (usuario logueado), no para el admin.
  - Los ejercicios estarán predefinidos localmente (catálogo bundled), ya que no existe endpoint de ejercicios en el API actual.
  - Las rutinas son personales de cada miembro y se almacenan localmente en Room.
  - Las notificaciones son **locales** (no FCM), programadas en el dispositivo.
  - La feature sigue la arquitectura existente: Clean Architecture + MVVM + Hilt + Room.
  - La navegación se integra al sistema type-safe (@Serializable) ya implementado.
- **Out of scope:**
  - Sincronización de rutinas con el servidor/API.
  - Asignación de rutinas por parte del admin.
  - Tracking de progreso o historial de workouts completados (posible fase 2).
  - Ejercicios con video o multimedia.
  - Firebase Cloud Messaging — se usa AlarmManager o WorkManager para notificaciones locales.

---

## 2) Pre-reading Log

- `core/datastore/AppDatabase.kt` — DB versión 4, entidades: UserEntity, ClientProfilePhotoEntity, AttendanceEntity. Hay migraciones definidas v1→v4.
- `core/datastore/UserEntity.kt` — tabla `users`, tiene `id` (Room PK autogen) y `email`. El `clientId` del API es diferente al Room PK.
- `core/navigation/AppRoutes.kt` — rutas type-safe existentes para el flujo de miembro: `UserLogin`, `UserHome(clientId)`, `MembershipPlans(clientId)`.
- `features/users/presentation/screens/UserHomeScreen.kt` — tiene una sección "Acciones Rápidas" con ítems stub (Mis Clases, Historial de Pagos, Progreso). Candidato natural de punto de entrada a rutinas.
- `features/users/presentation/components/UserBottomNavBar.kt` — bottom nav con tabs (Home, Planes, Perfil). Posible punto de integración de tab "Rutinas".
- `features/notifications/service/GymSyncMessagingService.kt` — FCM implementado pero desactivado. **No hay WorkManager, AlarmManager ni BroadcastReceiver** en el codebase.
- `AndroidManifest.xml` — `POST_NOTIFICATIONS` y `RECEIVE_BOOT_COMPLETED` están comentados (esperando Firebase). Necesitan descomentarse para notificaciones locales en Android 13+.
- `gradle/libs.versions.toml` — WorkManager **no está** en las dependencias. Necesita ser añadido. Versión Room = 2.7.0, Hilt = 2.56.2, minSdk = 24.
- `features/users/domain/entities/MemberProfile.kt` — contiene `id: Int` (Room PK del usuario logueado). Es el identificador disponible para asociar rutinas a un miembro.

---

## 3) Codebase Map

### Primary components/modules
| Módulo | Path | Rol |
|--------|------|-----|
| AppDatabase | `core/datastore/AppDatabase.kt` | Registrar nuevas entidades Room + migraciones |
| AppRoutes | `core/navigation/AppRoutes.kt` | Añadir rutas type-safe de rutinas |
| UserHomeScreen | `features/users/presentation/screens/UserHomeScreen.kt` | Punto de entrada (Acción Rápida o nuevo tab) |
| UserBottomNavBar | `features/users/presentation/components/UserBottomNavBar.kt` | Posible nuevo tab "Rutinas" |
| Navigation.kt | `core/Navigation/Navigation.kt` | Conectar nuevas rutas |
| **NUEVO:** features/routines/ | — | Feature completa: domain + data + presentation |

### Shared dependencies
- **Hilt:** `@HiltViewModel`, `@Module`, `@Singleton` — patrón establecido
- **Room:** `AppDatabase` — agregar DAOs + Entities + migración v4→v5
- **Navigation type-safe:** `@Serializable` routes, `NavHost`, `composable<T>`
- **Compose BOM 2026.01.00:** Scaffold, LazyColumn, BottomSheet, etc.
- **Theme:** `ui/theme/` — colores NavyBlue, ElectricBlue, SurfaceDark, TextPrimary, etc. ya definidos

### Data flow
```
Catálogo de ejercicios (hardcoded/bundled)
    → GetExercisesByMuscleGroupUseCase
    → ExerciseListViewModel
    → ExerciseListScreen

Usuario selecciona ejercicios → CreateRoutineUseCase
    → RoutineRepository → Room (RoutineDao)
    → RoutineDetailViewModel → RoutineDetailScreen

Días asignados → AssignDaysUseCase → Room
    → ScheduleNotificationsUseCase → AlarmManager/WorkManager

AlarmManager dispara → BroadcastReceiver → NotificationManagerCompat → notificación local
```

### Feature flags/config
- Permiso `POST_NOTIFICATIONS` requerido en Android 13+ (API 33+) — actualmente comentado en Manifest.
- Permiso `RECEIVE_BOOT_COMPLETED` para reprogramar alarmas tras reinicio — también comentado.
- WorkManager no está en `libs.versions.toml` — si se elige, hay que añadirlo.

### Potential blast radius
- `AppDatabase` (versión bump v4→v5, migración nueva).
- `AndroidManifest.xml` (nuevos permisos, nuevo BroadcastReceiver).
- `core/navigation/AppRoutes.kt` (nuevas rutas).
- `core/Navigation/Navigation.kt` (nuevos composables).
- `UserHomeScreen.kt` o `UserBottomNavBar.kt` (punto de entrada UI).
- `gradle/libs.versions.toml` y `app/build.gradle.kts` (dependencia WorkManager si se elige).

---

## 4) Root Cause Analysis

*No aplica — es una feature nueva, no un bug fix.*

---

## 5) Research

### Soluciones potenciales

#### 5.1 Catálogo de ejercicios: ¿Hardcoded o Room?

**Opción A — Hardcoded en Kotlin (objeto/lista predefinida)**
- Pros: Sin overhead de Room, sin migración, fácil de mantener en un archivo.
- Contras: No editable por el usuario ni por el admin. No escala si se quiere API futura.
- Recomendación: Adecuada para MVP si los ejercicios son fijos.

**Opción B — Preloaded en Room (primera instalación)**
- Pros: Uniforme con el resto del data layer, permite edición futura, búsqueda por SQL.
- Contras: Más complejidad (prepopulate o seed en `onCreate`/`onOpen`).
- Recomendación: Mejor si se planea editar o ampliar el catálogo.

#### 5.2 Notificaciones locales: AlarmManager vs WorkManager

**Opción A — AlarmManager + BroadcastReceiver**
- Pros: Nativo Android, sin dependencia extra, control exacto de hora, funciona con minSdk 24.
- Contras: Verboso (necesita BroadcastReceiver, PendingIntent, Manifest registro). Alarmas exactas requieren permiso `SCHEDULE_EXACT_ALARM` en Android 12+ (`USE_EXACT_ALARM` en 13+).
- Ideal para: recordatorios en hora específica del día (ej: 8:00 AM).

**Opción B — WorkManager**
- Pros: API moderna, maneja restricciones (batería, red), mejor para tareas diferibles. Sobrevive reinicios sin BroadcastReceiver extra. Bien integrado con Hilt (`HiltWorker`).
- Contras: No garantiza ejecución exacta a la hora precisa (puede retrasar minutos). Necesita añadir dependencia (`work-runtime-ktx`).
- Ideal para: recordatorios que no necesitan hora exacta (ej: "en algún momento del día asignado").

**Recomendación:** WorkManager si la hora de notificación es aproximada ("en la mañana del día asignado"). AlarmManager si el usuario elige la hora exacta. Para MVP, **WorkManager** es más simple de integrar con Hilt y no necesita BroadcastReceiver manual.

#### 5.3 Estructura de datos de Rutina

**Opción A — Simple (sin sets/reps)**
```
Routine(id, name, userId, days: List<Int>)
RoutineExercise(routineId, exerciseId, order)
Exercise(id, name, muscleGroup, description)
```
Pros: Fácil de implementar, suficiente para MVP.

**Opción B — Detallada (con sets/reps)**
```
Routine(id, name, userId, days: List<Int>)
RoutineExercise(routineId, exerciseId, sets, reps, restSeconds, order)
Exercise(id, name, muscleGroup, description, imageRes)
```
Pros: Más valor para el usuario, útil para tracking futuro.
Contras: UI más compleja (formulario por ejercicio).

**Recomendación:** Opción B desde el inicio, ya que agregar `sets/reps` después requiere migración adicional. Con valores por defecto (3 sets, 10 reps) el UX no se complica.

#### 5.4 Almacenamiento de días de la semana

**Opción A — Campo `days` como String JSON en Routine** (`"[1,3,5]"`)
- Pros: Tabla simple, sin join.
- Contras: No se puede hacer query por día, TypeConverter necesario.

**Opción B — Tabla `routine_days(routineId, dayOfWeek)`**
- Pros: Queryable por día (útil para saber qué rutina programar hoy), normalizado.
- Contras: Un join extra.

**Recomendación:** Opción B — necesitaremos consultar "¿qué rutinas corresponden a hoy?" para las notificaciones.

#### 5.5 Punto de entrada en la UI

**Opción A — Nuevo tab en UserBottomNavBar** ("Rutinas", 4to tab)
- Pros: Acceso prominente, al mismo nivel que Home y Planes.
- Contras: Requiere rediseño del UserBottomNavBar (actualmente 3 tabs).

**Opción B — Acceso desde "Acciones Rápidas" de UserHomeScreen**
- Pros: No rompe layout existente, más fácil de implementar.
- Contras: Menos visible, enterrada en scroll.

**Recomendación:** Opción A (nuevo tab) para mayor visibilidad. El UserBottomNavBar ya tiene espacio para un 4to ítem.

---

## 6) Clarification

Las siguientes decisiones deben ser confirmadas antes de diseñar la spec:

1. ~~**¿El catálogo de ejercicios es fijo (bundled) o editable?**~~ (RESOLVED)
   **Answer:** Editable por el admin — el admin puede agregar/editar ejercicios desde el panel admin. El catálogo se almacena en Room y se sincroniza con el servidor.

2. ~~**¿Se guardan sets y repeticiones por ejercicio en la rutina?**~~ (RESOLVED)
   **Answer:** Sí — se incluyen sets y repeticiones por ejercicio en la rutina.

3. ~~**¿Las rutinas se sincronizan al servidor o son locales en el dispositivo?**~~ (RESOLVED)
   **Answer:** Ambas — las rutinas se guardan localmente en Room y también se sincronizan con el servidor.

4. ~~**¿La notificación se envía a una hora fija o el usuario elige la hora?**~~ (RESOLVED)
   **Answer:** El usuario elige la hora de la notificación.

5. ~~**¿Qué pasa si el usuario no tiene rutina asignada para un día?**~~ (RESOLVED)
   **Answer:** Se envían alertas con recomendaciones (mensaje motivacional/sugerencia genérica).

6. ~~**¿Punto de entrada en la UI: nuevo tab en la barra inferior o desde "Acciones Rápidas"?**~~ (RESOLVED)
   **Answer:** Nuevo tab "Rutinas" en el bottom nav del miembro.

7. ~~**¿El miembro puede tener múltiples rutinas o solo una activa?**~~ (RESOLVED)
   **Answer:** Múltiples rutinas activas simultáneamente.

8. ~~**¿Se registra que el usuario completó una rutina (historial)?**~~ (RESOLVED)
   **Answer:** Sí — se registra historial de rutinas completadas.

9. ~~**Grupos musculares: ¿cuáles son los que deben incluirse?**~~ (RESOLVED)
   **Answer:** Sí, se incluyen grupos musculares. Usar la sugerencia base: Pecho, Espalda, Hombros, Bíceps, Tríceps, Piernas, Glúteos, Core, Cardio.

10. ~~**¿Cuántos ejercicios aproximadamente debe tener el catálogo inicial?**~~ (RESOLVED)
    **Answer:** Mínimo 4 ejercicios por grupo muscular para el MVP.

---

### Follow-up Clarifications (generadas por las respuestas anteriores)

11. ~~**¿Existe ya un endpoint en el servidor para gestionar ejercicios y rutinas?**~~ (RESOLVED)
    **Answer:** No existen endpoints en el backend. Para ejercicios, se podría consumir un servicio externo si está disponible. El admin no editará ejercicios.

12. ~~**¿Pantalla de admin para ejercicios: misma feature o feature separada?**~~ (RESOLVED)
    **Answer:** Fuera de scope — el admin no gestionará ejercicios.

13. ~~**Estrategia de sync (offline-first vs server-first):**~~ (RESOLVED)
    **Answer:** Offline-first — los datos locales tienen prioridad.

14. ~~**Hora de notificación: ¿una global o por rutina?**~~ (RESOLVED)
    **Answer:** Por rutina — cada rutina tiene su propia hora de notificación configurada por el usuario.

15. ~~**¿Cómo marca el usuario que completó una rutina?**~~ (RESOLVED)
    **Answer:** Botón "Terminar rutina" en la pantalla de detalle. El historial registra fecha + nombre de rutina.

---

### Follow-up Clarifications — Ronda 2

16. ~~**¿Fuente del catálogo de ejercicios: bundled o API externa?**~~ (RESOLVED)
    **Answer:** API externa — la app siempre funciona con red. Se usará wger.de (gratuita, sin API key) con caché local en Room.

17. ~~**¿La sincronización de rutinas al servidor queda fuera del MVP?**~~ (RESOLVED)
    **Answer:** Confirmado — las rutinas son solo locales (Room) en el MVP. La sync al servidor queda para una futura actualización cuando existan los endpoints en el backend GymSync.