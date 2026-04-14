# Tasks: Sistema de Rutinas de Ejercicio

**Spec:** doc/specs/feature-rutinas/02-specification.md
**Created:** 2026-04-12 00:00
**Last Updated:** 2026-04-12 00:00
**Last Decompose:** 2026-04-12 00:00

## Summary

| Status | Count |
|--------|-------|
| ⏳ Pending | 4 |
| 🔄 In Progress | 0 |
| ✅ Completed | 26 |
| **Total** | **30** |

---

## Phase 1: Foundation

### Task 1.1: Añadir dependencia WorkManager
**Status:** ✅ completed
**Started:** 2026-04-12 00:01
**Completed:** 2026-04-12 00:02
**Priority:** high
**Depends On:** none

**Description:**
Agregar WorkManager al proyecto. Se usa exclusivamente en `BootCompletedReceiver` para delegar la reprogramación de alarmas a background.

**`gradle/libs.versions.toml`** — añadir:
```toml
[versions]
workManager = "2.9.1"

[libraries]
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workManager" }
```

**`app/build.gradle.kts`** — añadir en dependencies:
```kotlin
implementation(libs.androidx.work.runtime.ktx)
```

**Acceptance Criteria:**
- [ ] El proyecto sincroniza Gradle sin error.
- [ ] `import androidx.work.WorkManager` compila correctamente.

**Files to Modify:**
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`

---

### Task 1.2: Actualizar AndroidManifest — permisos y receivers
**Status:** ✅ completed
**Started:** 2026-04-12 00:01
**Completed:** 2026-04-12 00:02
**Priority:** high
**Depends On:** none

**Description:**
Descomentar permisos de notificación y agregar declaraciones de los nuevos BroadcastReceivers.

**Permisos a agregar/descomentar** en `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.INTERNET" />
```

**Receivers a declarar** dentro de `<application>`:
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

**Acceptance Criteria:**
- [ ] La app compila sin error tras los cambios.
- [ ] `adb shell dumpsys package com.AppexSolutions.gymsync` lista los permisos declarados.

**Files to Modify:**
- `app/src/main/AndroidManifest.xml`

---

### Task 1.3: Crear entidades Room v5
**Status:** ✅ completed
**Started:** 2026-04-12 00:01
**Completed:** 2026-04-12 00:02
**Priority:** high
**Depends On:** none

**Description:**
Crear los 5 nuevos archivos de entidad Room en `features/routines/data/local/entity/`.

**`ExerciseEntity.kt`:**
```kotlin
@Entity(tableName = "exercises",
        indices = [Index(value = ["wger_id"], unique = true)])
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "wger_id") val wgerId: Int,
    val name: String,
    @ColumnInfo(name = "muscle_group") val muscleGroup: String,  // valor de enum MuscleGroup
    val description: String = "",
    @ColumnInfo(name = "cached_at") val cachedAt: Long
)
```

**`RoutineEntity.kt`:**
```kotlin
@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int,
    val name: String,
    @ColumnInfo(name = "notification_hour") val notificationHour: Int = 8,
    @ColumnInfo(name = "notification_minute") val notificationMinute: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
```

**`RoutineDayEntity.kt`:**
```kotlin
@Entity(
    tableName = "routine_days",
    primaryKeys = ["routine_id", "day_of_week"],
    foreignKeys = [ForeignKey(
        entity = RoutineEntity::class,
        parentColumns = ["id"],
        childColumns = ["routine_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class RoutineDayEntity(
    @ColumnInfo(name = "routine_id") val routineId: Int,
    @ColumnInfo(name = "day_of_week") val dayOfWeek: Int  // 1=Lun … 7=Dom
)
```

**`RoutineExerciseEntity.kt`:**
```kotlin
@Entity(
    tableName = "routine_exercises",
    foreignKeys = [
        ForeignKey(entity = RoutineEntity::class,
            parentColumns = ["id"], childColumns = ["routine_id"],
            onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ExerciseEntity::class,
            parentColumns = ["id"], childColumns = ["exercise_id"])
    ]
)
data class RoutineExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "routine_id") val routineId: Int,
    @ColumnInfo(name = "exercise_id") val exerciseId: Int,
    val sets: Int = 3,
    val reps: Int = 10,
    @ColumnInfo(name = "rest_secs") val restSeconds: Int = 60,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0
)
```

**`RoutineHistoryEntity.kt`:**
```kotlin
@Entity(tableName = "routine_history")
data class RoutineHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "routine_id") val routineId: Int,
    @ColumnInfo(name = "routine_name") val routineName: String,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "completed_at") val completedAt: Long
)
```

**Acceptance Criteria:**
- [ ] Los 5 archivos compilan sin error.
- [ ] Las anotaciones Room son correctas (sin imports faltantes).

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/local/entity/ExerciseEntity.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/local/entity/RoutineEntity.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/local/entity/RoutineDayEntity.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/local/entity/RoutineExerciseEntity.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/local/entity/RoutineHistoryEntity.kt`

---

### Task 1.4: Crear DAOs Room
**Status:** ✅ completed
**Started:** 2026-04-12 00:02
**Completed:** 2026-04-12 00:03
**Priority:** high
**Depends On:** Task 1.3

**Description:**
Crear los 5 DAOs en `features/routines/data/local/dao/`.

**`ExerciseDao.kt`:**
```kotlin
@Dao
interface ExerciseDao {
    @Upsert suspend fun upsertAll(exercises: List<ExerciseEntity>)
    @Query("SELECT * FROM exercises WHERE muscle_group = :group")
    fun getByMuscleGroup(group: String): Flow<List<ExerciseEntity>>
    @Query("SELECT * FROM exercises") suspend fun getAll(): List<ExerciseEntity>
    @Query("DELETE FROM exercises") suspend fun deleteAll()
    @Query("SELECT MAX(cached_at) FROM exercises") suspend fun getLatestCachedAt(): Long?
}
```

**`RoutineDao.kt`:**
```kotlin
@Dao
interface RoutineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routine: RoutineEntity): Long
    @Update suspend fun update(routine: RoutineEntity)
    @Query("DELETE FROM routines WHERE id = :id") suspend fun deleteById(id: Int)
    @Query("SELECT * FROM routines WHERE user_id = :userId ORDER BY created_at DESC")
    fun getByUserId(userId: Int): Flow<List<RoutineEntity>>
    @Query("SELECT * FROM routines WHERE id = :id")
    fun getById(id: Int): Flow<RoutineEntity?>
}
```

**`RoutineDayDao.kt`:**
```kotlin
@Dao
interface RoutineDayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(days: List<RoutineDayEntity>)
    @Query("DELETE FROM routine_days WHERE routine_id = :routineId")
    suspend fun deleteByRoutineId(routineId: Int)
    @Query("SELECT routine_id FROM routine_days WHERE day_of_week = :day")
    suspend fun getRoutineIdsByDay(day: Int): List<Int>
}
```

**`RoutineExerciseDao.kt`:**
```kotlin
@Dao
interface RoutineExerciseDao {
    @Insert suspend fun insert(re: RoutineExerciseEntity): Long
    @Query("DELETE FROM routine_exercises WHERE id = :id") suspend fun deleteById(id: Int)
    @Update suspend fun update(re: RoutineExerciseEntity)
    @Query("SELECT * FROM routine_exercises WHERE routine_id = :routineId ORDER BY sort_order")
    fun getByRoutineId(routineId: Int): Flow<List<RoutineExerciseEntity>>
}
```

