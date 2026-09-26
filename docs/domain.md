# Platform Domain

## 1. Purpose

This document defines the initial domain vocabulary for the Flexible Project Manager Platform Core.

Only generic application concepts belong here.

Business-domain concepts must be defined in their corresponding modules.

## Organization

Represents the organization using the platform.

Local deployments contain exactly one organization.

A future cloud deployment may contain multiple organizations.

Main attributes:

```text
id
name
slug
status
createdAt
updatedAt
```

Status:

```text
ACTIVE
DISABLED
```

## Installation

Represents a concrete installation of the application.

An installation belongs to an organization.

Examples:

```text
Windows workstation
Linux workstation
On-premise server
Cloud deployment
```

Main attributes:

```text
id
organizationId
installationKey
name
platform
applicationVersion
status
createdAt
lastSeenAt
```

Status:

```text
ACTIVE
DISABLED
UNLICENSED
```

## License

Represents authorization to use the product.

A license belongs to an organization and may optionally be restricted to an installation.

Main attributes:

```text
id
organizationId
installationId
licenseKey
type
status
issuedAt
expiresAt
signature
```

Types:

```text
TRIAL
SUBSCRIPTION
PERPETUAL
DEVELOPMENT
```

Status:

```text
ACTIVE
EXPIRED
REVOKED
INVALID
```

A license may provide features and limits.

Example features:

```text
projects
future-module
```

Example limits:

```text
maxUsers
maxInstallations
maxWorkers
```

## User

Represents a person capable of authenticating with the platform.

Main attributes:

```text
id
email
passwordHash
displayName
status
createdAt
updatedAt
```

Status:

```text
PENDING
ACTIVE
DISABLED
```

Passwords must never be stored directly.

## OrganizationMember

Represents membership of a user in an organization.

Main attributes:

```text
id
userId
organizationId
status
joinedAt
```

Status:

```text
INVITED
ACTIVE
DISABLED
```

This association allows the domain to support multiple organizations in future cloud deployments.

## Role

Represents a named collection of permissions.

Initial roles:

```text
ADMIN
USER
VIEWER
```

Roles are initially system-defined.

## Permission

Represents an operation a user may perform.

Examples:

```text
users:read
users:create
users:update

projects:read
projects:create
projects:update
projects:archive

organization:read
organization:update

license:read
license:manage

audit:read
```

Future modules may register additional permissions.

## RefreshToken

Represents a renewable authenticated session.

Main attributes:

```text
id
userId
tokenHash
createdAt
expiresAt
revokedAt
```

Refresh tokens must be stored as hashes.

## Project

Represents a generic project within the platform.

Project is deliberately domain-agnostic.

Main attributes:

```text
id
organizationId
name
description
status
createdBy
createdAt
updatedAt
```

Status:

```text
ACTIVE
ARCHIVED
```

For MVP 0.1 a project contains no domain-specific functionality.

## UserSetting

Represents a user preference.

Examples:

```text
language
theme
timezone
```

## OrganizationSetting

Represents configuration shared by an organization.

Module-specific configuration must not be mixed with Platform Core configuration without an explicit namespace.

## AuditEntry

Represents an immutable record of a relevant action.

Main attributes:

```text
id
organizationId
actorUserId
action
resourceType
resourceId
metadata
createdAt
```

Examples:

```text
LOGIN_SUCCESS
LOGIN_FAILED

USER_CREATED
USER_UPDATED
USER_DISABLED

PROJECT_CREATED
PROJECT_UPDATED
PROJECT_ARCHIVED
PROJECT_RESTORED

LICENSE_ACTIVATED
LICENSE_DEACTIVATED
```

## ApplicationEvent

Represents something that happened inside the application.

Application events may be consumed by internal components and exposed to connected clients.

Conceptual structure:

```text
id
type
timestamp
organizationId
resourceType
resourceId
payload
```

Application events are not necessarily persistent.

## Relationships

```text
Organization
│
├── Installation
├── License
├── OrganizationMember
│       │
│       └── User
│
├── Project
├── OrganizationSetting
└── AuditEntry

User
│
├── OrganizationMember
├── RefreshToken
├── UserSetting
└── AuditEntry
```

## Domain Boundary

The Platform Core may know:

```text
organizations
users
projects
licenses
settings
events
```

The Platform Core must NOT know what a project is used for.

Future modules own domain-specific project functionality.
