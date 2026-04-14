# Especificación Técnica — Refactorización MVVM + Clean Architecture + Navigation Type-Safe

**Slug:** `refactor-mvvm-clean-arch-nav3`
**Author:** Claude Code
**Date:** 2026-04-12
**Status:** Aprobado para implementación
**Brainstorm:** `doc/specs/refactor-mvvm-clean-arch-nav3/01-brainstorm.md`

---

## Resumen ejecutivo

Refactorización incremental de GYMSYNC en 4 fases ordenadas por feature (`auth → clients → users → admin`), seguida de una fase de navegación transversal. Cada fase deja la app en estado compilable y funcional. La migración de DI es gradual — `appContainer` se elimina en la última fase.

**Hotfix ya aplicado:** `navigation-common-ktx 2.9.7` eliminado de `build.gradle.kts` (2026-04-12).

---

## Principios de implementación

- **Nunca romper funcionalidad existente** entre fases.
- **Una capa a la vez**: DI → Repository/UseCase → ViewModel → UI/Navegación.
- **Hilt es el destino**: Todo nuevo código usa `@HiltViewModel`, `@Inject`, módulos Hilt.
- **`appContainer` sobrevive** hasta que su último consumidor desaparezca (Fase 4).
- **No crear pantallas nuevas** — solo reorganizar código existente.

---

## Fase 0 — Base compartida (prerequisito de todas las fases)

### Objetivo
Agregar la dependencia de `kotlinx-serialization` necesaria para type-safe routes, y añadir el plugin al build. Sin esto, las fases posteriores de navegación no compilan.

### Cambios en `gradle/libs.versions.toml`

