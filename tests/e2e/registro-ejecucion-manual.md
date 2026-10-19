# Registro de ejecución manual E2E

Una fila por request de la colección. Se llena **después de cada Send**: HTTP observado, lo que se vio en el JSON o en la consulta posterior, el resultado y una observación. Nunca pegar tokens, contraseñas ni capturas que los muestren.

Resultados posibles: `PASS` (HTTP, JSON y efecto coinciden), `FAIL` (algo no coincide o hubo un 500), `BLOCKED` (faltó una precondición, un actor o un dato previo), `NOT RUN` (no se ejecutó).

| Corrida | Quién | Fecha | runId | Commit de la API probada | Carpetas que registra |
|---|---|---|---|---|---|
| 1 | Alexander Fajardo | 2026-10-17 | r01 | 59695d7 Merge pull request #26 (test/e2e-collection-catalog) | 00 a 05 |
| 2 | Contribuidor 2 | pendiente | pendiente | pendiente | 06 a 09 (repite 00 a 05 solo para preparar datos) |

## 00 Preparación / conectividad

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Observación |
|---|---|---|---|---|---|---|---|
| PREP-01 | ANONYMOUS | `GET /api/venues/active` | 200 | 200 | [] arreglo vacío, antes del seed | PASS | - |
| PREP-02 | ANONYMOUS | `POST /api/users` | 201 | 201 | e2e_admin creado, sin password en la respuesta | PASS | — |
| PREP-03 | ANONYMOUS | `POST /api/users` | 201 | 201 | e2e_staff creado, sin password en la respuesta | PASS | — |
| PREP-04 | ANONYMOUS | `POST /api/users` | 201 | 201 | e2e_inactivo creado, sin password en la respuesta | PASS | — |
| PREP-05 | ANONYMOUS | `GET /api/events/{{publishedEventCode}}` | 200 | 200 | E2E-PUB-01 PUBLISHED, venue E2E-VEN-ACT, Neon Waves en artists | PASS | — |

## 01 Venues (2 rutas)

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Observación |
|---|---|---|---|---|---|---|---|
| TC-BIZ-001a | ANONYMOUS | `GET /api/venues/active` | 200 | 200 | E2E-VEN-ACT y E2E-VEN-UNO activos; E2E-VEN-INA no aparece | PASS | — |
| TC-BIZ-001b | ANONYMOUS | `GET /api/venues/{{venueActiveCode}}` | 200 | 200 | code E2E-VEN-ACT, capacity 500, active true | PASS | — |
| E2E-VEN-01-404 | ANONYMOUS | `GET /api/venues/NO-EXISTE-{{runId}}` | 404 | 404 | ErrorResponse completo, message Venue not found: NO-EXISTE-r01 | PASS | — |

## 02 Artists (3 rutas)

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Observación |
|---|---|---|---|---|---|---|---|
| TC-BIZ-002a | ANONYMOUS | `GET /api/artists/active` | 200 | 200 | 5 artistas de V2 activos; E2E Retired Artist no aparece | PASS | — |
| TC-BIZ-002b | ANONYMOUS | `GET /api/artists/by-stage-name` | 200 | 200 | stageName Neon Waves, id 2, active true | PASS | id copiado a artistId |
| TC-BIZ-002c | ANONYMOUS | `GET /api/artists/{{artistId}}` | 200 | 200 | id 2, stageName Neon Waves | PASS | — |
| E2E-ART-01-404 | ANONYMOUS | `GET /api/artists/999999` | 404 | 404 | ErrorResponse, message Artist not found: 999999 | PASS | — |
| E2E-ART-01-400 | ANONYMOUS | `GET /api/artists/abc` | 400 | 400 | message Invalid value for parameter: id, details.id Invalid value | PASS | — |

