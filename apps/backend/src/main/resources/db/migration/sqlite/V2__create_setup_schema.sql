CREATE TABLE organizations (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    slug TEXT,
    status TEXT NOT NULL CHECK (status IN ('ACTIVE', 'DISABLED')),
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE installations (
    id TEXT PRIMARY KEY NOT NULL,
    organization_id TEXT NOT NULL REFERENCES organizations(id),
    name TEXT NOT NULL,
    platform TEXT NOT NULL CHECK (platform IN ('WINDOWS', 'LINUX', 'CLOUD', 'OTHER')),
    application_version TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('ACTIVE', 'DISABLED', 'UNLICENSED')),
    created_at TEXT NOT NULL,
    last_seen_at TEXT
);

CREATE TABLE users (
    id TEXT PRIMARY KEY NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    display_name TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('ACTIVE', 'DISABLED')),
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE organization_members (
    id TEXT PRIMARY KEY NOT NULL,
    user_id TEXT NOT NULL REFERENCES users(id),
    organization_id TEXT NOT NULL REFERENCES organizations(id),
    status TEXT NOT NULL CHECK (status IN ('ACTIVE', 'DISABLED')),
    joined_at TEXT NOT NULL,
    UNIQUE (user_id, organization_id)
);

CREATE TABLE roles (
    id TEXT PRIMARY KEY NOT NULL,
    code TEXT NOT NULL UNIQUE,
    system_defined INTEGER NOT NULL CHECK (system_defined IN (0, 1))
);

CREATE TABLE permissions (
    id TEXT PRIMARY KEY NOT NULL,
    code TEXT NOT NULL UNIQUE
);

CREATE TABLE member_roles (
    member_id TEXT NOT NULL REFERENCES organization_members(id),
    role_id TEXT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (member_id, role_id)
);

CREATE TABLE role_permissions (
    role_id TEXT NOT NULL REFERENCES roles(id),
    permission_id TEXT NOT NULL REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE system_initialization (
    singleton_id INTEGER PRIMARY KEY CHECK (singleton_id = 1),
    initialized_at TEXT NOT NULL
);