**Agregar:**
```
kotlinx-serialization = "2.2.20"   # misma versión que Kotlin
```
```
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

### Cambios en `build.gradle.kts` (raíz)

**Agregar en plugins (applied false):**
```
alias(libs.plugins.kotlin.serialization)
```

### Cambios en `app/build.gradle.kts`

**Agregar plugin:**
```
alias(libs.plugins.kotlin.serialization)
```

### Acceptance criteria Fase 0
- [ ] El proyecto compila sin errores tras agregar el plugin.
- [ ] No hay cambios en comportamiento de la app.

---

## Fase 1 — Feature `auth`: Consolidación y limpieza

### Objetivo
Eliminar el sistema de login legacy (`GymViewModel` + factory overload), consolidar en `LoginViewModel` (Hilt), y preparar `LoginScreen` para ser el único punto de entrada admin.

### 1.1 Archivos a ELIMINAR

| Archivo | Razón |
|---|---|
| `features/auth/presentation/viewmodels/GymViewModel.kt` | Reemplazado por `LoginViewModel` |
| `features/auth/presentation/viewmodels/GymLoginViewModelFactory.kt` | Sin consumidores tras esta fase |
| `features/auth/di/AuthModule.kt` (manual legacy) | Reemplazado por `AuthHiltModule` |
| `features/auth/di/GymModule.kt` (manual legacy) | Sin consumidores |

**Verificación antes de eliminar:** Buscar referencias de `GymViewModel`, `GymLoginViewModelFactory`, `AuthModule`, `GymModule` en todo el proyecto. Si existe algún uso activo fuera de `LoginScreens.kt`, **no eliminar** y reportar al usuario.

### 1.2 Archivos a MODIFICAR

#### `features/auth/presentation/screens/LoginScreens.kt`
- **Eliminar** el overload `LoginScreen(factory: GymLoginViewModelFactory, ...)` completo (líneas ~234-357).
- **Eliminar** los imports de `GymViewModel` y `GymLoginViewModelFactory`.
- **Mover** `EnableBiometricDialog` a un nuevo archivo `features/auth/presentation/components/BiometricDialogs.kt`.
- **Mover** `ErrorDialog` a `features/auth/presentation/components/AuthDialogs.kt`.
- El archivo `LoginScreens.kt` queda solo con la función `LoginScreen(onLoginSuccess, onForgotPasswordClick, viewModel: LoginViewModel)`.

#### `features/auth/presentation/viewmodels/LoginViewModel.kt`
- Sin cambios de lógica en esta fase.

### 1.3 Archivos a CREAR

#### `features/auth/presentation/components/AuthDialogs.kt`
- Contiene: `ErrorDialog` (movida de `LoginScreens.kt`)
- Sin cambios funcionales.

#### `features/auth/presentation/components/BiometricDialogs.kt`
- Contiene: `EnableBiometricDialog` (movida de `LoginScreens.kt`)
- Sin cambios funcionales.

### 1.4 Cambios en navegación (parcial)

#### `core/Navigation/Navigation.kt`
- La ruta `Screen.Login` ya llama a `LoginScreen()` sin factory — **no requiere cambio de lógica**, solo verificar que los imports son correctos tras eliminar los archivos legacy.

### Acceptance criteria Fase 1
- [ ] El proyecto compila sin referencias a `GymViewModel` ni `GymLoginViewModelFactory`.
- [ ] Login admin funciona exactamente igual (email + password + biometría).
- [ ] `EnableBiometricDialog` y `ErrorDialog` siguen apareciendo correctamente.
- [ ] No hay archivos legacy de auth restantes.

---

## Fase 2 — Feature `clients`: Eliminar DAO directo, migrar a Hilt

### Objetivo
Eliminar el acceso directo de `ClientsViewModel` a `ProfilePhotoDao`. Crear `GetAllClientPhotosUseCase`. Migrar todo el feature `clients` a Hilt, eliminando `ClientModule` manual y las factories.

### 2.1 Archivos a CREAR

#### `features/clients/domain/usecases/GetAllClientPhotosUseCase.kt`
- **Responsabilidad:** Cargar el mapa `clientId → localPhotoUri` para una lista de IDs.
- **Dependencias:** `ProfilePhotoDao` (inyectado vía Hilt desde `DatabaseModule`), `ProfilePhotoManager`.
- **Función principal:** `suspend operator fun invoke(clientIds: List<Int>): Map<Int, String>` — devuelve solo los IDs cuya foto existe en el filesystem.
- **Nota:** Esta clase **sí** puede depender de `ProfilePhotoDao` directamente porque vive en la capa de datos/dominio, no en presentación.

#### `features/clients/di/ClientsHiltModule.kt`
- `@Module @InstallIn(SingletonComponent::class)`
- `@Binds` para `ClientRepository → ClientsRepoImplements`
- `@Provides` para `GetAllClientPhotosUseCase` (con sus dependencias Room inyectadas desde `DatabaseModule`)
- `@Provides` para `ProfilePhotoManager` (depende de `CameraDataSource`)

### 2.2 Archivos a MODIFICAR

#### `features/clients/presentation/viewmodels/ClientsViewmodel.kt`
- **Eliminar** dependencia de `ProfilePhotoDao`.
- **Reemplazar** con `GetAllClientPhotosUseCase` inyectada.
- **Eliminar** la lógica de `java.io.File(uri).exists()` — delegada al use case.
- **Convertir** a `@HiltViewModel @Inject constructor(getClientsUsecase, getAllClientPhotosUseCase)`.
- El método `loadClientPhotos()` llama al use case en lugar de iterar el DAO.

#### `features/clients/presentation/viewmodels/CreateUserViewModel.kt`
- **Convertir** a `@HiltViewModel @Inject constructor(...)`.
- **Mover** `getCurrentIsoTimestamp()` y `formatToIsoTimestamp()` a `core/util/DateUtils.kt` (nuevo archivo de utilidades de fecha).
- La clase queda sin funciones utilitarias top-level en su archivo.

#### `features/clients/presentation/viewmodels/EditClientViewModel.kt`
- **Convertir** a `@HiltViewModel @Inject constructor(clientId: Int, ...)`.
- **Nota:** `clientId` es un parámetro de navegación — inyectarlo via `SavedStateHandle`. Cambio: `private val clientId: Int = savedStateHandle["clientId"] ?: 0`.

#### `features/clients/data/repositories/ClientsRepoImplements.kt`
- Agregar `@Inject constructor` para que Hilt pueda proveerlo.
- Sin cambios de lógica.

### 2.3 Archivos a CREAR (utilidades)

#### `core/util/DateUtils.kt`
- Contiene `getCurrentIsoTimestamp()` y `formatToIsoTimestamp()` (movidas de `CreateUserViewModel.kt`).
- Ambas funciones permanecen `@RequiresApi(Build.VERSION_CODES.O)`.

### 2.4 Archivos a ELIMINAR

| Archivo | Razón |
|---|---|
| `features/clients/presentation/viewmodels/ClientViewModelFactory.kt` | Reemplazado por Hilt |
| `features/clients/presentation/viewmodels/CreateUserViewModelFactory.kt` | Reemplazado por Hilt |
| `features/clients/presentation/viewmodels/EditClientViewModelFactory.kt` | Reemplazado por Hilt |
| `features/clients/di/ClientModule.kt` | Reemplazado por `ClientsHiltModule` |

### 2.5 Cambios en `core/Navigation/Navigation.kt`

- Eliminar `val clientsModule = remember { ClientsModule(appContainer) }`.
- Las pantallas `ClientsScreen`, `CreateUserScreen`, `EditClientScreen` ya no reciben `factory`. Usan `hiltViewModel()` internamente.
- Eliminar parámetro `factory` de las llamadas en el NavHost.

### 2.6 Cambios en pantallas (UI — sin lógica nueva)

#### `features/clients/presentation/screens/ClientsScreen.kt`
- Reemplazar `factory: ClientsViewModelFactory` por `viewModel: ClientsViewModel = hiltViewModel()`.

#### `features/clients/presentation/screens/CreateUserScreen.kt`
- Reemplazar `factory: CreateUserViewModelFactory` por `viewModel: CreateUserViewModel = hiltViewModel()`.

#### `features/clients/presentation/screens/EditClientScreen.kt`
- Reemplazar `factory: EditClientViewModelFactory` por `viewModel: EditClientViewModel = hiltViewModel()`.

### Acceptance criteria Fase 2
- [ ] `ClientsViewModel` no importa ningún DAO de Room.
- [ ] `ProfilePhotoDao` solo es referenciado desde la capa de datos y use cases.
- [ ] CRUD de clientes funciona igual: crear, editar, eliminar, toggle activo, fotos de perfil.
- [ ] Fotos de perfil se siguen cargando en la lista de clientes.
- [ ] No existen factories manuales en el feature `clients`.

---

## Fase 3 — Feature `users`: Interfaz UserDataRepository, endpoint real, biometría para miembro

### Objetivo
Crear la interfaz `UserDataRepository`, conectar `UserLoginViewModel` al endpoint real de autenticación, e implementar biometría offline para el flujo de miembro (igual que admin). Eliminar `FakeUserRepository` como dependencia directa de ViewModels.

### 3.1 Diseño de `UserDataRepository`

#### `features/users/domain/repositories/UserDataRepository.kt` (CREAR)
```
interface UserDataRepository {
    // Perfil del miembro autenticado
    suspend fun getMemberProfile(userId: Int): MemberProfile

