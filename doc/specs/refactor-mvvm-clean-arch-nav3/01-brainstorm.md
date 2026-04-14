# Refactorizar GYMSYNC — MVVM + Clean Architecture + Navigation 3.0

**Slug:** `refactor-mvvm-clean-arch-nav3`
**Author:** Claude Code
**Date:** 2026-04-12
**Branch:** `preflight/refactor-mvvm-clean-arch-nav3`
**Related:** branch `issue/qr-generator2`, análisis previo en sesión

---

## 1) Intent & Assumptions

- **Task brief:** Refactorizar la app Android GYMSYNC para que cumpla MVVM, los principios SOLID, y Clean Architecture correctamente en todas las capas (presentación, dominio, datos), y migrar el sistema de navegación a Navigation 3.0, sin eliminar ninguna funcionalidad existente.

- **Assumptions:**
  - El stack tecnológico actual se mantiene: Kotlin 2.2.20, Compose, Hilt 2.56.2, Room 2.7.0, Retrofit 3.0, minSdk 24.
  - La migración es incremental por feature, no un rewrite completo de una vez.
  - "Sin eliminar funcionalidades" incluye: biometría, QR generator, scanner, dashboard, CRUD de clientes, fotos de perfil, planes de membresía, login de usuario y admin.
  - `FakeUserRepository` se mantiene como stub mientras no exista un endpoint real de autenticación de miembros.
  - La URL base del servidor (`http://98.80.61.113:3000/api/v1/`) no cambia.
  - Navigation 3.0 se refiere a `androidx.navigation3`, la nueva API Compose-first lanzada en alpha en 2025.

- **Out of scope:**
  - Activar Firebase/FCM (ya comentado, decisión pendiente del usuario).
  - Cambiar la lógica de negocio de asistencia/QR (feature en rama activa `issue/qr-generator2`).
  - Agregar nuevas pantallas o endpoints de API.
  - Migrar a multimodule Gradle.
  - Tests unitarios / instrumentados (aunque la refactorización los habilita).

---

## 2) Pre-reading Log

- `app/build.gradle.kts`: AGP 9.0, Kotlin 2.2.20, navigation-compose 2.8.5 + navigation-common-ktx 2.9.7 (versión inconsistente), Room+KSP, Hilt+kapt, CameraX, ZXing, MLKit.
- `gradle/libs.versions.toml`: Confirma la inconsistencia de Navigation. `navigation-compose = "2.8.5"` vs `navigationCommonKtx = "2.9.7"`.
- `core/Navigation/Navigation.kt`: Sistema principal de navegación con string-routes, `sealed class Screen`. `AdminNavGraph` **no está integrado** — el admin va a `ClientsScreen` directamente sin bottom nav. `ClientsModule` y `UserModule` se instancian con `remember{}` dentro del `NavHost`.
- `features/admin/presentation/AdminNavGraph.kt`: NavGraph completo con `Scaffold + AdminBottomNavBar`, rutas `admin/*`, acceso a Dashboard y Scanner. **Huérfano** — ningún punto de `AppNavigation` lo invoca.
- `core/di/appContainer.kt`: DI manual con red, Room, DataStore, fotos. Se instancia en `MainActivity.onCreate()`.
- `MainActivity.kt`: `@AndroidEntryPoint` + instancia manual de `appContainer`. Mezcla dos sistemas de DI.
- `features/auth/di/AuthHiltModule.kt`: Solo `auth` feature tiene binding Hilt correcto (`@Binds AuthRepository`).
- `features/admin/di/AdminModule.kt`: Módulo vacío. `AttendanceRepository` se inyecta vía `@Inject constructor` directamente.
- `features/clients/domain/repositories/ClientRepository.kt`: Interfaz bien definida para CRUD + roles + gyms. No incluye operaciones de fotos — están separadas en `ProfilePhotoDao`.
- `features/clients/presentation/viewmodels/ClientsViewmodel.kt`: Depende directamente de `ProfilePhotoDao` (Room DAO). Accede a `java.io.File` para verificar existencia. Viola DIP y capa de presentación.
- `features/clients/presentation/viewmodels/ClientViewModelFactory.kt`: Fábrica manual para `ClientsViewModel`, inyecta `ProfilePhotoDao` directamente.
- `features/clients/di/ClientModule.kt`: Módulo manual que une use cases + factories. No es un módulo Hilt.
- `features/users/di/UserModule.kt`: Módulo manual, instancia `FakeUserRepository` y `QrGenerator` sin interfaz.
- `features/users/presentation/viewmodels/UserViewModel.kt`: Contiene `Client.toMemberProfile()` con datos mock hardcodeados. Depende de `FakeUserRepository` sin interfaz.
- `features/users/presentation/viewmodels/UserLoginViewModel.kt`: Autenticación por matching de email en lista de clientes cacheada. Lógica de negocio completa en ViewModel.
- `features/auth/presentation/viewmodels/GymViewModel.kt`: ViewModel legacy con `isValidEmail()` hardcodeado. Usado solo por el overload factory de `LoginScreen`.
- `features/auth/presentation/screens/LoginScreens.kt`: Dos overloads completos de `LoginScreen` (Hilt + factory legacy), `EnableBiometricDialog` y `ErrorDialog` en el mismo archivo.
- `features/auth/presentation/viewmodels/LoginViewModel.kt`: Bien estructurado (Hilt, use cases, StateFlow). Accede a `BiometricAuthManager` directamente para verificar hardware — podría delegarse al use case.
- `features/admin/presentation/viewmodels/DashboardViewModel.kt`: `@HiltViewModel` correcto. Contiene lógica de cálculo de totales por día/semana/mes que debería vivir en un use case.
- `features/users/data/FakeUserRepository.kt`: Clase concreta sin interfaz. Tiene `mockClients`, `mockProfiles`, `membershipPlans`, y lógica de búsqueda. Varias responsabilidades.

