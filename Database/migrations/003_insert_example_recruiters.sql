-- Recrutador (issue #21): recrutadores de exemplo e backfill das curtidas antigas.
-- Depende da 002 (tabelas recruitment_area, recruiter e recruiter_like).
-- Idempotente:
--   - person: ON CONFLICT (email) DO NOTHING;
--   - recruiter: WHERE NOT EXISTS pela person_id;
--   - recruiter_like: só para enterprise_like órfãs (sem nenhuma recruiter_like de um recrutador da empresa) e
--     ON CONFLICT DO NOTHING. Reaplicar não ressuscita uma curtida removida enquanto outro recrutador mantém a da empresa.
-- Empresa, área e recrutador são resolvidos por e-mail/nome, nunca por id.
-- O script não seleciona nem imprime dados pessoais.

SET client_min_messages = warning;

BEGIN;

-- Dados de exemplo (plano › "Recrutadores de exemplo"). Senha 123456, sem endereço e sem descrição.
CREATE TEMP TABLE example_recruiter (
    enterprise_email VARCHAR(150) NOT NULL,
    name             VARCHAR(150) NOT NULL,
    email            VARCHAR(150) NOT NULL,
    cpf              CHAR(11)     NOT NULL,
    birth_date       DATE         NOT NULL,
    area             VARCHAR(50)  NOT NULL,
    -- Recrutador que recebe o backfill das enterprise_like órfãs da empresa (um por empresa)
    backfill         BOOLEAN      NOT NULL
) ON COMMIT DROP;

INSERT INTO example_recruiter (enterprise_email, name, email, cpf, birth_date, area, backfill)
VALUES ('pastelzinho.frito@pastelsoft.com.br', 'Carla Mendes Prado',    'carla.prado@pastelsoft.com.br',     '71460258371', '1990-04-12', 'TI',                      TRUE),
       ('contato@techsolutions.com.br',        'Rafael Teixeira Lopes', 'rafael.lopes@techsolutions.com.br', '30195847601', '1988-09-03', 'TI',                      TRUE),
       ('contato@techsolutions.com.br',        'Juliana Freitas Moura', 'juliana.moura@techsolutions.com.br', '84621593005', '1993-02-27', 'RH',                      FALSE),
       ('carreiras@dataanalytics.com',         'Paulo Henrique Ramos',  'paulo.ramos@dataanalytics.com',     '59037481205', '1985-11-18', 'Segurança da Informação', TRUE),
       ('jobs@cloudscale.com',                 'Tatiane Rocha Batista', 'tatiane.batista@cloudscale.com',    '26384910551', '1996-06-08', 'Operações',               TRUE);

-- person: só quando a empresa existe, para não deixar uma person RECRUITER sem recruiter
INSERT INTO person (type, name, email, description, password_hash, address_id)
SELECT 'RECRUITER', data.name, data.email, NULL, crypt('123456', gen_salt('bf')), NULL
  FROM example_recruiter data
  JOIN person pe    ON pe.email = data.enterprise_email AND pe.type = 'ENTERPRISE'
  JOIN enterprise e ON e.person_id = pe.id
ON CONFLICT (email) DO NOTHING;

-- recruiter: só para a person RECRUITER de exemplo que ainda não tem recruiter
INSERT INTO recruiter (person_id, enterprise_id, area_id, cpf, birth_date)
SELECT pr.id, e.id, ra.id, data.cpf, data.birth_date
  FROM example_recruiter data
  JOIN person pr          ON pr.email = data.email AND pr.type = 'RECRUITER'
  JOIN person pe          ON pe.email = data.enterprise_email AND pe.type = 'ENTERPRISE'
  JOIN enterprise e       ON e.person_id = pe.id
  JOIN recruitment_area ra ON ra.name = data.area
 WHERE NOT EXISTS (SELECT 1 FROM recruiter r WHERE r.person_id = pr.id);

-- Backfill: cada enterprise_like(E, C) órfã ganha a recruiter_like do recrutador de backfill de E
INSERT INTO recruiter_like (recruiter_id, candidate_id)
SELECT r.id, el.candidate_id
  FROM enterprise_like el
  JOIN enterprise e         ON e.id = el.enterprise_id
  JOIN person pe            ON pe.id = e.person_id
  JOIN example_recruiter data ON data.enterprise_email = pe.email AND data.backfill
  JOIN person pr            ON pr.email = data.email AND pr.type = 'RECRUITER'
  JOIN recruiter r          ON r.person_id = pr.id AND r.enterprise_id = el.enterprise_id
 WHERE NOT EXISTS (
           SELECT 1
             FROM recruiter_like rl
             JOIN recruiter ro ON ro.id = rl.recruiter_id
            WHERE ro.enterprise_id = el.enterprise_id
              AND rl.candidate_id  = el.candidate_id
       )
ON CONFLICT DO NOTHING;

COMMIT;
