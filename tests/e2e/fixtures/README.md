# Fixtures E2E

Datos controlados para la Fase 5. Todo se hace sobre una base **exclusiva**, `pulsepass_e2e`, que se recrea desde cero en cada corrida. Nunca se usan estos comandos contra la base `pulsepass` de desarrollo.

Los comandos están escritos para Git Bash (o la terminal de IntelliJ con Git Bash) y suponen el contenedor `pulsepass-db` del `docker-compose.yml` del proyecto.

## 1. Recrear la base E2E

Con la aplicación **detenida**:

```bash
docker compose up -d
docker exec pulsepass-db psql -U postgres -c "DROP DATABASE IF EXISTS pulsepass_e2e WITH (FORCE)"
docker exec pulsepass-db psql -U postgres -c "CREATE DATABASE pulsepass_e2e"
```

Revisa que los dos comandos digan `pulsepass_e2e`: la base de desarrollo (`pulsepass`) no se toca.

## 2. Arrancar la aplicación contra la base E2E

Desde la raíz del proyecto, en la misma terminal:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/pulsepass_e2e
export PULSEPASS_JWT_SECRET_BASE64=$(openssl rand -base64 32)
./mvnw spring-boot:run
```

Flyway crea el esquema (V1 a V4) al arrancar. La clave es aleatoria y solo vive en esa terminal: no se escribe en ningún archivo. Si se reinicia la aplicación, la clave cambia y todos los tokens anteriores dejan de servir (hay que repetir los login).

## 3. Cargar el seed

Primero envía en Postman las requests **PREP-01 a PREP-04** (registran `e2e_admin`, `e2e_staff` y `e2e_inactivo`). Luego, en otra terminal:

```bash
docker exec -i pulsepass-db psql -U postgres -d pulsepass_e2e < tests/e2e/fixtures/seed-e2e.sql
```

En PowerShell: `Get-Content tests/e2e/fixtures/seed-e2e.sql | docker exec -i pulsepass-db psql -U postgres -d pulsepass_e2e`.

Al final debe aparecer:

```text
 username  | role
-----------+-------
 e2e_admin | ADMIN
 e2e_staff | STAFF
```

Si dice `Faltan e2e_admin o e2e_staff`, falta enviar PREP-02 o PREP-03. Si dice `solo se ejecuta en pulsepass_e2e`, el comando apunta a otra base. El seed se puede correr dos veces sin duplicar nada.

### Qué crea

| Fixture | Variable de Postman | Para qué |
|---|---|---|
| Venue `E2E-VEN-ACT` (activo, capacidad 500) | `venueActiveCode` | Consultas y creación de eventos |
| Venue `E2E-VEN-INA` (inactivo) | `venueInactiveCode` | Crear un evento en un venue inactivo → 409 |
| Venue `E2E-VEN-UNO` (activo, capacidad 1) | — | Aforo de una sola entrada |
| Artista `E2E Retired Artist` (inactivo) | — | No debe salir en `/api/artists/active` |
| Evento `E2E-PUB-01` (PUBLISHED, edad 18, con Neon Waves) | `publishedEventCode` | Compras, consultas y tickets pagados |
| Evento `E2E-DRAFT-01` (DRAFT, con Neon Waves) | `draftEventCode` | Visibilidad de borradores |
| Evento `E2E-TOPUB-01` (DRAFT) | `eventToPublishCode` | USER y STAFF intentan publicarlo |
| Evento `E2E-EDAD-99` (PUBLISHED, edad 99) | `ageEventCode` | Compra sin la edad mínima → 409 |
| Evento `E2E-AFORO-1` (PUBLISHED, venue de capacidad 1) | `capacityEventCode` | Agotar el aforo → SOLD_OUT |
| `e2e_admin` pasa de USER a ADMIN | `adminIdentifier` | Operaciones de administración |
| `e2e_staff` pasa de USER a STAFF | `staffIdentifier` | Operaciones de taquilla |

Los usuarios `USER_A`, `USER_B` y el usuario C **no** están en el seed: se registran por la API en la carpeta 03, porque el registro también es parte de lo que se prueba.

## 4. Comandos de la carpeta 09

Se ejecutan solo cuando la request de la carpeta 09 lo indica.

**Desactivar la cuenta inactiva** (después de FLOW-F1):

```bash
docker exec pulsepass-db psql -U postgres -d pulsepass_e2e -c "UPDATE users SET active = FALSE WHERE username = 'e2e_inactivo'"
```

**Revocar STAFF** (después de FLOW-G1):

```bash
docker exec pulsepass-db psql -U postgres -d pulsepass_e2e -c "DELETE FROM user_roles WHERE role = 'STAFF' AND user_id = (SELECT id FROM users WHERE username = 'e2e_staff')"
```

**Restaurar STAFF** (después de FLOW-G2):

```bash
docker exec pulsepass-db psql -U postgres -d pulsepass_e2e -c "INSERT INTO user_roles (user_id, role) SELECT id, 'STAFF' FROM users WHERE username = 'e2e_staff' ON CONFLICT DO NOTHING"
```

Cada comando debe responder `UPDATE 1`, `DELETE 1` o `INSERT 0 1`. Si responde `0`, la cuenta no existe en esta base y los casos que dependen de él quedan BLOCKED.

## Repetir una corrida

Volver al paso 1. Al recrear la base, los valores del ambiente (`runId`, usuarios, `newEventCode`) se pueden reutilizar tal cual. Si por algún motivo no se recrea la base, hay que cambiar `runId` y todos los valores del ambiente que lo llevan (`e2e_andrea_r01`, `andrea.r01@example.test`, `E2E-NEW-r01`, …), o el registro responderá 409.