    // Planes de membresía disponibles
    suspend fun getMembershipPlans(): List<MembershipPlan>
}
```

#### `features/users/data/FakeUserDataRepository.kt` (CREAR — wrapper de FakeUserRepository)
- Implementa `UserDataRepository`.
- Delega a `FakeUserRepository` internamente (no se elimina la lógica mock).
- `FakeUserRepository.kt` se convierte en implementación interna, no expuesta directamente.

#### `features/users/data/RemoteUserDataRepository.kt` (CREAR — stub para futura implementación real)
- Implementa `UserDataRepository`.
- Por ahora delega a `FakeUserDataRepository` o devuelve datos básicos.
- Marcado con `// TODO: conectar a endpoint real de perfiles de miembro`.

### 3.2 Autenticación de miembro — endpoint real

#### `features/users/domain/usecases/LoginMemberUseCase.kt` (CREAR)
- Llama al mismo endpoint de login que `LoginUseCase` de `auth` (reutiliza `GymSyncAPI.login()`).
- Guarda la sesión en `UserDao` (Room) igual que el flujo admin.
- Retorna `Result<AuthSession>`.

#### `features/users/domain/usecases/LoginMemberWithBiometricUseCase.kt` (CREAR)
- Equivalente a `LoginWithBiometricUseCase` del feature `auth`.
- Lee la sesión guardada en `UserDao` para el último usuario con `biometricEnabled = true` y rol de miembro.
- Lanza el prompt biométrico vía `BiometricAuthManager`.
- Retorna `Flow<BiometricLoginResult>`.

