# Checklist de ejecución manual E2E

Orden obligatorio de las requests. Cada una se envía a mano con **Send** desde Postman, con el ambiente *PulsePass local E2E* seleccionado. Si una precondición falla, los casos que dependen de ella se marcan `BLOCKED` en `registro-ejecucion-manual.md`.

## Antes de la primera request

- [ ] Base `pulsepass_e2e` recreada desde cero (`tests/e2e/fixtures/README.md`, paso 1).
- [ ] Aplicación arrancada contra `pulsepass_e2e` con una clave JWT aleatoria (paso 2).
- [ ] Colección y ambiente importados; ambiente seleccionado; contraseñas puestas en *Current value*.
- [ ] `runId` decidido (si no se reinició la base, cambiarlo junto con los valores que lo llevan).

## 00 Preparación / conectividad
Antes de esta carpeta: base pulsepass_e2e vacía y la aplicación arrancada contra ella (ver tests/e2e/README.md). Las tres cuentas privilegiadas se registran por el registro público y después seed-e2e.sql les da su rol. Orden: PREP-01 → PREP-02 → PREP-03 → PREP-04 → ejecutar seed-e2e.sql → PREP-05.

- [ ] `PREP-01` GET `/api/venues/active` · ANONYMOUS · esperado **200**
- [ ] `PREP-02` POST `/api/users` · ANONYMOUS · esperado **201**
- [ ] `PREP-03` POST `/api/users` · ANONYMOUS · esperado **201**
- [ ] `PREP-04` POST `/api/users` · ANONYMOUS · esperado **201**
- [ ] **Ejecutar `seed-e2e.sql`** (`tests/e2e/fixtures/README.md`, paso 3) y revisar que la consulta final muestre e2e_admin con ADMIN y e2e_staff con STAFF.
- [ ] `PREP-05` GET `/api/events/{{publishedEventCode}}` · ANONYMOUS · esperado **200**

## 01 Venues (2 rutas)
Flujo A: consultas públicas, sin token.

- [ ] `TC-BIZ-001a` GET `/api/venues/active` · ANONYMOUS · esperado **200**
- [ ] `TC-BIZ-001b` GET `/api/venues/{{venueActiveCode}}` · ANONYMOUS · esperado **200**
- [ ] `E2E-VEN-01-404` GET `/api/venues/NO-EXISTE-{{runId}}` · ANONYMOUS · esperado **404**

## 02 Artists (3 rutas)
Flujo A: consultas públicas de artistas. Aquí se copia `artistId`.

- [ ] `TC-BIZ-002a` GET `/api/artists/active` · ANONYMOUS · esperado **200**
- [ ] `TC-BIZ-002b` GET `/api/artists/by-stage-name` · ANONYMOUS · esperado **200** — **copiar:** Copia el `id` de la respuesta a la variable `artistId`.
- [ ] `TC-BIZ-002c` GET `/api/artists/{{artistId}}` · ANONYMOUS · esperado **200**
- [ ] `E2E-ART-01-404` GET `/api/artists/999999` · ANONYMOUS · esperado **404**
- [ ] `E2E-ART-01-400` GET `/api/artists/abc` · ANONYMOUS · esperado **400**

## 03 Auth / usuarios (registro, login, me)
Flujo B. Después de cada login copia `accessToken` a la variable del actor (pestaña Variables del ambiente, columna *Current value*). Los tokens duran 15 minutos: si más adelante aparece un 401 inesperado, repite el login de ese actor.

