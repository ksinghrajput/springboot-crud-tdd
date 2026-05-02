# demo

A Spring Boot 4 REST API for managing users, built as a learning project. Demonstrates a standard layered architecture (controller → service → repository) with an H2 in-memory database, bean validation, a global exception handler, and a three-layer JUnit 5 test suite (unit, web slice, JPA slice).

## Tech stack

- **Java 17**
- **Spring Boot 4.0.5** — web MVC, Data JPA, Validation, H2 console
- **H2** — in-memory database (recreated on every startup)
- **Lombok** — `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` on the entity
- **JUnit 5 + Mockito** — unit tests with `@MockitoBean`
- **Maven Wrapper** — no system-installed Maven needed

## Prerequisites

- JDK 17+
- The Maven wrapper is committed — use `./mvnw` (Git Bash / Linux / macOS) or `mvnw.cmd` (Windows cmd). Do not install Maven separately.

## Run the application

```bash
./mvnw spring-boot:run
```

App starts on `http://localhost:8080`. Console prints `server started` once ready.

### H2 console

When the datasource properties in `src/main/resources/application.properties` are active, the H2 web console is available at:

```
http://localhost:8080/h2-console
```

Use JDBC URL `jdbc:h2:mem:testdb` with user `sa` and no password.

## Build