#### `features/users/domain/usecases/EnableMemberBiometricUseCase.kt` (CREAR)
- Equivalente a `EnableBiometricUseCase` del feature `auth`.
- Actualiza `biometricEnabled` en `UserDao` para el email del miembro.

#### `features/users/domain/usecases/HasMemberBiometricSessionUseCase.kt` (CREAR)
- Consulta `UserDao` para verificar si existe un usuario miembro con biometría habilitada.
- Retorna `Boolean`.

### 3.3 ViewModels a modificar/crear

#### `features/users/presentation/viewmodels/UserLoginViewModel.kt`
- **Eliminar** lógica de matching de email contra lista cacheada de clientes.
- **Eliminar** dependencia de `GetClientsUsecase`.
- **Convertir** a `@HiltViewModel @Inject constructor(loginMemberUseCase, loginMemberWithBiometricUseCase, enableMemberBiometricUseCase, hasMemberBiometricSessionUseCase, biometricAuthManager)`.
- **Nuevo UiState** `UserLoginUiState` incluye: `showBiometricButton`, `biometricLoginInProgress`, `showEnableBiometricDialog`, `lastLoggedEmail` (igual que `LoginBiometricUiState` en admin).
- **Flujo idéntico al admin:** login normal → si biometría disponible y no configurada, ofrecer activarla → navegar.

#### `features/users/presentation/viewmodels/UserViewModel.kt`
- **Eliminar** la función de extensión `Client.toMemberProfile()` (moverla a mapper).
- **Eliminar** dependencia directa de `FakeUserRepository`.
- **Convertir** a `@HiltViewModel @Inject constructor(getMemberProfileUseCase: GetClientByIdUseCase, userDataRepository: UserDataRepository, savedStateHandle: SavedStateHandle, qrGenerator: QrGenerator)`.
- `clientId` viene de `savedStateHandle["clientId"]`.
- `fakeRepository.membershipPlans` se reemplaza por `userDataRepository.getMembershipPlans()`.

### 3.4 Mapper a crear

#### `features/users/data/mapper/MemberProfileMapper.kt` (CREAR)
- Contiene `Client.toMemberProfile(): MemberProfile` (movida de `UserViewModel`).
- Los datos mock de planes/estadísticas permanecen aquí hasta que exista endpoint real.

### 3.5 DI Hilt para `users`

#### `features/users/di/UsersHiltModule.kt` (CREAR)
- `@Binds UserDataRepository → FakeUserDataRepository` (hasta que exista endpoint real).
- `@Provides QrGenerator` (singleton).
- `@Provides BiometricAuthManager` (si no está ya provisto globalmente en `AuthHiltModule`).

### 3.6 Archivos a ELIMINAR

| Archivo | Razón |
|---|---|
| `features/users/di/UserModule.kt` | Reemplazado por `UsersHiltModule` |
| `features/users/presentation/viewmodels/UserLoginViewModelFactory.kt` | Reemplazado por Hilt |
| `features/users/presentation/viewmodels/UserViewModelFactory.kt` | Reemplazado por Hilt |

### 3.7 Cambios en UI — pantallas de `users`

#### `features/users/presentation/screens/UserLoginScreen.kt`
- Reemplazar `factory: UserLoginViewModelFactory` por `viewModel: UserLoginViewModel = hiltViewModel()`.
- Agregar botón biométrico (igual que `LoginScreen` admin): visible si `uiState.showBiometricButton`.
- Agregar `EnableBiometricDialog` (reusar de `features/auth/presentation/components/BiometricDialogs.kt`).

