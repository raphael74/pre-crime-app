# Pre-Crime Application: Antigravity Context

This project is a reference implementation of **Domain-Driven Design (DDD)** and **Onion Architecture** using a modern
Kotlin/Spring Boot backend and an Angular frontend. It is themed around a futuristic "Pre-Crime" department.

## Project Overview

- **Purpose**: Demonstrate clean architecture, reliable event-driven communication (Transactional Outbox), and modern
  tech stacks.
- **Backend**: Kotlin 2.3.20, Java 25, Spring Boot 4.0.5.
- **Frontend**: Angular 21 with a "HUD" (Heads-Up Display) aesthetic.
- **Architecture**:
  - **Onion Architecture**: Strict separation between `domain`, `application`, and `infrastructure` layers.
  - **DDD**: Uses jMolecules for DDD primitives (Aggregates, Entities, Value Objects, Domain Events).
  - **Event-Driven**: Kafka is used for asynchronous communication between aggregates.
  - **Reliability**: Implements the **Transactional Outbox Pattern** to ensure domain events are published to Kafka
    only after successful database transactions.

## Core Aggregates

1. **Precog Division**: Responsible for foreseeing future crimes and recording prevention statistics.
2. **Law Enforcement Unit**: Responsible for executing "pre-arrests" based on visions received via domain events.
3. **Pre-Apology**: Automatically generates apologies and compensation statements for the families of pre-arrested
   individuals.

## Technical Stack

| Component            | Technology                         |
|----------------------|------------------------------------|
| **Language**         | Kotlin (Backend), TypeScript (UI)  |
| **Frameworks**       | Spring Boot, Angular               |
| **Persistence**      | PostgreSQL 18, jOOQ, Flyway        |
| **Messaging**        | Kafka 4.2.0, Apicurio Registry     |
| **Testing**          | JUnit 5, ArchUnit, jMolecules-test |
| **Containerization** | Docker Compose                     |

## Building and Running

### Prerequisites

- Java 25+
- Node.js (v22+)
- Docker & Docker Compose

### Infrastructure

Start the database and Kafka:

```bash
docker-compose up -d
```

### Backend

Run the Spring Boot application:

```bash
./mvnw spring-boot:run
```

Tests: `./mvnw test`

### Frontend

Navigate to the `ui` directory and start the dev server:

```bash
cd ui
npm install
npm start
```

The UI will be available at `http://localhost:4200` (proxied to `/api` on `localhost:8080`).

## Development Conventions

- **Onion Constraints**: Architectural boundaries are enforced by **ArchUnit** and **jMolecules** in
  `ArchitectureTest.kt`.
- **Domain First**: Business logic resides strictly in the `domain` package and should have no dependencies on external
  frameworks (except jMolecules annotations).
- **Type-Safe SQL**: All database interactions use **jOOQ**. Do not use raw strings for queries if possible.
- **Reliable Messaging**: Never publish directly to Kafka from the application service. Use `DomainEventPublisher`,
  which persists events to the `outbox` table within the same transaction.
- **OpenAPI First**: The REST API is defined in `openapi.yaml`. Backend controller interfaces and DTOs are generated
  using the `openapi-generator-maven-plugin`. Frontend services and models are generated using
  `@openapitools/openapi-generator-cli`.
- **Kotlin Style**: Use idiomatic Kotlin (data classes, expressions, null safety). Avoid `!!` and prefer `val` over
  `var`.
- **UI Architecture**: Angular components should be standalone. Use `HttpClient` for backend interactions via the
  configured proxy.

# Angular Development

Follow the following best practices:

You are an expert in TypeScript, Angular, and scalable web application development. You write functional, maintainable, performant, and accessible code following Angular and TypeScript best practices.

## TypeScript Best Practices

- Use strict type checking
- Prefer type inference when the type is obvious
- Avoid the `any` type; use `unknown` when type is uncertain

## Angular Best Practices

- Always use standalone components over NgModules
- Must NOT set `standalone: true` inside Angular decorators. It's the default in Angular v20+.
- Do NOT set `changeDetection: ChangeDetectionStrategy.OnPush` explicitly. `OnPush` is the default in Angular v22+.
- Use signals for state management
- Implement lazy loading for feature routes
- Do NOT use the `@HostBinding` and `@HostListener` decorators. Put host bindings inside the `host` object of the `@Component` or `@Directive` decorator instead
- Use `NgOptimizedImage` for all static images.
  - `NgOptimizedImage` does not work for inline base64 images.

## Accessibility Requirements

- It MUST pass all AXE checks.
- It MUST follow all WCAG AA minimums, including focus management, color contrast, and ARIA attributes.

### Components

- Keep components small and focused on a single responsibility
- Use `input()` and `output()` functions instead of decorators
- Use `computed()` for derived state
- Prefer inline templates for small components
- Prefer Signal Forms (`@angular/forms/signals`) for new forms. They are stable in Angular v22+ and provide signal-based state, type-safe field access, and schema-based validation
- When not using Signal Forms, prefer Reactive forms instead of Template-driven ones
- Do NOT use `ngClass`, use `class` bindings instead
- Do NOT use `ngStyle`, use `style` bindings instead
- When using external templates/styles, use paths relative to the component TS file.

## State Management

- Use signals for local component state
- Use `computed()` for derived state
- Keep state transformations pure and predictable
- Do NOT use `mutate` on signals, use `update` or `set` instead

## Templates

- Keep templates simple and avoid complex logic
- Use native control flow (`@if`, `@for`, `@switch`) instead of `*ngIf`, `*ngFor`, `*ngSwitch`
- Use the async pipe to handle observables
- Do not assume globals like (`new Date()`) are available.

## Services

- Design services around a single responsibility
- Use the `providedIn: 'root'` option for singleton services
- Prefer the `@Service` decorator over `@Injectable({providedIn: 'root'})` for new singleton services (Angular v22+)
- Use the `inject()` function instead of constructor injection
