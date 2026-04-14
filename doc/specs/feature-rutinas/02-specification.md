# Especificación Técnica — Sistema de Rutinas de Ejercicio

**Slug:** `feature-rutinas`
**Author:** Claude Code
**Date:** 2026-04-12
**Status:** Aprobado para implementación
**Brainstorm:** `doc/specs/feature-rutinas/01-brainstorm.md`

---

## Resumen ejecutivo

Agregar a GYMSYNC un sistema de rutinas de ejercicio completo para el rol miembro. Incluye catálogo de ejercicios consumido desde la API pública **wger.de** (con caché local en Room), creación y gestión de múltiples rutinas personales (ejercicios + sets/reps + días de la semana + hora de notificación), historial de rutinas completadas, y notificaciones locales exactas por AlarmManager. Las rutinas son locales (Room) — la sincronización al servidor queda diferida a una fase futura. La feature es accesible desde un nuevo 4.º tab "Rutinas" en `UserBottomNavBar`.

---

## Decisiones de diseño incorporadas

| # | Decisión |
|---|----------|
| Catálogo de ejercicios | API externa **wger.de** (pública, sin API key) + caché Room con TTL 7 días |
| Sets y repeticiones | Incluidos en `RoutineExerciseEntity` (defaults: 3 sets / 10 reps / 60 s rest) |
| Almacenamiento rutinas | Solo local (Room), offline-first. Sync al servidor = fase futura |
| Notificaciones | `AlarmManager` alarmas exactas, una por cada (rutina × día asignado) |
| Hora de notificación | Por rutina, el usuario elige hora:minuto |
| Días sin rutina | Notificación motivacional desde lista de frases predefinidas |
| Punto de entrada UI | Nuevo tab índice 3 "Rutinas" en `UserBottomNavBar` |
| Múltiples rutinas | Sí, ilimitadas por usuario |
| Historial | Tabla `routine_history`, botón "Terminar rutina" en `RoutineDetailScreen` |
| Grupos musculares | Pecho, Espalda, Hombros, Bíceps, Tríceps, Piernas, Glúteos, Core, Cardio |
| Ejercicios mínimos | ≥ 4 por grupo muscular en el MVP (proporcionados por wger.de) |
| Admin ejercicios | Fuera de scope — admin no gestiona ejercicios |

---

## Principios de implementación

- **Nunca romper compilación entre fases** — cada fase deja la app funcional.
- **Patrón establecido**: Clean Architecture + MVVM + `@HiltViewModel` + `@Module`.
- **Offline-first**: Room es la fuente de verdad; la red solo actualiza el caché de ejercicios.
- **AlarmManager exacto**: Requiere `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`. El usuario acepta el permiso en tiempo de ejecución (Android 12+).
- **Un NavGraph** anidado para la sección Rutinas, conectado al `NavHost` principal.

---

## Fase 0 — Preparación: Gradle, Manifest y Room v5

### Objetivo
Habilitar los permisos de notificación, declarar los nuevos receivers, y migrar la DB a la versión 5 con las 5 nuevas tablas.

### 0.1 — `gradle/libs.versions.toml`

No se necesitan dependencias nuevas (Retrofit y OkHttp ya están en el proyecto). Verificar que `room-ktx` esté presente (ya lo está: `2.7.0`).

```toml
# Verificar que existan (no agregar si ya están):
# room = "2.7.0"
# retrofit = <version existente>
# okhttp = <version existente>
```

### 0.2 — `AndroidManifest.xml`

**Descomentar / agregar permisos:**
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<!-- Android 13+ (API 33+): usar en su lugar: -->
<!-- <uses-permission android:name="android.permission.USE_EXACT_ALARM" /> -->
<uses-permission android:name="android.permission.INTERNET" />
```

**Declarar BroadcastReceivers dentro de `<application>`:**
```xml
<receiver
    android:name=".features.routines.notifications.RoutineNotificationReceiver"
    android:exported="false" />

