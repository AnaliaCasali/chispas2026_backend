# Chispas 2026 — Backend

API REST de **Chispas**, la plataforma del IES 63 para que docentes y estudiantes trabajen con carpetas, tareas, horarios y capacidades.

- **Producción:** https://chispas.ies63lastoscas.edu.ar (la API vive en `/api/v1`)
- **Frontend:** [chispas2026_frontend](https://github.com/AnaliaCasali/chispas2026_frontend)

## Tecnologías

- Java 25 y Spring Boot 4.1 (Web, Data JPA, Security, Validation y Mail)
- MySQL 8 con Hibernate
- Autenticación con JWT
- Tests con JUnit 5, Mockito y Testcontainers (MySQL en Docker), y cobertura con JaCoCo
- Docker, con imagen publicada en GitHub Container Registry

## Requisitos

- JDK 25 (no hace falta Maven: se usa el wrapper `mvnw`)
- MySQL 8 local, o Docker
- Docker Desktop para correr los tests

## Correr en local

1. Copiar `.env.example` como `.env` y completar los valores. Generar `JWT_SECRET` con `openssl rand -base64 32`, y usar `JPA_DDL_AUTO=update`.
2. Levantar la app:

   ```bash
   ./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
   ```

La API queda en http://localhost:8080/api/v1.

### Base de datos

No hace falta crear nada a mano:

- La base se crea sola (`createDatabaseIfNotExist=true`) y Hibernate crea las tablas a partir de las entidades.
- En el **primer arranque**, si la tabla `usuario` está vacía, se cargan los datos de ejemplo de `src/main/resources/db/chispasinserts.sql`. En los arranques siguientes no se vuelve a cargar.
- Para arrancar con la base vacía, usar `DATOS_INICIALES=false`.

## Variables de entorno

| Variable | Para qué |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | Conexión a MySQL |
| `JPA_DDL_AUTO` | `update` (por defecto): Hibernate crea y actualiza las tablas |
| `DATOS_INICIALES` | `true` (por defecto) carga los datos de ejemplo la primera vez |
| `JWT_SECRET`, `JWT_EXPIRATION` | Firma y duración del token (base64 de 32 bytes, en ms) |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | Envío de mails (Gmail con contraseña de aplicación) |
| `FRONTEND_URL`, `CORS_ALLOWED_ORIGINS` | URL del frontend y orígenes permitidos |
| `ZONA_HORARIA` | Zona para calcular la materia actual (`America/Argentina/Buenos_Aires`) |

## Tests

```bash
./mvnw verify
```

- Los tests levantan su propio MySQL con **Testcontainers**, así que solo hace falta tener Docker abierto: no usan el `.env` ni un MySQL local.
- La base de los tests sale de `src/test/resources/db/chispas_bd.sql` (esquema y datos), con `ddl-auto=validate` para comprobar que las entidades coinciden.
- El reporte de cobertura queda en `target/site/jacoco/index.html`.

En IntelliJ: después de cambiar el `pom.xml`, recargar el proyecto Maven antes de correr los tests.

## Endpoints principales

| Ruta | Descripción |
|---|---|
| `/api/v1/auth` | Login, registro y recuperación de contraseña |
| `/api/v1/inicio` | Pantalla de inicio |
| `/api/v1/mi-carpeta` | Carpetas y materias |
| `/api/v1/mi-horario` | Horario y materia actual |
| `/api/v1/tareas` | Tareas y entregas |
| `/api/v1/estudiantes` | Perfil, observaciones y capacidades del estudiante |

En local, la documentación OpenAPI está en http://localhost:8080/swagger-ui.html (en producción está desactivada).

## Estructura

```
src/main/java/edu/ies63/chispas2026_backend/
├── config/        configuración (reloj, OpenAPI, datos iniciales)
├── controllers/   endpoints REST
├── dtos/          objetos de entrada y salida
├── enums/
├── exceptions/    errores y manejador global
├── models/        entidades JPA
├── repositories/  Spring Data JPA
├── security/      JWT y configuración de Spring Security
└── services/      lógica de negocio
deploy/            archivos del servidor (compose, MySQL, deploy.sh)
```

## CI/CD y deploy

- **`.github/workflows/ci.yml` (CI/CD):** en cada push y pull request corre `./mvnw verify`. Al mergear a `main`, además publica la imagen en `ghcr.io/analiacasali/chispas2026_backend`.
- **`.github/workflows/deploy.yml`:** cuando el CI termina bien en `main`, entra por SSH al servidor y ejecuta `deploy.sh back`, que baja la imagen nueva y reinicia el contenedor.
- **`deploy/`:** el `compose.yaml` del servidor (MySQL, back y front), su `.env.example`, la configuración de MySQL y `deploy.sh`. En el servidor viven en `/root/chispas/`, junto con el `.env` real, que no se sube al repo.