#### `features/users/presentation/screens/UserHomeScreen.kt`
- Reemplazar `factory: UserViewModelFactory` por `viewModel: UserViewModel = hiltViewModel()`.

#### `features/users/presentation/screens/MembershipPlansScreen.kt`
- Reemplazar `factory: UserViewModelFactory` por `viewModel: UserViewModel = hiltViewModel()`.

### 3.8 Cambios en `core/Navigation/Navigation.kt`

- Eliminar `val userModule = remember { UserModule(appContainer) }`.
- Eliminar parámetro `factory` de llamadas a `UserLoginScreen`, `UserHomeScreen`, `MembershipPlansScreen`.

### Acceptance criteria Fase 3
- [ ] Login de miembro usa endpoint real (mismo que admin, `POST /auth/login`).
- [ ] Botón de biometría aparece en pantalla de login de miembro si hay sesión guardada.
- [ ] Biometría offline funciona para miembro (sin conexión a red).
- [ ] Home del miembro carga su perfil y QR correctamente.
- [ ] Planes de membresía siguen mostrándose.
- [ ] No hay factories manuales en `users`.
- [ ] `FakeUserRepository` no es referenciado directamente desde ningún ViewModel.

---

## Fase 4 — Feature `admin`: Integrar AdminNavGraph + migrar a Hilt

### Objetivo
Conectar `AdminNavGraph` al flujo principal de navegación (tras login admin), migrar las factories manuales restantes en admin a Hilt, y limpiar `appContainer`.

### 4.1 Integrar AdminNavGraph en `AppNavigation`

#### `core/Navigation/Navigation.kt`
- La ruta `Screen.Login` actualmente navega a `Screen.Clients`. **Cambiar** para navegar a una nueva ruta `Screen.AdminHome` que renderiza `AdminNavGraph`.
- Agregar a `sealed class Screen`:
  ```kotlin
  object AdminHome : Screen("admin_home")
  ```
- El composable de `Screen.AdminHome.route` renderiza:
  ```kotlin
  AdminNavGraph(
      appContainer = appContainer,  // temporal hasta fin de Fase 4
      onLogout = {
          navController.navigate(Screen.RoleSelection.route) {
              popUpTo(0) { inclusive = true }
          }
      }
  )
  ```
- **Eliminar** las rutas directas de `Screen.Clients`, `Screen.CreateUser`, `Screen.EditClient` del NavHost principal — ahora viven dentro de `AdminNavGraph`.
- **Mantener** `Screen.Home` y `Screen.Profile` como placeholders hasta que sean implementados.

#### `features/admin/presentation/AdminNavGraph.kt`
- **Eliminar** parámetro `appContainer: appContainer`.
- Reemplazar `val clientsModule = remember { ClientsModule(appContainer) }` por uso de `hiltViewModel()` (ya resuelto en Fase 2).
- Las pantallas `ClientsScreen`, `CreateUserScreen`, `EditClientScreen` ya usan `hiltViewModel()` tras Fase 2.

### 4.2 Dashboard: Mover lógica de cálculo a UseCase

#### `features/admin/domain/usecases/GetAttendanceSummaryUseCase.kt` (CREAR)
- Recibe la lista de `AttendanceEntity` y calcula: `totalHoy`, `totalSemana`, `totalMes`, `asistenciasPorDia` (últimos 30 días), `asistenciasRecientes` (top 10).
- Retorna un data class `AttendanceSummary`.

#### `features/admin/presentation/viewmodels/DashboardViewModel.kt`
- Reemplazar los cálculos inline por llamada a `GetAttendanceSummaryUseCase`.
- ViewModel queda como: recibe flows de `AttendanceRepository`, los pasa al use case, expone `DashboardUiState`.
- Ya es `@HiltViewModel` — sin cambio en DI.

### 4.3 Eliminar `appContainer` definitivo