- [ ] `TC-AUTH-001` POST `/api/users` · ANONYMOUS · esperado **201**
- [ ] `TC-BIZ-006` POST `/api/users` · ANONYMOUS · esperado **201**
- [ ] `TC-BIZ-006-409` POST `/api/users` · ANONYMOUS · esperado **409**
- [ ] `TC-HTTP-001` POST `/api/users` · ANONYMOUS · esperado **400**
- [ ] `TC-HTTP-002` POST `/api/users` · ANONYMOUS · esperado **400**
- [ ] `TC-SEC-016a` POST `/api/users` · ANONYMOUS · esperado **201 o 400**
- [ ] `TC-AUTH-002` POST `/api/auth/login` · USER_A · esperado **200** — **copiar:** Copia `accessToken` a `userAToken` **y también** a `expiredToken` (se usa al final, cuando ya venció). Anota la hora.
- [ ] `TC-AUTH-003` POST `/api/auth/login` · USER_A · esperado **200**
- [ ] `TC-AUTH-004` POST `/api/auth/login` · USER_A · esperado **401**
- [ ] `TC-AUTH-005` POST `/api/auth/login` · ANONYMOUS · esperado **401**
- [ ] `E2E-AUTH-01-400` POST `/api/auth/login` · ANONYMOUS · esperado **400**
- [ ] `LOGIN-B` POST `/api/auth/login` · USER_B · esperado **200** — **copiar:** Copia `accessToken` a `userBToken`.
- [ ] `LOGIN-STAFF` POST `/api/auth/login` · STAFF · esperado **200** — **copiar:** Copia `accessToken` a `staffToken`.
- [ ] `LOGIN-ADMIN` POST `/api/auth/login` · ADMIN · esperado **200** — **copiar:** Copia `accessToken` a `adminToken`.
- [ ] `TC-AUTH-006` GET `/api/auth/me` · USER_A · esperado **200**
- [ ] `E2E-AUTH-02-B` GET `/api/auth/me` · USER_B · esperado **200**
- [ ] `E2E-AUTH-02-STAFF` GET `/api/auth/me` · STAFF · esperado **200**
- [ ] `E2E-AUTH-02-ADMIN` GET `/api/auth/me` · ADMIN · esperado **200**
- [ ] `TC-SEC-016b` POST `/api/auth/login` · USER_C · esperado **200** — **copiar:** Copia `accessToken` a `userCToken`.
- [ ] `TC-SEC-016c` GET `/api/auth/me` · USER_C · esperado **200**

## 04 Usuarios / consulta y propiedad (2 rutas)
Consultas de usuario: solo el propio usuario o ADMIN. Política para recursos ajenos: 403, decidido **antes** de mirar si existe (anti-enumeración).

- [ ] `E2E-USR-02` GET `/api/users/by-email` · USER_A · esperado **200**
- [ ] `E2E-USR-03` GET `/api/users/by-username` · USER_A · esperado **200**
- [ ] `TC-SEC-006` GET `/api/users/by-email` · USER_B · esperado **403**
- [ ] `TC-SEC-007` GET `/api/users/by-username` · USER_B · esperado **403**
- [ ] `E2E-USR-02-ENUM` GET `/api/users/by-email` · USER_B · esperado **403**
- [ ] `E2E-USR-02-401` GET `/api/users/by-email` · ANONYMOUS · esperado **401**
- [ ] `E2E-USR-02-ADMIN` GET `/api/users/by-email` · ADMIN · esperado **200**
- [ ] `E2E-USR-02-ADMIN-404` GET `/api/users/by-email` · ADMIN · esperado **404**
- [ ] `E2E-USR-02-400` GET `/api/users/by-email` · USER_A · esperado **400**

## 05 Events / administración y visibilidad (6 rutas)
Flujo C. ADMIN crea, asocia y publica; USER y STAFF no pueden. Los borradores solo los ven STAFF y ADMIN; para el resto responden 404, como si no existieran.