<receiver
    android:name=".features.routines.notifications.BootCompletedReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

### 0.3 — Esquema Room v5: nuevas entidades

**`ExerciseEntity`** — tabla `exercises` (caché de wger.de)
```
id          INTEGER  PK AUTOINCREMENT
wger_id     INTEGER  UNIQUE NOT NULL
name        TEXT     NOT NULL
muscle_group TEXT    NOT NULL   -- valor del enum MuscleGroup
description TEXT     DEFAULT ''
cached_at   INTEGER  NOT NULL   -- epoch ms
```

**`RoutineEntity`** — tabla `routines`
```
id                  INTEGER  PK AUTOINCREMENT
user_id             INTEGER  NOT NULL   -- FK → users.id
name                TEXT     NOT NULL
notification_hour   INTEGER  NOT NULL  DEFAULT 8
notification_minute INTEGER  NOT NULL  DEFAULT 0
created_at          INTEGER  NOT NULL
```

**`RoutineDayEntity`** — tabla `routine_days`
```
routine_id  INTEGER  NOT NULL   -- FK → routines.id
day_of_week INTEGER  NOT NULL   -- 1=Lun … 7=Dom (ISO 8601)
PRIMARY KEY (routine_id, day_of_week)
```

**`RoutineExerciseEntity`** — tabla `routine_exercises`
```
id          INTEGER  PK AUTOINCREMENT
routine_id  INTEGER  NOT NULL   -- FK → routines.id
exercise_id INTEGER  NOT NULL   -- FK → exercises.id
sets        INTEGER  NOT NULL  DEFAULT 3
reps        INTEGER  NOT NULL  DEFAULT 10
rest_secs   INTEGER  NOT NULL  DEFAULT 60
sort_order  INTEGER  NOT NULL  DEFAULT 0
```

**`RoutineHistoryEntity`** — tabla `routine_history`
```
id            INTEGER  PK AUTOINCREMENT
routine_id    INTEGER  NOT NULL
routine_name  TEXT     NOT NULL   -- desnormalizado (nombre al momento de completar)
user_id       INTEGER  NOT NULL
completed_at  INTEGER  NOT NULL   -- epoch ms
```

### 0.4 — `AppDatabase.kt`

- Cambiar `version = 4` → `version = 5`.
- Registrar las 5 nuevas entidades en `entities = [...]`.
- Agregar migración `MIGRATION_4_5` (CREATE TABLE para las 5 tablas).

### Acceptance criteria Fase 0
- [ ] La app compila y arranca sin crash.
- [ ] `adb shell dumpsys package <pkg>` muestra los permisos declarados.
- [ ] `AppDatabase.instance` se crea con versión 5 sin `fallbackToDestructiveMigration`.

---

## Fase 1 — Remote Data Source: wger.de

### Objetivo
Integrar la API pública wger.de para obtener ejercicios en español con su grupo muscular.

### 1.1 — DTOs (paquete `features/routines/data/remote/dto/`)

```
WgerPaginatedResponse<T>(count, next, results: List<T>)

WgerExerciseInfoDto(
    id: Int,
    category: WgerCategoryDto,         -- { id, name }
    muscles: List<WgerMuscleDto>,       -- [{ id, name_en }]
    translations: List<WgerTranslationDto>  -- [{ language, name, description }]
)
```

### 1.2 — Mapping de categorías wger → `MuscleGroup`

| wger category ID | `MuscleGroup` |
|-----------------|---------------|
| 8 (Arms)        | BICEPS / TRICEPS (por músculo principal en `muscles`) |
| 9 (Legs)        | PIERNAS |
| 10 (Abs)        | CORE |
| 11 (Chest)      | PECHO |
| 12 (Back)       | ESPALDA |
| 13 (Shoulders)  | HOMBROS |
| 14 (Calves)     | PIERNAS |
| *otros*         | CORE (fallback) |
| Glúteos         | GLUTEOS (wger muscle ID 8 = Glutes) |
| Cardio          | CARDIO (ejercicios de categoría Cardio si existen, o ejercicios curados manualmente) |

