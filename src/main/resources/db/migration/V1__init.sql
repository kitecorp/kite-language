-- 1. Create identity table
CREATE TABLE identity
(
    identity_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255)
);
CREATE INDEX idx_name ON identity (name);

-- 2. Create resource_type table (without FKs to resources yet)
CREATE TABLE resource_type
(
    resource_type_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    kind VARCHAR(255) NOT NULL
);

---
-- 1. Resource have a UUID id (because it’s externally referenced, globally unique, etc.)
-- 2. Other entities like Identity to use simple INT primary keys (which are compact and faster)
CREATE TABLE resources
(
    id               UUID PRIMARY KEY NOT NULL,
    created_on       TIMESTAMP WITHOUT TIME ZONE,
    updated_on       TIMESTAMP WITHOUT TIME ZONE,
    properties       JSONB,
    existing         BOOLEAN,
    resource_type_id INT UNIQUE       NOT NULL,
    identity_id      INT UNIQUE       NOT NULL,
    CONSTRAINT FK_RESOURCE_TYPE_ON_ID FOREIGN KEY (resource_type_id) REFERENCES resource_type (resource_type_id),
    CONSTRAINT FK_IDENTITY_ON_ID FOREIGN KEY (identity_id) REFERENCES identity (identity_id)
);
