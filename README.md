# Pre-Crime Application

This application is a reference implementation of **Domain-Driven Design (DDD)** and **Onion Architecture** using a
modern Kotlin/Spring Boot backend and an Angular frontend. It is themed around a futuristic "Pre-Crime" department.

## Purpose

The project demonstrates:

- **Onion Architecture**: Strict separation between `domain`, `application`, and `infrastructure` layers.
- **DDD Patterns**: Implementation of Aggregates, Entities, Value Objects, Domain Events, and Repositories using *
  *jMolecules**.
- **Event-Driven Communication**: Using **Kafka** for asynchronous communication between aggregates.
- **Transactional Outbox Pattern**: Ensuring reliable event delivery without distributed transactions.
- **OpenAPI-First**: API-first development with generated DTOs and Controller interfaces for both Backend and Frontend.

## Concept

The application is loosely based on the movie **"Minority Report"**. It models a futuristic "Pre-Crime" department
where:

- **Vision (Precog Division)**: Foresees future crimes and publishes visions.
- **Pre-Arrest (Law Enforcement)**: Responds to these visions by initiating a "pre-arrest" process, which must
  subsequently be confirmed (arrested) or cancelled.
- **Statistic (Feedback Loop)**: Successful arrests are reported back to update global prevention statistics.
- **Pre-emptive Apology**: Automatically issues apologies and "compensation" statements (with dystopian recovery fees)
  to the family of the pre-arrested individual.
- **Perpetrator**: Management of individuals identified as potential future criminals.
- **Audit**: Centralized auditing of all domain events.

## Domain Model

The following diagram illustrates the core aggregates and their relationships via ID references:

```mermaid
classDiagram
    direction LR
    class Vision {
        VisionId id
        PerpetratorId perpetratorId
        CrimeType crimeType
        LocalDateTime foreseenAt
    }
    class PreArrest {
        PreArrestId id
        VisionId visionId
        PerpetratorId perpetratorId
      OffsetDateTime preArrestIssueDate
        OffsetDateTime preArrestDate
        PreArrestStatus status
    }
    class PreApology {
        PreApologyId id
        VisionId visionId
        PerpetratorId perpetratorId
        Compensation compensation
        ApologyLetter apologyLetter
    }
    class Perpetrator {
        PerpetratorId id
        String firstName
        String lastName
    }
    class Statistic {
        StatisticId id
        Int totalCrimesPrevented
    }
    class AuditEntry {
        AuditEntryId id
        String eventType
    }

  Vision ..> Perpetrator: references
  PreArrest ..> Vision: references
  PreArrest ..> Perpetrator: references
  PreApology ..> Vision: references
  PreApology ..> Perpetrator: references
```

## Database Schema

```mermaid
erDiagram
    perpetrator {
        uuid id PK
        bigint version
        varchar first_name
        varchar last_name
    }
    vision {
        uuid id PK
        bigint version
        uuid perpetrator_id FK
        varchar crime_type
        timestamp foreseen_at
    }
    pre_arrest {
        uuid id PK
        bigint version
        uuid vision_id FK
        uuid perpetrator_id FK
        varchar status
        timestamptz pre_arrest_issued_date
        timestamptz pre_arrest_date "nullable"
    }
    pre_apology {
        uuid id PK
        uuid pre_arrest_id FK
        uuid perpetrator_id FK
        decimal base_amount
        decimal jetpack_fuel_deduction
        decimal halo_rental_fee
        decimal net_payout
        varchar apology_text
        timestamptz created_at
    }
    statistic {
        uuid id PK
        bigint version
        int total_crimes_prevented
    }
    audit_log {
        uuid id PK
        varchar event_type
        json payload
        timestamptz recorded_at
    }
    outbox {
        uuid id PK
        varchar event_class
        json event
        varchar status
        timestamptz created_at
        timestamptz processed_at "nullable"
    }
    inbox {
        uuid id PK
        varchar consumer_group PK
        timestamptz processed_at
    }

    perpetrator ||--o{ vision : ""
    perpetrator ||--o{ pre_arrest : ""
    perpetrator ||--o{ pre_apology : ""
    vision ||--o{ pre_arrest : ""
    pre_arrest ||--o{ pre_apology : ""
```

