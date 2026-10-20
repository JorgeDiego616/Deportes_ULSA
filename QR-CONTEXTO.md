# Contexto del módulo de Códigos QR — Deportes ULSA

> **LEER ESTE ARCHIVO AL INICIAR CUALQUIER SESIÓN** sobre este proyecto. Sirve
> como contexto para otra IA o para un compañero que trabaje el backend: explica
> qué se implementó, cómo está organizado, qué contrato espera el cliente, dónde
> está cada pieza (incluido el backend UlsaHub) y **en qué punto se dejó el
> trabajo** (sección 13).

---

## 1. Resumen

- **App:** Android nativa, Kotlin + Jetpack Compose, repo
  `github.com/JorgeDiego616/Deportes_ULSA`.
- **Rama de trabajo:** `feat/login-log` (tiene 3 commits arriba de los que se
  ramificó: log de login, módulo QR, contexto QR). **Los cambios de la última
  sesión están SIN commitear** (ver sección 13).
- **Objetivo del módulo:** una pantalla para **crear un código QR de evento**
  donde el usuario **edita los datos del pase** (portador + evento) desde un
  formulario.
- **Estado actual:** implementado y **compila** (`gradlew :app:compileDebugKotlin`
  → BUILD SUCCESSFUL). Funciona con **mock** (`MockQrApi`) que "ecoe" los datos
  del formulario, porque el backend todavía no expone endpoints de QR.
- **El botón "Crear código QR" está en la tab Profile** (antes estaba en Home).
- **Sin Pull Request:** la rama está subida, pero el PR se abrirá cuando el
  backend esté listo.

---

## 2. Decisiones tomadas (y por qué)

| Tema | Decisión |
|------|----------|
| Modo de creación | Por defecto `static_event_checkin` (QR de evento, sin expiración, sin `user_id`). `QrViewModel` ya tiene el auto-refresh listo para modo dinámico (`refresh_at`), pero no se usa hoy. |
| Ubicación del botón | Tab **Profile** (`profileHomeView`), parámetro `onNavigateToQr` → ruta interna `qr_screen`. |
| Datos editables | **Formulario** en la pantalla: nombre, matrícula, facultad (portador) + título, tipo de evento, sede (evento). Llena el `CreateQrRequest`. |
| Backend inexistente | Se implementó con **mock** (`MockQrApi`) para poder probar toda la UI y la generación real del QR (ZXing). |
| Mock y `user_summary` | El mock **siempre** devuelve `user_summary` con lo del formulario (aunque el contrato estricto de estático lo tiene `null`) para que la previsualización se vea completa. Decisión pendiente para el backend real. |
| Serialización | **Gson** (ya lo usa toda la app), con `@SerializedName` snake_case. Se descartó kotlinx-serialization para no mezclar estilos. |
| Generación del QR | **ZXing** en el cliente; el token se trata como string opaco (nunca se meten datos dentro del QR). |
| Host/puerto | Mismo gateway que el login: `http://10.0.2.2:4000/` (solo emulador). |
| Autenticación | El mismo JWT del login como header `Authorization: Bearer <token>`, leído de `SessionPreferences`. |

---

## 3. Dónde está todo (rutas IMPORTANTES)

- **App (repo):** `C:\Users\jorge\Documents\ULSAapp\Deportes_ULSA`
- **Backend UlsaHub (repo aparte, fuera de la app):**
  `C:\Users\jorge\Documents\ULSA\7mo\Base de datos avanzada\1er parcial\UlsaHub`
  - Gateway (Apollo): `UlsaHub\Proyecto\gateway` → puerto **4000**
  - Servicios: `UlsaHub\Proyecto\node_modules-v2\services\`
    - `seguridad-service` → puerto **4005** (login, usuarios, roles)
    - `academico-service` → puerto **4004**
    - `deportes-service` → puerto **4001** (equipos, prácticas — sin eventos)
    - `mapa-service` → puerto **4002**
    - `auditoria-service` → existe pero **NO está registrado** en el gateway
  - Docker (`UlsaHub\Docker\docker-compose.yml`): MongoDB `27018` (¡no 27017!), Redis `6380`, Neo4j `7475/7688`, ScyllaDB `9043`, RabbitMQ `5673`.
- **Docs del proyecto (en la raíz de la app):** `QR-CONTEXTO.md` (este), `GUIA-ESTUDIO.md`.

---

## 4. Contexto del backend (login y roles) — LO QUE SE INVESTIGÓ

### Cómo se conecta la app
- El login de la app **NO es un GET**: es **GraphQL**, una sola URL
  (`http://10.0.2.2:4000/`) y **siempre POST** (`@POST(".")` en
  `ui/auth/network/AuthService.kt`).