Una vez que `AdminNavGraph` no recibe `appContainer` y todos los features usan Hilt:

#### `core/Navigation/Navigation.kt`
- Eliminar parámetro `appContainer: appContainer` de `AppNavigation`.
- Eliminar todos los `remember { XModule(appContainer) }` restantes.

#### `MainActivity.kt`
- Eliminar `lateinit var appContainer: appContainer`.
- Eliminar `appContainer = appContainer(this)`.
- Eliminar `AppNavigation(appContainer = appContainer)` → `AppNavigation()`.

#### `core/di/appContainer.kt`
- **ELIMINAR** el archivo completo.

### 4.4 Acceptance criteria Fase 4
- [ ] Tras login admin, se muestra `AdminNavGraph` con bottom nav (Dashboard, Scanner, Clients).
- [ ] Scanner y Dashboard son accesibles desde el flujo normal.
- [ ] Logout desde admin navega de vuelta a `RoleSelection`.
- [ ] `appContainer.kt` no existe en el proyecto.
- [ ] `MainActivity` no instancia ningún DI manual.
- [ ] Cálculos del dashboard (hoy/semana/mes) funcionan igual.

---

## Fase 5 — Navegación: Type-safe routes

### Objetivo
Migrar `AppNavigation` y `AdminNavGraph` de string routes a objetos `@Serializable`. Esta fase es independiente de las anteriores (puede hacerse en cualquier orden respecto a Fases 1-4, pero lógicamente al final para no mezclar dos sistemas de route).

### 5.1 Nuevo archivo de rutas

#### `core/navigation/AppRoutes.kt` (CREAR)
Contiene todos los objetos de ruta tipados:
```kotlin
@Serializable object RoleSelection
@Serializable object Login
@Serializable object UserLogin
@Serializable object AdminHome
@Serializable data class UserHome(val clientId: Int)
@Serializable data class MembershipPlans(val clientId: Int)
@Serializable object Home
@Serializable object Profile
```

#### `features/admin/navigation/AdminRoutes.kt` (CREAR)
```kotlin
@Serializable object AdminDashboard
@Serializable object AdminScanner
@Serializable object AdminClients
@Serializable object AdminCreateUser
@Serializable data class AdminEditClient(val clientId: Int)
```

### 5.2 Migración de `Navigation.kt`

- Eliminar `sealed class Screen`.
- Reemplazar cada `composable(Screen.X.route)` por `composable<AppRoutes.X>`.
- Reemplazar `navController.navigate(Screen.X.route)` por `navController.navigate(AppRoutes.X)`.
- Reemplazar `navArgument("clientId") { type = NavType.IntType }` — ya no necesario (inferido automáticamente).
- Reemplazar `backStackEntry.arguments?.getInt("clientId")` por `backStackEntry.toRoute<AppRoutes.UserHome>().clientId`.
- Eliminar `popUpTo(Screen.X.route)` → `popUpTo<AppRoutes.X>`.

### 5.3 Migración de `AdminNavGraph.kt`

- Eliminar `private object AdminRoutes { const val X = "..." }`.
- Usar `AdminRoutes.kt` (tipado).
- Mismas transformaciones que 5.2.

### 5.4 `shouldRefresh` pattern

El patrón actual `shouldRefresh = currentRoute == Screen.Clients.route` es frágil. Reemplazar por:
- `ClientsScreen` expone un método `refresh()` en su ViewModel que se llama desde `LaunchedEffect(Unit)` al entrar a la pantalla.
- Eliminar el parámetro `shouldRefresh` de `ClientsScreen`.

### Acceptance criteria Fase 5
- [ ] No existen strings de ruta hardcodeados en el código de navegación.
- [ ] `sealed class Screen` eliminado.
- [ ] `private object AdminRoutes` eliminado.
- [ ] `clientId` se extrae con `toRoute<>()`, sin `arguments?.getInt()`.
- [ ] Toda la navegación funciona igual que antes.

---

## Mapa completo de cambios por archivo

### ARCHIVOS A CREAR