> **Nota:** wger.de no tiene categoría "Cardio" separada. Para CARDIO se incluirán ≥ 4 ejercicios curados manualmente como lista bundled en el código (no desde API) y se precargan en Room en la migración v4→v5.

### 1.3 — `WgerApiService` (Retrofit interface)

Endpoint:
```
GET https://wger.de/api/v2/exerciseinfo/
  ?format=json
  &language=4        (español)
  &limit=100
  &offset={offset}
```

### 1.4 — `WgerNetworkModule` (Hilt `@Module`)

- Instancia separada de Retrofit para `wger.de` (base URL distinta a GymSync API).
- Sin interceptores de autenticación (API pública).
- `@Named("wger")` para distinguirlo del cliente GymSync.

### 1.5 — `ExerciseRemoteDataSource`

- Función `suspend fetchAll(): List<WgerExerciseInfoDto>` — pagina automáticamente hasta `next == null`.
- Filtra ejercicios sin traducción en español (language ID 4); si no hay traducción ES usa inglés como fallback.

### Acceptance criteria Fase 1
- [ ] `WgerApiService` devuelve ejercicios reales de wger.de en un test manual.
- [ ] Mapping a `MuscleGroup` cubre los 9 grupos (incluye CARDIO con datos bundled).

---

## Fase 2 — Domain Layer

### Objetivo
Definir entidades de dominio, interfaces de repositorio y casos de uso.

### 2.1 — Entidades de dominio (`features/routines/domain/entities/`)

```kotlin
enum class MuscleGroup {
    PECHO, ESPALDA, HOMBROS, BICEPS, TRICEPS,
    PIERNAS, GLUTEOS, CORE, CARDIO
}

data class Exercise(
    val id: Int,
    val name: String,
    val muscleGroup: MuscleGroup,
    val description: String
)

data class RoutineExercise(
    val id: Int,
    val exercise: Exercise,
    val sets: Int,
    val reps: Int,
    val restSeconds: Int,
    val sortOrder: Int
)

data class Routine(
    val id: Int,
    val userId: Int,
    val name: String,
    val days: List<Int>,               // 1=Lun … 7=Dom
    val notificationHour: Int,
    val notificationMinute: Int,
    val exercises: List<RoutineExercise>,
    val createdAt: Long
)

data class RoutineHistory(
    val id: Int,
    val routineId: Int,
    val routineName: String,
    val userId: Int,
    val completedAt: Long
)
```

### 2.2 — Interfaces de repositorio (`features/routines/domain/repositories/`)

```kotlin
interface ExerciseRepository {
    fun getExercisesByMuscleGroup(group: MuscleGroup): Flow<List<Exercise>>
    suspend fun refreshExercises()          // fetch wger + store in Room
    suspend fun isCacheStale(): Boolean     // cached_at > 7 days
}

interface RoutineRepository {
    fun getRoutinesForUser(userId: Int): Flow<List<Routine>>
    fun getRoutineById(routineId: Int): Flow<Routine?>
    fun getRoutinesForDay(userId: Int, dayOfWeek: Int): Flow<List<Routine>>
    suspend fun createRoutine(routine: Routine): Int     // returns generated id
    suspend fun updateRoutine(routine: Routine)
    suspend fun deleteRoutine(routineId: Int)
    suspend fun addExerciseToRoutine(routineId: Int, exerciseId: Int, sets: Int, reps: Int, restSeconds: Int)
    suspend fun removeExerciseFromRoutine(routineExerciseId: Int)
    suspend fun updateRoutineExercise(routineExerciseId: Int, sets: Int, reps: Int, restSeconds: Int)
}

interface RoutineHistoryRepository {
    fun getHistoryForUser(userId: Int): Flow<List<RoutineHistory>>
    suspend fun recordCompletion(routineId: Int, routineName: String, userId: Int)
}
```