## 03 Auth / usuarios (registro, login, me)

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Observación |
|---|---|---|---|---|---|---|---|
| TC-AUTH-001 | ANONYMOUS | `POST /api/users` | 201 | 201 | e2e_andrea_r01 creado, sin password ni token en la respuesta | PASS | — |
| TC-BIZ-006 | ANONYMOUS | `POST /api/users` | 201 | 201 | e2e_bruno_r01 creado, id 5 distinto de A, sin password | PASS | — |
| TC-BIZ-006-409 | ANONYMOUS | `POST /api/users` | 409 | 409 | ErrorResponse, message Username already exists: e2e_andrea_r01 | PASS | — |
| TC-HTTP-001 | ANONYMOUS | `POST /api/users` | 400 | 400 | message Validation failed, details.email Email must be valid | PASS | — |
| TC-HTTP-002 | ANONYMOUS | `POST /api/users` | 400 | 400 | message Validation failed, details.password Password must have between 12 and 72 characters | PASS | — |
| TC-SEC-016a | ANONYMOUS | `POST /api/users` | 201 o 400 | 201 | e2e_carla_r01 creado; el campo roles fue ignorado (no rechazado) | PASS | Se confirma con TC-SEC-016c |
| TC-AUTH-002 | USER_A | `POST /api/auth/login` | 200 | 200 | accessToken presente, tokenType Bearer, expiresIn 900 | PASS | token copiado a userAToken y expiredToken |
| TC-AUTH-003 | USER_A | `POST /api/auth/login` | 200 | 200 | login por email OK, accessToken nuevo, tokenType Bearer | PASS | — |
| TC-AUTH-004 | USER_A | `POST /api/auth/login` | 401 | 401 | message Invalid credentials, sin accessToken | PASS | — |
| TC-AUTH-005 | ANONYMOUS | `POST /api/auth/login` | 401 | 401 | message Invalid credentials, idéntico a TC-AUTH-004 (no revela si el usuario existe) | PASS | — |
| E2E-AUTH-01-400 | ANONYMOUS | `POST /api/auth/login` | 400 | 400 | message Validation failed, details.password Password is required | PASS | — |
| LOGIN-B | USER_B | `POST /api/auth/login` | 200 | 200 | accessToken presente, tokenType Bearer, expiresIn 900 | PASS | token copiado a userBToken |
| LOGIN-STAFF | STAFF | `POST /api/auth/login` | 200 | 200 | accessToken presente, tokenType Bearer, expiresIn 900 | PASS | token copiado a staffToken |
| LOGIN-ADMIN | ADMIN | `POST /api/auth/login` | 200 | 200 | accessToken presente, tokenType Bearer, expiresIn 900 | PASS | token copiado a adminToken |
| TC-AUTH-006 | USER_A | `GET /api/auth/me` | 200 | 200 | username e2e_andrea_r01, email andrea.r01@example.test, roles [USER]; sin password ni token | PASS | — |
| E2E-AUTH-02-B | USER_B | `GET /api/auth/me` | 200 | 200 | username e2e_bruno_r01, roles [USER] | PASS | — |
| E2E-AUTH-02-STAFF | STAFF | `GET /api/auth/me` | 200 | 200 | username e2e_staff, roles [STAFF] | PASS | — |
| E2E-AUTH-02-ADMIN | ADMIN | `GET /api/auth/me` | 200 | 200 | username e2e_admin, roles [ADMIN] | PASS | — |
| TC-SEC-016b | USER_C | `POST /api/auth/login` | 200 | 200 | login de e2e_carla_r01 OK, accessToken presente | PASS | token copiado a userCToken |
| TC-SEC-016c | USER_C | `GET /api/auth/me` | 200 | 200 | e2e_carla_r01 con roles [USER]; el rol ADMIN inyectado no se aplicó | PASS | — |

## 04 Usuarios / consulta y propiedad (2 rutas)

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Observación |
|---|---|---|---|---|---|---|---|
| E2E-USR-02 | USER_A | `GET /api/users/by-email` | 200 | 200 | datos propios de e2e_andrea_r01, firstName Andrea, birthDate 1998-05-15, sin password | PASS | — |
| E2E-USR-03 | USER_A | `GET /api/users/by-username` | 200 | 200 | datos propios, email andrea.r01@example.test | PASS | — |
| TC-SEC-006 | USER_B | `GET /api/users/by-email` | 403 | 403 | ErrorResponse, message No tiene permisos para realizar esta operación; ningún dato de A | PASS | — |
| TC-SEC-007 | USER_B | `GET /api/users/by-username` | 403 | 403 | ErrorResponse 403; ningún dato de A | PASS | — |
| E2E-USR-02-ENUM | USER_B | `GET /api/users/by-email` | 403 | 403 | 403 igual que TC-SEC-006 aunque el email no existe (no revela qué emails existen) | PASS | — |
| E2E-USR-02-401 | ANONYMOUS | `GET /api/users/by-email` | 401 | 401 | ErrorResponse, message Authentication required | PASS | — |
| E2E-USR-02-ADMIN | ADMIN | `GET /api/users/by-email` | 200 | 200 | ADMIN ve los datos de e2e_andrea_r01 | PASS | — |
| E2E-USR-02-ADMIN-404 | ADMIN | `GET /api/users/by-email` | 404 | 404 | message User not found: nadie.r01@example.test | PASS | — |
| E2E-USR-02-400 | USER_A | `GET /api/users/by-email` | 400 | 400 | message Missing required parameter: email, details.email Parameter is required | PASS | — |