- [ ] `E2E-EVT-03` GET `/api/events/published` · ANONYMOUS · esperado **200**
- [ ] `E2E-EVT-02` GET `/api/events/{{publishedEventCode}}` · ANONYMOUS · esperado **200**
- [ ] `TC-SEC-004` GET `/api/events/{{draftEventCode}}` · ANONYMOUS · esperado **404**
- [ ] `TC-SEC-004b` GET `/api/events/{{draftEventCode}}` · USER_A · esperado **404**
- [ ] `TC-SEC-015` GET `/api/events/{{draftEventCode}}` · ADMIN · esperado **200**
- [ ] `TC-SEC-015b` GET `/api/events/{{draftEventCode}}` · STAFF · esperado **200**
- [ ] `TC-SEC-001a` POST `/api/events` · ANONYMOUS · esperado **401**
- [ ] `TC-SEC-001b` POST `/api/events` · USER_A · esperado **403**
- [ ] `TC-SEC-001c` POST `/api/events` · STAFF · esperado **403**
- [ ] `TC-BIZ-003` POST `/api/events` · ADMIN · esperado **201**
- [ ] `TC-BIZ-015a` POST `/api/events` · ADMIN · esperado **409**
- [ ] `TC-BIZ-015b` POST `/api/events` · ADMIN · esperado **409**
- [ ] `TC-BIZ-015c` POST `/api/events` · ADMIN · esperado **409**
- [ ] `TC-BIZ-015d` POST `/api/events` · ADMIN · esperado **404**
- [ ] `TC-BIZ-015e` POST `/api/events` · ADMIN · esperado **400**
- [ ] `TC-BIZ-015f` POST `/api/events` · ADMIN · esperado **400**
- [ ] `E2E-EVT-02-NEW-ADMIN` GET `/api/events/{{newEventCode}}` · ADMIN · esperado **200**
- [ ] `E2E-EVT-02-NEW-ANON` GET `/api/events/{{newEventCode}}` · ANONYMOUS · esperado **404**
- [ ] `TC-SEC-003` POST `/api/events/{{newEventCode}}/artists/{{artistId}}` · USER_A · esperado **403**
- [ ] `TC-BIZ-005` POST `/api/events/{{newEventCode}}/artists/{{artistId}}` · ADMIN · esperado **200**
- [ ] `TC-BIZ-005-409` POST `/api/events/{{newEventCode}}/artists/{{artistId}}` · ADMIN · esperado **409**
- [ ] `TC-BIZ-005-404` POST `/api/events/{{newEventCode}}/artists/999999` · ADMIN · esperado **404**
- [ ] `TC-SEC-002a` PATCH `/api/events/{{eventToPublishCode}}/publish` · USER_A · esperado **403**
- [ ] `TC-SEC-002b` PATCH `/api/events/{{eventToPublishCode}}/publish` · STAFF · esperado **403**
- [ ] `TC-SEC-002c` GET `/api/events/{{eventToPublishCode}}` · ADMIN · esperado **200**
- [ ] `TC-BIZ-004` PATCH `/api/events/{{newEventCode}}/publish` · ADMIN · esperado **200**
- [ ] `TC-BIZ-004-409` PATCH `/api/events/{{newEventCode}}/publish` · ADMIN · esperado **409**
- [ ] `E2E-EVT-02-NEW-PUB` GET `/api/events/{{newEventCode}}` · ANONYMOUS · esperado **200**
- [ ] `E2E-EVT-03-NEW` GET `/api/events/published` · ANONYMOUS · esperado **200**
- [ ] `TC-SEC-005` GET `/api/events/by-artist` · ANONYMOUS · esperado **200**
- [ ] `E2E-EVT-06-ADMIN` GET `/api/events/by-artist` · ADMIN · esperado **200**
- [ ] `E2E-EVT-06-404` GET `/api/events/by-artist` · ANONYMOUS · esperado **404**

## 06 Tickets / compra, consulta y ciclo de vida (6 rutas)
Flujo D. T1 se cancela y T2 se usa: **nunca** se reutiliza un ticket para dos operaciones irreversibles. Cada denegación se comprueba antes de la transición válida y se vuelve a leer el ticket.

