# Estado de Implementación F01 — Autenticación y Gestión de Sesión

## ✅ Completado

### 1. AuthRepository (Interface + Implementación)
- **Archivos creados:**
  - `app/src/main/java/com/ulsa/deportes/ui/auth/repository/AuthRepository.kt`
  - `app/src/main/java/com/ulsa/deportes/ui/auth/repository/AuthRepositoryImpl.kt`
- Funcionalidad: Desacopla la lógica de autenticación del ViewModel, usa `Result` sealed class para manejar éxito, error y error de red.

### 2. Modelos de Autenticación Actualizados
- **Archivo:** `app/src/main/java/com/ulsa/deportes/ui/auth/model/AuthModel.kt`
- Añadido: `RegisterVariables`, `RegisterInput`, `RegisterData`, `RegisterPayload`, `RegisterUiState`

### 3. AuthService Genérico
- **Archivo:** `app/src/main/java/com/ulsa/deportes/ui/auth/network/AuthService.kt`
- Cambio: `graphql` ahora es genérico `<T>` para reutilizar en login y registro.

### 4. LoginViewModel Refactorizado
- **Archivo:** `app/src/main/java/com/ulsa/deportes/ui/auth/viewmodel/LoginViewModel.kt`
- Ahora usa `AuthRepository` inyectado (no `AuthRetrofitClient` directo)
- Factory para inyección de dependencias
- Previene solicitudes duplicadas
- `StateFlow<LoginUiState>` como estado observable

### 5. RegisterViewModel Nuevo
- **Archivo:** `app/src/main/java/com/ulsa/deportes/ui/auth/viewmodel/RegisterViewModel.kt`
- Validaciones de campos (matrícula, nombre, email, password)
- Manejo de errores por campo (`fieldErrors`) y error general
- Usa `AuthRepository.register()`

### 6. SessionPreferences con Validación JWT
- **Archivo:** `app/src/main/java/com/ulsa/deportes/ui/auth/data/SessionPreferences.kt`
- `isTokenValid()`: decodifica claim `exp` del JWT (base64url)
- `hasValidSession()`: verifica token + expiración, limpia si expiró
- No verifica firma criptográfica (solo expiración local)

### 7. Navegación Inicial Inteligente
- **Archivo:** `app/src/main/java/com/ulsa/deportes/ui/navigation/AppNavigation.kt`
- Lógica: Onboarding → (si completado) → Sesión válida → Tabs | Login
- Elimina `skipLoginForTesting` hardcodeado

### 8. Pantallas de UI
- **LoginScreenView.kt:** Botón "Regístrate" navega a registro
- **RegisterScreenView.kt:** Formulario completo con validaciones, errores por campo, enlace "Inicia sesión"
- **AppNavigation.kt:** Rutas `login`, `user_register`, `tabs` conectadas correctamente

### 9. LogoutViewModel (Existente, Funcional)
- **Archivo:** `app/src/main/java/com/ulsa/deportes/ui/auth/viewmodel/LogoutViewModel.kt`
- Limpia sesión local y navega a Login con `popUpTo("tabs") { inclusive = true }`

---

## ⚠️ Pendiente / Con Issues

### 1. Pruebas Unitarias (3 de 7 fallando)
**Archivo:** `app/src/androidTest/java/com/ulsa/deportes/ui/auth/viewmodel/LoginViewModelTest.kt`

**Fallos actuales:**
| Test | Error | Causa |
|------|-------|-------|
| `credencialesIncorrectas_exponeErrorComprensible_noNavegaAInicio` | `expected:<Correo o contraseña incorrectos> but was:<null>` | El mock no devuelve el error esperado |
| `clearError_limpiaMensajeDeError` | `AssertionError` | Estado no se actualiza como se espera |
| `errorDeRed_dejaDeIndicarCarga_exponeMensajeApropiado_noNavegaAInicio` | `error debe mencionar conexión` | Mensaje de error no coincide |

**Posibles causas:**
- Mockito no está mockeando correctamente `AuthRepository.login()` en androidTest
- El `runBlockingTest` (deprecated) no avanza las corrutinas correctamente en el hilo de prueba
- `viewModelScope` en AndroidViewModel usa `MainScope()` que no es el `TestDispatcher`

**Solución propuesta:**
- Migrar tests a `runTest` + `TestDispatcher` (kotlinx-coroutines-test 1.8+)
- Usar `Dispatchers.setMain(testDispatcher)` en `@Before`
- Verificar que los mocks se inyectan correctamente en el ViewModel

### 2. Compilación Principal
- ✅ `./gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL**

---

## 📋 Próximos Pasos (Para Retomar)

1. **Arreglar tests unitarios:**
   - Cambiar `runBlockingTest` → `runTest` con `TestDispatcher`
   - Configurar `Dispatchers.setMain()` / `resetMain()` en `@Before`/`@After`
   - Verificar inyección correcta de `authRepository` mockeado

2. **Verificar flujo completo en emulador:**
   - Onboarding → Login → Tabs
   - Login → Register → Login → Tabs
   - Login con token expirado → Login
   - Logout desde Profile → Login

3. **Backend (UlsaHub) - Fuera de alcance actual:**
   - Implementar `registrarUsuario` mutation en `seguridad-service`
   - Verificar contrato GraphQL coincide con `RegisterVariables`
   - Probar integración real

---

## 📁 Archivos Modificados/Creados en Esta Sesión

### Nuevos:
- `app/src/main/java/com/ulsa/deportes/ui/auth/repository/AuthRepository.kt`
- `app/src/main/java/com/ulsa/deportes/ui/auth/repository/AuthRepositoryImpl.kt`
- `app/src/main/java/com/ulsa/deportes/ui/auth/viewmodel/RegisterViewModel.kt`
- `app/src/main/java/com/ulsa/deportes/ui/auth/view/RegisterScreenView.kt`
- `app/src/androidTest/java/com/ulsa/deportes/ui/auth/viewmodel/LoginViewModelTest.kt`

### Modificados:
- `app/src/main/java/com/ulsa/deportes/ui/auth/model/AuthModel.kt`
- `app/src/main/java/com/ulsa/deportes/ui/auth/network/AuthService.kt`
- `app/src/main/java/com/ulsa/deportes/ui/auth/viewmodel/LoginViewModel.kt`
- `app/src/main/java/com/ulsa/deportes/ui/auth/data/SessionPreferences.kt`
- `app/src/main/java/com/ulsa/deportes/ui/auth/view/LoginScreenView.kt`
- `app/src/main/java/com/ulsa/deportes/ui/navigation/AppNavigation.kt`
- `app/build.gradle.kts` (dependencias de test)

---

## 🔧 Comandos Útiles

```bash
# Compilar
./gradlew :app:compileDebugKotlin --no-daemon

# Tests unitarios (requiere emulador corriendo)
./gradlew :app:connectedDebugAndroidTest --no-daemon

# Ver reporte de tests
# file:///C:/Users/jorge/Documents/ULSAapp/Deportes_ULSA/app/build/reports/androidTests/connected/debug/index.html
```

---

**Última actualización:** 20/10/2026
**Rama:** `feat/login-log` (cambios sin commitear)