- La mutation `login(email, password)` la resuelve `seguridad-service`:
  - Contraseña: **bcrypt** (`bcrypt.compare`).
  - JWT: **8 horas** de vigencia, claims `{ id: String, rol: Rol }`.
  - Errores que devuelve: `"Credenciales inválidas."` y
    `"Este usuario está deshabilitado."` → coinciden con `mapServerError()` de
    `LoginViewModel.kt`.
- **Los GETs de la app NO van a UlsaHub:**
  | Llamada | Endpoint | Backend |
  |---|---|---|
  | Login | POST `/` (GraphQL) | **UlsaHub** gateway :4000 |
  | Home | GET `/api/home/sports/` | Render.com (Django, externo) |
  | Teams | GET `gist.githubusercontent.com/.../teams.json` | GitHub Gist |
  | News | GET (URL dinámica) | Render.com |
  | QR | — mock — | **no existe** en UlsaHub |

### Roles (respuesta a "¿necesito ser ADMIN?")
- Enum `Rol` en `seguridad-service`: `ESTUDIANTE`, `ENTRENADOR`, `PROFESOR`,
  `ADMIN`, `BECARIO`. El rol viaja dentro del JWT y del `usuario` del login.
- **El módulo QR NO revisa roles en ningún lado.** El motivo de que "no se
  pudiera editar nada" era que no había formulario ni endpoints, no el rol.

### deportes-service (dónde iría el QR)
- `schema.graphql` actual: `Equipo`, `Practica`, `reportePracticasPorDeporte`.
- **NO hay eventos, tickets, QR ni capacidad.** Hay que agregarlos (sección 12).

---

## 5. Estructura de archivos de la app (módulo QR)

Paquete base: `com.ulsa.deportes.ui.qrSection.qrHome`

```
ui/qrSection/qrHome/
├── model/QrModels.kt          # Data classes (@SerializedName), enums, QrFormData + DemoQrForm
├── network/QrApi.kt           # Interfaz Retrofit + QrApiClient (base :4000 + Bearer)
├── network/QrDependencies.kt  # Interruptor USE_MOCK -> mock o real
├── network/MockQrApi.kt       # Implementación simulada que "ecoe" el formulario
├── util/QrGenerator.kt        # token (String) -> Bitmap con ZXing
├── viewmodel/QrViewModel.kt   # QrUiState + QrViewModel + Factory
└── view/QrScreenView.kt       # Pantalla Compose con formulario + ticket QR
```

**Otros archivos tocados:**

- `app/build.gradle.kts` → `com.google.zxing:core:3.5.3` y
  `material-icons-extended:1.7.8` (antes tenía el literal `<version>` que
  rompía el build).
- `ui/navigation/AppNavigation.kt` → ruta interna `qr_screen`; navegación desde
  **Profile** (`profileHomeView(onLogout, onNavigateToQr)`).
- `ui/profileSection/profileHome/view/profileHomeView.kt` → botón
  **"Crear código QR"** + `onNavigateToQr`.

---

## 6. Flujo actual

1. Usuario entra a la tab **Profile** y presiona **"Crear código QR"** →
   `navController.navigate("qr_screen")`.
2. `QrScreenView` abre con el **formulario precargado** (`DemoQrForm.defaults()`)
   y carga la capacidad del evento (`loadCapacity(form.eventId)`).
3. El usuario edita portador (nombre, matrícula, facultad) y evento (título,
   tipo, sede).
4. Botón **"Crear código QR"** → `viewModel.createQr(form.toRequest())`.
5. `QrViewModel` → `QrUiState`: `Idle` → `Loading` → `Success` / `Error`
   (IOException / HttpException / genérico, con mensajes en español).
6. En `Success`, `MockQrApi` **ecoe** los datos del formulario en
   `event_summary` / `user_summary`.