```bash
./mvnw clean package      # produces target/demo-0.0.1-SNAPSHOT.jar
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

## Authentication

The API uses **stateless JWT (HS256) authentication**. Public endpoints: `/api/auth/register`, `/api/auth/login`, `/h2-console/**`. Everything under `/api/users/**` requires a valid `Authorization: Bearer <token>` header.

A seeded admin is created on first startup:

| Email             | Password   | Role  |
|-------------------|------------|-------|
| `admin@demo.com`  | `admin123` | ADMIN |

### Auth endpoints

| Method | Path                  | Body                              | Success |
|--------|-----------------------|-----------------------------------|---------|
| POST   | `/api/auth/register`  | `{ name, email, password, age }`  | 201     |
| POST   | `/api/auth/login`     | `{ email, password }`             | 200     |

### Auth flow (curl)

```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Kishan","email":"kishan@example.com","password":"secret123","age":25}'

# 2. Login -> capture token
TOKEN=$(curl -sX POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"kishan@example.com","password":"secret123"}' | jq -r .token)

# 3. Call protected endpoint
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN"
```

### Role-based access on `/api/users/**`

| Method | Path                       | Required role     |
|--------|----------------------------|-------------------|
| GET    | `/api/users/**`            | USER or ADMIN     |
| POST   | `/api/users`               | ADMIN             |
| PUT    | `/api/users/{id}`          | ADMIN             |
| DELETE | `/api/users/{id}`          | ADMIN             |

### JWT configuration

Set in `src/main/resources/application.properties` (overridable via env):

```properties
app.jwt.secret=${JWT_SECRET:change-me-in-prod-min-32-chars-please-1234}
app.jwt.expiration-ms=3600000
```

> The default secret is for local dev only. Override `JWT_SECRET` in production with a value of at least 32 characters.

## REST API

Base path: `/api/users` — **all endpoints require authentication**.

| Method | Path                          | Description                       | Success | Role          |
|--------|-------------------------------|-----------------------------------|---------|---------------|
| GET    | `/api/users`                  | List all users                    | 200     | USER or ADMIN |
| GET    | `/api/users/{id}`             | Get user by id                    | 200     | USER or ADMIN |
| GET    | `/api/users/email/{email}`    | Get user by email                 | 200     | USER or ADMIN |
| POST   | `/api/users`                  | Create a user (validated payload) | 201     | ADMIN         |
| PUT    | `/api/users/{id}`             | Update an existing user           | 200     | ADMIN         |
| DELETE | `/api/users/{id}`             | Delete a user                     | 204     | ADMIN         |

### User payload

```json
{
  "name": "Kishan",
  "email": "kishan@example.com",
  "age": 25
}
```

Validation rules (enforced by `@Valid` on request bodies):

- `name` — required, not blank
- `email` — required, valid email format, unique in DB
- `age` — between 0 and 150

### Error responses

Handled centrally by `exception.GlobalExceptionHandler`. Shape:

```json
{
  "timestamp": "2026-04-22T09:30:47",
  "status": 404,
  "error": "Not Found",
  "message": "User 99",
  "path": "/api/users/99"
}
```

| Exception                            | Status |
|--------------------------------------|--------|
| `ResourceNotFoundException`          | 404    |
| `DataIntegrityViolationException`    | 409    |
| `BadCredentialsException`            | 401    |
| `AuthenticationException`            | 401    |
| `AccessDeniedException`              | 403    |
| `IllegalArgumentException`           | 400    |
| `MethodArgumentTypeMismatchException`| 400    |
| Any other `Exception`                | 500    |

> **Known gap:** `MethodArgumentNotValidException` (thrown by `@Valid` failures) is currently not handled explicitly, so validation errors return 500 instead of 400. Fix is to add a dedicated `@ExceptionHandler` in `GlobalExceptionHandler`.

## Testing

```bash
./mvnw test                                    # all tests (24)
./mvnw test -Dtest=UserServiceTest             # one class
./mvnw test -Dtest=UserServiceTest#getAllUsers_returnsList   # one method
```

### Test layout

| File                       | Type            | What it exercises                                  | Count |
|----------------------------|-----------------|----------------------------------------------------|-------|
| `UserServiceTest`          | Pure unit       | Service logic with a mocked repository             | 9     |
| `UserControllerTest`       | `@WebMvcTest`   | HTTP layer + `MockMvc` with a mocked service       | 7     |
| `UserRepositoryTest`       | `@DataJpaTest`  | JPA queries + constraints against in-memory H2     | 7     |
| `DemoApplicationTests`     | `@SpringBootTest` | Full-context smoke test                          | 1     |

Each layer uses a different technique on purpose — this follows the classic **test pyramid**: many fast unit tests at the bottom, fewer slice tests in the middle, a handful of full-context tests at the top.

## Project structure

```
src/main/java/
├── com/example/demo/
│   └── DemoApplication.java          @SpringBootApplication entry point
├── config/
│   ├── SecurityConfig.java           SecurityFilterChain + auth rules
│   └── AdminSeeder.java              CommandLineRunner that seeds admin@demo.com
├── controller/
│   ├── UserController.java           REST endpoints (protected)
│   └── AuthController.java           /api/auth/register, /api/auth/login
├── service/
│   ├── UserService.java              Business logic
│   └── AuthService.java              Register + login orchestration
├── repository/
│   └── UserRepository.java           Spring Data JPA interface
├── security/
│   ├── JwtService.java               Generate + parse HS256 tokens (JJWT)
│   ├── JwtAuthFilter.java            OncePerRequestFilter — extracts Bearer token
│   ├── JwtAuthenticationEntryPoint.java  Returns JSON 401 on missing/bad auth
│   └── CustomUserDetailsService.java Loads users by email for AuthenticationManager
├── dto/
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   └── AuthResponse.java
├── model/
│   ├── User.java                     JPA entity (now includes password + role)
│   └── Role.java                     enum { USER, ADMIN }
└── exception/
    ├── GlobalExceptionHandler.java   @RestControllerAdvice (auth handlers added)
    ├── ResourceNotFoundException.java
    └── ErrorResponse.java
```

### Package layout caveat

This project deliberately uses **top-level packages** (`controller`, `service`, `repository`, `model`, `exception`) instead of nesting them under `com.example.demo`. By default, `@SpringBootApplication` only component-scans its own package and subpackages, so `DemoApplication` is annotated with explicit scan instructions:

```java
@SpringBootApplication(scanBasePackages = { "com.example.demo", "controller", "service", "exception", "config", "security" })
@EntityScan("model")
@EnableJpaRepositories("repository")
```

If you add new packages, remember to extend the scan list. The test classes also mirror this structure and provide nested `@SpringBootConfiguration` test configs to keep the JPA and MVC slices working.

### Configuration files

Two files named `application.properties` exist in this repo:

- `src/main/resources/application.properties` — **on the classpath**, this is the one that's actually read at runtime. Put datasource, JPA, and H2 console settings here.
- `application.properties` (repo root) — **not on the classpath**, currently ignored. Slated for consolidation.

## Notes on dependencies

`pom.xml` uses the Spring Boot 4.0.5 starter names (`spring-boot-starter-webmvc`, `spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test`, `spring-boot-h2console`) rather than the pre-4.x conventional names (`spring-boot-starter-web`, `spring-boot-starter-test`). If a dependency fails to resolve after an upgrade, cross-check against the Spring Boot 4.0.5 BOM rather than assuming a typo.

Spring Boot 4 also moved several classes:

| Class               | Old package                                           | New package                                           |
|---------------------|-------------------------------------------------------|-------------------------------------------------------|
| `@WebMvcTest`       | `org.springframework.boot.test.autoconfigure.web.servlet` | `org.springframework.boot.webmvc.test.autoconfigure`  |
| `@DataJpaTest`      | `org.springframework.boot.test.autoconfigure.orm.jpa`     | `org.springframework.boot.data.jpa.test.autoconfigure`|
| `TestEntityManager` | `org.springframework.boot.test.autoconfigure.orm.jpa`     | `org.springframework.boot.jpa.test.autoconfigure`     |
| `ObjectMapper`      | `com.fasterxml.jackson.databind`                          | `tools.jackson.databind` (Jackson 3)                  |
| `@MockBean`         | `org.springframework.boot.test.mock.mockito`              | `@MockitoBean` in `org.springframework.test.context.bean.override.mockito` |

## License

Unlicensed — for learning/interview preparation.
