# Contexto del módulo de Códigos QR — Deportes ULSA

> Archivo pensado para reutilizarse como contexto (para otra IA o para un
> compañero que trabaja el backend). Explica qué se implementó, cómo está
> organizado, qué contrato espera el cliente y qué falta.

---

## 1. Resumen

- **App:** Android nativa, Kotlin + Jetpack Compose, dentro del repo
  `github.com/JorgeDiego616/Deportes_ULSA`.
- **Rama de trabajo:** `feat/login-log` (se eligió esta rama porque ya existía de
  un cambio anterior; contiene 2 commits).
- **Objetivo de este módulo:** un botón en la tab **Home** que lleva a una
  pantalla para **crear un código QR de evento** (modo `static_event_checkin`).
- **Estado:** implementado y **compila** (`gradlew :app:compileDebugKotlin` →
  BUILD SUCCESSFUL). Funciona hoy con una **implementación simulada (mock)**,
  porque el backend todavía no expone endpoints de QR.
- **Sin Pull Request:** la rama está subida, pero el PR se abrirá cuando el
  backend esté listo.

---

## 2. Decisiones tomadas (y por qué)

| Tema | Decisión |
|------|----------|
| Modo de creación | Solo `static_event_checkin` (QR de evento, sin expiración, sin `user_id`, sin auto-refresh). |
| Ubicación del botón | Al final de la tab Home; al presionarlo **navega** a una pantalla dedicada de QR. |
| Backend inexistente | Se implementó con **mock** (`MockQrApi`) para poder probar toda la UI y la generación real del QR. |
| Serialización | **Gson** (ya lo usa toda la app), con `@SerializedName`. Se descartó kotlinx-serialization para no agregar dependencias ni mezclar estilos. |
| Generación del QR | **ZXing** en el cliente; el token se trata como string opaco. |
| Host/puerto | Se usa el **mismo gateway que el login**: `http://10.0.2.2:4000/`. |
| Autenticación | El mismo JWT del login, como header `Authorization: Bearer <token>`, leído de `SessionPreferences`. |

---

## 3. Contexto del backend actual (MUY IMPORTANTE)

El login de la app apunta a `http://10.0.2.2:4000/` (visto desde el emulador),
que es el **gateway de Apollo Federation** del proyecto **UlsaHub**
(`.../UlsaHub/Proyecto`). En ese backend:

- **`seguridad-service`** → ya tiene `login(email, password): AuthPayload!` y
  `registrarUsuario(...)`, con JWT de 8h (`JWT_SECRET` en su `.env`). **Coincide
  con lo que espera la app.**
- **`deportes-service`** → tiene `Equipo`, `Practica` y un reporte. **No hay
  eventos, tickets, QR ni capacidad.**
- El gateway es **GraphQL**, no REST.

**Conclusión:** el backend de QR **no existe todavía**. Por eso el cliente usa
mock. Además, si el backend se implementa como GraphQL (lo más probable, para
seguir el estilo del gateway), habrá que cambiar `QrApi` al formato "sobre"
GraphQL (igual que `ui/auth/network/AuthService.kt`), no a rutas REST.

Pregunta a resolver con quien haga el backend: **¿va a exponer REST o GraphQL?**

---

## 4. Estructura de archivos agregada

Paquete base: `com.ulsa.deportes.ui.qrSection.qrHome`

```
ui/qrSection/qrHome/
├── model/QrModels.kt          # Data classes (@SerializedName), enums y DemoQrEvent
├── network/QrApi.kt           # Interfaz Retrofit + QrApiClient (base :4000 + Bearer)
├── network/QrDependencies.kt  # Interruptor USE_MOCK -> mock o real
├── network/MockQrApi.kt       # Implementación simulada de QrApi
├── util/QrGenerator.kt        # token (String) -> Bitmap con ZXing
├── viewmodel/QrViewModel.kt   # QrUiState + QrViewModel + Factory
└── view/QrScreenView.kt       # Pantalla Compose
```

**Archivos modificados:**

- `app/build.gradle.kts` → dependencia `com.google.zxing:core:3.5.3`.
- `ui/navigation/AppNavigation.kt` → ruta interna `qr_screen` dentro de las tabs
  y navegación desde Home.
- `ui/homeSection/homeHome/view/HomeHomeView.kt` → parámetro `onNavigateToQr` y
  botón **"Crear código QR"** al final de la lista del Home.

---

## 5. Flujo actual

1. Usuario entra a la tab **Home** (usa datos de la API REST externa de Render).
2. Al final, botón **"Crear código QR"** → `navController.navigate("qr_screen")`.
3. `QrScreenView`:
   - Crea la `QrApi` con `QrDependencies.createApi(context)`.
   - Carga la capacidad del evento al abrir (`loadCapacity`).
   - Botón "Crear código QR" → `viewModel.createQr(request)`.
4. `QrViewModel` → `QrUiState`: `Idle` → `Loading` → `Success` / `Error`.
5. En `Success`, se genera el **QR real con ZXing** a partir de `qr.token`
   (en un hilo de fondo, con `produceState`) y se muestran los datos del evento.

