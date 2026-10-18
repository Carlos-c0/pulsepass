# Registro de ejecución manual E2E

Una fila por request de la colección. Se llena **después de cada Send**: HTTP observado, lo que se vio en el JSON o en la consulta posterior, el resultado y una observación. Nunca pegar tokens, contraseñas ni capturas que los muestren.

Resultados posibles: `PASS` (HTTP, JSON y efecto coinciden), `FAIL` (algo no coincide o hubo un 500), `BLOCKED` (faltó una precondición, un actor o un dato previo), `NOT RUN` (no se ejecutó).

| Corrida | Quién | Fecha | runId | Commit de la API probada | Carpetas que registra |
|---|---|---|---|---|---|
| 1 | Contribuidor 1 | pendiente | pendiente | pendiente (`git log -1 --oneline`) | 00 a 05 |
| 2 | Contribuidor 2 | pendiente | pendiente | pendiente | 06 a 09 (repite 00 a 05 solo para preparar datos) |

## 00 Preparación / conectividad

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Observación |
|---|---|---|---|---|---|---|---|
| PREP-01 | ANONYMOUS | `GET /api/venues/active` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| PREP-02 | ANONYMOUS | `POST /api/users` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| PREP-03 | ANONYMOUS | `POST /api/users` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| PREP-04 | ANONYMOUS | `POST /api/users` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| PREP-05 | ANONYMOUS | `GET /api/events/{{publishedEventCode}}` | 200 | pendiente | pendiente | NOT RUN | pendiente |

## 01 Venues (2 rutas)

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Observación |
|---|---|---|---|---|---|---|---|
| TC-BIZ-001a | ANONYMOUS | `GET /api/venues/active` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-001b | ANONYMOUS | `GET /api/venues/{{venueActiveCode}}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-VEN-01-404 | ANONYMOUS | `GET /api/venues/NO-EXISTE-{{runId}}` | 404 | pendiente | pendiente | NOT RUN | pendiente |

## 02 Artists (3 rutas)

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Observación |
|---|---|---|---|---|---|---|---|
| TC-BIZ-002a | ANONYMOUS | `GET /api/artists/active` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-002b | ANONYMOUS | `GET /api/artists/by-stage-name` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-002c | ANONYMOUS | `GET /api/artists/{{artistId}}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-ART-01-404 | ANONYMOUS | `GET /api/artists/999999` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-ART-01-400 | ANONYMOUS | `GET /api/artists/abc` | 400 | pendiente | pendiente | NOT RUN | pendiente |

## 03 Auth / usuarios (registro, login, me)

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Observación |
|---|---|---|---|---|---|---|---|
| TC-AUTH-001 | ANONYMOUS | `POST /api/users` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-006 | ANONYMOUS | `POST /api/users` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-006-409 | ANONYMOUS | `POST /api/users` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-001 | ANONYMOUS | `POST /api/users` | 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-002 | ANONYMOUS | `POST /api/users` | 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-016a | ANONYMOUS | `POST /api/users` | 201 o 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-002 | USER_A | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-003 | USER_A | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-004 | USER_A | `POST /api/auth/login` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-005 | ANONYMOUS | `POST /api/auth/login` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-AUTH-01-400 | ANONYMOUS | `POST /api/auth/login` | 400 | pendiente | pendiente | NOT RUN | pendiente |
| LOGIN-B | USER_B | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| LOGIN-STAFF | STAFF | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| LOGIN-ADMIN | ADMIN | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-006 | USER_A | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-AUTH-02-B | USER_B | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-AUTH-02-STAFF | STAFF | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-AUTH-02-ADMIN | ADMIN | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-016b | USER_C | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-016c | USER_C | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | pendiente |

## 04 Usuarios / consulta y propiedad (2 rutas)

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Observación |
|---|---|---|---|---|---|---|---|
| E2E-USR-02 | USER_A | `GET /api/users/by-email` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-USR-03 | USER_A | `GET /api/users/by-username` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-006 | USER_B | `GET /api/users/by-email` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-007 | USER_B | `GET /api/users/by-username` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-USR-02-ENUM | USER_B | `GET /api/users/by-email` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-USR-02-401 | ANONYMOUS | `GET /api/users/by-email` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-USR-02-ADMIN | ADMIN | `GET /api/users/by-email` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-USR-02-ADMIN-404 | ADMIN | `GET /api/users/by-email` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-USR-02-400 | USER_A | `GET /api/users/by-email` | 400 | pendiente | pendiente | NOT RUN | pendiente |

## 05 Events / administración y visibilidad (6 rutas)

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Observación |
|---|---|---|---|---|---|---|---|
| E2E-EVT-03 | ANONYMOUS | `GET /api/events/published` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-EVT-02 | ANONYMOUS | `GET /api/events/{{publishedEventCode}}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-004 | ANONYMOUS | `GET /api/events/{{draftEventCode}}` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-004b | USER_A | `GET /api/events/{{draftEventCode}}` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-015 | ADMIN | `GET /api/events/{{draftEventCode}}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-015b | STAFF | `GET /api/events/{{draftEventCode}}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-001a | ANONYMOUS | `POST /api/events` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-001b | USER_A | `POST /api/events` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-001c | STAFF | `POST /api/events` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-003 | ADMIN | `POST /api/events` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015a | ADMIN | `POST /api/events` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015b | ADMIN | `POST /api/events` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015c | ADMIN | `POST /api/events` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015d | ADMIN | `POST /api/events` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015e | ADMIN | `POST /api/events` | 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015f | ADMIN | `POST /api/events` | 400 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-EVT-02-NEW-ADMIN | ADMIN | `GET /api/events/{{newEventCode}}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-EVT-02-NEW-ANON | ANONYMOUS | `GET /api/events/{{newEventCode}}` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-003 | USER_A | `POST /api/events/{{newEventCode}}/artists/{{artistId}}` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-005 | ADMIN | `POST /api/events/{{newEventCode}}/artists/{{artistId}}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-005-409 | ADMIN | `POST /api/events/{{newEventCode}}/artists/{{artistId}}` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-005-404 | ADMIN | `POST /api/events/{{newEventCode}}/artists/999999` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-002a | USER_A | `PATCH /api/events/{{eventToPublishCode}}/publish` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-002b | STAFF | `PATCH /api/events/{{eventToPublishCode}}/publish` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-002c | ADMIN | `GET /api/events/{{eventToPublishCode}}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-004 | ADMIN | `PATCH /api/events/{{newEventCode}}/publish` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-004-409 | ADMIN | `PATCH /api/events/{{newEventCode}}/publish` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-EVT-02-NEW-PUB | ANONYMOUS | `GET /api/events/{{newEventCode}}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-EVT-03-NEW | ANONYMOUS | `GET /api/events/published` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-005 | ANONYMOUS | `GET /api/events/by-artist` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-EVT-06-ADMIN | ADMIN | `GET /api/events/by-artist` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-EVT-06-404 | ANONYMOUS | `GET /api/events/by-artist` | 404 | pendiente | pendiente | NOT RUN | pendiente |
