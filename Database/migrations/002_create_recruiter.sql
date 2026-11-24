-- Recrutador (issue #21): área de recrutamento, recrutador e curtida do recrutador.
-- Depende da 001 (valor 'RECRUITER' no ENUM person_type, já commitado por ela).
-- Idempotente: IF NOT EXISTS, ON CONFLICT DO NOTHING, DROP NOT NULL (sem efeito se a coluna já aceita NULL)
-- e um bloco DO que consulta pg_constraint antes do ADD CONSTRAINT.

SET client_min_messages = warning;

BEGIN;

-- Lista fixa de áreas de recrutamento
CREATE TABLE IF NOT EXISTS recruitment_area (
    id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO recruitment_area (name)
VALUES ('TI'),
       ('RH'),
       ('Marketing'),
       ('Segurança da Informação'),
       ('Financeiro'),
       ('Comercial'),
       ('Operações')
ON CONFLICT (name) DO NOTHING;

-- Recrutador não tem endereço: address_id passa a ser opcional só para ele
ALTER TABLE person ALTER COLUMN address_id DROP NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM pg_catalog.pg_constraint
         WHERE conname = 'person_address_required_check'
           AND conrelid = 'person'::regclass
    ) THEN
        ALTER TABLE person
            ADD CONSTRAINT person_address_required_check CHECK (type = 'RECRUITER' OR address_id IS NOT NULL);
    END IF;
END
$$;

-- Recrutador: especialização de person, vinculado a uma única empresa
CREATE TABLE IF NOT EXISTS recruiter (
    id            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    person_id     INTEGER     NOT NULL UNIQUE,
    person_type   person_type NOT NULL DEFAULT 'RECRUITER',
    enterprise_id INTEGER     NOT NULL REFERENCES enterprise (id)       ON DELETE CASCADE,
    area_id       INTEGER     NOT NULL REFERENCES recruitment_area (id) ON DELETE RESTRICT,
    cpf           CHAR(11)    NOT NULL,
    birth_date    DATE        NOT NULL,

    -- Pode repetir o CPF de um candidato, mas não o de outro recrutador
    CONSTRAINT recruiter_cpf_key UNIQUE (cpf),
    CONSTRAINT recruiter_person_type_check CHECK (person_type = 'RECRUITER'),
    CONSTRAINT recruiter_person_fkey FOREIGN KEY (person_id, person_type)
        REFERENCES person (id, type) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS recruiter_enterprise_id_idx ON recruiter (enterprise_id);

-- Curtida do recrutador em candidato. A enterprise_like vira a curtida consolidada,
-- mantida pelo serviço (enterprise_like(E, C) <=> existe recruiter_like de um recrutador de E em C)
CREATE TABLE IF NOT EXISTS recruiter_like (
    recruiter_id INTEGER   NOT NULL REFERENCES recruiter (id) ON DELETE CASCADE,
    candidate_id INTEGER   NOT NULL REFERENCES candidate (id) ON DELETE CASCADE,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (recruiter_id, candidate_id)
);

CREATE INDEX IF NOT EXISTS recruiter_like_candidate_id_idx ON recruiter_like (candidate_id);

COMMIT;