---

## 3) Codebase Map

### Primary components / modules

| Módulo / Archivo | Rol actual | Problema |
|---|---|---|
| `core/Navigation/Navigation.kt` | NavHost principal, string routes | AdminNavGraph huérfano, DI en composable |
| `features/admin/presentation/AdminNavGraph.kt` | NavGraph admin con bottom nav | No integrado en flujo principal |
| `core/di/appContainer.kt` | DI manual legacy | Dios-objeto, 5+ responsabilidades |
| `features/auth/di/AuthHiltModule.kt` | Hilt binding de AuthRepository | Correcto, modelo a seguir |
| `features/clients/di/ClientModule.kt` | Factory manual de viewmodels | No es Hilt, crea use cases sin ciclo de vida |
| `features/users/di/UserModule.kt` | Factory manual para users | `FakeUserRepository` sin interfaz |
| `features/clients/presentation/viewmodels/ClientsViewmodel.kt` | Lista de clientes | Depende de DAO directamente |
| `features/users/presentation/viewmodels/UserViewModel.kt` | Home del miembro + QR | Mapper mock hardcodeado en VM |
| `features/users/presentation/viewmodels/UserLoginViewModel.kt` | Login de miembro | Auth logic en VM sin use case real |
| `features/auth/presentation/viewmodels/GymViewModel.kt` | Login admin (legacy) | `isValidEmail` en VM, duplica `LoginViewModel` |
| `features/auth/presentation/screens/LoginScreens.kt` | Pantalla de login | 2 overloads + 2 dialogs = 4 responsabilidades |
| `features/admin/presentation/viewmodels/DashboardViewModel.kt` | Dashboard de asistencia | Cálculos de negocio en VM |
| `features/users/data/FakeUserRepository.kt` | Mock de datos de miembros | Sin interfaz, múltiples responsabilidades |

### Shared dependencies

- `appContainer` — punto central de todo el DI legacy; referenciado por `Navigation.kt`, `ClientsModule`, `UserModule`.
- `GymSyncAPI` — única interfaz de red para todos los dominios.
- `AuthPreferences` (DataStore) — token + biometric flag, compartido entre auth y network.
- `ProfilePhotoDao` — usado por `ClientsViewModel` (directo) y por use cases de foto.
- `QrGenerator` — instanciado directamente en `UserModule`, sin interfaz.

### Data flow actual

```
MainActivity
  └─ appContainer (manual DI)
  └─ AppNavigation(appContainer)
       ├─ remember { ClientsModule(appContainer) }
       ├─ remember { UserModule(appContainer) }
       └─ NavHost
            ├─ RoleSelection
            ├─ Login → LoginScreen (hiltViewModel) ← Hilt correcto
            ├─ UserLogin → UserLoginScreen (factory manual)
            ├─ UserHome → UserHomeScreen (factory manual)
            ├─ MembershipPlans → MembershipPlansScreen (factory manual)
            ├─ Clients → ClientsScreen (factory manual) ← DAO directo en VM
            ├─ CreateUser → CreateUserScreen (factory manual)
            └─ EditClient → EditClientScreen (factory manual)

AdminNavGraph (HUÉRFANO — nadie lo llama)
  └─ Dashboard (hiltViewModel) ← Hilt correcto
  └─ Scanner (hiltViewModel) ← Hilt correcto
  └─ Clients/Create/Edit (factory manual)
```

