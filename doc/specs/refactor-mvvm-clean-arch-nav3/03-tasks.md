# Tasks: Refactorización MVVM + Clean Architecture + Navigation Type-Safe

**Spec:** doc/specs/refactor-mvvm-clean-arch-nav3/02-specification.md
**Created:** 2026-04-12 00:00
**Last Updated:** 2026-04-12 00:00
**Last Decompose:** 2026-04-12 00:00

---

## Summary

| Status | Count |
|--------|-------|
| ⏳ Pending | 0 |
| 🔄 In Progress | 0 |
| ✅ Completed | 38 |
| **Total** | **41** |

---

## Phase 1: Fundación — Gradle Setup

### Task 1.1: Agregar plugin kotlinx-serialization
**Status:** ✅ completed
**Started:** 2026-04-12 00:00
**Completed:** 2026-04-12 00:01
**Priority:** high
**Depends On:** none

**Description:**
Navigation 2.8.x type-safe routes requieren `kotlinx-serialization`. Agregar el plugin y la dependencia al proyecto sin tocar ningún código de negocio.

**Technical Requirements:**

En `gradle/libs.versions.toml`:
- Agregar en `[plugins]`: `kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }`

En `build.gradle.kts` (raíz), bloque `plugins { }`:
- Agregar: `alias(libs.plugins.kotlin.serialization) apply false`

En `app/build.gradle.kts`, bloque `plugins { }`:
- Agregar: `alias(libs.plugins.kotlin.serialization)`

**Acceptance Criteria:**
- [ ] El proyecto compila sin errores tras el cambio.
- [ ] `@Serializable` de `kotlinx.serialization` puede usarse en archivos Kotlin del módulo `app`.
- [ ] No hay cambios en el comportamiento de la app.

**Files to Modify:**
- `gradle/libs.versions.toml`
- `build.gradle.kts` (raíz)
- `app/build.gradle.kts`

---

## Phase 2: Feature `auth` — Consolidación

### Task 2.1: Verificar que GymViewModel y GymLoginViewModelFactory no tienen uso activo
**Status:** ✅ completed
**Started:** 2026-04-12 00:02
**Completed:** 2026-04-12 00:03
**Priority:** high
**Depends On:** none

**Description:**
Antes de eliminar los archivos legacy de auth, confirmar que no hay referencias activas en el codebase. Buscar en todos los archivos `.kt` las cadenas `GymViewModel`, `GymLoginViewModelFactory`, `AuthModule` (manual, en `features/auth/di/`), `GymModule`.

Si se encuentran referencias fuera de los propios archivos legacy y de `LoginScreens.kt`, **detener la tarea** y reportar al usuario con las ubicaciones exactas.

**Technical Requirements:**
- Buscar con grep/IDE: `GymViewModel`, `GymLoginViewModelFactory`
- Los únicos usos válidos son dentro de los propios archivos y en `LoginScreens.kt` (overload legacy)
- `AuthModule.kt` en `features/auth/di/` (NO el `AuthHiltModule`) — verificar que nadie lo referencia
- `GymModule.kt` — verificar que nadie lo referencia

**Acceptance Criteria:**
- [ ] Se confirma que `GymViewModel` no es llamado desde ninguna pantalla o navegación activa.
- [ ] Se confirma que `GymLoginViewModelFactory` no es instanciada en `Navigation.kt` ni en ningún screen.
- [ ] Reporte de búsqueda documentado (puede ser un comentario en el commit).

**Files to Modify:** ninguno (solo lectura)

---

### Task 2.2: Extraer EnableBiometricDialog a BiometricDialogs.kt
**Status:** ✅ completed
**Started:** 2026-04-12 00:03
**Completed:** 2026-04-12 00:04
**Priority:** medium
**Depends On:** Task 2.1

**Description:**
Mover el composable `EnableBiometricDialog` de `LoginScreens.kt` a un archivo dedicado, sin cambiar su implementación.

**Technical Requirements:**
- Crear `features/auth/presentation/components/BiometricDialogs.kt`
- Mover la función `EnableBiometricDialog(onConfirm: () -> Unit, onSkip: () -> Unit)` exactamente como está.
- Actualizar el import en `LoginScreens.kt`.
- El archivo nuevo debe estar en el mismo paquete: `com.AppexSolutions.gymsync.features.auth.presentation.components`

**Acceptance Criteria:**
- [ ] `BiometricDialogs.kt` existe y contiene `EnableBiometricDialog`.
- [ ] `LoginScreens.kt` importa desde la nueva ubicación.
- [ ] El diálogo de biometría sigue apareciendo correctamente en la pantalla de login admin.
- [ ] El proyecto compila sin errores.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/auth/presentation/components/BiometricDialogs.kt`

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/auth/presentation/screens/LoginScreens.kt`

---

### Task 2.3: Extraer ErrorDialog a AuthDialogs.kt
**Status:** ✅ completed
**Started:** 2026-04-12 00:03
**Completed:** 2026-04-12 00:04
**Priority:** medium
**Depends On:** Task 2.1

**Description:**
Mover el composable `ErrorDialog` de `LoginScreens.kt` a un archivo dedicado.

**Technical Requirements:**
- Crear `features/auth/presentation/components/AuthDialogs.kt`
- Mover `ErrorDialog(message: String, onDismiss: () -> Unit)` exactamente como está.
- Paquete: `com.AppexSolutions.gymsync.features.auth.presentation.components`
- Actualizar import en `LoginScreens.kt`.

**Acceptance Criteria:**
- [ ] `AuthDialogs.kt` existe y contiene `ErrorDialog`.
- [ ] `LoginScreens.kt` importa desde la nueva ubicación.
- [ ] El diálogo de error sigue apareciendo en login admin ante credenciales incorrectas.
- [ ] El proyecto compila.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/auth/presentation/components/AuthDialogs.kt`

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/auth/presentation/screens/LoginScreens.kt`

---

### Task 2.4: Eliminar overload legacy de LoginScreen y limpiar LoginScreens.kt
**Status:** ✅ completed
**Started:** 2026-04-12 00:04
**Completed:** 2026-04-12 00:05
**Priority:** high
**Depends On:** Task 2.2, Task 2.3

**Description:**
Eliminar el segundo overload de `LoginScreen` que recibe `factory: GymLoginViewModelFactory`. Tras esta tarea, `LoginScreens.kt` contiene solo la versión Hilt.

**Technical Requirements:**
- Eliminar de `LoginScreens.kt` las líneas ~234-357 (el overload con `factory: GymLoginViewModelFactory`).
- Eliminar los imports de `GymViewModel` y `GymLoginViewModelFactory`.
- El primer overload `LoginScreen(onLoginSuccess, onForgotPasswordClick, viewModel: LoginViewModel = hiltViewModel())` permanece intacto.
- Los composables `EnableBiometricDialog` y `ErrorDialog` ya fueron movidos en Tasks 2.2 y 2.3, sus llamadas en el overload Hilt ahora usan los imports desde los nuevos archivos.