7. Se genera el **QR real con ZXing** a partir de `qr.token` (en hilo de fondo
   con `produceState`) y se muestra el ticket (título, tipo, sede, portador,
   puerta, estado activo/expirado).

---

## 7. Contrato que espera el cliente (para el backend)

### `POST /qr` (o mutation GraphQL equivalente — ver sección 12)
Request (**snake_case**, los últimos 6 campos vienen del formulario):
```json
{
  "event_id": "string",
  "creation_mode": "static_event_checkin",
  "user_id": null,
  "valid_from": "2026-10-06T00:00:00Z",
  "valid_until": "2026-11-05T00:00:00Z",
  "security_config": { "qr_ttl_seconds": null, "geofence": null },
  "event_title": "Clásico ULSA 2026",
  "event_category": "Fútbol",
  "event_venue": "Estadio La Salle",
  "student_name": "Jorge Chaparro",
  "student_id": "14446",
  "faculty": "Ingeniería"
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
- **"Expired" se calcula en el cliente**: `now > access_rules.valid_until`.

### `GET /events/{eventId}/capacity`
```json
{ "capacity_limit": 500, "spots_remaining": 137 }
```

### Auth
`Authorization: Bearer <token del login>`.

### Reglas de negocio
- El QR contiene **solo el token**; nombre, facultad, venue, etc. se muestran en
  la app, nunca dentro del token.
- La capacidad es del **evento**, no del ticket: endpoint aparte.
- Geofence lo valida el escáner del staff, no el cliente.

---

## 8. Cómo conectar el backend real

En `network/QrDependencies.kt`:
```kotlin
const val USE_MOCK = false
```
Eso construye `QrApiClient.create { SessionPreferences(ctx).accessToken() }`,
apuntando a `http://10.0.2.2:4000/` con el Bearer del login.

> Recordatorio: `false` **solo funcionará** cuando el backend exponga los
> endpoints. Y si el backend es **GraphQL** (lo más probable), hay que
> reescribir `QrApi` al formato "sobre" GraphQL (query + variables), igual que
> `ui/auth/network/AuthService.kt`.

---

## 9. Pendientes / notas

- **Backend (UlsaHub):** no hay eventos, tickets, QR ni capacidad en
  `deportes-service`. Falta: schema + resolvers de `crearQR` y capacidad, firma
  del token (`jti`, `tid`, `eid`, `sub`, `mode`, `iat`, `exp`), validación
  atómica al escanear, tablas `tickets` y `access_logs`, geofence, pantalla de
  escaneo del staff. (¿Rest o GraphQL? — ver sección 10).
- **Modo dinámico:** el `QrViewModel` ya refresca si `refresh_at` viene con
  valor, pero falta guardar `user_id` en `SessionPreferences` (hoy solo guarda
  tokens + email) y exponer el toggle en la UI si se quiere activar.
- **Evento real:** el formulario usa `eventId = "evento-demo-ulsa"`. Cuando
  existan eventos, el `event_id` debe venir de la data real (Home/API de
  Render) o de una query nueva.
- El **evento de QR no se conecta** a la data del Home (fuentes distintas).
- En la rama queda el `Log.d("apiView", "Debugear")` en `LoginScreenView.kt`
  (commit `ceb999b`) — **no se ha quitado** (así lo dejó el usuario, para
  debuggear).

---

## 10. Cómo continuar este trabajo (checklist)

1. Confirmar con el backend si será **REST** o **GraphQL**.
2. Si es REST: el contrato de la sección 7 ya coincide; poner `USE_MOCK = false`
   y apuntar `BASE_URL` al host/puerto correctos.
3. Si es GraphQL: crear la mutation `crearQR` + query de capacidad en
   `deportes-service`, registrarla en el gateway, y reescribir
   `network/QrApi.kt` con el sobre `query + variables` (referencia:
   `ui/auth/model/AuthModel.kt`).
4. Probar en el emulador con el backend levantado (¡recordar que Mongo corre en
   `27018` en el docker-compose de UlsaHub!).
5. Decidir si en modo estático el backend devuelve `user_summary` (`null` según
   contrato) o si se acepta el desvío actual del mock.
6. (Opcional) Quitar el `Log.d("apiView", "Debugear")` del login.

---

## 11. Convenciones del proyecto (para seguir el estilo)

