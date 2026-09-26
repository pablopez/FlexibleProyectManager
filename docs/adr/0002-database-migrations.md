# ADR-0002: Database Migrations with Flyway

- Status: Accepted
- Date: 2026-09-26

## Context

Flexible Project Manager is designed as a local-first application.

MVP 0.1 uses SQLite as its primary database.

A future cloud deployment is expected to use PostgreSQL.

The application therefore needs a database migration strategy that:

- works reliably with SQLite;
- supports PostgreSQL;
- keeps schema changes explicit and reviewable;
- works well with Spring Boot;
- avoids hiding database-specific behavior behind a large abstraction layer;
- allows both database implementations to evolve while preserving the same domain model.

SQLite and PostgreSQL are different database engines and do not provide identical DDL capabilities.

Trying to force every migration to use exactly the same SQL would make some schema changes unnecessarily difficult and could hide important database-specific behavior.

## Decision

Flexible Project Manager will use **Flyway** for relational database schema migrations.

Migration scripts will be written as explicit SQL files.

SQLite and PostgreSQL will use separate migration directories.

Conceptual structure:

```text
apps/backend/
└── src/main/resources/
    └── db/
        └── migration/
            ├── sqlite/
            │   ├── V1__initial_schema.sql
            │   └── ...
            └── postgresql/
                ├── V1__initial_schema.sql
                └── ...
```

The active Spring profile determines which migration location is used.

Example deployment profiles:

```text
local
    database: SQLite
    migration location: db/migration/sqlite

postgres
    database: PostgreSQL
    migration location: db/migration/postgresql
```

The exact Spring configuration will be defined during implementation.

## Migration Numbering

Equivalent schema changes must use the same migration version in both database implementations.

Example:

```text
sqlite/V1__initial_schema.sql
postgresql/V1__initial_schema.sql

sqlite/V2__add_project_archiving.sql
postgresql/V2__add_project_archiving.sql
```

The SQL may differ between engines, but the semantic result must be equivalent.

A migration version must represent the same logical schema change in both databases.

## Source of Truth

Flyway migrations are the source of truth for the physical relational schema.

Hibernate/JPA mappings must conform to the migrated schema.

Hibernate must not create or mutate the production schema automatically.

Schema generation settings such as:

```text
ddl-auto=create
ddl-auto=create-drop
ddl-auto=update
```

must not be used as the production migration mechanism.

Schema validation may be enabled where appropriate.

## SQLite Rules

SQLite is the primary database for local MVP deployments.

SQLite migrations must account for SQLite-specific limitations.

Rules:

1. Do not assume PostgreSQL DDL features are available in SQLite.
2. Keep migrations small and explicit.
3. Avoid unnecessary table alterations.
4. Prefer schema designs that can evolve cleanly in both SQLite and PostgreSQL.
5. Do not place explicit transaction statements inside Flyway migration scripts.
6. Do not run multiple application instances concurrently against the same local SQLite database during migration.
7. Foreign-key enforcement must be enabled for application connections.
8. Application transactions should remain short.
9. SQLite database files must live outside the packaged application resources.

Recommended runtime configuration includes:

```text
foreign_keys = ON
journal_mode = WAL
busy_timeout = configured
```

The exact connection configuration will be defined during backend implementation.

## PostgreSQL Rules

PostgreSQL is the expected database for future cloud deployments.

PostgreSQL migrations may use PostgreSQL-specific SQL where required, provided that the corresponding SQLite migration produces the same domain-level result.

PostgreSQL-specific optimizations must not leak into the Platform Core domain model.

## Portability

Database portability is defined at the domain and persistence-contract level, not as identical SQL syntax.

For example:

```text
Project.id
Project.organizationId
Project.name
Project.status
Project.createdAt
```

must have equivalent meaning in both databases even if their physical column definitions differ.

Application code should avoid relying on database-specific behavior unless it is isolated inside the infrastructure layer.

## Testing

Migration compatibility must be tested against both database engines.

SQLite integration tests must execute against a real temporary SQLite database.

PostgreSQL integration tests should execute against a real PostgreSQL instance using Testcontainers.

At minimum, CI should verify that:

```text
SQLite
    empty database
        -> all migrations succeed
        -> application starts
        -> schema validation succeeds

PostgreSQL
    empty database
        -> all migrations succeed
        -> application starts
        -> schema validation succeeds
```

When a new migration is added, both migration sets must be updated in the same change unless the migration is genuinely database-specific and has no schema equivalent.

## Migration Immutability

Once a migration has been merged and may have been executed by another installation, it must not be edited.

Corrections must be introduced as a new migration.

Example:

```text
V3__create_users.sql
V4__correct_users_index.sql
```

Do not modify `V3` after release.

## Initial Scope

Slice 0 only needs enough migration infrastructure to prove that:

```text
Spring Boot starts
        |
        v
SQLite database opens
        |
        v
Flyway runs
        |
        v
schema history exists
        |
        v
health endpoint reports database connectivity
```

The complete Platform Core schema does not need to be implemented during Slice 0.

Business tables should be introduced incrementally with the vertical slices that require them.

## Consequences

### Positive

- SQLite is treated as a first-class production database for local deployments.
- PostgreSQL remains a supported future deployment target.
- Schema changes are explicit and auditable.
- Database-specific behavior remains visible.
- JPA does not silently alter production databases.
- Migrations can be tested independently on both engines.

### Negative

- Some migrations will need two SQL implementations.
- SQLite and PostgreSQL migration files must be kept semantically synchronized.
- CI needs integration coverage for both engines.

These costs are accepted because explicit database behavior is preferable to pretending that SQLite and PostgreSQL are identical.

## Alternatives Considered

### Liquibase

Liquibase supports SQLite and PostgreSQL and provides a richer database-independent change model.

It was not selected for MVP 0.1 because SQLite support for individual schema-change operations is not uniform, and the project benefits from keeping migration SQL explicit.

### Hibernate automatic schema updates

Rejected.

Automatic schema mutation is convenient during experiments but is not an acceptable migration strategy for a distributable local application.

### SQLite only

Rejected as an architectural constraint.

SQLite is the MVP deployment database, but the Platform Core should remain compatible with a future PostgreSQL deployment.

## Out of Scope

This ADR does not define:

- backup and restore;
- database encryption;
- cloud database provisioning;
- tenant isolation;
- replication;
- high availability;
- Video QC analysis databases.

Those concerns will be decided separately when required.