### Potential blast radius

Un cambio en la navegación afecta prácticamente todo: `AppNavigation`, `AdminNavGraph`, todos los `Screen` composables que reciben lambdas de navegación, y los parámetros que hoy pasan como `Int` por string route. Orden recomendado de cambios: DI → Repositorios/UseCases → ViewModels → Navegación.

---

## 4) Root Cause Analysis

> Esta sección se adapta: no es un bug, es deuda técnica acumulada. Se mapean los orígenes de cada problema.

- **Origen 1 — Construcción incremental sin plan de migración:** `appContainer` fue el DI inicial correcto para una app pequeña. Al introducir Hilt solo para `auth`, quedaron dos sistemas paralelos sin estrategia de convergencia. Evidencia: `MainActivity` instancia `appContainer` manualmente a pesar de tener `@AndroidEntryPoint`.

- **Origen 2 — AdminNavGraph creado en aislamiento:** El `AdminNavGraph` fue construido como mejora (tiene bottom nav, rutas limpias), pero nunca se conectó a `AppNavigation`. La pantalla `Screen.Clients` sigue apuntando al `ClientsScreen` simple. Evidencia: `Navigation.kt:84` navega a `Screen.Clients.route` que lleva a `ClientsScreen` directamente.

- **Origen 3 — Ausencia de interfaz para `FakeUserRepository`:** La decisión de usar datos fake sin interfaz impidió la inversión de dependencia. `UserViewModel` y `UserModule` dependen de la clase concreta. Evidencia: `UserViewModel.kt:21`, `UserModule.kt:13`.

- **Origen 4 — Acceso directo al DAO en `ClientsViewModel`:** Las fotos de perfil tienen use cases (`SaveProfilePhotoUseCase`, `GetProfilePhotoUseCase`), pero `ClientsViewModel` accede a `ProfilePhotoDao` directamente porque las fotos se cargan en bulk (todos los clientes) y no existe un use case de "obtener todas las fotos". Se tomó un atajo. Evidencia: `ClientsViewModel.kt:15,57`.

- **Origen 5 — Versión inconsistente de Navigation:** Se declaró `navigation-common-ktx 2.9.7` (quizás por error al actualizar) mientras `navigation-compose` quedó en `2.8.5`. Son artefactos de grupos distintos pero del mismo BOM de Navigation — mezclar versiones puede causar `ClassNotFoundException` en runtime.

- **Decisión sobre causa raíz:** El problema central es la ausencia de una estrategia de DI coherente. Todo lo demás (DAO en VM, AdminNavGraph huérfano, FakeRepository sin interfaz) son consecuencias de tener `appContainer` y Hilt coexistiendo sin un plan de convergencia.

---

## 5) Research

### Sobre Navigation 3.0 (`androidx.navigation3`)

Navigation 3.0 es una **reescritura completa** lanzada en alpha en 2025. Sus diferencias fundamentales con Navigation 2.x son:

- **API completamente distinta:** En lugar de `NavHost` + `NavController`, usa `NavDisplay` + un `NavBackStack` mutable basado en `MutableList<Any>`. No hay `NavController`.
- **Composable-first por diseño:** Las rutas son objetos Kotlin serializables (`@Serializable`), igual que en Navigation 2.8 type-safe. Pero el mecanismo de renderizado es diferente: `NavEntry` con `LocalNavAnimatedContentScope`.
- **Estado visible del back stack:** El `NavBackStack` es un `SnapshotStateList<Any>` que la UI puede leer y modificar directamente — esto da más control pero requiere gestión manual del estado.
- **Sin soporte oficial de Hilt Navigation Compose todavía:** `hilt-navigation-compose` en su versión actual (`1.2.0`) no tiene integración estable con Nav 3.0 alpha. `hiltViewModel()` en destinos de Nav 3.0 requiere trabajo adicional.
- **Estado de madurez:** Alpha en 2025. No hay garantía de estabilidad de API. Cambios breaking entre alphas son frecuentes.

**Riesgo concreto para GYMSYNC:** Hilt 2.56.2 + Navigation 3.0 alpha + minSdk 24 es una combinación no probada en producción. La falta de integración estable `hiltViewModel()` en Nav 3.0 requeriría fábricas manuales para todos los `@HiltViewModel`, o añadir complejidad con `ViewModelStoreOwner` personalizado — lo cual contradice el objetivo de simplificar la arquitectura.

### Sobre Navigation 2.8.x con Type-Safe Routes