- Patrón MVVM por sección: `ui/<seccion>Section/<seccion>Home/{model,network,view,viewmodel}`.
- Preferencias centralizadas: `common/preferences/AppPreferences` (base) y
  clases por feature (`OnboardingPreferences`, `SessionPreferences`).
- Cliente Retrofit por feature (cada `network/` tiene su objeto `...Client`).
- `android:networkSecurityConfig` ya permite cleartext a `10.0.2.2`
  (no hace falta `usesCleartextTraffic="true"` global).
- `minSdk = 26`, así que `java.time` está disponible sin desugaring.

---

## 12. Contrato futuro (GraphQL, una vez exista el servicio en UlsaHub)

La mutation equivalente en `deportes-service` sería:

```graphql
mutation CrearQR($input: CrearQRInput!) {
  crearQR(input: $input) {
    ticket_id
    qr { token refresh_at }
    ticket { status used_at used_at_gate }
    event_summary { title category venue starts_at }
    user_summary { student_name student_id faculty }
    access_rules { entry_gate valid_from valid_until }
  }
}
# input CrearQRInput {
#   event_id: ID!
#   creation_mode: CreationMode!      # static_event_checkin | dynamic_individual_pass
#   user_id: ID                       # obligatorio solo en dinámico
#   valid_from: String!
#   valid_until: String!
#   security_config: SecurityConfigInput
#   event_title: String!
#   event_category: String!
#   event_venue: String!
#   student_name: String!
#   student_id: String!
#   faculty: String!
# }
# query CapacidadDeEvento($eventId: ID!) { capacidadDeEvento(eventId: $eventId)
#   { capacity_limit spots_remaining } }
```

El `QrViewModel` ya recibe el token **opaco** y refresca el QR si `refresh_at`
viene con valor (modo dinámico). En modo estático (hoy) el mock devuelve
`refresh_at: null`.

---

## 13. 🕒 Última sesión — en qué lo dejamos (07/oct/2026)

### Lo que se hizo en esta sesión (TODO sin commitear en `feat/login-log`)
1. **Se movió el botón "Crear código QR" de Home → Profile**:
   - `HomeHomeView.kt`: se eliminó el botón y el parámetro `onNavigateToQr`.
   - `profileHomeView.kt`: botón nuevo con ícono de QR + `onNavigateToQr`.
   - `AppNavigation.kt`: la navegación a `qr_screen` ahora sale del Profile.
2. **Se agregó formulario a la pantalla de QR** (`QrScreenView.kt`), con
   portador (nombre, matrícula, facultad) y evento (título, tipo, sede), y el
   ticket muestra el tipo de evento.
3. **El mock ahora "ecoe" el formulario** (`MockQrApi.kt`): devuelve lo que el
   usuario escribe (con fallbacks si hay campos vacíos).
4. **Modelos listos para el backend** (`QrModels.kt`):
   - `CreateQrRequest` ampliado con `event_title`, `event_category`,
     `event_venue`, `student_name`, `student_id`, `faculty`.
   - Nuevo `QrFormData` + `QrFormData.toRequest()` + `DemoQrForm`.
5. **Fix que destraba el build**: `material-icons-extended:<version>` → `1.7.8`.
6. **Se documentó este archivo** (secciones 3, 4, 12 y esta 13).

### Verificado
- `gradlew :app:compileDebugKotlin` → **BUILD SUCCESSFUL** (exit 0).

### El backend investigado (UlsaHub)
- Ruta: `C:\Users\jorge\Documents\ULSA\7mo\Base de datos avanzada\1er parcial\UlsaHub`
- Login y roles: ver sección 4. **No hace falta rol ADMIN para el QR**; el
  problema era que no había formulario ni endpoints.
- `deportes-service` no tiene eventos/tickets/QR → es donde hay que implementar
  (sección 12).

### Próximo paso natural
- Decidir REST vs GraphQL e implementar `crearQR` + capacidad en UlsaHub, o
  seguir puliendo la UI (toggle estático/dinámico, usar evento real del Home).
- Al cerrar: **commitear** los cambios de esta sesión en `feat/login-log`.

---

*Última actualización: 07/oct/2026, rama `feat/login-log` (cambios de sesión sin commitear).
Commits previos de la rama: `ceb999b` (log login), `b6262e2` (módulo QR), `7c5953f` (contexto QR).*