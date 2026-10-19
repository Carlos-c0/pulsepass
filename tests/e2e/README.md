# Pruebas E2E manuales con Postman (Fase 5)

Recorrido de caja negra de PulsePass: Postman → servidor Spring Boot real → Spring Security → controllers → services → PostgreSQL. No hay mocks, scripts de Postman, Collection Runner, Newman ni CLI: cada request se envía a mano con **Send** y una persona revisa la respuesta.

| Archivo | Para qué |
|---|---|
| `postman/PulsePass-Manual-E2E.postman_collection.json` | Las 127 requests, en 10 carpetas, cada una con su ID, actor, precondiciones y resultado esperado en la descripción |
| `postman/PulsePass-local.postman_environment.json` | Variables del ambiente, **sin** contraseñas ni tokens |
| `fixtures/seed-e2e.sql` y `fixtures/README.md` | Base exclusiva `pulsepass_e2e`, datos de prueba y comandos SQL de la carpeta 09 |
| `checklist-e2e-manual.md` | Orden obligatorio de ejecución |
| `registro-ejecucion-manual.md` | Bitácora: esperado, observado y PASS / FAIL / BLOCKED / NOT RUN |

Cubre las 22 rutas: los 20 endpoints de negocio y `POST /api/auth/login` y `GET /api/auth/me`.

## Requisitos

- Docker en ejecución y el `docker-compose.yml` del proyecto.
- JDK 21 (la API se arranca con `./mvnw spring-boot:run`).
- Postman Desktop (o la versión web con Postman Agent para llegar a `localhost`).
- Git Bash para los comandos (incluye `openssl`).

## Preparación

1. **Base y aplicación.** Sigue los pasos 1 y 2 de `fixtures/README.md`: recrea `pulsepass_e2e` y arranca la API contra ella con una clave JWT aleatoria.
2. **Importar.** En Postman: *Import* → arrastra los dos archivos de `postman/`. Arriba a la derecha elige el ambiente **PulsePass local E2E**.
3. **Contraseñas.** En el ambiente, columna **Current value**, escribe una contraseña de 12 a 72 caracteres para `userAPassword`, `userBPassword`, `userCPassword`, `adminPassword`, `staffPassword` e `inactivePassword`. Deja vacía la columna *Initial value*: es la que se exporta y se comparte.
4. **Datos.** Envía PREP-01 a PREP-04 y luego carga el seed (paso 3 de `fixtures/README.md`). PREP-05 confirma que quedó bien.

## Cómo ejecutar cada request

1. Abre la request y lee su descripción (pestaña *Docs* o el ícono de documentación): ID, actor, precondiciones, HTTP esperado y qué revisar.
2. Revisa la pestaña **Authorization**: debe decir *Bearer Token* con la variable del actor (`{{userAToken}}`, `{{staffToken}}`, …) o *No Auth* para un anónimo. Pasa el mouse sobre la variable para ver si tiene valor.
3. Pulsa **Send**.
4. Compara **Status**, **Headers** (`Content-Type: application/json`) y **Body** con lo esperado. Un 200 o 201 no basta: revisa los campos que pide la descripción.
5. Si la descripción dice **Copiar**, copia ese valor del Body a la variable indicada (pestaña del ambiente, columna *Current value*) y guarda.
6. Anota la fila en `registro-ejecucion-manual.md`.

### Copiar un token

Después de cada login: en el Body, selecciona solo el valor de `accessToken` (sin comillas), cópialo y pégalo en la variable del actor, columna **Current value**. Luego envía la request `/me` de ese actor para confirmar que el token es el correcto.

Los tokens duran 15 minutos (`expiresIn` = 900). Si en mitad del recorrido aparece un 401 que no se esperaba, repite el login de ese actor (carpeta 03) y vuelve a copiar el token. Si se reinicia la aplicación, todos los tokens dejan de servir porque la clave cambia.

## Resultados

| Resultado | Cuándo |
|---|---|
| `PASS` | El HTTP, el JSON y el efecto de negocio o de seguridad coinciden con lo esperado |
| `FAIL` | Algo no coincide, o apareció un 500 |
| `BLOCKED` | No se pudo ejecutar porque faltó una precondición, un actor, un token o un dato de una respuesta anterior |
| `NOT RUN` | No se ejecutó |

Un caso que depende de otro que falló o quedó bloqueado se marca `BLOCKED`, nunca `PASS`. Cualquier 500 en un escenario nominal es `FAIL`.

## Políticas que comprueban las pruebas

- **Recurso ajeno → 403** (`expectedForeignResourceStatus`). La API decide el 403 antes de mirar si el recurso existe, así que pedir el email de otro responde 403 exista o no. Un ADMIN sí recibe 404 cuando no existe.
- **Borrador → 404** para anónimos y USER, como si no existiera. STAFF y ADMIN lo ven.
- **Bearer inválido → 401** siempre, también en rutas públicas.
- **Denegar no cambia nada.** Después de cada 403 sobre algo que se puede modificar (publicar, asociar, cancelar, usar, comprar) hay una request que vuelve a leer el recurso con un actor autorizado.
- **Tickets distintos para operaciones irreversibles.** T1 se cancela y T2 se usa; nunca el mismo.

## Confidencialidad

- Nunca escribas contraseñas ni tokens en *Initial value*, en la colección, en la bitácora ni en un commit.
- Si exportas el ambiente para compartirlo, primero borra todos los *Current value* de contraseñas y tokens (o exporta solo los *Initial value*, que están vacíos).
- Las capturas de evidencia no deben mostrar el encabezado Authorization ni el `accessToken`. Si alguna lo muestra, guárdala en `evidencias-privadas/` (está en `.gitignore`).
- `foreignKeyToken` sí está en el ambiente: está firmado con una clave aleatoria que se descartó, así que no sirve en ningún servidor.

## Repetir

Para empezar de cero, recrea la base (paso 1 de `fixtures/README.md`), reinicia la aplicación y vuelve a PREP-01. Con la base nueva, los mismos valores del ambiente sirven otra vez.
