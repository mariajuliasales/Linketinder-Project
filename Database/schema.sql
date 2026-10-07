SET client_min_messages = warning;

DROP TABLE IF EXISTS vacancy_match, enterprise_like, candidate_like,
                     vacancy_competence, enterprise_competence, candidate_competence,
                     competence, vacancy, enterprise, candidate, person, address CASCADE;
DROP TYPE  IF EXISTS vacancy_status, person_type CASCADE;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE vacancy_status AS ENUM ('OPEN', 'PAUSED', 'CLOSED');
CREATE TYPE person_type    AS ENUM ('CANDIDATE', 'ENTERPRISE');

CREATE TABLE address (
    id           INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cep          CHAR(8),
    street       VARCHAR(150),
    number       VARCHAR(10),
    complement   VARCHAR(100),
    neighborhood VARCHAR(100),
    city         VARCHAR(100) NOT NULL,
    state        CHAR(2)      NOT NULL,
    country      VARCHAR(60)  NOT NULL DEFAULT 'Brasil'
);

CREATE INDEX address_state_city_idx ON address (state, city);

COMMENT ON TABLE address IS 'Pode ser compartilhado (ex.: vaga na sede da empresa); ao alterar, cria-se um novo';

CREATE TABLE person (
    id            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type          person_type  NOT NULL,
    name          VARCHAR(150) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    description   TEXT,
    password_hash VARCHAR(255) NOT NULL,
    address_id    INTEGER      NOT NULL REFERENCES address (id) ON DELETE RESTRICT,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),

    -- Alvo da FK composta (person_id, person_type) de candidate e enterprise
    CONSTRAINT person_id_type_key UNIQUE (id, type)
);

CREATE TABLE candidate (
    id          INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    person_id   INTEGER      NOT NULL UNIQUE,
    person_type person_type  NOT NULL DEFAULT 'CANDIDATE',
    cpf         CHAR(11)     NOT NULL UNIQUE,
    birth_date  DATE         NOT NULL,
    training    VARCHAR(150) NOT NULL,

    CONSTRAINT candidate_person_type_check CHECK (person_type = 'CANDIDATE'),
    CONSTRAINT candidate_person_fkey FOREIGN KEY (person_id, person_type)
        REFERENCES person (id, type) ON DELETE CASCADE
);

CREATE TABLE enterprise (
    id          INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    person_id   INTEGER     NOT NULL UNIQUE,
    person_type person_type NOT NULL DEFAULT 'ENTERPRISE',
    cnpj        CHAR(14)    NOT NULL UNIQUE,

    CONSTRAINT enterprise_person_type_check CHECK (person_type = 'ENTERPRISE'),
    CONSTRAINT enterprise_person_fkey FOREIGN KEY (person_id, person_type)
        REFERENCES person (id, type) ON DELETE CASCADE
);

CREATE TABLE vacancy (
    id            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    enterprise_id INTEGER        NOT NULL REFERENCES enterprise (id) ON DELETE CASCADE,
    title         VARCHAR(150)   NOT NULL,
    description   TEXT           NOT NULL,
    status        vacancy_status NOT NULL DEFAULT 'OPEN',
    address_id    INTEGER        NOT NULL REFERENCES address (id) ON DELETE RESTRICT,
    created_at    TIMESTAMP      NOT NULL DEFAULT now()
);

CREATE INDEX vacancy_enterprise_id_idx ON vacancy (enterprise_id);
CREATE INDEX vacancy_status_idx        ON vacancy (status);

CREATE TABLE competence (
    id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE candidate_competence (
    candidate_id  INTEGER NOT NULL REFERENCES candidate (id)  ON DELETE CASCADE,
    competence_id INTEGER NOT NULL REFERENCES competence (id) ON DELETE RESTRICT,
    PRIMARY KEY (candidate_id, competence_id)
);

CREATE TABLE enterprise_competence (
    enterprise_id INTEGER NOT NULL REFERENCES enterprise (id) ON DELETE CASCADE,
    competence_id INTEGER NOT NULL REFERENCES competence (id) ON DELETE RESTRICT,
    PRIMARY KEY (enterprise_id, competence_id)
);

CREATE TABLE vacancy_competence (
    vacancy_id    INTEGER NOT NULL REFERENCES vacancy (id)    ON DELETE CASCADE,
    competence_id INTEGER NOT NULL REFERENCES competence (id) ON DELETE RESTRICT,
    PRIMARY KEY (vacancy_id, competence_id)
);

CREATE INDEX candidate_competence_competence_idx  ON candidate_competence (competence_id);
CREATE INDEX enterprise_competence_competence_idx ON enterprise_competence (competence_id);
CREATE INDEX vacancy_competence_competence_idx    ON vacancy_competence (competence_id);

CREATE TABLE candidate_like (
    candidate_id INTEGER   NOT NULL REFERENCES candidate (id) ON DELETE CASCADE,
    vacancy_id   INTEGER   NOT NULL REFERENCES vacancy (id)   ON DELETE CASCADE,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (candidate_id, vacancy_id)
);

CREATE INDEX candidate_like_vacancy_id_idx ON candidate_like (vacancy_id);

CREATE TABLE enterprise_like (
    enterprise_id INTEGER   NOT NULL REFERENCES enterprise (id) ON DELETE CASCADE,
    candidate_id  INTEGER   NOT NULL REFERENCES candidate (id)  ON DELETE CASCADE,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (enterprise_id, candidate_id)
);

CREATE INDEX enterprise_like_candidate_id_idx ON enterprise_like (candidate_id);

CREATE TABLE vacancy_match (
    id           INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    candidate_id INTEGER   NOT NULL REFERENCES candidate (id) ON DELETE CASCADE,
    vacancy_id   INTEGER   NOT NULL REFERENCES vacancy (id)   ON DELETE CASCADE,
    matched_at   TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT vacancy_match_candidate_vacancy_key UNIQUE (candidate_id, vacancy_id),
    CONSTRAINT vacancy_match_like_fkey FOREIGN KEY (candidate_id, vacancy_id)
        REFERENCES candidate_like (candidate_id, vacancy_id) ON DELETE CASCADE
);

CREATE INDEX vacancy_match_vacancy_id_idx ON vacancy_match (vacancy_id);