## 05 Events / administración y visibilidad (6 rutas)

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Observación |
|---|---|---|---|---|---|---|---|
| E2E-EVT-03 | ANONYMOUS | `GET /api/events/published` | 200 | 200 | E2E-PUB-01, E2E-EDAD-99, E2E-AFORO-1 con venueName y venueCity; sin DRAFT-01 ni TOPUB-01 | PASS | — |
| E2E-EVT-02 | ANONYMOUS | `GET /api/events/{{publishedEventCode}}` | 200 | 200 | E2E-PUB-01 PUBLISHED, minimumAge 18, Neon Waves en artists | PASS | — |
| TC-SEC-004 | ANONYMOUS | `GET /api/events/{{draftEventCode}}` | 404 | 404 | message Event not found: E2E-DRAFT-01; sin nombre ni fecha del borrador | PASS | — |
| TC-SEC-004b | USER_A | `GET /api/events/{{draftEventCode}}` | 404 | 404 | 404 igual que sin token; USER no ve borradores | PASS | — |
| TC-SEC-015 | ADMIN | `GET /api/events/{{draftEventCode}}` | 200 | 200 | ADMIN ve E2E-DRAFT-01 con status DRAFT | PASS | — |
| TC-SEC-015b | STAFF | `GET /api/events/{{draftEventCode}}` | 200 | 200 | STAFF ve E2E-DRAFT-01 con status DRAFT | PASS | — |
| TC-SEC-001a | ANONYMOUS | `POST /api/events` | 401 | 401 | message Authentication required; no se creó el evento (lo confirma TC-BIZ-003) | PASS | — |
| TC-SEC-001b | USER_A | `POST /api/events` | 403 | 403 | ErrorResponse, message No tiene permisos para realizar esta operación | PASS | — |
| TC-SEC-001c | STAFF | `POST /api/events` | 403 | 403 | ErrorResponse 403; STAFF no crea eventos | PASS | — |
| TC-BIZ-003 | ADMIN | `POST /api/events` | 201 | 201 | E2E-NEW-r01 creado en DRAFT, venue E2E-VEN-ACT, artists vacío | PASS | Confirma que TC-SEC-001a/b/c no crearon nada |
| TC-BIZ-015a | ADMIN | `POST /api/events` | 409 | 409 | message Event code already exists: E2E-NEW-r01 | PASS | — |
| TC-BIZ-015b | ADMIN | `POST /api/events` | 409 | 409 | message Venue is not active: E2E-VEN-INA | PASS | — |
| TC-BIZ-015c | ADMIN | `POST /api/events` | 409 | 409 | message Event date must be in the future. | PASS | — |
| TC-BIZ-015d | ADMIN | `POST /api/events` | 404 | 404 | message Venue not found: NO-EXISTE | PASS | — |
| TC-BIZ-015e | ADMIN | `POST /api/events` | 400 | 400 | message Malformed JSON request (categoría MUSICA inválida) | PASS | — |
| TC-BIZ-015f | ADMIN | `POST /api/events` | 400 | 400 | Validation failed; details con eventCode, venueCode y minimumAge (Minimum age cannot be negative) | PASS | — |
| E2E-EVT-02-NEW-ADMIN | ADMIN | `GET /api/events/{{newEventCode}}` | 200 | 200 | E2E-NEW-r01 en DRAFT, artists vacío | PASS | — |
| E2E-EVT-02-NEW-ANON | ANONYMOUS | `GET /api/events/{{newEventCode}}` | 404 | 404 | message Event not found: E2E-NEW-r01; el borrador nuevo no se filtra | PASS | — |
| TC-SEC-003 | USER_A | `POST /api/events/{{newEventCode}}/artists/{{artistId}}` | 403 | 403 | ErrorResponse 403; no se asoció nada (lo confirma TC-BIZ-005) | PASS | — |
| TC-BIZ-005 | ADMIN | `POST /api/events/{{newEventCode}}/artists/{{artistId}}` | 200 | 200 | artists con un solo elemento: Neon Waves | PASS | Confirma que TC-SEC-003 no mutó nada |
| TC-BIZ-005-409 | ADMIN | `POST /api/events/{{newEventCode}}/artists/{{artistId}}` | 409 | 409 | message Artist Neon Waves is already associated with event E2E-NEW-r01 | PASS | — |
| TC-BIZ-005-404 | ADMIN | `POST /api/events/{{newEventCode}}/artists/999999` | 404 | 404 | message Artist not found: 999999 | PASS | — |
| TC-SEC-002a | USER_A | `PATCH /api/events/{{eventToPublishCode}}/publish` | 403 | 403 | ErrorResponse 403; USER no publica | PASS | — |
| TC-SEC-002b | STAFF | `PATCH /api/events/{{eventToPublishCode}}/publish` | 403 | 403 | ErrorResponse 403; STAFF no publica | PASS | — |
| TC-SEC-002c | ADMIN | `GET /api/events/{{eventToPublishCode}}` | 200 | 200 | E2E-TOPUB-01 sigue en DRAFT tras las denegaciones | PASS | — |
| TC-BIZ-004 | ADMIN | `PATCH /api/events/{{newEventCode}}/publish` | 200 | 200 | E2E-NEW-r01 pasa a PUBLISHED, Neon Waves sigue en artists | PASS | — |
| TC-BIZ-004-409 | ADMIN | `PATCH /api/events/{{newEventCode}}/publish` | 409 | 409 | message Only DRAFT events can be published. Current status: PUBLISHED | PASS | — |
| E2E-EVT-02-NEW-PUB | ANONYMOUS | `GET /api/events/{{newEventCode}}` | 200 | 200 | E2E-NEW-r01 visible sin token, status PUBLISHED | PASS | — |
| E2E-EVT-03-NEW | ANONYMOUS | `GET /api/events/published` | 200 | 200 | ahora incluye E2E-NEW-r01; sigue sin DRAFT-01 ni TOPUB-01 | PASS | — |
| TC-SEC-005 | ANONYMOUS | `GET /api/events/by-artist` | 200 | 200 | E2E-PUB-01 y E2E-NEW-r01; sin E2E-DRAFT-01 | PASS | — |
| E2E-EVT-06-ADMIN | ADMIN | `GET /api/events/by-artist` | 200 | 200 | E2E-PUB-01, E2E-NEW-r01 y E2E-DRAFT-01 (ADMIN ve el borrador) | PASS | — |
| E2E-EVT-06-404 | ANONYMOUS | `GET /api/events/by-artist` | 404 | 404 | message Artist not found: Nadie r01 | PASS | — |