### 2.3 — Casos de uso (`features/routines/domain/usecases/`)

| Caso de uso | Descripción |
|-------------|-------------|
| `RefreshExercisesUseCase` | Llama `ExerciseRepository.refreshExercises()` si caché es stale |
| `GetExercisesByMuscleGroupUseCase` | `Flow<List<Exercise>>` filtrado por grupo |
| `CreateRoutineUseCase` | Crea rutina + programa alarmas |
| `GetUserRoutinesUseCase` | `Flow<List<Routine>>` del usuario actual |
| `GetRoutineDetailUseCase` | `Flow<Routine?>` con ejercicios |
| `AddExerciseToRoutineUseCase` | Agrega ejercicio a rutina existente |
| `RemoveExerciseFromRoutineUseCase` | Elimina ejercicio de rutina |
| `UpdateRoutineExerciseUseCase` | Actualiza sets/reps/rest |
| `DeleteRoutineUseCase` | Elimina rutina + cancela alarmas asociadas |
| `CompleteRoutineUseCase` | Registra en historial |
| `GetRoutineHistoryUseCase` | `Flow<List<RoutineHistory>>` del usuario |
| `GetRoutinesForDayUseCase` | Usado por `BootCompletedReceiver` para reprogramar |
| `ScheduleRoutineAlarmsUseCase` | Programa AlarmManager para todos los días de la rutina |
| `CancelRoutineAlarmsUseCase` | Cancela alarmas de una rutina |

### Acceptance criteria Fase 2
- [ ] No hay dependencias de Android en entidades de dominio ni en casos de uso.
- [ ] Las interfaces de repositorio compilan sin implementación concreta.

---

## Fase 3 — Implementación de repositorios y módulo Hilt

### Objetivo
Conectar DAOs + Remote con los repositorios de dominio, y registrar todo en Hilt.

### 3.1 — DAOs (`features/routines/data/local/dao/`)

- `ExerciseDao`: `upsertAll`, `getByMuscleGroup(): Flow`, `getAll`, `deleteAll`, `getLatestCachedAt`
- `RoutineDao`: `insert`, `update`, `delete`, `getByUserId(): Flow`, `getById(): Flow`, `getByDayOfWeek(): Flow`
- `RoutineDayDao`: `insertAll`, `deleteByRoutineId`
- `RoutineExerciseDao`: `insert`, `delete`, `update`, `getByRoutineId(): Flow`
- `RoutineHistoryDao`: `insert`, `getByUserId(): Flow`

### 3.2 — Implementaciones de repositorio (`features/routines/data/repositories/`)

- `ExerciseRepositoryImpl`: caché TTL 7 días (compara `cachedAt` vs `System.currentTimeMillis()`).
- `RoutineRepositoryImpl`: Room como única fuente de verdad. `getRoutineById` retorna `Routine` compuesto con `JOIN` via `RoutineWithDetails` Room relation.
- `RoutineHistoryRepositoryImpl`: solo escritura y lectura de `RoutineHistoryDao`.

### 3.3 — `RoutinesHiltModule` (`features/routines/di/RoutinesHiltModule.kt`)

```
@Module @InstallIn(SingletonComponent)
- @Binds ExerciseRepository → ExerciseRepositoryImpl
- @Binds RoutineRepository → RoutineRepositoryImpl
- @Binds RoutineHistoryRepository → RoutineHistoryRepositoryImpl
- @Provides WgerApiService (via @Named("wger") Retrofit)
```

### Acceptance criteria Fase 3
- [ ] Hilt genera sin error los bindings.
- [ ] `getRoutinesForUser` devuelve lista vacía (no crash) cuando no hay datos.

---

## Fase 4 — Notificaciones locales

### Objetivo
Notificaciones exactas por AlarmManager, reprogramación tras reinicio, y mensajes motivacionales para días sin rutina.

### 4.1 — Canal de notificaciones

Crear canal `CHANNEL_ID = "routines_channel"` con importancia `IMPORTANCE_DEFAULT` en `GymSyncApplication.onCreate()` (o en un `NotificationHelper.createChannels(context)`).