**Acceptance Criteria:**
- [ ] `LoginScreens.kt` tiene solo un overload de `LoginScreen`.
- [ ] No hay imports de `GymViewModel` ni `GymLoginViewModelFactory` en ningún archivo.
- [ ] Login admin con email/password y biometría funciona correctamente.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/auth/presentation/screens/LoginScreens.kt`

---

### Task 2.5: Eliminar archivos legacy de auth
**Status:** ✅ completed
**Started:** 2026-04-12 00:05
**Completed:** 2026-04-12 00:06
**Priority:** high
**Depends On:** Task 2.4

**Description:**
Eliminar los 4 archivos legacy tras confirmar que ningún archivo los referencia.

**Technical Requirements:**
Eliminar:
1. `features/auth/presentation/viewmodels/GymViewModel.kt`
2. `features/auth/presentation/viewmodels/GymLoginViewModelFactory.kt`
3. `features/auth/di/AuthModule.kt` (el manual, NO `AuthHiltModule.kt`)
4. `features/auth/di/GymModule.kt`

Verificar antes de cada eliminación que no hay imports que fallen.

**Acceptance Criteria:**
- [ ] Los 4 archivos no existen en el proyecto.
- [ ] El proyecto compila sin errores tras las eliminaciones.
- [ ] El feature `auth/di/` contiene solo `AuthHiltModule.kt`.

**Files to Delete:**
- `GymViewModel.kt`
- `GymLoginViewModelFactory.kt`
- `features/auth/di/AuthModule.kt`
- `features/auth/di/GymModule.kt`

---

## Phase 3: Feature `clients` — Hilt + UseCase foto

### Task 3.1: Crear DateUtils.kt con utilidades de fecha
**Status:** ✅ completed
**Started:** 2026-04-12 00:10
**Completed:** 2026-04-12 00:11
**Priority:** medium
**Depends On:** none

**Description:**
Extraer las funciones de transformación de fecha que actualmente viven en `CreateUserViewModel.kt` a una clase de utilidades del core.

**Technical Requirements:**
Crear `core/util/DateUtils.kt` con paquete `com.AppexSolutions.gymsync.core.util`:

```kotlin
@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentIsoTimestamp(): String {
    return ZonedDateTime.now(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatToIsoTimestamp(dateString: String): String? {
    if (dateString.isBlank()) return null
    return try {
        val localDate = java.time.LocalDate.parse(dateString)
        localDate.atStartOfDay(ZoneOffset.UTC)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
    } catch (e: Exception) {
        null
    }
}
```

Estas funciones son movidas (no copiadas) — eliminarlas de `CreateUserViewModel.kt` y actualizar las llamadas para usar `DateUtils.getCurrentIsoTimestamp()` y `DateUtils.formatToIsoTimestamp()`.

**Acceptance Criteria:**
- [ ] `core/util/DateUtils.kt` existe con las dos funciones.
- [ ] `CreateUserViewModel.kt` no contiene funciones top-level de fecha.
- [ ] La creación de usuario sigue funcionando con fechas correctas.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/core/util/DateUtils.kt`

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/clients/presentation/viewmodels/CreateUserViewModel.kt`

---

### Task 3.2: Crear GetAllClientPhotosUseCase
**Status:** ✅ completed
**Started:** 2026-04-12 00:10
**Completed:** 2026-04-12 00:11
**Priority:** high
**Depends On:** none

**Description:**
Crear el use case que encapsula la carga en bulk de fotos de perfil para una lista de clientes. Esto elimina el acceso directo de `ClientsViewModel` a `ProfilePhotoDao`.

**Technical Requirements:**
Crear `features/clients/domain/usecases/GetAllClientPhotosUseCase.kt`:

```kotlin
class GetAllClientPhotosUseCase @Inject constructor(
    private val profilePhotoDao: ProfilePhotoDao,
    private val profilePhotoManager: ProfilePhotoManager
) {
    suspend operator fun invoke(clientIds: List<Int>): Map<Int, String> {
        val photoMap = mutableMapOf<Int, String>()
        for (id in clientIds) {
            val uri = profilePhotoDao.getPhotoUri(id)
            if (uri != null && java.io.File(uri).exists()) {
                photoMap[id] = uri
            }
        }
        return photoMap
    }
}
```

Este use case **sí** puede depender de `ProfilePhotoDao` directamente porque vive en la capa de dominio/datos — no en presentación.

**Acceptance Criteria:**
- [ ] `GetAllClientPhotosUseCase` existe y compila.
- [ ] Recibe `List<Int>` de IDs y retorna `Map<Int, String>` (id → uri).
- [ ] Solo incluye en el mapa las fotos cuyo archivo existe en el filesystem.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/clients/domain/usecases/GetAllClientPhotosUseCase.kt`

---

### Task 3.3: Crear ClientsHiltModule
**Status:** ✅ completed
**Started:** 2026-04-12 12:00
**Completed:** 2026-04-12 12:05
**Priority:** high
**Depends On:** Task 3.2

**Description:**
Crear el módulo Hilt para el feature `clients`, que reemplaza a `ClientModule.kt` (manual).

**Technical Requirements:**
Crear `features/clients/di/ClientsHiltModule.kt`:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class ClientsHiltModule {

    @Binds
    @Singleton
    abstract fun bindClientRepository(impl: ClientsRepoImplements): ClientRepository

    companion object {
        @Provides
        @Singleton
        fun provideGetAllClientPhotosUseCase(
            profilePhotoDao: ProfilePhotoDao,
            profilePhotoManager: ProfilePhotoManager
        ): GetAllClientPhotosUseCase = GetAllClientPhotosUseCase(profilePhotoDao, profilePhotoManager)

        @Provides
        @Singleton
        fun provideProfilePhotoManager(cameraDataSource: CameraDataSource): ProfilePhotoManager =
            ProfilePhotoManager(cameraDataSource)
    }
}
```

`ProfilePhotoDao` y `CameraDataSource` ya son provistos por `DatabaseModule` — verificar que estén disponibles como dependencias.

**Acceptance Criteria:**
- [ ] `ClientsHiltModule` compila sin errores.
- [ ] Hilt puede inyectar `ClientRepository`, `GetAllClientPhotosUseCase`, `ProfilePhotoManager`.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/clients/di/ClientsHiltModule.kt`

---

### Task 3.4: Refactorizar ClientsViewModel a @HiltViewModel
**Status:** ✅ completed
**Started:** 2026-04-12 12:06
**Completed:** 2026-04-12 12:10
**Priority:** high
**Depends On:** Task 3.2, Task 3.3

**Description:**
Eliminar la dependencia directa de `ProfilePhotoDao` en `ClientsViewModel` y reemplazar por `GetAllClientPhotosUseCase`.

**Technical Requirements:**
- Cambiar la firma del constructor a:
  ```kotlin
  @HiltViewModel
  class ClientsViewModel @Inject constructor(
      private val getClientsUsecase: GetClientsUsecase,
      private val getAllClientPhotosUseCase: GetAllClientPhotosUseCase
  ) : ViewModel()
  ```
- Reemplazar `loadClientPhotos()`:
  ```kotlin
  private suspend fun loadClientPhotos() {
      val clients = _uiState.value.clients
      if (clients.isEmpty()) return
      val photoMap = getAllClientPhotosUseCase(clients.map { it.id })
      _uiState.update { it.copy(clientPhotoUris = photoMap) }
  }
  ```
- Eliminar todo import de `ProfilePhotoDao` y `java.io.File`.
- Agregar anotaciones `@HiltViewModel` y `@Inject`.

**Acceptance Criteria:**
- [ ] `ClientsViewModel` no importa `ProfilePhotoDao`.
- [ ] `ClientsViewModel` no importa `java.io.File`.
- [ ] Las fotos de perfil siguen apareciendo en la lista de clientes.
- [ ] El ViewModel compila con las anotaciones Hilt.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/clients/presentation/viewmodels/ClientsViewmodel.kt`

---

### Task 3.5: Refactorizar CreateUserViewModel a @HiltViewModel
**Status:** ✅ completed
**Started:** 2026-04-12 12:06
**Completed:** 2026-04-12 12:10
**Priority:** high
**Depends On:** Task 3.1, Task 3.3

**Description:**
Convertir `CreateUserViewModel` a `@HiltViewModel` con inyección de dependencias Hilt.

**Technical Requirements:**
- Cambiar la firma:
  ```kotlin
  @HiltViewModel
  class CreateUserViewModel @Inject constructor(
      private val createUserUseCase: CreateUserUseCase,
      private val getRolesUseCase: GetRolesUseCase,
      private val getGymsUseCase: GetGymsUseCase,
      private val saveProfilePhotoUseCase: SaveProfilePhotoUseCase
  ) : ViewModel()
  ```
- Eliminar el `? = null` de `saveProfilePhotoUseCase` — Hilt siempre lo provee.
- Las llamadas a `getCurrentIsoTimestamp()` y `formatToIsoTimestamp()` ahora usan `DateUtils.xxx()` (de Task 3.1).
- Eliminar las funciones top-level del archivo (ya movidas a `DateUtils`).

**Acceptance Criteria:**
- [ ] `CreateUserViewModel` tiene `@HiltViewModel` y `@Inject constructor`.
- [ ] No hay funciones top-level en el archivo del ViewModel.
- [ ] La creación de nuevos clientes funciona igual.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/clients/presentation/viewmodels/CreateUserViewModel.kt`

---

### Task 3.6: Refactorizar EditClientViewModel a @HiltViewModel con SavedStateHandle
**Status:** ✅ completed
**Started:** 2026-04-12 12:06
**Completed:** 2026-04-12 12:10
**Priority:** high
**Depends On:** Task 3.3

**Description:**
Convertir `EditClientViewModel` a `@HiltViewModel`. El `clientId` ya no se pasa en el constructor como `Int` — se obtiene de `SavedStateHandle` (inyectado automáticamente por Hilt Navigation Compose).

**Technical Requirements:**
- Nueva firma:
  ```kotlin
  @HiltViewModel
  class EditClientViewModel @Inject constructor(
      private val savedStateHandle: SavedStateHandle,
      private val getClientByIdUseCase: GetClientByIdUseCase,
      private val updateClientUseCase: UpdateClientUseCase,
      private val deleteClientUseCase: DeleteClientUseCase,
      private val toggleUserActiveUseCase: ToggleUserActiveUseCase,
      private val saveProfilePhotoUseCase: SaveProfilePhotoUseCase,
      private val getProfilePhotoUseCase: GetProfilePhotoUseCase
  ) : ViewModel()
  ```
- Cambiar `private val clientId: Int` por:
  ```kotlin
  private val clientId: Int = savedStateHandle["clientId"] ?: 0
  ```
- Eliminar `? = null` de `saveProfilePhotoUseCase` y `getProfilePhotoUseCase`.
- Todo el resto de la lógica permanece igual.

**Nota importante:** Para que `SavedStateHandle["clientId"]` funcione, la ruta de navegación debe pasar `clientId` como argumento. Esto se conecta con la tarea de navegación type-safe (Task 6.4). Mientras tanto, la ruta string `"edit_client/{clientId}"` ya pasa el argumento correctamente.

**Acceptance Criteria:**
- [ ] `EditClientViewModel` tiene `@HiltViewModel` y `@Inject constructor(savedStateHandle, ...)`.
- [ ] `clientId` se lee de `savedStateHandle`.
- [ ] Editar, eliminar, toggle activo y fotos de perfil funcionan igual.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/clients/presentation/viewmodels/EditClientViewModel.kt`

---

### Task 3.7: Agregar @Inject a ClientsRepoImplements
**Status:** ✅ completed
**Started:** 2026-04-12 12:05
**Completed:** 2026-04-12 12:06
**Priority:** high
**Depends On:** Task 3.3

**Description:**
Para que `ClientsHiltModule` pueda usar `@Binds`, `ClientsRepoImplements` necesita `@Inject constructor`.

**Technical Requirements:**
- Abrir `features/clients/data/repositories/ClientsRepoImplements.kt`.
- Agregar `@Inject` al constructor: `class ClientsRepoImplements @Inject constructor(private val api: GymSyncAPI)`.
- Sin cambios en la lógica.

**Acceptance Criteria:**
- [ ] `ClientsRepoImplements` tiene `@Inject constructor`.
- [ ] Hilt puede crear instancias sin `@Provides` explícito.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/clients/data/repositories/ClientsRepoImplements.kt`

---

### Task 3.8: Migrar pantallas de clients a hiltViewModel()
**Status:** ✅ completed
**Started:** 2026-04-12 12:10
**Completed:** 2026-04-12 12:15
**Priority:** high
**Depends On:** Task 3.4, Task 3.5, Task 3.6

**Description:**
Reemplazar el parámetro `factory: XViewModelFactory` en los tres screens de clients por `hiltViewModel()`.

**Technical Requirements:**

`ClientsScreen.kt`:
- Cambiar firma: reemplazar `factory: ClientsViewModelFactory` por `viewModel: ClientsViewModel = hiltViewModel()`
- Eliminar el uso de `factory` internamente.

`CreateUserScreen.kt`:
- Cambiar firma: reemplazar `factory: CreateUserViewModelFactory` por `viewModel: CreateUserViewModel = hiltViewModel()`

`EditClientScreen.kt`:
- Cambiar firma: reemplazar `factory: EditClientViewModelFactory` por `viewModel: EditClientViewModel = hiltViewModel()`

En los tres archivos: eliminar imports de las factories correspondientes.

**Acceptance Criteria:**
- [ ] Los 3 screens compilan sin factories.
- [ ] Las pantallas de lista, creación y edición de clientes funcionan.

**Files to Modify:**
- `ClientsScreen.kt`
- `CreateUserScreen.kt`
- `EditClientScreen.kt`

---

### Task 3.9: Actualizar Navigation.kt — eliminar ClientsModule
**Status:** ✅ completed
**Started:** 2026-04-12 12:15
**Completed:** 2026-04-12 12:18
**Priority:** high
**Depends On:** Task 3.8

**Description:**
`AppNavigation` ya no necesita instanciar `ClientsModule` ni pasar factories a las pantallas de clients.

**Technical Requirements:**
En `core/Navigation/Navigation.kt`:
- Eliminar: `val clientsModule = remember { ClientsModule(appContainer) }`
- Eliminar el parámetro `factory = clientsModule.provideClientsViewModelFactory()` de `ClientsScreen(...)`.
- Eliminar el parámetro `factory = clientsModule.provideCreateUserViewModelFactory()` de `CreateUserScreen(...)`.
- Eliminar el parámetro `factory = clientsModule.provideEditClientViewModelFactory(clientId)` de `EditClientScreen(...)`.
- Eliminar imports de `ClientsModule`.

**Acceptance Criteria:**
- [ ] `Navigation.kt` no importa `ClientsModule`.
- [ ] Las pantallas admin siguen siendo accesibles y funcionales.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/core/Navigation/Navigation.kt`

---

### Task 3.10: Eliminar factories manuales y ClientModule
**Status:** ✅ completed
**Started:** 2026-04-12 12:18
**Completed:** 2026-04-12 12:20
**Priority:** medium
**Depends On:** Task 3.9

**Description:**
Eliminar los 4 archivos que ya no tienen consumidores.

**Technical Requirements:**
Eliminar:
1. `features/clients/presentation/viewmodels/ClientViewModelFactory.kt`
2. `features/clients/presentation/viewmodels/CreateUserViewModelFactory.kt`
3. `features/clients/presentation/viewmodels/EditClientViewModelFactory.kt`
4. `features/clients/di/ClientModule.kt`

Verificar antes que ningún archivo tiene imports de estos.

**Acceptance Criteria:**
- [ ] Los 4 archivos eliminados.
- [ ] El proyecto compila.
- [ ] El feature `clients/di/` contiene solo `ClientsHiltModule.kt`.

**Files to Delete:**
- `ClientViewModelFactory.kt`
- `CreateUserViewModelFactory.kt`
- `EditClientViewModelFactory.kt`
- `features/clients/di/ClientModule.kt`

---

## Phase 4: Feature `users` — Repositorio, endpoint real, biometría

### Task 4.1: Crear interfaz UserDataRepository
**Status:** ✅ completed
**Started:** 2026-04-12 13:00
**Completed:** 2026-04-12 13:02
**Priority:** high
**Depends On:** none

**Description:**
Definir el contrato de la única interfaz de datos para el dominio de usuarios/miembros.

**Technical Requirements:**
Crear `features/users/domain/repositories/UserDataRepository.kt`:

```kotlin
package com.AppexSolutions.gymsync.features.users.domain.repositories

import com.AppexSolutions.gymsync.features.users.domain.entities.MemberProfile
import com.AppexSolutions.gymsync.features.users.domain.entities.MembershipPlan

interface UserDataRepository {
    suspend fun getMemberProfile(userId: Int): MemberProfile
    suspend fun getMembershipPlans(): List<MembershipPlan>
}
```

**Acceptance Criteria:**
- [ ] Interfaz creada con los dos métodos.
- [ ] El proyecto compila.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/users/domain/repositories/UserDataRepository.kt`

---

### Task 4.2: Crear MemberProfileMapper
**Status:** ✅ completed
**Started:** 2026-04-12 13:05
**Completed:** 2026-04-12 13:07
**Priority:** medium
**Depends On:** Task 4.1

**Description:**
Extraer la función de extensión `Client.toMemberProfile()` de `UserViewModel` a una capa de mapper en datos.

**Technical Requirements:**
Crear `features/users/data/mapper/MemberProfileMapper.kt`:

```kotlin
package com.AppexSolutions.gymsync.features.users.data.mapper

import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.users.domain.entities.MemberProfile
import com.AppexSolutions.gymsync.features.users.domain.entities.PlanStatus

fun Client.toMemberProfile(): MemberProfile {
    val plans = listOf("Pro", "Premium", "Ultimate")
    val assignedPlan = plans[id % plans.size]
    val isActive = activo
    val status = if (isActive) PlanStatus.ACTIVO else PlanStatus.VENCIDO

    return MemberProfile(
        id = id,
        nombres = nombres,
        apellidos = apellidos,
        email = email,
        telefono = telefono,
        currentPlan = assignedPlan,
        planStatus = status,
        nextPaymentDate = if (isActive) "10 Abril 2026" else "—",
        daysRemaining = if (isActive) (15 + (id * 7) % 30) else 0,
        currentStreak = if (isActive) (3 + (id * 5) % 25) else 0,
        monthlyVisits = if (isActive) (5 + (id * 3) % 20) else 0,
        qrCode = "GYMSYNC-USR-${id.toString().padStart(3, '0')}-2026"
    )
}
```

Eliminar la función de `UserViewModel.kt`.

**Acceptance Criteria:**
- [ ] `MemberProfileMapper.kt` existe.
- [ ] `UserViewModel.kt` no contiene `Client.toMemberProfile()`.
- [ ] El Home del miembro sigue mostrando el perfil correctamente.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/users/data/mapper/MemberProfileMapper.kt`

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/users/presentation/viewmodels/UserViewModel.kt`

---

### Task 4.3: Crear FakeUserDataRepository
**Status:** ✅ completed
**Started:** 2026-04-12 13:05
**Completed:** 2026-04-12 13:07
**Priority:** high
**Depends On:** Task 4.1

**Description:**
Crear la implementación mock de `UserDataRepository` que delega a `FakeUserRepository`. `FakeUserRepository.kt` NO se elimina — se convierte en implementación interna.

**Technical Requirements:**
Crear `features/users/data/FakeUserDataRepository.kt`:

```kotlin
@Singleton
class FakeUserDataRepository @Inject constructor() : UserDataRepository {

    private val fake = FakeUserRepository()

    override suspend fun getMemberProfile(userId: Int): MemberProfile {
        return fake.getProfileForClient(userId) ?: fake.getDefaultProfile()
    }

    override suspend fun getMembershipPlans(): List<MembershipPlan> {
        return fake.membershipPlans
    }
}
```

**Acceptance Criteria:**
- [ ] `FakeUserDataRepository` implementa `UserDataRepository`.
- [ ] Los métodos retornan datos del `FakeUserRepository` existente.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/users/data/FakeUserDataRepository.kt`

---

### Task 4.4: Crear use cases de autenticación de miembro
**Status:** ✅ completed
**Started:** 2026-04-12 13:00
**Completed:** 2026-04-12 13:04
**Priority:** high
**Depends On:** none

**Description:**
Crear los 4 use cases que replican el patrón de biometría del admin para el flujo de miembro.

**Technical Requirements:**

**`LoginMemberUseCase.kt`** — llama al mismo endpoint de login que `LoginUseCase`:
```kotlin
class LoginMemberUseCase @Inject constructor(
    private val api: GymSyncAPI,
    private val userDao: UserDao,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthSession> {
        return try {
            val response = api.login(LoginRequest(email, password))
            // Guardar sesión en Room igual que LoginUseCase del admin
            userDao.insertUser(UserEntity(
                email = email,
                name = "${response.user.nombres} ${response.user.apellidos}",
                token = response.token,
                biometricEnabled = false,
                lastLogin = Date()
            ))
            authPreferences.saveToken(response.token)
            Result.success(AuthSession(token = response.token, id_user = response.user.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**`HasMemberBiometricSessionUseCase.kt`**:
```kotlin
class HasMemberBiometricSessionUseCase @Inject constructor(
    private val userDao: UserDao,
    private val biometricAuthManager: BiometricAuthManager
) {
    suspend operator fun invoke(): Boolean {
        if (!biometricAuthManager.isHardwareAvailable()) return false
        if (!biometricAuthManager.isBiometricEnrolled()) return false
        return userDao.countBiometricUsers() > 0
    }
}
```

**`EnableMemberBiometricUseCase.kt`**:
```kotlin
class EnableMemberBiometricUseCase @Inject constructor(
    private val userDao: UserDao
) {
    suspend operator fun invoke(email: String, enable: Boolean = true) {
        userDao.updateBiometricStatus(email, enable)
    }
}
```

**`LoginMemberWithBiometricUseCase.kt`**: Idéntico a `LoginWithBiometricUseCase` del admin — lanza biometría, lee sesión de Room. Puede reusar `BiometricAuthManager` + `UserDao`.

Todos en paquete: `features/users/domain/usecases/`

**Acceptance Criteria:**
- [ ] Los 4 use cases compilan.
- [ ] `LoginMemberUseCase` guarda sesión en Room tras login exitoso.
- [ ] `HasMemberBiometricSessionUseCase` retorna `true` solo si hay hardware disponible Y usuario con biometría guardada.

**Files to Create:**
- `LoginMemberUseCase.kt`
- `HasMemberBiometricSessionUseCase.kt`
- `EnableMemberBiometricUseCase.kt`
- `LoginMemberWithBiometricUseCase.kt`

---

### Task 4.5: Crear UsersHiltModule
**Status:** ✅ completed
**Started:** 2026-04-12 13:07
**Completed:** 2026-04-12 13:09
**Priority:** high
**Depends On:** Task 4.3, Task 4.4

**Description:**
Crear el módulo Hilt del feature `users`.

**Technical Requirements:**
Crear `features/users/di/UsersHiltModule.kt`:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class UsersHiltModule {

    @Binds
    @Singleton
    abstract fun bindUserDataRepository(impl: FakeUserDataRepository): UserDataRepository

    companion object {
        @Provides
        @Singleton
        fun provideQrGenerator(): QrGenerator = QrGenerator()
    }
}
```

`BiometricAuthManager` debe estar disponible desde `AuthHiltModule` o `DatabaseModule`. Verificar que ya tiene `@Inject constructor` o `@Provides`. Si no, agregarlo en este módulo.

**Acceptance Criteria:**
- [ ] `UsersHiltModule` compila.
- [ ] Hilt puede inyectar `UserDataRepository`, `QrGenerator`.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/users/di/UsersHiltModule.kt`

---

### Task 4.6: Refactorizar UserViewModel a @HiltViewModel
**Status:** ✅ completed
**Started:** 2026-04-12 13:09
**Completed:** 2026-04-12 13:14
**Priority:** high
**Depends On:** Task 4.2, Task 4.3, Task 4.5

**Description:**
Convertir `UserViewModel` a `@HiltViewModel`, eliminar `FakeUserRepository` como dependencia directa, y usar `SavedStateHandle` para `clientId`.

**Technical Requirements:**
Nueva firma:
```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getClientByIdUseCase: GetClientByIdUseCase,
    private val userDataRepository: UserDataRepository,
    private val qrGenerator: QrGenerator
) : ViewModel() {
    private val clientId: Int = savedStateHandle["clientId"] ?: 0
    ...
}
```

- Reemplazar `fakeRepository.membershipPlans` por `userDataRepository.getMembershipPlans()` (llamada en coroutine).
- Usar `MemberProfileMapper.toMemberProfile()` (de Task 4.2) en lugar de la función inline.
- Eliminar import de `FakeUserRepository`.

**Acceptance Criteria:**
- [ ] `UserViewModel` no importa `FakeUserRepository`.
- [ ] `UserViewModel` no contiene `Client.toMemberProfile()`.
- [ ] Home del miembro carga perfil y QR correctamente.
- [ ] Planes de membresía siguen apareciendo.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/users/presentation/viewmodels/UserViewModel.kt`

---

### Task 4.7: Refactorizar UserLoginViewModel a @HiltViewModel + endpoint real + biometría
**Status:** ✅ completed
**Started:** 2026-04-12 13:09
**Completed:** 2026-04-12 13:14
**Priority:** high
**Depends On:** Task 4.4, Task 4.5

**Description:**
Reemplazar completamente la lógica de matching de email por autenticación real contra el endpoint, e implementar el flujo de biometría idéntico al admin.

**Technical Requirements:**
Nueva firma:
```kotlin
@HiltViewModel
class UserLoginViewModel @Inject constructor(
    private val loginMemberUseCase: LoginMemberUseCase,
    private val loginMemberWithBiometricUseCase: LoginMemberWithBiometricUseCase,
    private val enableMemberBiometricUseCase: EnableMemberBiometricUseCase,
    private val hasMemberBiometricSessionUseCase: HasMemberBiometricSessionUseCase,
    private val biometricAuthManager: BiometricAuthManager
) : ViewModel()
```

Actualizar `UserLoginUiState` para incluir:
```kotlin
data class UserLoginUiState(
    val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val error: String? = null,
    val loggedClientId: Int? = null,  // mantenido para la navegación
    val showBiometricButton: Boolean = false,
    val biometricLoginInProgress: Boolean = false,
    val showEnableBiometricDialog: Boolean = false,
    val lastLoggedEmail: String = ""
)
```

Flujo `login()`:
1. Llamar `loginMemberUseCase(email, password)`.
2. En éxito: verificar si ofrecer biometría (igual que admin).
3. En error: mostrar mensaje.

Funciones adicionales: `loginWithBiometric(activity)`, `confirmEnableBiometric()`, `skipEnableBiometric()`, `checkBiometricAvailability()` — idénticas al `LoginViewModel` admin.

Eliminar: `cachedClients`, `fetchClients()`, `retryLoginAfterFetch()`.

**Acceptance Criteria:**
- [ ] Login de miembro usa `POST /api/v1/auth/login` (mismo endpoint que admin).
- [ ] Si las credenciales son válidas, se guarda sesión en Room.
- [ ] Botón de biometría aparece tras primer login exitoso.
- [ ] Login biométrico offline funciona para miembro.
- [ ] `UserLoginViewModel` no tiene referencias a `GetClientsUsecase`.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/users/presentation/viewmodels/UserLoginViewModel.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/users/presentation/screens/UserLoginUiState.kt` (si existe por separado)

---

### Task 4.8: Actualizar UserLoginScreen con botón biométrico
**Status:** ✅ completed
**Started:** 2026-04-12 13:14
**Completed:** 2026-04-12 13:18
**Priority:** high
**Depends On:** Task 4.7

**Description:**
Agregar el botón de biometría y el diálogo de activación a la pantalla de login de miembro, replicando exactamente el diseño de `LoginScreen` (admin).

**Technical Requirements:**
En `UserLoginScreen.kt`:
- Cambiar firma: `viewModel: UserLoginViewModel = hiltViewModel()`
- Agregar `val activity = LocalContext.current as? FragmentActivity`
- Agregar botón biométrico visible cuando `uiState.showBiometricButton == true` (mismo diseño con `Icons.Filled.Fingerprint`).
- Agregar `EnableBiometricDialog` cuando `uiState.showEnableBiometricDialog == true` (reusar de `BiometricDialogs.kt`).
- Agregar `LaunchedEffect(uiState.loggedClientId)` para navegar cuando `loggedClientId != null`.

**Acceptance Criteria:**
- [ ] La pantalla de login de miembro tiene botón de huella visible tras sesión guardada.
- [ ] El diálogo de activación de biometría aparece tras primer login exitoso.
- [ ] La navegación a `UserHome` ocurre tras login exitoso (igual que antes).

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/users/presentation/screens/UserLoginScreen.kt`

---

### Task 4.9: Migrar UserHomeScreen y MembershipPlansScreen a hiltViewModel()
**Status:** ✅ completed
**Started:** 2026-04-12 13:14
**Completed:** 2026-04-12 13:18
**Priority:** medium
**Depends On:** Task 4.6

**Description:**
Reemplazar `factory: UserViewModelFactory` en las dos pantallas por `hiltViewModel()`.

**Technical Requirements:**
`UserHomeScreen.kt`: `viewModel: UserViewModel = hiltViewModel()`
`MembershipPlansScreen.kt`: `viewModel: UserViewModel = hiltViewModel()`

Eliminar imports de `UserViewModelFactory` en ambas.

**Acceptance Criteria:**
- [ ] Ambas pantallas compilan sin factories.
- [ ] Home del miembro y planes de membresía funcionan.

**Files to Modify:**
- `UserHomeScreen.kt`
- `MembershipPlansScreen.kt`

---

### Task 4.10: Actualizar Navigation.kt — eliminar UserModule
**Status:** ✅ completed
**Started:** 2026-04-12 13:18
**Completed:** 2026-04-12 13:22
**Priority:** high
**Depends On:** Task 4.8, Task 4.9

**Description:**
`AppNavigation` ya no necesita `UserModule`.

**Technical Requirements:**
En `core/Navigation/Navigation.kt`:
- Eliminar: `val userModule = remember { UserModule(appContainer) }`
- Eliminar los parámetros `factory = userModule.xxx()` de `UserLoginScreen`, `UserHomeScreen`, `MembershipPlansScreen`.
- Eliminar imports de `UserModule`.

**Acceptance Criteria:**
- [ ] `Navigation.kt` no importa `UserModule`.
- [ ] Flujo de login de miembro, home y planes funcionan.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/core/Navigation/Navigation.kt`

---

### Task 4.11: Eliminar factories manuales y UserModule
**Status:** ✅ completed
**Started:** 2026-04-12 13:22
**Completed:** 2026-04-12 13:24
**Priority:** medium
**Depends On:** Task 4.10

**Description:**
Eliminar los 3 archivos legacy del feature `users`.

**Technical Requirements:**
Eliminar:
1. `features/users/presentation/viewmodels/UserLoginViewModelFactory.kt`
2. `features/users/presentation/viewmodels/UserViewModelFactory.kt`
3. `features/users/di/UserModule.kt`

**Acceptance Criteria:**
- [ ] Los 3 archivos eliminados.
- [ ] El proyecto compila.

**Files to Delete:**
- `UserLoginViewModelFactory.kt`
- `UserViewModelFactory.kt`
- `features/users/di/UserModule.kt`

---

## Phase 5: Feature `admin` — Integración AdminNavGraph + eliminar appContainer

### Task 5.1: Crear GetAttendanceSummaryUseCase
**Status:** ✅ completed
**Started:** 2026-04-12 14:00
**Completed:** 2026-04-12 14:05
**Priority:** medium
**Depends On:** none

**Description:**
Mover los cálculos de agregación de asistencia (hoy/semana/mes) del `DashboardViewModel` a un use case.

**Technical Requirements:**
Crear `features/admin/domain/usecases/GetAttendanceSummaryUseCase.kt`:

```kotlin
class GetAttendanceSummaryUseCase @Inject constructor() {

    data class AttendanceSummary(
        val totalHoy: Int,
        val totalSemana: Int,
        val totalMes: Int,
        val asistenciasPorDia: List<AttendanceByDate>,
        val asistenciasRecientes: List<AttendanceEntity>
    )

    operator fun invoke(
        todas: List<AttendanceEntity>,
        porDia: List<AttendanceByDate>
    ): AttendanceSummary {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hoy = formatter.format(Calendar.getInstance().time)
        val inicioSemana = getStartOfWeek(formatter)
        val inicioMes = getStartOfMonth(formatter)
        val ultimos30Dias = getLast30Days(formatter)

        return AttendanceSummary(
            totalHoy = todas.count { it.fecha == hoy },
            totalSemana = todas.count { it.fecha >= inicioSemana && it.fecha <= hoy },
            totalMes = todas.count { it.fecha >= inicioMes && it.fecha <= hoy },
            asistenciasPorDia = porDia.filter { it.fecha >= ultimos30Dias },
            asistenciasRecientes = todas.take(10)
        )
    }
    // Métodos privados de fecha aquí
}
```

**Acceptance Criteria:**
- [ ] `GetAttendanceSummaryUseCase` compila.
- [ ] Recibe las listas y devuelve `AttendanceSummary` con todos los campos.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/admin/domain/usecases/GetAttendanceSummaryUseCase.kt`

---

### Task 5.2: Refactorizar DashboardViewModel para usar GetAttendanceSummaryUseCase
**Status:** ✅ completed
**Started:** 2026-04-12 14:06
**Completed:** 2026-04-12 14:10
**Priority:** medium
**Depends On:** Task 5.1

**Description:**
Delegar los cálculos al use case, manteniendo el `combine` de flows existente.

**Technical Requirements:**
- Agregar `private val getAttendanceSummaryUseCase: GetAttendanceSummaryUseCase` al constructor `@Inject`.
- En el `combine`, reemplazar los cálculos inline:
  ```kotlin
  combine(...) { todas, porDia ->
      val summary = getAttendanceSummaryUseCase(todas, porDia)
      DashboardUiState(
          totalHoy = summary.totalHoy,
          totalSemana = summary.totalSemana,
          totalMes = summary.totalMes,
          asistenciasPorDia = summary.asistenciasPorDia,
          asistenciasRecientes = summary.asistenciasRecientes,
          isLoading = false
      )
  }
  ```
- Eliminar los métodos privados `getStartOfWeek`, `getStartOfMonth`, `getLast30Days` del ViewModel.

**Acceptance Criteria:**
- [ ] `DashboardViewModel` no contiene cálculos de fecha.
- [ ] El dashboard sigue mostrando totales correctos.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/admin/presentation/viewmodels/DashboardViewModel.kt`

---

### Task 5.3: Agregar ruta AdminHome a Screen y conectar AdminNavGraph
**Status:** ✅ completed
**Started:** 2026-04-12 14:00
**Completed:** 2026-04-12 14:05
**Priority:** high
**Depends On:** Task 3.9, Task 4.10

**Description:**
Conectar `AdminNavGraph` al flujo principal de navegación. Tras login admin, el usuario entra al `AdminNavGraph` con bottom nav (Dashboard, Scanner, Clients) en lugar de ir directo a `ClientsScreen`.

**Technical Requirements:**
En `core/Navigation/Navigation.kt`:

1. Agregar a `sealed class Screen`:
   ```kotlin
   object AdminHome : Screen("admin_home")
   ```

2. Cambiar la ruta de Login — en `onLoginSuccess`:
   ```kotlin
   navController.navigate(Screen.AdminHome.route) {
       popUpTo(Screen.RoleSelection.route) { inclusive = false }
   }
   ```

3. Agregar composable para `AdminHome`:
   ```kotlin
   composable(Screen.AdminHome.route) {
       AdminNavGraph(
           appContainer = appContainer,
           onLogout = {
               navController.navigate(Screen.RoleSelection.route) {
                   popUpTo(0) { inclusive = true }
               }
           }
       )
   }
   ```

4. Eliminar las rutas directas de `Screen.Clients`, `Screen.CreateUser`, `Screen.EditClient` del `NavHost` principal — ahora viven dentro de `AdminNavGraph`.

**Nota:** `appContainer` se mantiene en `AdminNavGraph` temporalmente — se eliminará en Task 5.5.

**Acceptance Criteria:**
- [ ] Tras login admin, aparece la bottom nav bar con Dashboard, Scanner, Clients.
- [ ] Dashboard y Scanner son accesibles.
- [ ] CRUD de clientes funciona desde el NavGraph admin.
- [ ] Logout admin regresa a `RoleSelectionScreen`.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/core/Navigation/Navigation.kt`

---

### Task 5.4: Limpiar AdminNavGraph — eliminar appContainer
**Status:** ✅ completed
**Started:** 2026-04-12 14:06
**Completed:** 2026-04-12 14:10
**Priority:** high
**Depends On:** Task 5.3, Task 3.8

**Description:**
`AdminNavGraph` ya no necesita `appContainer` ni `ClientsModule` porque las pantallas de clients usan `hiltViewModel()` (completado en Task 3.8).

**Technical Requirements:**
En `features/admin/presentation/AdminNavGraph.kt`:
- Eliminar parámetro `appContainer: appContainer` de la firma.
- Eliminar `val clientsModule = remember { ClientsModule(appContainer) }`.
- Las llamadas a `ClientsScreen`, `CreateUserScreen`, `EditClientScreen` ya no pasan `factory`.
- Eliminar imports de `appContainer`, `ClientsModule`.

En `core/Navigation/Navigation.kt`:
- Actualizar la llamada a `AdminNavGraph(onLogout = ...)` — ya no pasa `appContainer`.

**Acceptance Criteria:**
- [ ] `AdminNavGraph` no importa `appContainer` ni `ClientsModule`.
- [ ] Todo el flujo admin sigue funcionando.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/admin/presentation/AdminNavGraph.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/core/Navigation/Navigation.kt`

---

### Task 5.5: Eliminar appContainer de Navigation.kt y MainActivity
**Status:** ✅ completed
**Started:** 2026-04-12 14:11
**Completed:** 2026-04-12 14:14
**Priority:** high
**Depends On:** Task 5.4

**Description:**
Con todos los features migrados a Hilt, `AppNavigation` ya no necesita `appContainer`. Eliminar la instanciación manual del contenedor de DI legacy.

**Technical Requirements:**

En `core/Navigation/Navigation.kt`:
- Eliminar parámetro `appContainer: appContainer` de `AppNavigation`.
- Verificar que no quedan usos de `appContainer` en el archivo.

En `MainActivity.kt`:
- Eliminar `lateinit var appContainer: appContainer`
- Eliminar `appContainer = appContainer(this)` en `onCreate`
- Cambiar `AppNavigation(appContainer = appContainer)` por `AppNavigation()`

**Acceptance Criteria:**
- [ ] `MainActivity` no instancia `appContainer`.
- [ ] `AppNavigation` no recibe `appContainer` como parámetro.
- [ ] La app arranca correctamente.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/core/Navigation/Navigation.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/MainActivity.kt`

---

### Task 5.6: Eliminar appContainer.kt
**Status:** ✅ completed
**Started:** 2026-04-12 14:14
**Completed:** 2026-04-12 14:15
**Priority:** high
**Depends On:** Task 5.5

**Description:**
Eliminar el archivo final del DI manual legacy. Verificar exhaustivamente que no hay referencias.

**Technical Requirements:**
- Buscar en todo el proyecto referencias a `appContainer` (clase, no variable).
- Si existe alguna referencia, resolverla antes de eliminar.
- Eliminar `core/di/appContainer.kt`.

**Acceptance Criteria:**
- [ ] `appContainer.kt` no existe.
- [ ] El proyecto compila sin errores.
- [ ] La app funciona completamente en modo Hilt puro.

**Files to Delete:**
- `app/src/main/java/com/AppexSolutions/gymsync/core/di/appContainer.kt`

---

## Phase 6: Navegación — Type-Safe Routes

### Task 6.1: Crear AppRoutes.kt con rutas @Serializable
**Status:** ✅ completed
**Started:** 2026-04-12 15:00
**Completed:** 2026-04-12 15:03
**Priority:** medium
**Depends On:** Task 1.1, Task 5.5

**Description:**
Definir todos los objetos de ruta tipados para el NavHost principal.

**Technical Requirements:**
Crear `core/navigation/AppRoutes.kt`:

```kotlin
package com.AppexSolutions.gymsync.core.navigation

import kotlinx.serialization.Serializable

@Serializable object RoleSelection
@Serializable object Login
@Serializable object UserLogin
@Serializable object AdminHome
@Serializable data class UserHome(val clientId: Int)
@Serializable data class MembershipPlans(val clientId: Int)
@Serializable object Home
@Serializable object Profile
```

**Acceptance Criteria:**
- [ ] `AppRoutes.kt` existe con los 8 objetos de ruta.
- [ ] El archivo compila sin errores.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/core/navigation/AppRoutes.kt`

---

### Task 6.2: Crear AdminRoutes.kt con rutas @Serializable
**Status:** ✅ completed
**Started:** 2026-04-12 15:00
**Completed:** 2026-04-12 15:03
**Priority:** medium
**Depends On:** Task 1.1

**Description:**
Definir los objetos de ruta tipados para el NavGraph del admin.

**Technical Requirements:**
Crear `features/admin/navigation/AdminRoutes.kt`:

```kotlin
package com.AppexSolutions.gymsync.features.admin.navigation

import kotlinx.serialization.Serializable

@Serializable object AdminDashboard
@Serializable object AdminScanner
@Serializable object AdminClients
@Serializable object AdminCreateUser
@Serializable data class AdminEditClient(val clientId: Int)
```

**Acceptance Criteria:**
- [ ] `AdminRoutes.kt` existe con los 5 objetos de ruta.
- [ ] Compila sin errores.

**Files to Create:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/admin/navigation/AdminRoutes.kt`

---

### Task 6.3: Migrar AppNavigation a type-safe routes
**Status:** ✅ completed
**Started:** 2026-04-12 15:04
**Completed:** 2026-04-12 15:10
**Priority:** medium
**Depends On:** Task 6.1, Task 5.5

**Description:**
Reemplazar todas las string routes en `Navigation.kt` por los objetos `@Serializable` de `AppRoutes`.

**Technical Requirements:**
En `core/Navigation/Navigation.kt`:

- **Eliminar** `sealed class Screen` completa.
- **Reemplazar** `NavHost(startDestination = Screen.RoleSelection.route)` por `NavHost(startDestination = RoleSelection)`.
- **Reemplazar cada `composable(Screen.X.route)`** por `composable<AppRoutes.X>`.
- **Rutas con argumentos** (`UserHome`, `MembershipPlans`):
  - Ya no necesitan `navArgument("clientId") { type = NavType.IntType }`.
  - `backStackEntry.arguments?.getInt("clientId")` → `backStackEntry.toRoute<AppRoutes.UserHome>().clientId`.
- **Navegación**: `navController.navigate(Screen.X.route)` → `navController.navigate(AppRoutes.X)`.
- **popUpTo**: `popUpTo(Screen.X.route)` → `popUpTo<AppRoutes.X>`.
- Eliminar imports de `NavType`, `navArgument` si ya no se usan.

**Acceptance Criteria:**
- [ ] `sealed class Screen` eliminada.
- [ ] No hay strings de ruta hardcodeados en el archivo.
- [ ] Toda la navegación principal funciona igual.
- [ ] `clientId` se obtiene via `toRoute<>()`.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/core/Navigation/Navigation.kt`

---

### Task 6.4: Migrar AdminNavGraph a type-safe routes
**Status:** ✅ completed
**Started:** 2026-04-12 15:04
**Completed:** 2026-04-12 15:10
**Priority:** medium
**Depends On:** Task 6.2, Task 5.4

**Description:**
Reemplazar el `private object AdminRoutes` de strings por los objetos `@Serializable` de `AdminRoutes.kt`.

**Technical Requirements:**
En `features/admin/presentation/AdminNavGraph.kt`:

- **Eliminar** `private object AdminRoutes { const val ... }`.
- **Reemplazar** cada `composable(AdminRoutes.X)` por `composable<AdminNavRoutes.X>` (usar alias para evitar conflicto de nombres con el `private object` eliminado).
- **`AdminEditClient`**: `backStackEntry.arguments?.getString("clientId")?.toIntOrNull()` → `backStackEntry.toRoute<AdminNavRoutes.AdminEditClient>().clientId`.
- Navegación: `navController.navigate(AdminRoutes.X)` → `navController.navigate(AdminNavRoutes.X)`.
- `popUpTo(AdminRoutes.DASHBOARD)` → `popUpTo<AdminNavRoutes.AdminDashboard>`.
- `startDestination = AdminRoutes.DASHBOARD` → `startDestination = AdminNavRoutes.AdminDashboard`.
- Actualizar el cálculo de `selectedTab`:
  ```kotlin
  val selectedTab = when {
      currentBackStack?.destination?.hasRoute<AdminNavRoutes.AdminDashboard>() == true -> AdminTab.Dashboard.index
      currentBackStack?.destination?.hasRoute<AdminNavRoutes.AdminScanner>() == true -> AdminTab.Scanner.index
      currentBackStack?.destination?.hasRoute<AdminNavRoutes.AdminClients>() == true -> AdminTab.Clients.index
      else -> AdminTab.Dashboard.index
  }
  ```

**Acceptance Criteria:**
- [ ] `private object AdminRoutes` eliminado.
- [ ] No hay strings de ruta en `AdminNavGraph`.
- [ ] Tab selection del bottom nav funciona correctamente.
- [ ] El `clientId` en `AdminEditClient` se obtiene via `toRoute<>()`.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/admin/presentation/AdminNavGraph.kt`

---

### Task 6.5: Reemplazar patrón shouldRefresh por LaunchedEffect en ClientsScreen
**Status:** ✅ completed
**Started:** 2026-04-12 15:11
**Completed:** 2026-04-12 15:14
**Priority:** low
**Depends On:** Task 6.4

**Description:**
El patrón `shouldRefresh = currentRoute == ...` es frágil (comparación de strings) y ya no funciona con type-safe routes. Reemplazar por un `LaunchedEffect` que siempre refresca al entrar.

**Technical Requirements:**
En `AdminNavGraph.kt`:
- Eliminar `shouldRefresh = currentRoute == AdminNavRoutes.AdminClients` de `ClientsScreen(...)`.

En `ClientsScreen.kt`:
- Eliminar el parámetro `shouldRefresh: Boolean` de la firma.
- Agregar al inicio del composable:
  ```kotlin
  LaunchedEffect(Unit) {
      viewModel.refresh()
  }
  ```
- Eliminar cualquier lógica que usara `shouldRefresh`.

En `Navigation.kt` (si aún había `shouldRefresh` en la ruta `Screen.Clients`):
- Eliminar también.

**Acceptance Criteria:**
- [ ] `ClientsScreen` no tiene parámetro `shouldRefresh`.
- [ ] La lista de clientes siempre carga datos frescos al entrar.
- [ ] No hay comparaciones de strings de ruta para determinar refresh.

**Files to Modify:**
- `app/src/main/java/com/AppexSolutions/gymsync/features/clients/presentation/screens/ClientsScreen.kt`
- `app/src/main/java/com/AppexSolutions/gymsync/features/admin/presentation/AdminNavGraph.kt`

---

## Parallelization Strategy

### Parallel Group A — Independientes desde el inicio (sin dependencias entre sí)
- **Task 1.1** (Gradle serialization plugin)
- **Task 2.1** (Verificar GymViewModel uso)
- **Task 3.1** (DateUtils)
- **Task 3.2** (GetAllClientPhotosUseCase)
- **Task 4.1** (UserDataRepository interfaz)
- **Task 4.4** (Use cases auth de miembro)
- **Task 5.1** (GetAttendanceSummaryUseCase)

### Parallel Group B — Tras Parallel Group A
- **Task 2.2** + **Task 2.3** (extraer dialogs — paralelas entre sí)
- **Task 3.3** (ClientsHiltModule) — tras Task 3.2
- **Task 4.2** (MemberProfileMapper) — tras Task 4.1
- **Task 4.3** (FakeUserDataRepository) — tras Task 4.1

### Parallel Group C — Fase auth concluye, clients y users en paralelo
Tras Phases 2 y 3 iniciales:
- **Tasks 3.4-3.10** (clients completo)
- **Tasks 4.5-4.11** (users completo)
Estas dos cadenas pueden ejecutarse en paralelo entre sí.

### Sequential Dependencies críticas

```
Task 1.1 → Tasks 6.1, 6.2 (serialization antes de type-safe routes)

Task 2.1 → Task 2.2 → Task 2.4 → Task 2.5 (verificar → extraer → limpiar → eliminar)

Task 3.2 → Task 3.3 → Task 3.4 → Task 3.8 → Task 3.9 → Task 3.10
(use case → módulo → VM → screens → nav → eliminar)

Task 4.1 → Task 4.3 → Task 4.5 → Task 4.6, 4.7 → Task 4.8 → Task 4.9 → Task 4.10 → Task 4.11

Tasks 3.9 + 4.10 → Task 5.3 (Navigation limpia antes de agregar AdminHome)
Task 5.3 → Task 5.4 → Task 5.5 → Task 5.6 (integración y limpieza appContainer)

Task 1.1 + Task 5.5 → Task 6.1, 6.2 → Task 6.3, 6.4 → Task 6.5
```

### Orden de inicio recomendado para una sesión de implementación

1. **Task 1.1** — Gradle setup (prerequisito global)
2. **Task 2.1** — Verificar antes de tocar auth
3. Phases 2 y 3 en paralelo si hay capacidad
4. Phase 4 tras confirmar Phase 3 estable
5. Phase 5 tras confirmar Phase 4 estable
6. Phase 6 al final (type-safe routes)