**`RoutineHistoryDao.kt`:**
```kotlin
@Dao
interface RoutineHistoryDao {
    @Insert suspend fun insert(history: RoutineHistoryEntity): Long
    @Query("SELECT * FROM routine_history WHERE user_id = :userId ORDER BY completed_at DESC")
    fun getByUserId(userId: Int): Flow<List<RoutineHistoryEntity>>
}
```

**Acceptance Criteria:**
- [ ] Los 5 DAOs compilan sin error.
- [ ] KSP genera los DAOs correctamente (sin error en `build`).

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/local/dao/ExerciseDao.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/local/dao/RoutineDao.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/local/dao/RoutineDayDao.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/local/dao/RoutineExerciseDao.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/local/dao/RoutineHistoryDao.kt`

---

### Task 1.5: Migración AppDatabase v4 → v5
**Status:** ✅ completed
**Started:** 2026-04-12 00:03
**Completed:** 2026-04-12 00:04
**Priority:** high
**Depends On:** Task 1.3, Task 1.4

**Description:**
Actualizar `AppDatabase.kt` a versión 5 con las 5 nuevas entidades y la migración correspondiente.

**Cambios en `AppDatabase.kt`:**

1. Cambiar `version = 4` → `version = 5`.

2. Registrar entidades nuevas en el array `entities`:
```kotlin
entities = [
    UserEntity::class,
    ClientProfilePhotoEntity::class,
    AttendanceEntity::class,
    // nuevas:
    ExerciseEntity::class,
    RoutineEntity::class,
    RoutineDayEntity::class,
    RoutineExerciseEntity::class,
    RoutineHistoryEntity::class
]
```

3. Agregar migración `MIGRATION_4_5`:
```kotlin
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                wger_id INTEGER NOT NULL UNIQUE,
                name TEXT NOT NULL,
                muscle_group TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                cached_at INTEGER NOT NULL
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS routines (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                user_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                notification_hour INTEGER NOT NULL DEFAULT 8,
                notification_minute INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS routine_days (
                routine_id INTEGER NOT NULL,
                day_of_week INTEGER NOT NULL,
                PRIMARY KEY (routine_id, day_of_week),
                FOREIGN KEY (routine_id) REFERENCES routines(id) ON DELETE CASCADE
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS routine_exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                routine_id INTEGER NOT NULL,
                exercise_id INTEGER NOT NULL,
                sets INTEGER NOT NULL DEFAULT 3,
                reps INTEGER NOT NULL DEFAULT 10,
                rest_secs INTEGER NOT NULL DEFAULT 60,
                sort_order INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY (routine_id) REFERENCES routines(id) ON DELETE CASCADE,
                FOREIGN KEY (exercise_id) REFERENCES exercises(id)
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS routine_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                routine_id INTEGER NOT NULL,
                routine_name TEXT NOT NULL,
                user_id INTEGER NOT NULL,
                completed_at INTEGER NOT NULL
            )
        """)
    }
}
```

4. Registrar la migración en el builder:
```kotlin
.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
```

**Acceptance Criteria:**
- [ ] La app arranca en un dispositivo/emulador con DB v4 pre-existente sin crash.
- [ ] Las 5 nuevas tablas existen tras la migración (`adb shell sqlite3 /data/data/.../databases/gymsync.db ".tables"`).
- [ ] No se usa `fallbackToDestructiveMigration`.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/core/datastore/AppDatabase.kt`

---

## Phase 2: Core Implementation

### Task 2.1: DTOs wger.de y WgerApiService
**Status:** ✅ completed
**Started:** 2026-04-12 00:04
**Completed:** 2026-04-12 00:05
**Priority:** high
**Depends On:** none

**Description:**
Crear los DTOs de respuesta de la API wger.de y la interfaz Retrofit.

**Paquete:** `features/routines/data/remote/dto/`

**`WgerDtos.kt`:**
```kotlin
data class WgerPaginatedResponse<T>(
    val count: Int,
    val next: String?,
    val results: List<T>
)

data class WgerExerciseInfoDto(
    val id: Int,
    val category: WgerCategoryDto,
    val muscles: List<WgerMuscleDto>,
    @SerializedName("muscles_secondary") val musclesSecondary: List<WgerMuscleDto>,
    val translations: List<WgerTranslationDto>
)

data class WgerCategoryDto(val id: Int, val name: String)

data class WgerMuscleDto(val id: Int, @SerializedName("name_en") val nameEn: String)

data class WgerTranslationDto(
    val language: Int,   // 4 = español, 2 = inglés
    val name: String,
    val description: String
)
```

**`WgerApiService.kt`** en `features/routines/data/remote/`:
```kotlin
interface WgerApiService {
    @GET("exerciseinfo/")
    suspend fun getExercises(
        @Query("format") format: String = "json",
        @Query("language") language: Int = 4,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): WgerPaginatedResponse<WgerExerciseInfoDto>
}
```

**Acceptance Criteria:**
- [ ] Los DTOs compilan correctamente con Gson/Moshi.
- [ ] `WgerApiService` tiene la firma correcta con `suspend`.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/remote/dto/WgerDtos.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/remote/WgerApiService.kt`

---

### Task 2.2: Enum MuscleGroup y mapping de categorías wger
**Status:** ✅ completed
**Started:** 2026-04-12 00:04
**Completed:** 2026-04-12 00:05
**Priority:** high
**Depends On:** none

**Description:**
Definir el enum `MuscleGroup` en domain y el mapper de categorías wger → `MuscleGroup`.

**`MuscleGroup.kt`** en `features/routines/domain/entities/`:
```kotlin
enum class MuscleGroup(val displayName: String) {
    PECHO("Pecho"),
    ESPALDA("Espalda"),
    HOMBROS("Hombros"),
    BICEPS("Bíceps"),
    TRICEPS("Tríceps"),
    PIERNAS("Piernas"),
    GLUTEOS("Glúteos"),
    CORE("Core"),
    CARDIO("Cardio")
}
```

**`WgerMuscleGroupMapper.kt`** en `features/routines/data/remote/`:
```kotlin
object WgerMuscleGroupMapper {
    // Mapeo de category.id de wger → MuscleGroup
    private val categoryMap = mapOf(
        8 to null,   // Arms — decidir por músculo primario
        9 to MuscleGroup.PIERNAS,
        10 to MuscleGroup.CORE,
        11 to MuscleGroup.PECHO,
        12 to MuscleGroup.ESPALDA,
        13 to MuscleGroup.HOMBROS,
        14 to MuscleGroup.PIERNAS  // Calves
    )

    // Mapeo de muscle.id → MuscleGroup (para categoría "Arms")
    private val muscleMap = mapOf(
        1 to MuscleGroup.BICEPS,   // Biceps brachii
        8 to MuscleGroup.TRICEPS,  // Triceps brachii
        10 to MuscleGroup.GLUTEOS  // Gluteus maximus
    )

    fun map(dto: WgerExerciseInfoDto): MuscleGroup {
        val fromCategory = categoryMap[dto.category.id]
        if (fromCategory != null) return fromCategory
        // Para Arms (categoryId=8): usar el primer músculo primario
        val primaryMuscleId = dto.muscles.firstOrNull()?.id
        return muscleMap[primaryMuscleId] ?: MuscleGroup.CORE
    }
}
```

**Nota:** Ejercicios de `CARDIO` no vienen de wger — se precargan como datos bundled en la migración (ver Task 2.9).

**Acceptance Criteria:**
- [ ] `MuscleGroup.values()` devuelve exactamente 9 grupos.
- [ ] El mapper cubre los 9 grupos sin lanzar excepciones con datos reales de wger.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/domain/entities/MuscleGroup.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/remote/WgerMuscleGroupMapper.kt`

---

### Task 2.3: WgerNetworkModule (Hilt)
**Status:** ✅ completed
**Started:** 2026-04-12 00:05
**Completed:** 2026-04-12 00:06
**Priority:** high
**Depends On:** Task 2.1

**Description:**
Crear el módulo Hilt que provee la instancia Retrofit dedicada a wger.de.

**`WgerNetworkModule.kt`** en `features/routines/di/`:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object WgerNetworkModule {

    @Provides
    @Singleton
    @Named("wger")
    fun provideWgerRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://wger.de/api/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().build())
        .build()

    @Provides
    @Singleton
    fun provideWgerApiService(@Named("wger") retrofit: Retrofit): WgerApiService =
        retrofit.create(WgerApiService::class.java)
}
```

**Acceptance Criteria:**
- [ ] Hilt genera el módulo sin errores de compilación.
- [ ] `WgerApiService` puede ser inyectado en un repositorio.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/di/WgerNetworkModule.kt`

---

### Task 2.4: ExerciseRemoteDataSource
**Status:** ✅ completed
**Started:** 2026-04-12 00:05
**Completed:** 2026-04-12 00:06
**Priority:** high
**Depends On:** Task 2.1, Task 2.2

**Description:**
Crear el data source remoto que pagina la API wger.de y devuelve todos los ejercicios mapeados.

**`ExerciseRemoteDataSource.kt`** en `features/routines/data/remote/`:
```kotlin
class ExerciseRemoteDataSource @Inject constructor(
    private val api: WgerApiService
) {
    /**
     * Obtiene todos los ejercicios de wger.de paginando hasta que `next == null`.
     * Filtra por idioma español (4); si un ejercicio no tiene traducción ES usa inglés (2).
     */
    suspend fun fetchAll(): List<WgerExerciseInfoDto> {
        val result = mutableListOf<WgerExerciseInfoDto>()
        var offset = 0
        do {
            val page = api.getExercises(offset = offset)
            result.addAll(page.results)
            offset += 100
        } while (page.next != null)
        return result.filter { dto ->
            dto.translations.any { it.language == 4 || it.language == 2 }
        }
    }

    fun getBestTranslation(dto: WgerExerciseInfoDto): WgerTranslationDto? =
        dto.translations.firstOrNull { it.language == 4 }
            ?: dto.translations.firstOrNull { it.language == 2 }
}
```

**Acceptance Criteria:**
- [ ] `fetchAll()` no lanza excepción con datos reales de wger.de.
- [ ] Pagina correctamente (no se detiene en la primera página si hay más).

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/remote/ExerciseRemoteDataSource.kt`

---

### Task 2.5: Entidades de dominio
**Status:** ✅ completed
**Started:** 2026-04-12 00:04
**Completed:** 2026-04-12 00:05
**Priority:** high
**Depends On:** Task 2.2

**Description:**
Crear las 4 data classes de dominio en `features/routines/domain/entities/`. No deben tener imports de Android/Room.

**`Exercise.kt`:**
```kotlin
data class Exercise(
    val id: Int,
    val name: String,
    val muscleGroup: MuscleGroup,
    val description: String
)
```

**`Routine.kt`:**
```kotlin
data class Routine(
    val id: Int,
    val userId: Int,
    val name: String,
    val days: List<Int>,
    val notificationHour: Int,
    val notificationMinute: Int,
    val exercises: List<RoutineExercise>,
    val createdAt: Long
)
```

**`RoutineExercise.kt`:**
```kotlin
data class RoutineExercise(
    val id: Int,
    val exercise: Exercise,
    val sets: Int,
    val reps: Int,
    val restSeconds: Int,
    val sortOrder: Int
)
```

**`RoutineHistory.kt`:**
```kotlin
data class RoutineHistory(
    val id: Int,
    val routineId: Int,
    val routineName: String,
    val userId: Int,
    val completedAt: Long
)
```

**Acceptance Criteria:**
- [ ] Ningún archivo importa clases de `android.*` o `androidx.*`.
- [ ] Compilan sin error.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/domain/entities/Exercise.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/domain/entities/Routine.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/domain/entities/RoutineExercise.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/domain/entities/RoutineHistory.kt`

---

### Task 2.6: Interfaces de repositorio
**Status:** ✅ completed
**Started:** 2026-04-12 00:05
**Completed:** 2026-04-12 00:06
**Priority:** high
**Depends On:** Task 2.5

**Description:**
Crear las 3 interfaces de repositorio en `features/routines/domain/repositories/`.

**`ExerciseRepository.kt`:**
```kotlin
interface ExerciseRepository {
    fun getExercisesByMuscleGroup(group: MuscleGroup): Flow<List<Exercise>>
    suspend fun refreshExercises()
    suspend fun isCacheStale(): Boolean  // true si cached_at > 7 días o caché vacía
}
```

**`RoutineRepository.kt`:**
```kotlin
interface RoutineRepository {
    fun getRoutinesForUser(userId: Int): Flow<List<Routine>>
    fun getRoutineById(routineId: Int): Flow<Routine?>
    fun getRoutinesForDay(userId: Int, dayOfWeek: Int): Flow<List<Routine>>
    suspend fun createRoutine(routine: Routine): Int
    suspend fun updateRoutine(routine: Routine)
    suspend fun deleteRoutine(routineId: Int)
    suspend fun addExerciseToRoutine(routineId: Int, exerciseId: Int, sets: Int, reps: Int, restSeconds: Int)
    suspend fun removeExerciseFromRoutine(routineExerciseId: Int)
    suspend fun updateRoutineExercise(routineExerciseId: Int, sets: Int, reps: Int, restSeconds: Int)
}
```

**`RoutineHistoryRepository.kt`:**
```kotlin
interface RoutineHistoryRepository {
    fun getHistoryForUser(userId: Int): Flow<List<RoutineHistory>>
    suspend fun recordCompletion(routineId: Int, routineName: String, userId: Int)
}
```

**Acceptance Criteria:**
- [ ] Las interfaces compilan sin imports de Room o Android.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/domain/repositories/ExerciseRepository.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/domain/repositories/RoutineRepository.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/domain/repositories/RoutineHistoryRepository.kt`

---

### Task 2.7: Casos de uso
**Status:** ✅ completed
**Started:** 2026-04-12 00:06
**Completed:** 2026-04-12 00:07
**Priority:** high
**Depends On:** Task 2.6

**Description:**
Crear los 14 casos de uso en `features/routines/domain/usecases/`. Cada uno tiene `@Inject constructor` y delega a los repositorios.

Crear un archivo por caso de uso (o agrupar relacionados). Lista completa:

| Clase | Función principal |
|-------|------------------|
| `RefreshExercisesUseCase` | `suspend operator fun invoke()` — llama `repo.refreshExercises()` si `isCacheStale()` |
| `GetExercisesByMuscleGroupUseCase` | `operator fun invoke(group: MuscleGroup): Flow<List<Exercise>>` |
| `CreateRoutineUseCase` | `suspend operator fun invoke(name, userId, days, hour, minute): Int` — crea rutina y programa alarmas |
| `GetUserRoutinesUseCase` | `operator fun invoke(userId): Flow<List<Routine>>` |
| `GetRoutineDetailUseCase` | `operator fun invoke(routineId): Flow<Routine?>` |
| `AddExerciseToRoutineUseCase` | `suspend operator fun invoke(routineId, exerciseId, sets, reps, rest)` |
| `RemoveExerciseFromRoutineUseCase` | `suspend operator fun invoke(routineExerciseId)` |
| `UpdateRoutineExerciseUseCase` | `suspend operator fun invoke(id, sets, reps, rest)` |
| `DeleteRoutineUseCase` | `suspend operator fun invoke(routineId)` — cancela alarmas + borra de Room |
| `CompleteRoutineUseCase` | `suspend operator fun invoke(routineId, routineName, userId)` — registra historial |
| `GetRoutineHistoryUseCase` | `operator fun invoke(userId): Flow<List<RoutineHistory>>` |
| `GetRoutinesForDayUseCase` | `suspend operator fun invoke(userId, dayOfWeek): List<Routine>` |
| `ScheduleRoutineAlarmsUseCase` | `operator fun invoke(routine: Routine)` — delega a `RoutineAlarmScheduler` |
| `CancelRoutineAlarmsUseCase` | `operator fun invoke(routineId, days)` — delega a `RoutineAlarmScheduler` |

**Nota:** `ScheduleRoutineAlarmsUseCase` y `CancelRoutineAlarmsUseCase` reciben `RoutineAlarmScheduler` por inyección. Son los únicos casos de uso que tienen dependencias de Android (Context via Hilt).

**Acceptance Criteria:**
- [ ] Los 14 casos de uso compilan.
- [ ] `CreateRoutineUseCase` llama a `ScheduleRoutineAlarmsUseCase` después de crear la rutina.
- [ ] `DeleteRoutineUseCase` llama a `CancelRoutineAlarmsUseCase` antes de borrar.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/domain/usecases/` (14 archivos)

---

### Task 2.8: Mappers domain ↔ Room
**Status:** ✅ completed
**Started:** 2026-04-12 00:06
**Completed:** 2026-04-12 00:07
**Priority:** high
**Depends On:** Task 1.3, Task 2.5

**Description:**
Crear funciones de extensión para convertir entre entidades Room y entidades de dominio.

**`ExerciseMappers.kt`** en `features/routines/data/local/mapper/`:
```kotlin
fun ExerciseEntity.toDomain() = Exercise(
    id = id, name = name,
    muscleGroup = MuscleGroup.valueOf(muscleGroup),
    description = description
)

fun WgerExerciseInfoDto.toEntity(
    muscleGroup: MuscleGroup,
    translation: WgerTranslationDto
) = ExerciseEntity(
    wgerId = id,
    name = translation.name,
    muscleGroup = muscleGroup.name,
    description = translation.description,
    cachedAt = System.currentTimeMillis()
)
```

**`RoutineMappers.kt`:**
```kotlin
// RoutineEntity + days + exercises → Routine domain
fun RoutineEntity.toDomain(
    days: List<Int>,
    exercises: List<RoutineExercise>
) = Routine(id, userId, name, days, notificationHour, notificationMinute, exercises, createdAt)

// RoutineExerciseEntity + ExerciseEntity → RoutineExercise domain
fun RoutineExerciseEntity.toDomain(exercise: Exercise) =
    RoutineExercise(id, exercise, sets, reps, restSeconds, sortOrder)

// RoutineHistoryEntity → RoutineHistory domain
fun RoutineHistoryEntity.toDomain() =
    RoutineHistory(id, routineId, routineName, userId, completedAt)
```

**Acceptance Criteria:**
- [ ] `MuscleGroup.valueOf(entity.muscleGroup)` no lanza `IllegalArgumentException` con datos reales.
- [ ] Los mappers compilan sin errores.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/local/mapper/ExerciseMappers.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/local/mapper/RoutineMappers.kt`

---

### Task 2.9: Implementaciones de repositorios
**Status:** ✅ completed
**Started:** 2026-04-12 00:07
**Completed:** 2026-04-12 00:09
**Priority:** high
**Depends On:** Task 1.4, Task 2.4, Task 2.6, Task 2.8

**Description:**
Implementar los 3 repositorios de dominio.

**`ExerciseRepositoryImpl.kt`:**
- `isCacheStale()`: consulta `ExerciseDao.getLatestCachedAt()` — retorna `true` si `null` o si `(now - cachedAt) > 7 days`.
- `refreshExercises()`: llama `ExerciseRemoteDataSource.fetchAll()`, mapea con `WgerMuscleGroupMapper`, inserta en Room con `upsertAll`. También inserta los **4 ejercicios CARDIO bundled** con `wgerId = -1, -2, -3, -4` (IDs negativos para distinguirlos).

**Ejercicios CARDIO bundled (hardcoded en `refreshExercises`):**
```kotlin
val cardioExercises = listOf(
    ExerciseEntity(wgerId=-1, name="Carrera en cinta", muscleGroup="CARDIO", description="Cardio básico en cinta a ritmo moderado.", cachedAt=now),
    ExerciseEntity(wgerId=-2, name="Bicicleta estática", muscleGroup="CARDIO", description="Cardio de bajo impacto en bicicleta.", cachedAt=now),
    ExerciseEntity(wgerId=-3, name="Saltos de tijera", muscleGroup="CARDIO", description="Jumping jacks para elevar la frecuencia cardíaca.", cachedAt=now),
    ExerciseEntity(wgerId=-4, name="Cuerda para saltar", muscleGroup="CARDIO", description="Saltar cuerda a ritmo constante durante 10 min.", cachedAt=now)
)
```

**`RoutineRepositoryImpl.kt`:**
- `getRoutinesForUser()` y `getRoutineById()`: combinan `RoutineDao` + `RoutineDayDao` + `RoutineExerciseDao` + `ExerciseDao` en un `combine { }` o carga manual en `map {}` del Flow.
- `createRoutine()`: inserta `RoutineEntity`, luego inserta todos los `RoutineDayEntity`. Retorna el `id` generado.
- `deleteRoutine()`: borra de `routines` (CASCADE borra days y exercises automáticamente).

**`RoutineHistoryRepositoryImpl.kt`:**
- Delegación directa a `RoutineHistoryDao`.

**Acceptance Criteria:**
- [ ] `ExerciseRepositoryImpl.refreshExercises()` hace upsert de ejercicios reales + 4 CARDIO bundled.
- [ ] `RoutineRepositoryImpl.createRoutine()` retorna un ID válido (> 0).
- [ ] `isCacheStale()` devuelve `true` la primera vez (DB vacía).

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/repositories/ExerciseRepositoryImpl.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/repositories/RoutineRepositoryImpl.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/data/repositories/RoutineHistoryRepositoryImpl.kt`

---

### Task 2.10: RoutinesHiltModule
**Status:** ✅ completed
**Started:** 2026-04-12 00:09
**Completed:** 2026-04-12 00:09
**Priority:** high
**Depends On:** Task 2.3, Task 2.9

**Description:**
Crear el módulo Hilt principal de la feature que registra los bindings de repositorios.

**`RoutinesHiltModule.kt`** en `features/routines/di/`:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RoutinesHiltModule {

    @Binds @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    @Binds @Singleton
    abstract fun bindRoutineRepository(impl: RoutineRepositoryImpl): RoutineRepository

    @Binds @Singleton
    abstract fun bindRoutineHistoryRepository(impl: RoutineHistoryRepositoryImpl): RoutineHistoryRepository
}
```

Los DAOs deben proveerse desde `DatabaseModule.kt` (o en este módulo si no están allí):
```kotlin
@Provides fun provideExerciseDao(db: AppDatabase): ExerciseDao = db.exerciseDao()
@Provides fun provideRoutineDao(db: AppDatabase): RoutineDao = db.routineDao()
@Provides fun provideRoutineDayDao(db: AppDatabase): RoutineDayDao = db.routineDayDao()
@Provides fun provideRoutineExerciseDao(db: AppDatabase): RoutineExerciseDao = db.routineExerciseDao()
@Provides fun provideRoutineHistoryDao(db: AppDatabase): RoutineHistoryDao = db.routineHistoryDao()
```

Recordar exponer los DAOs en `AppDatabase.kt`:
```kotlin
abstract fun exerciseDao(): ExerciseDao
abstract fun routineDao(): RoutineDao
abstract fun routineDayDao(): RoutineDayDao
abstract fun routineExerciseDao(): RoutineExerciseDao
abstract fun routineHistoryDao(): RoutineHistoryDao
```

**Acceptance Criteria:**
- [ ] Hilt compila sin `DuplicateBindings` o errores de `@Component`.
- [ ] Un `@HiltViewModel` que inyecte `ExerciseRepository` compila correctamente.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/di/RoutinesHiltModule.kt`

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/core/datastore/AppDatabase.kt` (agregar abstract DAO accessors)
- `app/src/main/java/com/AppexSolutions/gymsync/core/di/DatabaseModule.kt` (agregar @Provides DAOs)

---

### Task 2.11: RoutineAlarmScheduler
**Status:** ✅ completed
**Started:** 2026-04-12 00:07
**Completed:** 2026-04-12 00:09
**Priority:** high
**Depends On:** Task 2.5

**Description:**
Crear el scheduler que interactúa con `AlarmManager` para programar y cancelar alarmas exactas por rutina/día.

**`RoutineAlarmScheduler.kt`** en `features/routines/notifications/`:
```kotlin
@Singleton
class RoutineAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(routine: Routine) {
        routine.days.forEach { dayOfWeek ->
            val alarmId = routine.id * 10 + dayOfWeek
            val triggerAtMillis = nextAlarmMillis(dayOfWeek, routine.notificationHour, routine.notificationMinute)
            val intent = Intent(context, RoutineNotificationReceiver::class.java).apply {
                putExtra("routineId", routine.id)
                putExtra("routineName", routine.name)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, alarmId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
                )
            }
        }
    }

    fun cancel(routineId: Int, days: List<Int>) {
        days.forEach { dayOfWeek ->
            val alarmId = routineId * 10 + dayOfWeek
            val intent = Intent(context, RoutineNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, alarmId, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }

    /**
     * Calcula el epoch ms del próximo [dayOfWeek] a [hour]:[minute].
     * dayOfWeek: 1=Lun, …, 7=Dom (Calendar.MONDAY=2, por lo que se mapea).
     */
    private fun nextAlarmMillis(dayOfWeek: Int, hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            // mapear 1-7 (ISO) a Calendar day-of-week
            val calDay = when (dayOfWeek) {
                1 -> Calendar.MONDAY; 2 -> Calendar.TUESDAY
                3 -> Calendar.WEDNESDAY; 4 -> Calendar.THURSDAY
                5 -> Calendar.FRIDAY; 6 -> Calendar.SATURDAY
                else -> Calendar.SUNDAY
            }
            set(Calendar.DAY_OF_WEEK, calDay)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.WEEK_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }
}
```

**Acceptance Criteria:**
- [ ] `schedule()` no lanza excepción en dispositivo con API 24.
- [ ] `cancel()` no lanza excepción si la alarma no existe.
- [ ] En Android 12+ verifica `canScheduleExactAlarms()` antes de programar.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/notifications/RoutineAlarmScheduler.kt`

---

### Task 2.12: BroadcastReceivers de notificaciones
**Status:** ✅ completed
**Started:** 2026-04-12 00:09
**Completed:** 2026-04-12 00:09
**Priority:** high
**Depends On:** Task 2.11

**Description:**
Implementar `RoutineNotificationReceiver` y `BootCompletedReceiver`.

**`RoutineNotificationReceiver.kt`:**
```kotlin
class RoutineNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "routines_channel"
        private val MOTIVATIONAL = listOf(
            "¡Hoy es un buen día para moverse! 💪",
            "Recuerda: el mejor entrenamiento es el que haces.",
            "Un día de descanso activo también cuenta.",
            "¿Ya tomaste suficiente agua hoy?",
            "Pequeños pasos, grandes resultados."
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        val routineName = intent.getStringExtra("routineName")
        val notificationId = intent.getIntExtra("routineId", 0)

        val (title, body) = if (routineName != null) {
            "¡Hora de entrenar!" to "Tienes una rutina hoy: $routineName"
        } else {
            "GymSync" to MOTIVATIONAL.random()
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)  // usar icono existente del proyecto
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
```

**`BootCompletedReceiver.kt`:**
```kotlin
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        WorkManager.getInstance(context)
            .enqueue(OneTimeWorkRequestBuilder<RescheduleAlarmsWorker>()
                .setInitialDelay(10, TimeUnit.SECONDS)
                .build())
    }
}
```

**`RescheduleAlarmsWorker.kt`** (Worker de Hilt):
```kotlin
@HiltWorker
class RescheduleAlarmsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val routineRepository: RoutineRepository,
    private val scheduler: RoutineAlarmScheduler
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        // Obtiene userId del DataStore/SharedPrefs del usuario activo
        // TODO: inyectar AuthPreferences para obtener userId
        return Result.success()
    }
}
```

**Nota:** La implementación completa de `RescheduleAlarmsWorker` requiere el `userId` del usuario activo. Obtenerlo de `AuthPreferences` (ya existe en `core/datastore/datastore.kt`).

**Acceptance Criteria:**
- [ ] `RoutineNotificationReceiver.onReceive()` muestra notificación correctamente.
- [ ] El canal `routines_channel` existe antes de que llegue la primera notificación.
- [ ] `BootCompletedReceiver` encola el Worker sin crash.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/notifications/RoutineNotificationReceiver.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/notifications/BootCompletedReceiver.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/notifications/RescheduleAlarmsWorker.kt`

---

### Task 2.13: Canal de notificaciones
**Status:** ✅ completed
**Started:** 2026-04-12 00:09
**Completed:** 2026-04-12 00:09
**Priority:** medium
**Depends On:** Task 2.12

**Description:**
Crear el canal de notificaciones `routines_channel` al arrancar la app.

En `GymSyncApplication.onCreate()`:
```kotlin
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
```

**Acceptance Criteria:**
- [ ] El canal aparece en Ajustes → Notificaciones de la app.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/GymSyncApplication.kt`

---

## Phase 3: Presentation

### Task 3.1: ViewModels (5)
**Status:** ✅ completed
**Started:** 2026-04-12 00:10
**Completed:** 2026-04-12 00:11
**Priority:** high
**Depends On:** Task 2.7

**Description:**
Crear los 5 `@HiltViewModel` de la sección Rutinas.

**`RoutinesViewModel.kt`:**
```kotlin
@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val getUserRoutines: GetUserRoutinesUseCase,
    private val deleteRoutine: DeleteRoutineUseCase
) : ViewModel() {
    data class UiState(val routines: List<Routine> = emptyList(), val isLoading: Boolean = true, val error: String? = null)
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadRoutines(userId: Int) {
        viewModelScope.launch {
            getUserRoutines(userId).collect { list ->
                _uiState.update { it.copy(routines = list, isLoading = false) }
            }
        }
    }

    fun deleteRoutine(routineId: Int) { viewModelScope.launch { deleteRoutine(routineId) } }
}
```

**`CreateRoutineViewModel.kt`:**
```kotlin
@HiltViewModel
class CreateRoutineViewModel @Inject constructor(
    private val createRoutine: CreateRoutineUseCase
) : ViewModel() {
    data class UiState(
        val name: String = "",
        val selectedDays: Set<Int> = emptySet(),
        val notificationHour: Int = 8,
        val notificationMinute: Int = 0,
        val isSaving: Boolean = false,
        val savedRoutineId: Int? = null,
        val error: String? = null
    )
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun setName(name: String) { _uiState.update { it.copy(name = name) } }
    fun toggleDay(day: Int) { _uiState.update { s -> s.copy(selectedDays = if (day in s.selectedDays) s.selectedDays - day else s.selectedDays + day) } }
    fun setNotificationTime(hour: Int, minute: Int) { _uiState.update { it.copy(notificationHour = hour, notificationMinute = minute) } }

    fun save(userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val id = createRoutine(_uiState.value.name, userId, _uiState.value.selectedDays.toList(), _uiState.value.notificationHour, _uiState.value.notificationMinute)
            _uiState.update { it.copy(isSaving = false, savedRoutineId = id) }
        }
    }
}
```

**`RoutineDetailViewModel.kt`:**
- State: `(routine: Routine?, isLoading, isCompleting, completedSuccessfully, error)`
- Actions: `loadRoutine(id)`, `removeExercise(reId)`, `updateExercise(id, sets, reps, rest)`, `completeRoutine(userId)`

**`ExercisePickerViewModel.kt`:**
- State: `(selectedGroup: MuscleGroup, exercises, selectedIds, isRefreshing, error)`
- Actions: `selectGroup(MuscleGroup)`, `toggleExercise(id)`, `confirmSelection(routineId)`, `refresh()`
- `init { }` bloque: si caché stale, llama `RefreshExercisesUseCase`.

**`RoutineHistoryViewModel.kt`:**
- State: `(history: List<RoutineHistory>, isLoading)`
- Action: `loadHistory(userId)`

**Acceptance Criteria:**
- [ ] Los 5 ViewModels compilan con `@HiltViewModel`.
- [ ] `CreateRoutineViewModel.save()` emite `savedRoutineId != null` tras crear.
- [ ] `ExercisePickerViewModel` auto-refresca si la caché está stale.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/presentation/viewmodels/RoutinesViewModel.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/presentation/viewmodels/CreateRoutineViewModel.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/presentation/viewmodels/RoutineDetailViewModel.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/presentation/viewmodels/ExercisePickerViewModel.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/presentation/viewmodels/RoutineHistoryViewModel.kt`

---

### Task 3.2: RoutinesScreen
**Status:** ✅ completed
**Started:** 2026-04-12 01:00
**Completed:** 2026-04-12 01:30
**Priority:** high
**Depends On:** Task 3.1

**Description:**
Pantalla principal del tab Rutinas. Lista de rutinas del usuario con FAB para crear.

**Composable `RoutinesScreen(userId: Int, onNavigateToCreate: () -> Unit, onNavigateToDetail: (Int) -> Unit, onNavigateToHistory: () -> Unit, viewModel: RoutinesViewModel)`:**

- `LaunchedEffect(userId) { viewModel.loadRoutines(userId) }`
- `Scaffold`:
  - `TopAppBar`: título "Mis Rutinas", icono History al final → `onNavigateToHistory()`
  - `FloatingActionButton`: icono `+` → `onNavigateToCreate()`
  - Content: cuando `isLoading` → `CircularProgressIndicator` centrado
  - Cuando `routines.isEmpty()` → estado vacío (icono FitnessCenter + "Crea tu primera rutina")
  - Cuando no vacío → `LazyColumn` de `RoutineCard`

**`RoutineCard`** composable:
```
Card con:
  - Nombre de rutina (título)
  - Row de chips de días: abrev. L/M/X/J/V/S/D coloreados
  - Fila: icono reloj + "HH:MM" | icono FitnessCenter + "N ejercicios"
  - Swipe-to-delete: SwipeToDismissBox → AlertDialog de confirmación → viewModel.deleteRoutine(id)
  - onClick → onNavigateToDetail(routine.id)
```

**Acceptance Criteria:**
- [ ] La pantalla renderiza sin crash con lista vacía.
- [ ] Swipe-to-delete muestra diálogo y borra la rutina correctamente.
- [ ] Navega a CreateRoutine y RoutineDetail correctamente.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/presentation/screens/RoutinesScreen.kt`

---

### Task 3.3: CreateRoutineScreen
**Status:** ✅ completed
**Started:** 2026-04-12 01:00
**Completed:** 2026-04-12 01:30
**Priority:** high
**Depends On:** Task 3.1

**Description:**
Pantalla para crear una nueva rutina (nombre, días, hora de notificación).

**Composable `CreateRoutineScreen(userId: Int, onRoutineCreated: (routineId: Int) -> Unit, onBack: () -> Unit, viewModel: CreateRoutineViewModel)`:**

```
Scaffold:
  TopAppBar: "Nueva Rutina" + botón Back
  Content (Column, vertically scrollable):
    - OutlinedTextField: "Nombre de la rutina"
    - Sección "Días":
        FlowRow con 7 FilterChip (Lun/Mar/Mié/Jue/Vie/Sáb/Dom)
        selectedDays determina estado selected de cada chip
    - Sección "Hora de notificación":
        Row: icono reloj + texto "HH:MM"
        Al pulsar → mostrar TimePickerDialog (Material3)
        viewModel.setNotificationTime(hour, minute)
    - Button "Continuar" (fillMaxWidth):
        habilitado si name.isNotBlank() && selectedDays.isNotEmpty()
        onClick → viewModel.save(userId)
        muestra CircularProgressIndicator si isSaving

LaunchedEffect(uiState.savedRoutineId) {
    if (savedRoutineId != null) onRoutineCreated(savedRoutineId)
}
```

**Acceptance Criteria:**
- [ ] Botón "Continuar" deshabilitado sin nombre o sin días seleccionados.
- [ ] Al guardar navega a `ExercisePicker` pasando el `routineId`.
- [ ] El TimePicker muestra y aplica la hora correctamente.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/presentation/screens/CreateRoutineScreen.kt`

---

### Task 3.4: ExercisePickerScreen
**Status:** ✅ completed
**Started:** 2026-04-12 01:00
**Completed:** 2026-04-12 01:30
**Priority:** high
**Depends On:** Task 3.1

**Description:**
Pantalla para seleccionar ejercicios de un catálogo filtrado por grupo muscular y configurar sets/reps.

**Composable `ExercisePickerScreen(routineId: Int, onBack: () -> Unit, viewModel: ExercisePickerViewModel)`:**

```
Scaffold:
  TopAppBar: "Agregar ejercicios" + Back
  Content:
    - ScrollableTabRow con los 9 MuscleGroup.values()
      Tab seleccionado → viewModel.selectGroup(group)
    - PullToRefreshBox → viewModel.refreshExercises()
    - LazyColumn de ExercisePickerItem:
        Cada ítem:
          Row: Checkbox + Nombre ejercicio + descripción truncada
          Si checked → AnimatedVisibility con:
            Row de 3 OutlinedTextField (NumericInput): Sets / Reps / Descanso(s)
    - Error banner si `error != null`

  BottomAppBar:
    Button "Agregar seleccionados (N)" (N = selectedIds.size)
    habilitado si N > 0
    onClick → viewModel.confirmSelection(routineId) → onBack()
```

**Acceptance Criteria:**
- [ ] Los tabs de grupos musculares filtran la lista correctamente.
- [ ] Seleccionar un ejercicio expande el formulario sets/reps.
- [ ] "Agregar seleccionados" llama `AddExerciseToRoutineUseCase` para cada ejercicio y vuelve atrás.
- [ ] Pull-to-refresh actualiza el caché.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/presentation/screens/ExercisePickerScreen.kt`

---

### Task 3.5: RoutineDetailScreen
**Status:** ✅ completed
**Started:** 2026-04-12 01:00
**Completed:** 2026-04-12 01:30
**Priority:** high
**Depends On:** Task 3.1

**Description:**
Pantalla de detalle de una rutina con lista de ejercicios y botón "Terminar rutina".

**Composable `RoutineDetailScreen(routineId: Int, userId: Int, onBack: () -> Unit, onAddExercises: (Int) -> Unit, viewModel: RoutineDetailViewModel)`:**

```
Scaffold:
  TopAppBar: nombre de rutina + Back
  FAB: "✓ Terminar rutina" (Extended FAB con icono CheckCircle)
    onClick → muestra AlertDialog de confirmación
    En confirmación → viewModel.completeRoutine(userId)

  Content LazyColumn:
    Header (item):
      - Row días asignados (chips)
      - Row: icono reloj + hora notificación
      - TextButton "Editar" → ModalBottomSheet para editar nombre/días/hora

    Sección "Ejercicios" (item):
      - TextButton "Añadir ejercicios" → onAddExercises(routineId)

    items(exercises):
      RoutineExerciseItem:
        - Nombre + grupo muscular (badge)
        - "N series × N reps | Descanso: Ns"
        - Tap → ModalBottomSheet editar sets/reps/rest
        - SwipeToDismissBox → viewModel.removeExercise(reId)

LaunchedEffect(completedSuccessfully) {
    if (completedSuccessfully) {
        showSnackbar("¡Rutina completada!")
        onBack()
    }
}
```

**Acceptance Criteria:**
- [ ] El botón "Terminar rutina" muestra diálogo y registra en historial.
- [ ] Swipe-to-delete en ejercicio elimina el item de la rutina.
- [ ] BottomSheet editar abre con valores actuales y guarda cambios.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/presentation/screens/RoutineDetailScreen.kt`

---

### Task 3.6: RoutineHistoryScreen
**Status:** ✅ completed
**Started:** 2026-04-12 01:00
**Completed:** 2026-04-12 01:30
**Priority:** medium
**Depends On:** Task 3.1

**Description:**
Pantalla de historial de rutinas completadas.

**Composable `RoutineHistoryScreen(userId: Int, onBack: () -> Unit, viewModel: RoutineHistoryViewModel)`:**

```
LaunchedEffect(userId) { viewModel.loadHistory(userId) }

Scaffold:
  TopAppBar: "Historial" + Back
  Content:
    Si isLoading → CircularProgressIndicator
    Si history.isEmpty() → estado vacío: "Aún no has completado ninguna rutina"
    Sino → LazyColumn de HistoryItem:
      Row:
        - Icono CheckCircle (color primary)
        - Column:
            Text: routineName (style MaterialTheme.typography.bodyLarge)
            Text: completedAt formateado "dd MMM yyyy · HH:mm" (style bodySmall, color onSurfaceVariant)
```

**Acceptance Criteria:**
- [ ] La lista se ordena por `completedAt DESC`.
- [ ] Las fechas se formatean correctamente.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/presentation/screens/RoutineHistoryScreen.kt`

---

### Task 3.7: Rutas AppRoutes + RoutinesNavGraph
**Status:** ✅ completed
**Started:** 2026-04-12 02:00
**Completed:** 2026-04-12 02:30
**Priority:** high
**Depends On:** none (pueden crearse independientemente de las pantallas)

**Description:**
Agregar las 5 nuevas rutas type-safe y crear el NavGraph de rutinas.

**Cambios en `AppRoutes.kt`** — añadir al final:
```kotlin
@Serializable object Routines
@Serializable object CreateRoutine
@Serializable data class RoutineDetail(val routineId: Int)
@Serializable data class ExercisePicker(val routineId: Int)
@Serializable object RoutineHistory
```

**Crear `RoutinesNavGraph.kt`** en `features/routines/navigation/`:
```kotlin
fun NavGraphBuilder.routinesNavGraph(navController: NavController, userId: Int) {
    composable<Routines> {
        RoutinesScreen(
            userId = userId,
            onNavigateToCreate = { navController.navigate(CreateRoutine) },
            onNavigateToDetail = { id -> navController.navigate(RoutineDetail(id)) },
            onNavigateToHistory = { navController.navigate(RoutineHistory) }
        )
    }
    composable<CreateRoutine> {
        CreateRoutineScreen(
            userId = userId,
            onRoutineCreated = { routineId ->
                navController.navigate(ExercisePicker(routineId)) {
                    popUpTo<CreateRoutine> { inclusive = true }
                }
            },
            onBack = { navController.popBackStack() }
        )
    }
    composable<RoutineDetail> { backStackEntry ->
        val route: RoutineDetail = backStackEntry.toRoute()
        RoutineDetailScreen(
            routineId = route.routineId,
            userId = userId,
            onBack = { navController.popBackStack() },
            onAddExercises = { id -> navController.navigate(ExercisePicker(id)) }
        )
    }
    composable<ExercisePicker> { backStackEntry ->
        val route: ExercisePicker = backStackEntry.toRoute()
        ExercisePickerScreen(
            routineId = route.routineId,
            onBack = { navController.popBackStack() }
        )
    }
    composable<RoutineHistory> {
        RoutineHistoryScreen(
            userId = userId,
            onBack = { navController.popBackStack() }
        )
    }
}
```

**Acceptance Criteria:**
- [ ] `AppRoutes.kt` compila con las nuevas rutas `@Serializable`.
- [ ] `RoutinesNavGraph` compila sin errores de navegación type-safe.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/core/Navigation/AppRoutes.kt`

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/routines/navigation/RoutinesNavGraph.kt`

---

### Task 3.8: Integración Navigation.kt + UserBottomNavBar
**Status:** ✅ completed
**Started:** 2026-04-12 03:30
**Completed:** 2026-04-12 03:45
**Priority:** high
**Depends On:** Task 3.7

**Description:**
Conectar el NavGraph de rutinas al NavHost principal y agregar el 4.º tab a la barra inferior.

**Cambios en `Navigation.kt`:**
Dentro del `NavHost` del flujo de usuario (donde se declaran `composable<UserHome>`, etc.), añadir:
```kotlin
routinesNavGraph(navController, clientId)
```

**Cambios en `UserBottomNavBar.kt`** — agregar 4.º `NavigationBarItem`:
```kotlin
NavigationBarItem(
    icon = {
        Icon(
            Icons.Default.FitnessCenter,
            contentDescription = "Rutinas",
            modifier = Modifier.size(26.dp)
        )
    },
    label = { Text("Rutinas") },
    selected = selectedTab == 3,
    onClick = { onTabSelected(3) },
    colors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        indicatorColor = MaterialTheme.colorScheme.primaryContainer
    )
)
```

**Cambios en `UserHomeScreen.kt`:**
Asegurarse de que cuando `selectedTab == 3` se muestre el contenido de `Routines` o se navegue a la ruta `Routines` (depende de la implementación actual del switching de tabs en la pantalla Home). Pasar `clientId` como `userId` a las pantallas de rutinas.

**Acceptance Criteria:**
- [ ] El tab "Rutinas" aparece correctamente en la barra inferior.
- [ ] Seleccionar el tab navega a `RoutinesScreen`.
- [ ] Los otros 3 tabs siguen funcionando sin regresión.
- [ ] No hay warning de Compose sobre `Icons.Default.FitnessCenter` (importar `Icons.Default` correcto).

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/core/Navigation/Navigation.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/users/presentation/components/UserBottomNavBar.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/users/presentation/screens/UserHomeScreen.kt`

---

## Phase 4: Testing & Polish

### Task 4.1: Test de integración — Flujo crear rutina
**Status:** ⏳ pending
**Priority:** medium
**Depends On:** Task 3.3, Task 3.4

**Description:**
Verificar manualmente el flujo completo de creación de rutina de punta a punta:

1. Abrir app → login como miembro → tab "Rutinas"
2. Pulsar FAB `+` → `CreateRoutineScreen`
3. Introducir nombre "Rutina Fuerza", seleccionar Lun/Mié/Vie, hora 07:30
4. Pulsar "Continuar" → navega a `ExercisePickerScreen`
5. Seleccionar tab "Pecho" → seleccionar 2 ejercicios → configurar sets/reps
6. Seleccionar tab "Espalda" → seleccionar 1 ejercicio
7. Pulsar "Agregar seleccionados (3)" → navega de vuelta
8. En `RoutinesScreen` aparece la rutina con 3 ejercicios, días Lun/Mié/Vie, hora 07:30
9. Verificar en `adb shell dumpsys alarm` que hay 3 alarmas programadas

**Acceptance Criteria:**
- [ ] La rutina aparece en la lista tras crearse.
- [ ] Los ejercicios y su configuración (sets/reps) se guardan correctamente.
- [ ] Las alarmas están programadas en el sistema.

---

### Task 4.2: Test de integración — Completar rutina e historial
**Status:** ⏳ pending
**Priority:** medium
**Depends On:** Task 3.5, Task 3.6

**Description:**
Verificar el flujo de completar rutina y ver historial:

1. En `RoutinesScreen` → pulsar sobre "Rutina Fuerza" → `RoutineDetailScreen`
2. Pulsar FAB "✓ Terminar rutina" → diálogo de confirmación → confirmar
3. Snackbar "¡Rutina completada!" → pantalla vuelve a `RoutinesScreen`
4. En `TopAppBar` de Rutinas → pulsar icono History → `RoutineHistoryScreen`
5. Aparece "Rutina Fuerza" con fecha/hora actual

**Acceptance Criteria:**
- [ ] La entrada aparece en historial con nombre y timestamp correcto.
- [ ] La rutina sigue activa en la lista (completar no borra la rutina).

---

### Task 4.3: Permiso ExactAlarm — flujo Android 12+
**Status:** ⏳ pending
**Priority:** medium
**Depends On:** Task 2.11, Task 3.3

**Description:**
Asegurar que en dispositivos con API 31+ se solicita el permiso `SCHEDULE_EXACT_ALARM` cuando es necesario.

En `CreateRoutineScreen` o en `RoutinesScreen`, antes de llamar `viewModel.save()`, verificar:
```kotlin
val alarmManager = context.getSystemService(AlarmManager::class.java)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
    // Mostrar diálogo informativo + Intent a Settings
    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
    context.startActivity(intent)
    return  // no guardar hasta que el permiso esté concedido
}
```

**Acceptance Criteria:**
- [ ] En emulador API 31+ sin el permiso, se abre la pantalla de ajustes del permiso.
- [ ] Tras conceder el permiso, crear la rutina funciona correctamente.

---

### Task 4.4: Notificación motivacional para días sin rutina
**Status:** ⏳ pending
**Priority:** low
**Depends On:** Task 2.12

**Description:**
Programar una alarma diaria "de fallback" para los días en que el usuario no tiene rutinas asignadas.

**Estrategia:** En `RoutinesViewModel` (o en un `WorkManager` semanal), al cargar las rutinas calcular qué días de la semana (1-7) no tienen ninguna rutina asignada. Para esos días, programar una alarma a las 10:00 AM con `routineId = 0` y sin extra `routineName` → `RoutineNotificationReceiver` detecta `routineName == null` y muestra mensaje motivacional.

**Acceptance Criteria:**
- [ ] En un día sin rutinas programadas llega una notificación motivacional a las 10:00 AM.
- [ ] La notificación motivacional no aparece en días que ya tienen rutina.

---

## Parallelization Strategy

Tasks that can be executed in parallel (no dependencies between them):

### Parallel Group 1 — Foundation (todas independientes)
- Task 1.1: Dependencia WorkManager
- Task 1.2: Manifest permisos/receivers
- Task 1.3: Entidades Room

### Parallel Group 2 — Setup remoto y dominio (independientes entre sí)
- Task 2.1: DTOs WgerApiService
- Task 2.2: MuscleGroup enum
- Task 2.5: Entidades de dominio

### Parallel Group 3 — Pantallas (dependencias completadas en Phase 2)
- Task 3.2: RoutinesScreen
- Task 3.3: CreateRoutineScreen
- Task 3.4: ExercisePickerScreen
- Task 3.5: RoutineDetailScreen
- Task 3.6: RoutineHistoryScreen

### Sequential Dependencies
Orden obligatorio por dependencias críticas:

```
1.3 → 1.4 → 1.5         (entidades → DAOs → migración DB)
2.1 + 2.2 → 2.3         (DTOs + MuscleGroup → WgerNetworkModule)
2.3 + 2.2 → 2.4         (WgerApiService + mapper → RemoteDataSource)
2.2 → 2.5 → 2.6 → 2.7  (MuscleGroup → entidades domain → repos iface → use cases)
1.4 + 2.4 + 2.6 + 2.8 → 2.9  (todo data layer → repo impls)
2.3 + 2.9 → 2.10        (módulos Hilt completos)
2.7 → 3.1               (use cases → ViewModels)
3.1 → 3.2...3.6         (ViewModels → Pantallas)
3.7 → 3.8               (rutas → integración nav)
```