### 4.2 — `RoutineAlarmScheduler` (`features/routines/notifications/`)

- Método `schedule(routine: Routine)`: por cada `dayOfWeek` en `routine.days`, programa alarma repetida semanal via `AlarmManager.setExactAndAllowWhileIdle`.
- Alarm ID: `routine.id * 10 + dayOfWeek` (evita colisiones; asume `routineId < 1_000_000`).
- `PendingIntent` apunta a `RoutineNotificationReceiver` con extras: `routineId`, `routineName`.
- Método `cancel(routineId: Int, days: List<Int>)`: cancela todas las alarmas de la rutina.

### 4.3 — `RoutineNotificationReceiver : BroadcastReceiver`

- Recibe el intent de alarma.
- Si `routineName` presente en extras: muestra notificación "Tienes una rutina hoy: [nombre]".
- Si no hay extras (notificación motivacional): muestra un mensaje aleatorio de la lista predefinida.

**Lista de mensajes motivacionales (≥ 5 frases, hardcoded):**
```
"¡Hoy es un buen día para moverse! 💪"
"Recuerda: el mejor entrenamiento es el que haces."
"Un día de descanso activo también cuenta."
"¿Ya tomaste suficiente agua hoy?"
"Pequeños pasos, grandes resultados."
```

### 4.4 — `BootCompletedReceiver : BroadcastReceiver`

- Acción: `BOOT_COMPLETED`.
- Obtiene todas las rutinas del usuario activo (via `GetRoutinesForDayUseCase` o cargando todas desde DAO directamente).
- Reprograma todas las alarmas llamando a `RoutineAlarmScheduler.schedule(routine)` por cada rutina.
- Usa `WorkManager` de un solo disparo diferido 10 s para hacer el trabajo en background (el BroadcastReceiver no puede hacer operaciones largas).

> **Excepción a la regla AlarmManager**: solo en `BootCompletedReceiver` se usa un `OneTimeWorkRequest` de WorkManager para el trabajo de re-programación. WorkManager necesita ser añadido como dependencia.

### 4.5 — Dependencia WorkManager (solo para BootReceiver)

**`gradle/libs.versions.toml`:**
```toml
[versions]
workManager = "2.9.1"

[libraries]
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workManager" }
```

**`app/build.gradle.kts`:**
```kotlin
implementation(libs.androidx.work.runtime.ktx)
```

### 4.6 — Permiso runtime (Android 12+)

En `CreateRoutineScreen` / `RoutinesScreen`, al intentar crear la primera rutina o cuando `canScheduleExactAlarms()` es `false`, mostrar diálogo que lleva a `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM`.

### Acceptance criteria Fase 4
- [ ] Crear rutina para "Lunes 08:00" programa una alarma visible en `adb shell dumpsys alarm`.
- [ ] Eliminar rutina cancela sus alarmas.
- [ ] Tras reinicio del dispositivo las alarmas se reprograman.
- [ ] En un día sin rutinas asignadas llega notificación motivacional.

---

## Fase 5 — ViewModels

### Objetivo
Implementar los 5 ViewModels de la sección Rutinas con Hilt.

### `RoutinesViewModel` — lista principal

**State:** `RoutinesUiState(routines: List<Routine>, isLoading: Boolean, error: String?)`

**Actions:** `loadRoutines(userId)`, `deleteRoutine(routineId)`

### `CreateRoutineViewModel`

**State:** `CreateRoutineUiState(name, selectedDays: Set<Int>, notificationHour, notificationMinute, isSaving, savedRoutineId: Int?)`

**Actions:** `setName(String)`, `toggleDay(Int)`, `setNotificationTime(hour, minute)`, `save(userId)`

- `save()` llama a `CreateRoutineUseCase`, que también llama a `ScheduleRoutineAlarmsUseCase`.
- Cuando `savedRoutineId != null` la UI navega a `ExercisePicker(routineId)`.