- [ ] `TC-BIZ-007a` POST `/api/tickets` · USER_A · esperado **201** — **copiar:** Copia `ticketCode` a `ticketCancelableCode` (T1).
- [ ] `TC-BIZ-007b` POST `/api/tickets` · USER_A · esperado **201** — **copiar:** Copia `ticketCode` a `ticketUsableCode` (T2).
- [ ] `TC-SEC-008` POST `/api/tickets` · USER_A · esperado **403**
- [ ] `E2E-TKT-03-B` GET `/api/tickets/by-user` · USER_B · esperado **200**
- [ ] `E2E-TKT-01-401` POST `/api/tickets` · ANONYMOUS · esperado **401**
- [ ] `E2E-TKT-01-STAFF` POST `/api/tickets` · STAFF · esperado **403**
- [ ] `TC-BIZ-008a` GET `/api/tickets/{{ticketCancelableCode}}` · USER_A · esperado **200**
- [ ] `TC-BIZ-008b` GET `/api/tickets/by-user` · USER_A · esperado **200**
- [ ] `TC-SEC-009` GET `/api/tickets/{{ticketCancelableCode}}` · USER_B · esperado **403**
- [ ] `TC-SEC-010` GET `/api/tickets/by-user` · USER_B · esperado **403**
- [ ] `E2E-TKT-02-STAFF` GET `/api/tickets/{{ticketCancelableCode}}` · STAFF · esperado **200**
- [ ] `E2E-TKT-02-404` GET `/api/tickets/TCK-NOEXISTE` · USER_A · esperado **404**
- [ ] `TC-SEC-011` PATCH `/api/tickets/{{ticketCancelableCode}}/cancel` · USER_B · esperado **403**
- [ ] `TC-BIZ-010` PATCH `/api/tickets/{{ticketCancelableCode}}/cancel` · USER_A · esperado **200**
- [ ] `TC-BIZ-011` PATCH `/api/tickets/{{ticketCancelableCode}}/cancel` · USER_A · esperado **409**
- [ ] `TC-SEC-013` GET `/api/events/{{publishedEventCode}}/tickets/paid` · USER_A · esperado **403**
- [ ] `TC-BIZ-009` GET `/api/events/{{publishedEventCode}}/tickets/paid` · STAFF · esperado **200**
- [ ] `TC-SEC-012` PATCH `/api/tickets/{{ticketUsableCode}}/use` · USER_B · esperado **403**
- [ ] `TC-SEC-012b` PATCH `/api/tickets/{{ticketUsableCode}}/use` · USER_A · esperado **403**
- [ ] `TC-BIZ-008c` GET `/api/tickets/{{ticketUsableCode}}` · USER_A · esperado **200**
- [ ] `TC-SEC-014` PATCH `/api/tickets/{{ticketUsableCode}}/use` · STAFF · esperado **200**
- [ ] `TC-BIZ-013` PATCH `/api/tickets/{{ticketUsableCode}}/use` · STAFF · esperado **409**
- [ ] `TC-BIZ-014a` POST `/api/tickets` · USER_A · esperado **409**
- [ ] `TC-BIZ-014b` POST `/api/tickets` · USER_A · esperado **409**
- [ ] `TC-BIZ-014c` POST `/api/tickets` · USER_A · esperado **201** — **copiar:** Copia `ticketCode` a `ticketCapacityCode`. Comprueba `price` = 80000.00 (STUDENT = 0,8 × 100000).
- [ ] `TC-BIZ-014d` GET `/api/events/{{capacityEventCode}}` · ANONYMOUS · esperado **200**
- [ ] `TC-BIZ-014e` POST `/api/tickets` · USER_B · esperado **409**
- [ ] `TC-BIZ-015g` POST `/api/tickets` · USER_A · esperado **404**

## 07 Seguridad / escenarios negativos
JWT inválidos y encabezados incorrectos. Un Bearer inválido siempre responde 401, también en rutas públicas. Ninguno de estos casos debe llegar al controller.