## 06 Tickets / compra, consulta y ciclo de vida (6 rutas)

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Observación |
|---|---|---|---|---|---|---|---|
| TC-BIZ-007a | USER_A | `POST /api/tickets` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-007b | USER_A | `POST /api/tickets` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-008 | USER_A | `POST /api/tickets` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-TKT-03-B | USER_B | `GET /api/tickets/by-user` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-TKT-01-401 | ANONYMOUS | `POST /api/tickets` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-TKT-01-STAFF | STAFF | `POST /api/tickets` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-008a | USER_A | `GET /api/tickets/{{ticketCancelableCode}}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-008b | USER_A | `GET /api/tickets/by-user` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-009 | USER_B | `GET /api/tickets/{{ticketCancelableCode}}` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-010 | USER_B | `GET /api/tickets/by-user` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-TKT-02-STAFF | STAFF | `GET /api/tickets/{{ticketCancelableCode}}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2E-TKT-02-404 | USER_A | `GET /api/tickets/TCK-NOEXISTE` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-011 | USER_B | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-010 | USER_A | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-011 | USER_A | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-013 | USER_A | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-009 | STAFF | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-012 | USER_B | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-012b | USER_A | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-008c | USER_A | `GET /api/tickets/{{ticketUsableCode}}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-014 | STAFF | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-013 | STAFF | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-014a | USER_A | `POST /api/tickets` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-014b | USER_A | `POST /api/tickets` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-014c | USER_A | `POST /api/tickets` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-014d | ANONYMOUS | `GET /api/events/{{capacityEventCode}}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-014e | USER_B | `POST /api/tickets` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015g | USER_A | `POST /api/tickets` | 404 | pendiente | pendiente | NOT RUN | pendiente |

