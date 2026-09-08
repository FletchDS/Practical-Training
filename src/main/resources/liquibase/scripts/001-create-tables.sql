-- liquibase formatted sql

-- changeset vitalii:1
CREATE TABLE positions
(
    id          SERIAL PRIMARY KEY,
    title       VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- changeset vitalii:2
CREATE TABLE employees
(
    id          SERIAL PRIMARY KEY,
    first_name  VARCHAR(50)  NOT NULL,
    last_name   VARCHAR(50)  NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    phone       VARCHAR(20),
    hire_date   DATE,
    position_id INT          REFERENCES positions (id) ON DELETE SET NULL
);

-- changeset vitalii:3
CREATE TABLE skills
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- changeset vitalii:4
CREATE TABLE employee_skills
(
    employee_id INT NOT NULL REFERENCES employees (id) ON DELETE CASCADE,
    skill_id    INT NOT NULL REFERENCES skills (id) ON DELETE CASCADE,
    PRIMARY KEY (employee_id, skill_id)
);

-- changeset vitalii:5
CREATE TABLE projects
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    start_date  DATE         NOT NULL,
    end_date    DATE,
    status      VARCHAR(20)  NOT NULL DEFAULT 'planned',
    CHECK (status IN ('planned', 'active', 'finished', 'cancelled')),
    CHECK (end_date IS NULL OR end_date >= start_date)
);

-- changeset vitalii:6
CREATE TABLE project_assignments
(
    id          SERIAL PRIMARY KEY,
    project_id  INT  NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    employee_id INT  NOT NULL REFERENCES employees (id) ON DELETE CASCADE,
    role        VARCHAR(100),
    start_date  DATE NOT NULL,
    end_date    DATE,
    CHECK (end_date IS NULL OR end_date >= start_date),
    UNIQUE (project_id, employee_id, start_date)
);

-- changeset vitalii:7
CREATE INDEX idx_assignments_project ON project_assignments (project_id);
CREATE INDEX idx_assignments_employee ON project_assignments (employee_id);
CREATE INDEX idx_employee_skills_employee ON employee_skills (employee_id);
CREATE INDEX idx_employee_skills_skill ON employee_skills (skill_id);