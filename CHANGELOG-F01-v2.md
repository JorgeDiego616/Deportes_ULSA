# F01 - Implementación de Autenticación Real y Gestión de Sesión

## Resumen de Cambios

Implementación completa del requisito funcional F01: autenticación y gestión de sesión en la aplicación Android Deportes ULSA usando GraphQL gateway.

---

## Archivos Creados

### 1. `app/src/main/java/com/ulsa/deportes/ui/auth/domain/AuthRepository.kt`
Interfaz que desacopla la lógica de autenticación del ViewModel.

```kotlin
interface AuthRepository {
    data class LoginResult(val token: String, val user: UserDto)
    data class RegisterResult(val token: String, val user: UserDto)
    
    sealed class AuthError : Exception() { ... }
    
    suspend fun login(email: String, password: String): Result<LoginResult>
    suspend fun register(matricula: String, nombre: String, email: String, password: String): Result<RegisterResult>
    val isLoggedIn: Flow<Boolean>
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    suspend fun logout()
}
```

### 2. `app/src/main/java/com/ulsa/deportes/ui/auth/domain/AuthRepositoryImpl.kt`
Implementación concreta que usa el cliente GraphQL existente.

- Mutations `login` y `registrarUsuario` con variables tipadas
- Manejo de errores GraphQL (HTTP 200 con `errors` en body)
- Mapeo de errores del servidor a `AuthError` tipados
- Parseo de errores por campo para registro
- Persistencia de sesión vía `SessionPreferences`

### 3. `app/src/main/java/com/ulsa/deportes/ui/auth/viewmodel/RegisterViewModel.kt`
ViewModel para registro de usuarios.

- Validación de campos (matrícula, nombre, email, password)
- `StateFlow<RegisterUiState>` con errores por campo
- Limpieza de errores al escribir en campos
- Inyección de `AuthRepository`

### 4. `app/src/main/java/com/ulsa/deportes/ui/auth/view/RegisterScreenView.kt`
Pantalla Compose para registro.

- 4 campos: Matrícula, Nombre, Email, Contraseña
- Visualización de errores por campo (`isError`, colores rojos)
- Error general para errores no asociables
- Navegación a Login

### 5. `app/src/test/java/com/ulsa/deportes/ui/auth/domain/FakeAuthRepository.kt`
Fake para testing unitario de `LoginViewModel`.

---

## Archivos Modificados

### 1. `app/src/main/java/com/ulsa/deportes/ui/auth/viewmodel/LoginViewModel.kt`
**Antes:** Llamaba directo a `AuthRetrofitClient.authService`
**Después:** Depende de `AuthRepository` inyectado

```kotlin
class LoginViewModel(
    application: Application,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {
    fun login(email: String, password: String) {
        // Validación → Repository → Estado
    }
}
```

### 2. `app/src/main/java/com/ulsa/deportes/ui/auth/view/LoginScreenView.kt`
- Agregado link "¿No tienes cuenta? Registrarse"
- Navegación a pantalla de registro
- `clickable` en Text para navegación

### 3. `app/src/main/java/com/ulsa/deportes/ui/auth/data/SessionPreferences.kt`
**Nuevo método:** `isSessionValid(): Boolean`

```kotlin
fun isSessionValid(): Boolean {
    val token = refreshToken() ?: return false
    val payload = decodeJwtPayload(token)
    val exp = payload.getLong("exp")
    val now = System.currentTimeMillis() / 1000
    return exp > now
}

private fun decodeJwtPayload(token: String): JSONObject {
    val parts = token.split("\\.").dropLastWhile { it.isEmpty() }.toTypedArray()
    val decoded = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP)
    return JSONObject(String(decoded))
}
```

### 4. `app/src/main/java/com/ulsa/deportes/ui/auth/model/AuthModel.kt`
Agregados modelos para registro:

```kotlin
data class RegisterVariables(val matricula: String, val nombre: String, val email: String, val password: String)
data class RegisterData(val registrarUsuario: RegisterPayload)
data class RegisterPayload(val token: String, val usuario: UserDto)

data class RegisterUiState(
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val isRegistered: Boolean = false
)
```

### 5. `app/src/main/java/com/ulsa/deportes/ui/auth/network/AuthService.kt`
```kotlin
interface AuthService {
    @POST(".")
    suspend fun graphql(@Body body: GraphQLRequest): GraphQLResponse<LoginData>
    
    @POST(".")
    suspend fun graphqlRegister(@Body body: GraphQLRequest): GraphQLResponse<RegisterData>
}
```

### 6. `app/src/main/java/com/ulsa/deportes/ui/navigation/AppNavigation.kt`
**Reescritura completa del grafo de navegación:**