| Archivo | Fase | Propósito |
|---|---|---|
| `features/auth/presentation/components/AuthDialogs.kt` | 1 | `ErrorDialog` extraído |
| `features/auth/presentation/components/BiometricDialogs.kt` | 1 | `EnableBiometricDialog` extraído |
| `features/clients/domain/usecases/GetAllClientPhotosUseCase.kt` | 2 | Elimina DAO directo en VM |
| `features/clients/di/ClientsHiltModule.kt` | 2 | DI Hilt para clients |
| `core/util/DateUtils.kt` | 2 | Utilidades de fecha extraídas de VM |
| `features/users/domain/repositories/UserDataRepository.kt` | 3 | Interfaz para datos de miembro |
| `features/users/data/FakeUserDataRepository.kt` | 3 | Implementación mock |
| `features/users/data/RemoteUserDataRepository.kt` | 3 | Stub para endpoint real |
| `features/users/data/mapper/MemberProfileMapper.kt` | 3 | Mapper extraído de VM |
| `features/users/domain/usecases/LoginMemberUseCase.kt` | 3 | Auth real para miembro |
| `features/users/domain/usecases/LoginMemberWithBiometricUseCase.kt` | 3 | Biometría para miembro |
| `features/users/domain/usecases/EnableMemberBiometricUseCase.kt` | 3 | Activar biometría miembro |
| `features/users/domain/usecases/HasMemberBiometricSessionUseCase.kt` | 3 | Check sesión biométrica miembro |
| `features/users/di/UsersHiltModule.kt` | 3 | DI Hilt para users |
| `features/admin/domain/usecases/GetAttendanceSummaryUseCase.kt` | 4 | Cálculos extraídos de DashboardVM |
| `core/navigation/AppRoutes.kt` | 5 | Rutas type-safe app |
| `features/admin/navigation/AdminRoutes.kt` | 5 | Rutas type-safe admin |

### ARCHIVOS A MODIFICAR

| Archivo | Fase | Cambio principal |
|---|---|---|
| `features/auth/presentation/screens/LoginScreens.kt` | 1 | Eliminar overload legacy, mover dialogs |
| `features/clients/presentation/viewmodels/ClientsViewmodel.kt` | 2 | @HiltViewModel, eliminar DAO directo |
| `features/clients/presentation/viewmodels/CreateUserViewModel.kt` | 2 | @HiltViewModel, mover DateUtils |
| `features/clients/presentation/viewmodels/EditClientViewModel.kt` | 2 | @HiltViewModel, SavedStateHandle |
| `features/clients/data/repositories/ClientsRepoImplements.kt` | 2 | @Inject constructor |
| `features/clients/presentation/screens/ClientsScreen.kt` | 2 | hiltViewModel() |
| `features/clients/presentation/screens/CreateUserScreen.kt` | 2 | hiltViewModel() |
| `features/clients/presentation/screens/EditClientScreen.kt` | 2 | hiltViewModel() |
| `features/users/presentation/viewmodels/UserLoginViewModel.kt` | 3 | @HiltViewModel, endpoint real, biometría |
| `features/users/presentation/viewmodels/UserViewModel.kt` | 3 | @HiltViewModel, eliminar mapper inline |
| `features/users/presentation/screens/UserLoginScreen.kt` | 3 | hiltViewModel(), botón biométrico |
| `features/users/presentation/screens/UserHomeScreen.kt` | 3 | hiltViewModel() |
| `features/users/presentation/screens/MembershipPlansScreen.kt` | 3 | hiltViewModel() |
| `features/admin/presentation/AdminNavGraph.kt` | 4 | Eliminar appContainer, hiltViewModel() |
| `features/admin/presentation/viewmodels/DashboardViewModel.kt` | 4 | Delegar cálculos a UseCase |
| `core/Navigation/Navigation.kt` | 4+5 | AdminHome route, eliminar modules, type-safe routes |
| `MainActivity.kt` | 4 | Eliminar appContainer |
| `app/build.gradle.kts` | 0 | Plugin serialization (ya hotfix aplicado) |
| `gradle/libs.versions.toml` | 0 | Plugin serialization |

