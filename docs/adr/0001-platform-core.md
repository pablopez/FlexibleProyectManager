# ADR-0001: Platform Core as a Modular Monolith

## Status

Accepted

## Context

Flexible Project Manager is intended to become a reusable platform capable of supporting different business domains.

The first future business module is not part of the current development scope.

The initial product needs:

- authentication
- users
- organizations
- installations
- licensing
- projects
- settings
- auditing
- application events
- a web GUI

The platform must work locally while remaining compatible with a future cloud deployment.

## Decision

The initial application will be implemented as a modular monolith.

The backend will use:

```text
Java 21
Spring Boot
Spring Security
JPA / Hibernate
```

The frontend will use:

```text
React
TypeScript
Vite
```

Local persistence will use SQLite.

Future cloud deployments may use PostgreSQL.

Projects will remain generic.

Domain-specific functionality must be implemented outside the Platform Core.

The Platform Core must never depend on domain-specific modules.

## Consequences

### Positive

- Simple initial deployment
- Fast local development
- Clear module boundaries
- Low operational complexity
- Easier testing
- Future extraction of modules remains possible
- Local and cloud deployments can share the same domain model

### Negative

- Module boundaries must be enforced by architecture and code organization
- SQLite and PostgreSQL compatibility must be considered when writing persistence logic
- Some future capabilities may require architectural evolution

## Rejected Alternatives

### Microservices

Rejected for the initial version because they would introduce unnecessary:

- networking
- deployment complexity
- distributed transactions
- infrastructure
- observability requirements

without providing current business value.

### Kubernetes

Rejected for MVP 0.1.

The platform should remain deployable in Kubernetes in the future, but must not depend on Kubernetes.

### Domain-specific Platform Core

Rejected.

The Platform Core must remain reusable and must not contain concepts belonging to future business modules.