### `RoutineDetailViewModel`

**State:** `RoutineDetailUiState(routine: Routine?, isLoading, isCompleting, completedSuccessfully, error)`

**Actions:** `loadRoutine(routineId)`, `removeExercise(routineExerciseId)`, `updateExercise(id, sets, reps, rest)`, `completeRoutine(userId)`

### `ExercisePickerViewModel`

**State:** `ExercisePickerUiState(selectedGroup: MuscleGroup, exercises: List<Exercise>, selectedIds: Set<Int>, isRefreshing, error)`

**Actions:** `selectGroup(MuscleGroup)`, `toggleExercise(exerciseId)`, `confirmSelection(routineId, defaultSets, defaultReps)`, `refreshExercises()`

- `confirmSelection` llama `AddExerciseToRoutineUseCase` para cada ejercicio seleccionado.

### `RoutineHistoryViewModel`

**State:** `RoutineHistoryUiState(history: List<RoutineHistory>, isLoading)`

**Actions:** `loadHistory(userId)`

### Acceptance criteria Fase 5
- [ ] `@HiltViewModel` genera sin error en todos los ViewModels.
- [ ] `RoutinesViewModel` carga y expone lista vacía sin crash.

---

## Fase 6 — Pantallas Compose

### Objetivo
Implementar las 5 pantallas de la sección Rutinas.

### `RoutinesScreen`

- **Ruta:** `Routines` (raíz del tab)
- Scaffold con `TopAppBar` ("Mis Rutinas") + acción "Historial" (icono History)
- `LazyColumn` de `RoutineCard`:
  - Nombre de la rutina
  - Días asignados (chips: Lun / Mar / ...)
  - Hora de notificación
  - N ejercicios
  - Tap → navega a `RoutineDetail(routineId)`
  - Swipe-to-delete con confirmación
- FAB `+` → navega a `CreateRoutine`
- Estado vacío: ilustración + texto "Crea tu primera rutina"

### `CreateRoutineScreen`

- **Ruta:** `CreateRoutine`
- `OutlinedTextField` para nombre
- `FlowRow` de chips día (Lun–Dom), toggle selección
- `TimePickerDialog` para hora de notificación (muestra la hora seleccionada como texto)
- Botón "Continuar" (habilitado si nombre no vacío y ≥ 1 día seleccionado)
  → llama `viewModel.save(userId)` → al completar navega a `ExercisePicker(routineId)`

### `ExercisePickerScreen`

- **Ruta:** `ExercisePicker(routineId: Int)`
- `ScrollableTabRow` con los 9 grupos musculares
- `LazyColumn` de `ExercisePickerItem`:
  - Nombre + descripción
  - `Checkbox` selección
  - Al seleccionar, expande configuración inline: campos sets / reps / rest (tipo `OutlinedTextField` numérico)
- Banner de error si caché vacía y sin red
- Pull-to-refresh llama `viewModel.refreshExercises()`
- Botón flotante "Agregar seleccionados (N)" → `viewModel.confirmSelection(routineId, ...)` → navega Back

### `RoutineDetailScreen`

- **Ruta:** `RoutineDetail(routineId: Int)`
- Header: nombre, días, hora notificación (editable via BottomSheet)
- `LazyColumn` de `RoutineExerciseItem`:
  - Nombre ejercicio + grupo muscular
  - Sets × Reps, descanso
  - Swipe-to-delete
  - Tap → abre BottomSheet para editar sets/reps/rest
- Botón "Añadir ejercicios" → navega a `ExercisePicker(routineId)`
- FAB "✓ Terminar rutina" → muestra `AlertDialog` de confirmación → `viewModel.completeRoutine(userId)` → snackbar "¡Rutina completada!" → pop back

### `RoutineHistoryScreen`

- **Ruta:** `RoutineHistory`
- `LazyColumn` ordenada por `completedAt DESC`
- Cada ítem: fecha formateada (dd MMM yyyy HH:mm) + nombre de rutina
- Estado vacío: "Aún no has completado ninguna rutina"