## Architecture & Flow

### Onion Architecture

The project follows the **Onion Architecture** pattern, ensuring that the core domain is independent of external
concerns. This is enforced using **jMolecules** annotations and **ArchUnit**.

```mermaid
flowchart TD
  subgraph Infrastructure["Infrastructure Ring"]
    direction TB
    Rest["@RestController (Facade)"]
    RepoImpl["jOOQ / SQL (Persistence)"]
    Kafka["Kafka / Messaging (Integration)"]
    Security["Spring Security (Security)"]
  end

  subgraph Application["Application Ring"]
    direction TB
    AppService["@Service (Orchestration)"]
    EventHandler["@DomainEventHandler (Side Effects)"]
  end

  subgraph Domain["Domain Ring"]
    direction TB
    AR["@AggregateRoot"]
    VO["@ValueObject"]
    DE["@DomainEvent"]
    Rep["@Repository (Interface)"]
    Fact["@Factory"]
  end

  Infrastructure --> Application
  Application --> Domain
  Infrastructure -.->|Implements Interfaces| Domain
```

### Event Flow

The following sequence diagram illustrates the event-driven flow between aggregates:

```mermaid
sequenceDiagram
    participant P as Vision (Precog)
    participant L as Pre-Arrest (Law)
    participant A as Pre-Apology
    participant S as Statistic
    participant K as Kafka (Event Bus)
    Note over P: Crime Foreseen
  P ->> K: CrimeForeseen Event
  K -->> L: Consume CrimeForeseen
  Note over L: Initiate Pre-Arrest (Pending)

  alt Confirmed
    L ->> K: PreArrestExecuted Event
    K -->> S: Consume PreArrestExecuted
    Note over S: Record Prevention (Stats)
    K -->> A: Consume PreArrestExecuted
    Note over A: Generate Apology & Billing
    A ->> K: PreApologyIssued Event
  else Cancelled
    L ->> K: PreArrestCancelled Event
  end
```

## Technical Stack

| Component            | Technology                               |
|----------------------|------------------------------------------|
| **Backend**          | Kotlin 2.4.0, Java 25, Spring Boot 4.0.6 |
| **Frontend**         | Angular 21, TypeScript, Vanilla CSS      |
| **Persistence**      | PostgreSQL 18, jOOQ, Flyway              |
| **Messaging**        | Kafka 4.2.0                              |
| **Testing**          | JUnit 5, ArchUnit, jMolecules-test       |
| **Containerization** | Docker Compose                           |

## Building and Running

### Prerequisites

- Java 25+
- Node.js (v22+)
- Docker & Docker Compose

### Infrastructure

Start the database and Kafka:

```bash
cd docker
docker-compose up -d
```

### Backend

Run the Spring Boot application:

```bash
./mvnw spring-boot:run
```

Tests: `./mvnw test`

### Frontend

The frontend features a "HUD" (Heads-Up Display) aesthetic. Navigate to the `ui` directory and start the dev server:

```bash
cd ui
npm install
npm run generate-api
npm start
```

The UI will be available at `http://localhost:4200` (proxied to `/api` on `localhost:8080`).

## Documentation generated with AsciiDoc and Antora
The ect/antora folder contains the developer documentation concerning Onion and DDD principles.

To generate the Antora html documentation just run:
```bash
npx antora etc/antora/antora-playbook.yml
```

## Development Conventions

- **Onion Constraints**: Architectural boundaries are enforced by **ArchUnit** and **jMolecules** in
  `ArchitectureTest.kt`.
- **Domain First**: Business logic resides strictly in the `domain` package and should have no dependencies on external
  frameworks (except jMolecules annotations).
- **Type-Safe SQL**: All database interactions use **jOOQ**.
- **Reliable Messaging**: Never publish directly to Kafka from the application service. Use `DomainEventPublisher`,
  which persists events to the `outbox` table within the same transaction.
- **OpenAPI First**: The REST API is defined in `openapi.yaml`. Backend controller interfaces and DTOs are generated
  using the `openapi-generator-maven-plugin`.
- **Kotlin Style**: Use idiomatic Kotlin (data classes, expressions, null safety).