Navigation 2.8.0+ introdujo type-safe routes con `@Serializable` de kotlinx-serialization. Esto es **estable** y resuelve los principales problemas del sistema de string routes actual:

- Las rutas son data classes/objects tipados: `@Serializable data class EditClient(val clientId: Int)` en lugar de `"edit_client/{clientId}"`.
- `NavType` para argumentos se infiere automáticamente para tipos primitivos y serializables.
- Compatible 100% con `hiltViewModel()`, `NavController`, y el `NavHost` existente.
- Migración incremental: se pueden mezclar string routes y type-safe routes durante la transición.
- No requiere cambiar `hilt-navigation-compose`.

### Sobre la Inconsistencia de Versiones de Navigation

`navigation-compose 2.8.5` y `navigation-common-ktx 2.9.7` en el mismo proyecto es técnicamente inestable. `navigation-common-ktx` es una dependencia transitiva de `navigation-compose` — al declarar una versión superior explícitamente, Gradle resuelve a la mayor (2.9.7), pero el runtime de `navigation-compose 2.8.5` no fue diseñado contra `navigation-common 2.9.7`. La solución correcta es eliminar la declaración de `navigation-common-ktx` del `build.gradle.kts` y dejar que `navigation-compose` resuelva sus propias dependencias transitivas.

### Sobre la Migración de DI (appContainer → Hilt)

El patrón correcto para eliminar `appContainer` es:
1. Crear módulos Hilt para cada feature (`@Module @InstallIn(SingletonComponent::class)`).
2. Mover los `@Provides`/`@Binds` de `appContainer` a los módulos correspondientes.
3. Convertir ViewModels a `@HiltViewModel @Inject constructor(...)`.
4. Reemplazar `ViewModelProvider.Factory` + `remember { XModule(appContainer) }` en la navegación.
5. Eliminar `appContainer` y su instanciación en `MainActivity`.

El modelo ya existe en el proyecto: `AuthHiltModule` + `LoginViewModel @HiltViewModel` es el patrón correcto. Hay que replicarlo en `clients`, `users`, y `admin`.

### Sobre `FakeUserRepository`

El patrón correcto es definir una interfaz `UserProfileRepository` (en el dominio de `users`) y tener dos implementaciones:
- `FakeUserProfileRepository` (para desarrollo, sin cambios en su lógica mock actual).
- `RemoteUserProfileRepository` (cuando exista el endpoint real).

`FakeUserRepository` tiene tres responsabilidades distintas. Al crear la interfaz, la separación natural serían dos interfaces: `UserProfileRepository` y `MembershipPlanRepository`.

### Sobre `ClientsViewModel` + DAO directo

La solución es crear un use case `GetAllClientPhotosUseCase(profilePhotoDao, profilePhotoManager)` que encapsule el loop de carga de fotos. El ViewModel solo consume el use case. `ProfilePhotoDao` no escapa de la capa de datos.

### Opciones de solución para Navigation

**Opción A — Migrar a Navigation 3.0 alpha**
- Pros: Adopción de la nueva API estándar, back stack observable, más flexible.
- Contras: Alpha inestable, sin integración Hilt estable, reescritura completa de toda la navegación, incompatible con `hiltViewModel()` sin adaptadores, mayor riesgo en producción.
- Esfuerzo: **Alto** (reescritura total del sistema de navegación).

**Opción B — Navigation 2.8.x con type-safe routes (migración incremental)**
- Pros: Estable, compatible con Hilt al 100%, migración ruta por ruta, elimina los string routes frágiles, resuelve la inconsistencia de versiones.
- Contras: No es Navigation 3.0, pero es la versión estable más moderna.
- Esfuerzo: **Medio** (cambio ruta por ruta, compatible con código existente).

**Opción C — Mantener string routes 2.8.5, solo corregir inconsistencia de versiones**
- Pros: Mínimo esfuerzo en navegación, enfoca energía en MVVM/SOLID/DI.
- Contras: No mejora la fragilidad de string routes, `clientId` como string en AdminNavGraph sigue siendo un bug potencial.
- Esfuerzo: **Bajo** (solo eliminar `navigation-common-ktx` del build.gradle).

### Recomendación

**Opción B** para navegación (type-safe routes en 2.8.x, estable), combinada con migración completa a Hilt para DI e integración de `AdminNavGraph` en el flujo principal. Esto resuelve todos los problemas críticos identificados sin introducir el riesgo de una alpha API.

Si en el futuro Navigation 3.0 alcanza estabilidad y `hilt-navigation-compose` lo soporta oficialmente, la migración desde type-safe 2.8.x a Nav 3.0 es más directa que desde string routes.

---

