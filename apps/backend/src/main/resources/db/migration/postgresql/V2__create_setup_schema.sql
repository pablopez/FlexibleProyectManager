CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(100),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'DISABLED')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE installations (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    name VARCHAR(200) NOT NULL,
    platform VARCHAR(20) NOT NULL CHECK (platform IN ('WINDOWS', 'LINUX', 'CLOUD', 'OTHER')),
    application_version VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'DISABLED', 'UNLICENSED')),
    created_at TIMESTAMPTZ NOT NULL,
    last_seen_at TIMESTAMPTZ
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'DISABLED')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE organization_members (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'DISABLED')),
    joined_at TIMESTAMPTZ NOT NULL,
    UNIQUE (user_id, organization_id)
);

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    system_defined BOOLEAN NOT NULL
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE member_roles (
    member_id UUID NOT NULL REFERENCES organization_members(id),
    role_id UUID NOT NULL REFERENCES roles(id),
    PRIMARY KEY (member_id, role_id)
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id),
    permission_id UUID NOT NULL REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE system_initialization (
    singleton_id INTEGER PRIMARY KEY CHECK (singleton_id = 1),
    initialized_at TIMESTAMPTZ NOT NULL
);
