# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

Maven wrapper is committed — use `./mvnw` (bash) or `mvnw.cmd` (Windows cmd) rather than a system `mvn`.

- Run the app: `./mvnw spring-boot:run`
- Build jar: `./mvnw clean package` (artifact lands in `target/`)
- Run all tests: `./mvnw test`
- Run a single test class: `./mvnw test -Dtest=DemoApplicationTests`
- Run a single test method: `./mvnw test -Dtest=DemoApplicationTests#contextLoads`

Runtime:
- H2 in-memory DB (`jdbc:h2:mem:testdb`), schema is recreated on every start (`ddl-auto=create-drop`).
- H2 web console is enabled at `/h2-console` when the datasource properties are actually loaded (see caveat below).
- REST base path: `/api/users` (GET list, GET `/{id}`, GET `/email/{email}`, POST `/`, PUT `/{id}`, DELETE `/{id}`).

## Architecture

Standard Spring Boot 4.0.5 / Java 17 layered CRUD app (controller → service → JPA repository → H2). Lombok is used on the `User` entity (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`) and is wired as an annotation processor in `pom.xml` — keep Lombok installed in the IDE or annotation processing enabled or the entity will appear to have no getters/setters.

### Package layout caveat (important)

This project has an unusual package structure that future changes must preserve or deliberately fix:

- `com.example.demo.DemoApplication` — the `@SpringBootApplication` entry point.
- `controller`, `service`, `repository`, `model` — **top-level packages at the Java source root**, NOT nested under `com.example.demo`.

By default `@SpringBootApplication` only component-scans its own package and sub-packages, so these top-level packages sit **outside** the default scan path. If beans stop being discovered (e.g. `UserController` not registered, `UserService` autowiring fails), the fix is either:
- Move the packages under `com.example.demo.*`, or
- Add explicit scans on `DemoApplication`, e.g. `@SpringBootApplication(scanBasePackages = {"com.example.demo", "controller", "service", "repository"})` plus `@EntityScan("model")` and `@EnableJpaRepositories("repository")`.

When adding new controllers/services/repositories, follow whichever layout is in force at the time — don't mix both.

### Configuration file caveat

There are two files named `application.properties`:

- `src/main/resources/application.properties` — on the classpath, currently only sets `spring.application.name=demo`.
- `./application.properties` (repo root) — contains the H2 datasource, JPA, and H2 console settings, but is **not on the classpath** and is ignored at runtime unless the working directory matches or it is moved.

Datasource / JPA / H2-console changes should go in `src/main/resources/application.properties`. The root-level file is likely a misplacement — prefer consolidating into the resources file rather than duplicating.

## Dependencies note

`pom.xml` references Spring Boot 4.0.5 artifacts with non-standard starter names (`spring-boot-h2console`, `spring-boot-starter-webmvc`, `spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test`) rather than the conventional `spring-boot-starter-web` / `spring-boot-starter-test`. If a dependency fails to resolve, check against the Spring Boot 4.0.5 BOM before renaming.