El evento usado hoy es de demostración: `DemoQrEvent.request()` en
`model/QrModels.kt` (`eventId = "evento-demo-ulsa"`, validez de 30 días).

---

## 6. Contrato que espera el cliente (para el backend)

### `POST /qr`
Request (**snake_case**):
```json
{
  "event_id": "string",
  "creation_mode": "static_event_checkin",
  "user_id": null,
  "valid_from": "2026-10-06T00:00:00Z",
  "valid_until": "2026-11-05T00:00:00Z",
  "security_config": { "qr_ttl_seconds": null, "geofence": null }
}
```
- `creation_mode`: `dynamic_individual_pass` | `static_event_checkin`.
- `user_id`: obligatorio solo en `dynamic_individual_pass`.
- `security_config.geofence`: objeto todo-o-nada `{ "lat": Double, "lng": Double, "radius_m": Int }`.

Response:
```json
{
  "ticket_id": "string",
  "qr": { "token": "string", "refresh_at": null },
  "ticket": { "status": "active", "used_at": null, "used_at_gate": null },
  "event_summary": { "title": "...", "category": "...", "venue": "...", "starts_at": "..." },
  "user_summary": null,
  "access_rules": { "entry_gate": "...", "valid_from": "...", "valid_until": "..." }
}
```
- `ticket.status`: `active` | `used` | `cancelled` (no existe `expired`).
- `user_summary`: `null` en estático; si existe: `{ "student_name", "student_id", "faculty" }`.
- `qr.refresh_at`: segundos unix, solo en modo dinámico; `null` en estático.
- **"Expired" se calcula** en el cliente: `now > access_rules.valid_until`.

### `GET /events/{eventId}/capacity`
```json
{ "capacity_limit": 500, "spots_remaining": 137 }
```

### Auth
`Authorization: Bearer <token del login>`.

### Reglas de negocio (del documento original)
- El QR contiene **solo el token**; nombre, facultad, venue, etc. se muestran en
  la app, nunca dentro del token.
- La capacidad es del **evento**, no del ticket: endpoint aparte.
- Geofence lo valida el escáner del staff, no el cliente.

---

## 7. Cómo conectar el backend real

En `network/QrDependencies.kt`:
```kotlin
const val USE_MOCK = false
```
Eso ya construye `QrApiClient.create { SessionPreferences(ctx).accessToken() }`,
apuntando a `http://10.0.2.2:4000/` con el Bearer del login.

> Recordatorio: `false` **solo funcionará** cuando el backend exponga los
> endpoints. Y si el backend es **GraphQL**, hay que reescribir `QrApi` al
> formato GraphQL (mutation + query) en lugar de REST.

---

## 8. Pendientes / notas

- **Backend:** endpoints de QR (REST o GraphQL), firma del token (`jti`, `tid`,
  `eid`, `sub`, `mode`, `iat`, `exp`), validación atómica al escanear, tablas
  `tickets` y `access_logs`, geofence con ubicación del escáner, pantalla de
  escaneo del staff. **No implementado aún** (lo hace un compañero).
- **Modo dinámico (`dynamic_individual_pass`):** el `QrViewModel` ya tiene el
  auto-refresh preparado (usa `qr.refresh_at`), pero no se usa porque el botón
  está en modo estático. `user_id` no se guarda hoy en `SessionPreferences`
  (solo email/tokens); si se activa el dinámico, hay que guardarlo.
- **Eventos reales:** hoy se usa un evento demo. Si se quiere usar un evento real,
  hay que pasar su `event_id` a `QrScreenView` / `DemoQrEvent`.
- El **evento de QR actual no se conecta** a la data del Home (que viene de la API
  de Render); son fuentes distintas.

---

## 9. Convenciones del proyecto (para seguir el estilo)

- Patrón MVVM por sección: `ui/<seccion>Section/<seccion>Home/{model,network,view,viewmodel}`.
- Preferencias centralizadas: `common/preferences/AppPreferences` (base) y clases
  por feature (`OnboardingPreferences`, `SessionPreferences`).
- Cliente Retrofit por feature (cada `network/` tiene su objeto `...Client`).
- `android:networkSecurityConfig` ya permite cleartext a `10.0.2.2`
  (no hace falta `usesCleartextTraffic="true"` global).
- `minSdk = 26`, así que `java.time` está disponible sin desugaring.

---

## 10. Cómo continuar este trabajo (checklist)

1. Confirmar con el backend si será **REST** o **GraphQL**.
2. Si es REST: el contrato del punto 6 ya coincide; solo poner `USE_MOCK = false`
   y apuntar `BASE_URL` al host/puerto correctos.
3. Si es GraphQL: crear la mutation `crearQr` y la query de capacidad, y reescribir
   `network/QrApi.kt` con el sobre `query + variables` (modelo en
   `ui/auth/model/AuthModel.kt` como referencia).
4. Probar en el emulador con el backend levantado.
5. (Opcional) Quitar el `Log.d("apiView", "Debugear")` que quedó en
   `ui/auth/view/LoginScreenView.kt` (commit `ceb999b` de esta misma rama).

---

*Última actualización: rama `feat/login-log`, commits `ceb999b` y `b6262e2`.*