## 07 Seguridad / escenarios negativos

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Observación |
|---|---|---|---|---|---|---|---|
| TC-AUTH-007 | ANONYMOUS | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-008 | ANONYMOUS | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-009 | USER_A | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-010 | USER_A | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-011 | ANONYMOUS | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-012 | ANONYMOUS | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-015 | ANONYMOUS | `GET /api/venues/active` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-016 | ANONYMOUS | `GET /api/venues/active` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-017 | USER_A | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | pendiente |

## 08 Validaciones y errores HTTP

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Observación |
|---|---|---|---|---|---|---|---|
| TC-HTTP-003 | USER_A | `POST /api/tickets` | 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-004 | USER_A | `POST /api/tickets` | 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-005 | USER_A | `POST /api/tickets` | 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-006 | USER_A | `POST /api/tickets` | 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-007 | USER_A | `GET /api/no-existe` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-008 | USER_A | `DELETE /api/venues/active` | 405 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-009 | ADMIN | `GET /api/tickets/TCK-NOEXISTE` | 404 | pendiente | pendiente | NOT RUN | pendiente |

## 09 Flujos de negocio / secuencia recomendada

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Observación |
|---|---|---|---|---|---|---|---|
| FLOW-F0 | INACTIVE_USER | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLOW-F1 | INACTIVE_USER | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-013 | INACTIVE_USER | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-013b | INACTIVE_USER | `POST /api/auth/login` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| FLOW-G1 | STAFF | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-017 | STAFF | `PATCH /api/tickets/{{ticketCapacityCode}}/use` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| FLOW-G2 | STAFF | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLOW-G3 | USER_A | `GET /api/tickets/{{ticketCapacityCode}}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLOW-G4 | STAFF | `PATCH /api/tickets/{{ticketCapacityCode}}/use` | 200 | pendiente | pendiente | NOT RUN | pendiente |

## Cobertura de las 22 rutas

Marcar `sí` cuando al menos un caso nominal (2xx esperado) de la ruta quedó en PASS.

| Ruta | Endpoint | Casos | Nominal en PASS |
|---|---|---|---|
| E2E-VEN-01 | `GET /api/venues/{code}` | TC-BIZ-001b, E2E-VEN-01-404 | pendiente |
| E2E-VEN-02 | `GET /api/venues/active` | PREP-01, TC-BIZ-001a, TC-AUTH-015, TC-AUTH-016 | pendiente |
| E2E-EVT-01 | `POST /api/events` | TC-SEC-001a, TC-SEC-001b, TC-SEC-001c, TC-BIZ-003, TC-BIZ-015a, TC-BIZ-015b, TC-BIZ-015c, TC-BIZ-015d, TC-BIZ-015e, TC-BIZ-015f | pendiente |
| E2E-EVT-02 | `GET /api/events/{eventCode}` | PREP-05, E2E-EVT-02, TC-SEC-004, TC-SEC-004b, TC-SEC-015, TC-SEC-015b, E2E-EVT-02-NEW-ADMIN, E2E-EVT-02-NEW-ANON, TC-SEC-002c, E2E-EVT-02-NEW-PUB, TC-BIZ-014d | pendiente |
| E2E-EVT-03 | `GET /api/events/published` | E2E-EVT-03, E2E-EVT-03-NEW | pendiente |
| E2E-EVT-04 | `PATCH /api/events/{eventCode}/publish` | TC-SEC-002a, TC-SEC-002b, TC-BIZ-004, TC-BIZ-004-409 | pendiente |
| E2E-EVT-05 | `POST /api/events/{eventCode}/artists/{artistId}` | TC-SEC-003, TC-BIZ-005, TC-BIZ-005-409, TC-BIZ-005-404 | pendiente |
| E2E-EVT-06 | `GET /api/events/by-artist` | TC-SEC-005, E2E-EVT-06-ADMIN, E2E-EVT-06-404 | pendiente |
| E2E-ART-01 | `GET /api/artists/{id}` | TC-BIZ-002c, E2E-ART-01-404, E2E-ART-01-400 | pendiente |
| E2E-ART-02 | `GET /api/artists/by-stage-name` | TC-BIZ-002b | pendiente |
| E2E-ART-03 | `GET /api/artists/active` | TC-BIZ-002a | pendiente |
| E2E-USR-01 | `POST /api/users` | PREP-02, PREP-03, PREP-04, TC-AUTH-001, TC-BIZ-006, TC-BIZ-006-409, TC-HTTP-001, TC-HTTP-002, TC-SEC-016a | pendiente |
| E2E-USR-02 | `GET /api/users/by-email` | E2E-USR-02, TC-SEC-006, E2E-USR-02-ENUM, E2E-USR-02-401, E2E-USR-02-ADMIN, E2E-USR-02-ADMIN-404, E2E-USR-02-400 | pendiente |
| E2E-USR-03 | `GET /api/users/by-username` | E2E-USR-03, TC-SEC-007 | pendiente |
| E2E-TKT-01 | `POST /api/tickets` | TC-BIZ-007a, TC-BIZ-007b, TC-SEC-008, E2E-TKT-01-401, E2E-TKT-01-STAFF, TC-BIZ-014a, TC-BIZ-014b, TC-BIZ-014c, TC-BIZ-014e, TC-BIZ-015g, TC-HTTP-003, TC-HTTP-004, TC-HTTP-005, TC-HTTP-006 | pendiente |
| E2E-TKT-02 | `GET /api/tickets/{ticketCode}` | TC-BIZ-008a, TC-SEC-009, E2E-TKT-02-STAFF, E2E-TKT-02-404, TC-BIZ-008c, TC-HTTP-009, FLOW-G3 | pendiente |
| E2E-TKT-03 | `GET /api/tickets/by-user` | E2E-TKT-03-B, TC-BIZ-008b, TC-SEC-010 | pendiente |
| E2E-TKT-04 | `GET /api/events/{eventCode}/tickets/paid` | TC-SEC-013, TC-BIZ-009 | pendiente |
| E2E-TKT-05 | `PATCH /api/tickets/{ticketCode}/cancel` | TC-SEC-011, TC-BIZ-010, TC-BIZ-011 | pendiente |
| E2E-TKT-06 | `PATCH /api/tickets/{ticketCode}/use` | TC-SEC-012, TC-SEC-012b, TC-SEC-014, TC-BIZ-013, TC-SEC-017, FLOW-G4 | pendiente |
| E2E-AUTH-01 | `POST /api/auth/login` | TC-AUTH-002, TC-AUTH-003, TC-AUTH-004, TC-AUTH-005, E2E-AUTH-01-400, LOGIN-B, LOGIN-STAFF, LOGIN-ADMIN, TC-SEC-016b, FLOW-F0, TC-AUTH-013b | pendiente |
| E2E-AUTH-02 | `GET /api/auth/me` | TC-AUTH-006, E2E-AUTH-02-B, E2E-AUTH-02-STAFF, E2E-AUTH-02-ADMIN, TC-SEC-016c, TC-AUTH-007, TC-AUTH-008, TC-AUTH-009, TC-AUTH-010, TC-AUTH-011, TC-AUTH-012, TC-AUTH-017, FLOW-F1, TC-AUTH-013, FLOW-G1, FLOW-G2 | pendiente |

## Consolidación

| Carpeta | PASS | FAIL | BLOCKED | NOT RUN |
|---|---|---|---|---|
| 00 Preparación / conectividad | 0 | 0 | 0 | 5 |
| 01 Venues (2 rutas) | 0 | 0 | 0 | 3 |
| 02 Artists (3 rutas) | 0 | 0 | 0 | 5 |
| 03 Auth / usuarios (registro, login, me) | 0 | 0 | 0 | 20 |
| 04 Usuarios / consulta y propiedad (2 rutas) | 0 | 0 | 0 | 9 |
| 05 Events / administración y visibilidad (6 rutas) | 0 | 0 | 0 | 32 |
| 06 Tickets / compra, consulta y ciclo de vida (6 rutas) | 0 | 0 | 0 | 28 |
| 07 Seguridad / escenarios negativos | 0 | 0 | 0 | 9 |
| 08 Validaciones y errores HTTP | 0 | 0 | 0 | 7 |
| 09 Flujos de negocio / secuencia recomendada | 0 | 0 | 0 | 9 |

## Defectos

| ID | Caso | Pasos para reproducir | HTTP esperado | HTTP observado | Fase afectada (1-4) |
|---|---|---|---|---|---|
| — | — | — | — | — | — |