- [ ] `TC-AUTH-007` GET `/api/auth/me` · ANONYMOUS · esperado **401**
- [ ] `TC-AUTH-008` GET `/api/auth/me` · ANONYMOUS · esperado **401**
- [ ] `TC-AUTH-009` GET `/api/auth/me` · USER_A · esperado **401**
- [ ] `TC-AUTH-010` GET `/api/auth/me` · USER_A · esperado **401**
- [ ] `TC-AUTH-011` GET `/api/auth/me` · ANONYMOUS · esperado **401**
- [ ] `TC-AUTH-012` GET `/api/auth/me` · ANONYMOUS · esperado **401**
- [ ] `TC-AUTH-015` GET `/api/venues/active` · ANONYMOUS · esperado **401**
- [ ] `TC-AUTH-016` GET `/api/venues/active` · ANONYMOUS · esperado **401**
- [ ] `TC-AUTH-017` GET `/api/auth/me` · USER_A · esperado **200**

## 08 Validaciones y errores HTTP
Flujo E: regresión del contrato de errores. Todas usan un token válido del rol correcto, para que el error venga de la validación y no de la seguridad.

- [ ] `TC-HTTP-003` POST `/api/tickets` · USER_A · esperado **400**
- [ ] `TC-HTTP-004` POST `/api/tickets` · USER_A · esperado **400**
- [ ] `TC-HTTP-005` POST `/api/tickets` · USER_A · esperado **400**
- [ ] `TC-HTTP-006` POST `/api/tickets` · USER_A · esperado **400**
- [ ] `TC-HTTP-007` GET `/api/no-existe` · USER_A · esperado **404**
- [ ] `TC-HTTP-008` DELETE `/api/venues/active` · USER_A · esperado **405**
- [ ] `TC-HTTP-009` GET `/api/tickets/TCK-NOEXISTE` · ADMIN · esperado **404**

## 09 Flujos de negocio / secuencia recomendada
Escenarios que necesitan un cambio en la base de datos a mitad de camino (usuario desactivado y rol revocado). Van al final porque alteran fixtures. Los comandos SQL están en tests/e2e/fixtures/README.md.

- [ ] `FLOW-F0` POST `/api/auth/login` · INACTIVE_USER · esperado **200** — **copiar:** Copia `accessToken` a `inactiveToken`.
- [ ] `FLOW-F1` GET `/api/auth/me` · INACTIVE_USER · esperado **200**
- [ ] **SQL: desactivar la cuenta inactiva** (README de fixtures).
- [ ] `TC-AUTH-013` GET `/api/auth/me` · INACTIVE_USER · esperado **401**
- [ ] `TC-AUTH-013b` POST `/api/auth/login` · INACTIVE_USER · esperado **401**
- [ ] `FLOW-G1` GET `/api/auth/me` · STAFF · esperado **200**
- [ ] **SQL: revocar STAFF** (README de fixtures).
- [ ] `TC-SEC-017` PATCH `/api/tickets/{{ticketCapacityCode}}/use` · STAFF · esperado **403**
- [ ] `FLOW-G2` GET `/api/auth/me` · STAFF · esperado **200**
- [ ] **SQL: restaurar STAFF** (README de fixtures).
- [ ] `FLOW-G3` GET `/api/tickets/{{ticketCapacityCode}}` · USER_A · esperado **200**
- [ ] `FLOW-G4` PATCH `/api/tickets/{{ticketCapacityCode}}/use` · STAFF · esperado **200**

## Al terminar

- [ ] Las 22 rutas tienen al menos un caso nominal ejecutado (tabla de cobertura en `registro-ejecucion-manual.md`).
- [ ] Cada caso tiene PASS, FAIL, BLOCKED o NOT RUN; ningún caso omitido quedó como PASS.
- [ ] Los defectos encontrados están en la sección *Defectos*.
- [ ] Se borraron los *Current value* de contraseñas y tokens antes de exportar o compartir algo.
