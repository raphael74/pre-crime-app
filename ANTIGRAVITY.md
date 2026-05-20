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