### Acceptance criteria Fase 6
- [ ] Todas las pantallas renderizan sin crash con datos de prueba.
- [ ] El flujo completo Crear → Agregar ejercicios → Completar funciona de punta a punta.
- [ ] Swipe-to-delete en `RoutinesScreen` y `RoutineDetailScreen` funciona con confirmación.

---

## Fase 7 — Navegación e integración del tab

### Objetivo
Conectar todas las rutas al NavHost principal y agregar el 4.º tab.

### 7.1 — Nuevas rutas en `AppRoutes.kt`

```kotlin
@Serializable object Routines
@Serializable object CreateRoutine
@Serializable data class RoutineDetail(val routineId: Int)
@Serializable data class ExercisePicker(val routineId: Int)
@Serializable object RoutineHistory
```

### 7.2 — NavGraph de rutinas

Crear `features/routines/navigation/RoutinesNavGraph.kt`:

```kotlin
fun NavGraphBuilder.routinesNavGraph(navController: NavController, userId: Int) {
    composable<Routines> { RoutinesScreen(userId, ...) }
    composable<CreateRoutine> { CreateRoutineScreen(userId, ...) }
    composable<RoutineDetail> { backStackEntry ->
        val route: RoutineDetail = backStackEntry.toRoute()
        RoutineDetailScreen(route.routineId, userId, ...)
    }
    composable<ExercisePicker> { backStackEntry ->
        val route: ExercisePicker = backStackEntry.toRoute()
        ExercisePickerScreen(route.routineId, ...)
    }
    composable<RoutineHistory> { RoutineHistoryScreen(userId, ...) }
}
```

### 7.3 — `Navigation.kt`

En el `NavHost` del flujo de usuario (`UserHome`), añadir:
```kotlin
routinesNavGraph(navController, clientId)
```

### 7.4 — `UserBottomNavBar.kt`

Agregar 4.º `NavigationBarItem`:
```kotlin
NavigationBarItem(
    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Rutinas") },
    label = { Text("Rutinas") },
    selected = selectedTab == 3,
    onClick = { onTabSelected(3) },
    // mismos colores que los demás items
)
```

### 7.5 — `UserHomeScreen.kt`

Manejar `selectedTab == 3` para mostrar el NavHost de Rutinas (si se usa tab-content switching), o navegar a `Routines` route.

### Acceptance criteria Fase 7
- [ ] El tab "Rutinas" aparece en la barra inferior del miembro.
- [ ] La navegación entre todas las pantallas de rutinas funciona correctamente (incluidos back stack correcto y paso de parámetros).
- [ ] No hay regresión en los otros tabs (Inicio, Planes, Perfil).
- [ ] La app compila sin advertencias de navegación type-safe.

---

## Fuera de scope (MVP)

- Sincronización de rutinas al servidor GymSync (no hay endpoints aún).
- Gestión de ejercicios por el admin.
- Tracking de repeticiones completadas por serie (marcar sets individuales).
- Ejercicios con video o multimedia.
- Rutinas compartidas entre miembros.
- Estadísticas de progreso.

---

## Blast radius total

| Archivo | Cambio |
|---------|--------|
| `core/datastore/AppDatabase.kt` | v4 → v5, 5 nuevas entidades |
| `AndroidManifest.xml` | 3 permisos, 2 receivers |
| `gradle/libs.versions.toml` | WorkManager |
| `app/build.gradle.kts` | WorkManager dependency |
| `core/Navigation/AppRoutes.kt` | 5 nuevas rutas |
| `core/Navigation/Navigation.kt` | `routinesNavGraph(...)` |
| `features/users/presentation/components/UserBottomNavBar.kt` | 4.º tab |
| `features/users/presentation/screens/UserHomeScreen.kt` | Tab index 3 handling |
| **NUEVO** `features/routines/` | Feature completa (≈ 30 archivos nuevos) |