```kotlin
@Composable
fun AppNavigation() {
    val authRepository = remember { AuthRepositoryImpl(application) }
    
    val startDestination = remember {
        val onboardingPrefs = OnboardingPreferences(AppPreferences(context))
        val sessionPrefs = SessionPreferences(context)
        
        if (!onboardingPrefs.isOnboardingCompleted()) AppRoute.Onboarding.route
        else if (sessionPrefs.isSessionValid()) "tabs"
        else AppRoute.Login.route
    }
    
    NavHost(navController, startDestination) {
        composable(AppRoute.Onboarding.route) { ... }
        composable(AppRoute.Login.route) {
            LoginScreenView(
                viewModel = LoginViewModel.Factory(application, authRepository),
                onLoginSuccess = { navController.navigate("tabs") { popUpTo(AppRoute.Login.route) { inclusive = true } } },
                onNavigateToRegister = { navController.navigate(REGISTER_ROUTE) }
            )
        }
        composable(REGISTER_ROUTE) {
            RegisterScreenView(
                viewModel = RegisterViewModel.Factory(application, authRepository),
                onRegisterSuccess = { navController.navigate("tabs") { popUpTo(REGISTER_ROUTE) { inclusive = true }; popUpTo(AppRoute.Login.route) { inclusive = true } } },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable("tabs") { TabsScaffold(onLogout = { logoutViewModel.logout { navController.navigate(AppRoute.Login.route) { popUpTo("tabs") { inclusive = true } } } }) }
    }
}
```

**Lógica de navegación inicial:**

| Condición | Pantalla inicial |
|-----------|-----------------|
| Onboarding pendiente | Onboarding |
| Onboarding completado + JWT vigente | Tabs (Inicio) |
| Onboarding completado + sin token | Login |
| Onboarding completado + JWT expirado/inválido | Login |

---

## Flujo de Autenticación

### Login
1. Usuario ingresa email/password en `LoginScreenView`
2. `LoginViewModel.login()` valida campos no vacíos
3. `AuthRepository.login()` ejecuta mutation GraphQL
4. Si éxito: `SessionPreferences.saveSession(token, token, user)` + `_isLoggedIn = true`
5. UI observa `isLoggedIn = true` → navega a Tabs con `popUpTo(Login) { inclusive = true }`

### Registro
1. Usuario navega a Register desde Login
2. `RegisterViewModel.register()` valida todos los campos
3. `AuthRepository.register()` ejecuta mutation `registrarUsuario`
4. Errores GraphQL se parsean por campo (email, matricula, nombre, password)
5. Si éxito: guarda sesión + navega a Tabs limpiando Login y Register del back stack

### Restauración de Sesión (App Start)
1. `AppNavigation` decide `startDestination` en `remember`
2. Verifica `OnboardingPreferences.isOnboardingCompleted()`
3. Si completado: `SessionPreferences.isSessionValid()` (decodifica JWT, revisa `exp`)
3.1. Válido → "tabs"
3.2. Inválido/expirado → Login

### Logout
1. Usuario presiona "Cerrar sesión" en Perfil
2. `LogoutViewModel.logout()` → `SessionPreferences.clearSession()`
3. Navega a Login con `popUpTo("tabs") { inclusive = true }`

---

## Pruebas

```bash
./gradlew assembleDebug   # SUCCESS
./gradlew test            # SUCCESS (11 tests passing)
```

Tests existentes verificados:
- `ExampleUnitTest`
- `TeamStatsCalculatorTest` (4 tests)
- `MatchesViewModelTest` (6 tests)

---

## Dependencias Agregadas (build.gradle.kts)

```kotlin
testImplementation("org.mockito:mockito-core:5.11.0")
testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
testImplementation("androidx.test:core:1.5.0")
testImplementation("androidx.test:rules:1.5.0")
```

---

## Criterios de Aceptación Cumplidos

- [x] Login usa mutation real `login`
- [x] Registro crea cuentas mediante `registrarUsuario`
- [x] Errores de registro se muestran en campo correspondiente
- [x] LoginViewModel depende de AuthRepository (no AuthRetrofitClient)
- [x] Estado expuesto via StateFlow<LoginUiState>
- [x] Token persiste y verifica expiración al iniciar
- [x] App distingue onboarding pendiente / sesión válida / sesión ausente/expirada
- [x] Login elimina su formulario del back stack
- [x] Logout limpia sesión local y elimina pantallas protegidas del back stack
- [x] Onboarding no vuelve a mostrarse tras completarse
- [x] Tests unitarios existentes pasan