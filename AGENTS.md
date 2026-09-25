# Agent Development Instructions

## Project

Flexible Project Manager is a reusable project management platform.

The current development scope is ONLY the Platform Core.

Projects are currently generic containers and must be treated as a black box regarding future business functionality.

## Current Stack

Backend:

- Java 21
- Spring Boot
- Spring Security
- JPA / Hibernate

Frontend:

- React
- TypeScript
- Vite

Local persistence:

- SQLite

Future cloud persistence:

- PostgreSQL

API contract:

- OpenAPI 3.1
- `docs/api/openapi.yaml`

## Architecture Rules

The Platform Core must remain independent from any future business domain.

Do not introduce domain-specific concepts into the Platform Core.

Use clear separation between:

- Domain
- Application
- Infrastructure
- HTTP/API

Controllers must never expose JPA entities directly.

Use request and response DTOs.

Business logic must not live in controllers.

Persistence must be accessed through abstractions where appropriate.

Avoid unnecessary framework coupling in the domain model.

## API Contract

`docs/api/openapi.yaml` is the HTTP contract.

Do not modify the API contract unless explicitly requested.

Backend implementations must follow the OpenAPI contract.

Frontend API clients must follow the OpenAPI contract.

## Project Scope

For MVP 0.1 implement only:

- Installation
- Organization
- Licensing
- Authentication
- Users
- Roles / permissions
- Projects
- Settings
- Audit
- Application events
- GUI

Projects must remain generic.

## Explicitly Out of Scope

Do NOT introduce:

- Video QC
- FFmpeg
- C++
- Python
- AI / ML
- RabbitMQ
- Kafka
- Kubernetes
- Microservices
- Google Drive
- Dropbox
- S3
- SFTP
- External processing workers
- Video concepts
- Media concepts

unless explicitly requested.

## Deployment Philosophy

The application is local-first and cloud-ready.

Local deployment:

- One organization
- SQLite
- Single application installation
- Multiple users allowed

Future cloud deployment:

- Multiple organizations
- PostgreSQL
- Horizontal scaling

The domain model should not depend on the deployment mode.

## Development Principles

Prefer:

- Simple solutions
- Explicit code
- Small cohesive modules
- SOLID principles where useful
- Dependency inversion where it provides value
- Composition over inheritance
- Testable application services
- Clear boundaries

Avoid:

- Premature microservices
- Premature distributed systems
- Unnecessary abstractions
- Generic base classes without a concrete need
- Overengineering
- Large god services
- Business logic inside controllers
- Framework-specific domain models where avoidable

## Changes

Before making significant architectural changes:

1. Explain the proposed change.
2. Explain why it is needed.
3. Identify affected modules.
4. Wait for approval if the change modifies an existing architectural decision.

## Commits

Changes should be small and focused.

Suggested commit prefixes:

- feat:
- fix:
- refactor:
- test:
- docs:
- chore:
