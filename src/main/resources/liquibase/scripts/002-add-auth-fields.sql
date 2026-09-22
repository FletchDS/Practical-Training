-- liquibase formatted sql

-- changeset vitalii:8
ALTER TABLE employees
    ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN role     VARCHAR(20)  NOT NULL DEFAULT 'USER';

-- changeset vitalii:9
CREATE TABLE refresh_tokens
(
    id                BIGSERIAL PRIMARY KEY,
    token             TEXT      NOT NULL UNIQUE,
    employee_id       BIGINT    NOT NULL REFERENCES employees (id) ON DELETE CASCADE,
    expires_at        TIMESTAMP NOT NULL,
    revoked           BOOLEAN   NOT NULL DEFAULT FALSE,
    replaced_by_token TEXT
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_employee ON refresh_tokens (employee_id);