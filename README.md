# DecididorAPI

Proyecto personal/de diversión: una API + mini frontend (Thymeleaf) que toma una decisión al azar entre varias opciones que vos le das (ej. "¿dónde comemos?"), y después te explica *cómo* llegó a ese resultado.

**Regla de diseño central**: vos solo aportás las opciones (y elegís *qué mecanismo* de azar usar) — nunca influís en *cuál* opción gana. Cada algoritmo decide de forma autónoma; la gracia del proyecto es el mecanismo en sí más una explicación transparente y posterior de cómo decidió.

## Stack

- **Java 21** + **Spring Boot 3** (Web, Data JPA, Security, Validation, Thymeleaf)
- **PostgreSQL** como base de datos
- **JWT** (`jjwt`) para la API REST + `formLogin` con sesión para la UI web
- **springdoc-openapi** para documentación interactiva de la API
- Maven (con wrapper, `mvnw`/`mvnw.cmd` — no hace falta un `mvn` global instalado)
- Desplegado en [Railway](https://railway.app/)

## Arquitectura

Hexagonal / ports-and-adapters, un paquete por capa bajo `com.eojeda89.decididorapi`:

```mermaid
flowchart LR
    subgraph adapters_in["adapter/in/web"]
        REST["DecisionController\nAuthController"]
        WEB["AppController\nLoginController\n(Thymeleaf)"]
    end

    subgraph application["application"]
        PORTIN["port/in\n(casos de uso)"]
        SERVICE["service\nDecisionService · AuthService\nRegisterUserService"]
        PORTOUT["port/out\n(repositorios)"]
    end

    subgraph domain["domain"]
        MODEL["model\nDecision · Option · User\nAlgorithmType"]
        ALGOS["service/algorithms\n(6 algoritmos, clases puras)"]
    end

    subgraph adapters_out["adapter/out/persistence"]
        JPA["Entidades JPA + mappers\n+ Spring Data repositories"]
    end

    DB[(PostgreSQL)]

    REST --> PORTIN
    WEB --> PORTIN
    PORTIN --> SERVICE
    SERVICE --> ALGOS
    SERVICE --> PORTOUT
    PORTOUT -.implementado por.-> JPA
    JPA --> DB
```

- **`domain/model`** — tipos de dominio planos (`Decision`, `Option`, `User`, `AlgorithmType`, value objects de id). `Decision` es el agregado; `AlgorithmType` es la fuente de verdad que mapea cada algoritmo a su `code` de API (ej. `"dice-roll"`) y su `uiName` en español para el dropdown de Thymeleaf.
- **`domain/service`** — la interfaz `DecisionAlgorithm` y sus seis implementaciones en `domain/service/algorithms/` (lanzamiento de dados, carrera de hilos, ruleta, aleatorio ponderado, votación aleatorizada, Fisher-Yates). Son clases puras, sin Spring — el wiring vive en `configuration/DecisionAlgorithmConfig`.
- **`application/port/in` y `application/port/out`** — interfaces de casos de uso y de repositorios salientes. Es la capa de la que depende todo lo demás.
- **`application/service`** — implementaciones de los casos de uso. `DecisionService.decide()` hace un **persist en dos fases**: guarda la `Decision` sin ganador (para obtener ids de opción generados por la DB), corre el algoritmo en memoria, resuelve el índice ganador al id ya persistido, y recién ahí guarda de nuevo con el ganador.
- **`adapter/in/web`** — `DecisionController`/`AuthController` (API REST JSON bajo `/api/decisions` y `/auth`), y `AppController`/`LoginController` (flujos Thymeleaf server-rendered para `/`, `/form`, `/decide`, `/register`, `/login`).
- **`adapter/out/persistence`** — entidades JPA + repositorios Spring Data, con un mapper por agregado traduciendo entre dominio y entidad.
- **`security`** — modelo dual: `formLogin` con sesión para la UI, y JWT bearer stateless para la API, coexistiendo en un solo `SecurityFilterChain`.

## Cómo levantarlo en local

### Opción 1: todo en Docker (recomendado)

Levanta Postgres y la app juntos, sin instalar nada más que Docker:

```bash
docker-compose up
```

La app queda disponible en `http://localhost:8080`.

### Opción 2: app local + Postgres en Docker

```bash
docker-compose up db          # solo levanta Postgres
DB_URL=jdbc:postgresql://localhost:5432/decididor \
DB_USERNAME=ideadbowner DB_PASSWORD=ideadbowner \
  ./mvnw spring-boot:run       # corre la app contra ese Postgres
```

**Importante**: si corrés `./mvnw spring-boot:run` sin `DB_URL`, la app va a intentar conectarse al Postgres remoto que trae como default en `application.properties` (no a un Postgres local) — tenés que setear `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` explícitamente como en el ejemplo de arriba, o usar la Opción 1. Copiá [`.env.example`](.env.example) a `.env` (o exportá esas variables en tu shell) — Spring Boot no lee `.env` automáticamente, así que tenés que exportarlas vos o pasarlas como `-D` a la JVM.

`spring.jpa.hibernate.ddl-auto=update` — no hay migraciones Flyway/Liquibase todavía, el schema se deriva de las entidades JPA.

### Variables de entorno

| Variable | Default si no se setea | Descripción |
|---|---|---|
| `PORT` | `8080` | Puerto HTTP (Railway inyecta el suyo en producción) |
| `DB_URL` | Postgres remoto en Koyeb (ver `application.properties`) | URL JDBC completa, no el formato `postgresql://user:pass@host/db`. Para desarrollo local, setealo explícitamente (ver arriba) |
| `DB_USERNAME` | `ideadbowner` | Usuario de Postgres |
| `DB_PASSWORD` | `ideadbowner` | Password de Postgres |
| `security.jwt.secret` | `dev-secret-change` | Secreto para firmar JWT — **cambiar en cualquier entorno público** |
| `security.jwt.expiration-ms` | `3600000` (1h) | Duración del token JWT |

## Tests

```bash
./mvnw test                                                        # suite completa
./mvnw test -Dtest=DecisionServiceTest                             # una clase
./mvnw test -Dtest=DecisionServiceTest#decide_shouldPersistWinner  # un método
```

CI corre esta misma suite en cada push/PR a `master` y `develop` (ver [`.github/workflows/ci.yml`](.github/workflows/ci.yml)).

## Documentación de la API

Con la app corriendo, la documentación interactiva (Swagger UI) queda en `http://localhost:8080/swagger-ui.html`, y el JSON de OpenAPI en `http://localhost:8080/v3/api-docs`.

Endpoints principales:

- `POST /auth/register` — registra un usuario
- `POST /auth/login` — devuelve un JWT
- `POST /api/decisions` — toma una decisión (requiere `Authorization: Bearer <token>`)
- `GET /api/decisions?userId=` — historial de decisiones de un usuario

## Deploy

Desplegado en [Railway](https://railway.app/) vía un único `Dockerfile` (`eclipse-temurin:21-jdk`, alineado con el `java.version` del `pom.xml`). Postgres corre como un servicio Railway separado en el mismo proyecto (redes privadas `*.railway.internal`). `DB_URL` tiene que ser una URL JDBC completa (`jdbc:postgresql://postgres.railway.internal:5432/railway`) con las credenciales pasadas por separado en `DB_USERNAME`/`DB_PASSWORD` — el driver rechaza el formato `postgresql://user:pass@host/db` que entrega Railway si se pega directo en `DB_URL`.

## Licencia

[MIT](LICENSE)