## 6) Decisiones Tomadas (respuestas del usuario — 2026-04-12)

| # | Pregunta | Decisión |
|---|---|---|
| 1 | Nav 3.0 alpha vs 2.8.x type-safe | **Navigation 2.8.x type-safe routes (estable)** |
| 2 | appContainer: eliminar todo o gradual | **Migración gradual por feature** |
| 3 | Eliminar GymViewModel + overload legacy | **Sí, si no se usan y no afectan** |
| 4 | Integrar AdminNavGraph | **Sí, pero sin romper el flujo existente** |
| 5 | Login de usuario: endpoint real + biometría | **Usar endpoint real; mantener biometría offline para admin Y usuario** |
| 6 | FakeUserRepository: 1 o 2 interfaces | **Una sola interfaz `UserDataRepository`** |
| 7 | Orden de features | **auth → clients → users → admin** |
| 8 | Hotfix Navigation version | **Aplicado** — eliminado `navigation-common-ktx 2.9.7` de `build.gradle.kts` |

### Implicación nueva (respuesta 5)
El usuario quiere biometría offline para **ambos roles** (admin y miembro).
- Admin: ya implementado en `LoginViewModel` con `BiometricAuthManager` + `UserDao` (Room).
- Usuario/Miembro: actualmente `UserLoginViewModel` solo hace matching de email. Se necesita:
  1. Conectar a endpoint real de auth (ya existe según el usuario).
  2. Implementar `LoginWithBiometricUseCase` equivalente para el flujo de miembro.
  3. Guardar sesión biométrica en Room también para miembros.

---

## 7) Clarification (original, para referencia)

Estas son las decisiones que requieren confirmación antes de planificar la implementación:

1. **¿Navigation 3.0 alpha o Navigation 2.8.x type-safe (estable)?**
   Navigation 3.0 está en alpha y no tiene integración estable con Hilt. La recomendación técnica es usar type-safe routes en 2.8.x (estable). ¿Confirmas este cambio de enfoque, o es un requisito explícito usar Navigation 3.0 alpha aunque implique más riesgo?

2. **¿Eliminar completamente `appContainer` o mantenerlo durante la transición?**
   La eliminación completa requiere migrar todos los features a Hilt de una vez. La alternativa es migrar feature por feature, manteniendo `appContainer` hasta que todos los módulos estén migrados. ¿Cuál es el enfoque preferido?

3. **¿`GymViewModel` (login admin legacy) debe eliminarse o mantenerse?**
   `GymViewModel` + `GymLoginViewModelFactory` son el sistema de login admin "viejo" que coexiste con `LoginViewModel` (Hilt). El overload de `LoginScreen` con factory solo se usa si alguien pasa la factory manualmente — actualmente nadie lo hace en la navegación activa. ¿Se puede eliminar `GymViewModel` y el overload legacy de `LoginScreen`, consolidando en `LoginViewModel`?

4. **¿`AdminNavGraph` debe integrarse en `AppNavigation` o reemplazar el flujo actual del admin?**
   Actualmente, tras el login admin, se navega a `ClientsScreen` directamente (sin bottom nav). `AdminNavGraph` tiene bottom nav con Dashboard, Scanner y Clients. Integrarlo significaría que tras el login admin se entraría al `AdminNavGraph`. ¿Es eso el comportamiento deseado?

5. **¿El login de usuario (`UserLoginViewModel`) debe seguir autenticando por matching de email en la lista de clientes, o se va a crear un endpoint real de autenticación de miembros próximamente?**
   Si hay un endpoint real previsto, conviene crear `UserAuthUseCase` que llame a la API en lugar de hacer matching local. Si no, la refactorización puede mantener la lógica actual pero moverla a un use case.

6. **¿`FakeUserRepository` debe dividirse en interfaces separadas (`UserProfileRepository` + `MembershipPlanRepository`) o crear una sola interfaz `UserDataRepository`?**
   La separación es más limpia (ISP), pero agrega más archivos. ¿Cuál es la preferencia?

7. **¿Prioridad de features para la migración incremental?**
   Orden técnico sugerido: `auth` (ya parcialmente en Hilt) → `clients` (mayor impacto, DAO directo) → `users` (FakeRepo) → `admin` (AdminNavGraph). ¿Alguna feature tiene prioridad diferente?

8. **¿La inconsistencia de Navigation (`navigation-common-ktx 2.9.7`) debe corregirse como primer paso inmediato, independientemente del resto de la refactorización?**
   Es un cambio de 1 línea en `build.gradle.kts` que reduce el riesgo de crash en runtime. ¿Se puede aplicar como hotfix inmediato?