### ARCHIVOS A ELIMINAR

| Archivo | Fase | Razón |
|---|---|---|
| `features/auth/presentation/viewmodels/GymViewModel.kt` | 1 | Legacy, sin uso |
| `features/auth/presentation/viewmodels/GymLoginViewModelFactory.kt` | 1 | Legacy, sin uso |
| `features/auth/di/AuthModule.kt` | 1 | Reemplazado por AuthHiltModule |
| `features/auth/di/GymModule.kt` | 1 | Sin consumidores |
| `features/clients/presentation/viewmodels/ClientViewModelFactory.kt` | 2 | Reemplazado por Hilt |
| `features/clients/presentation/viewmodels/CreateUserViewModelFactory.kt` | 2 | Reemplazado por Hilt |
| `features/clients/presentation/viewmodels/EditClientViewModelFactory.kt` | 2 | Reemplazado por Hilt |
| `features/clients/di/ClientModule.kt` | 2 | Reemplazado por ClientsHiltModule |
| `features/users/presentation/viewmodels/UserLoginViewModelFactory.kt` | 3 | Reemplazado por Hilt |
| `features/users/presentation/viewmodels/UserViewModelFactory.kt` | 3 | Reemplazado por Hilt |
| `features/users/di/UserModule.kt` | 3 | Reemplazado por UsersHiltModule |
| `core/di/appContainer.kt` | 4 | Completamente migrado a Hilt |

---

## Dependencias entre fases

```
Fase 0 (serialization plugin)
  └── Fase 5 (type-safe routes) — requiere Fase 0

Fase 1 (auth cleanup) — independiente
  └── ninguna dependencia

Fase 2 (clients → Hilt) — requiere Fase 1 completada
  └── Fase 1 debe estar completa (LoginScreen limpia)

Fase 3 (users → Hilt + biometría) — requiere Fase 2
  └── reutiliza BiometricAuthManager del módulo auth

Fase 4 (admin + eliminar appContainer) — requiere Fases 2 y 3
  └── appContainer solo puede eliminarse cuando clients Y users estén en Hilt

Fase 5 (type-safe routes) — puede ejecutarse en paralelo con Fases 1-3,
                            pero lógicamente mejor tras Fase 4
```

---

## Notas de implementación críticas

1. **`BiometricAuthManager` para miembros:** El mismo `BiometricAuthManager` del feature `auth` se reutiliza para el flujo de miembro. Debe estar en el `SingletonComponent` de Hilt (verificar que `AuthHiltModule` lo provee, o moverlo a `DatabaseModule`/`NetworkModule`).

2. **`UserDao` para sesión de miembro:** El login de miembro debe guardar la sesión en la misma tabla `users` de Room que usa el admin. El campo `rolId` distinguirá si es admin o miembro. La query `getLastBiometricUser()` en `UserDao` debe filtrar opcionalmente por rol.

3. **Endpoint de login de miembro:** El usuario confirmó que el endpoint real ya existe. Se asume que es el mismo `POST /api/v1/auth/login` que usa el admin. Si es diferente, `LoginMemberUseCase` necesita su propio endpoint en `GymSyncAPI`.

4. **`@RequiresApi(O)` en `DateUtils`:** Las funciones de fecha requieren API 26+. `minSdk = 24` — verificar que los sitios de llamada tengan la anotación o usar `Build.VERSION.SDK_INT >= O`.

5. **`shouldRefresh` eliminado:** En Fase 5, `ClientsScreen` llama a `viewModel.refresh()` en `LaunchedEffect(Unit)` en lugar de recibir `shouldRefresh` como parámetro. Esto garantiza que siempre tiene datos frescos al entrar.

6. **`appContainer` en `AdminNavGraph`:** Durante las Fases 1-3, `AdminNavGraph` sigue recibiendo `appContainer` (para `ClientsModule`). En Fase 4, cuando `ClientsScreen` ya usa `hiltViewModel()`, se elimina. No eliminar antes.
