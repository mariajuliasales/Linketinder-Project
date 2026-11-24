-- Recrutador (issue #21): novo valor RECRUITER no ENUM person_type.
-- Fica sozinho e fora de BEGIN/COMMIT: o PostgreSQL não permite usar um valor de ENUM
-- na mesma transação em que ele foi criado, e a migration 002 usa 'RECRUITER'.
-- Idempotente: IF NOT EXISTS não faz nada quando o valor já existe.

SET client_min_messages = warning;

ALTER TYPE person_type ADD VALUE IF NOT EXISTS 'RECRUITER';
