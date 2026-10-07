-- Todas as senhas são '123456'

BEGIN;

CREATE FUNCTION pg_temp.new_address(p_cep CHAR(8), p_city VARCHAR, p_state CHAR(2)) RETURNS INTEGER AS $$
    INSERT INTO address (cep, city, state) VALUES (p_cep, p_city, p_state) RETURNING id;
$$ LANGUAGE sql;

INSERT INTO competence (name) VALUES
    ('Python'), ('Java'), ('Spring Framework'), ('Angular'), ('Groovy'), ('JavaScript'),
    ('TypeScript'), ('SQL'), ('Docker'), ('Kafka'), ('React');

INSERT INTO person (type, name, email, description, password_hash, address_id) VALUES
    ('CANDIDATE', 'Maria Julia Sales',       'mariajuliasales@gmail.com', 'Software Engineer',   crypt('123456', gen_salt('bf')), pg_temp.new_address('36700386', 'São Paulo',      'SP')),
    ('CANDIDATE', 'Lucas Andrade Silveira',  'lucas.andrade@gmail.com',   'Backend Developer',   crypt('123456', gen_salt('bf')), pg_temp.new_address('30110010', 'Belo Horizonte', 'MG')),
    ('CANDIDATE', 'Beatriz Lima Rocha',      'beatriz.rocha@outlook.com', 'Full Stack Engineer', crypt('123456', gen_salt('bf')), pg_temp.new_address('22041001', 'Rio de Janeiro', 'RJ')),
    ('CANDIDATE', 'Gabriel Santos Oliveira', 'gabriel.santos@dev.com',    'Data Engineer',       crypt('123456', gen_salt('bf')), pg_temp.new_address('80010000', 'Curitiba',       'PR')),
    ('CANDIDATE', 'Fernanda Costa Souza',    'fernanda.souza@gmail.com',  'DevOps Engineer',     crypt('123456', gen_salt('bf')), pg_temp.new_address('88010000', 'Florianópolis',  'SC')),
    ('CANDIDATE', 'Sandubinha da Silva',     'sandubinha@email.com',
     'Desenvolvedor apaixonado por backend e por bons lanches.',                     crypt('123456', gen_salt('bf')), pg_temp.new_address('74000000', 'Goiânia',        'GO'));

INSERT INTO candidate (person_id, cpf, birth_date, training)
SELECT p.id, data.cpf, data.birth_date::DATE, data.training
FROM (VALUES
    ('mariajuliasales@gmail.com', '12345678909', '2002-03-15', 'Ciência da Computação'),
    ('lucas.andrade@gmail.com',   '38459201066', '2000-07-02', 'Engenharia de Software'),
    ('beatriz.rocha@outlook.com', '83741920037', '1997-05-21', 'Análise e Desenvolvimento de Sistemas'),
    ('gabriel.santos@dev.com',    '19283746031', '2003-01-10', 'Estatística'),
    ('fernanda.souza@gmail.com',  '56473829083', '1995-08-30', 'Redes de Computadores'),
    ('sandubinha@email.com',      '52998224725', '1999-11-20', 'Sistemas de Informação')
) AS data (email, cpf, birth_date, training)
JOIN person p ON p.email = data.email;

INSERT INTO competence (name) VALUES ('Ilustrador');

INSERT INTO candidate_competence (candidate_id, competence_id)
SELECT c.id, co.id
FROM (VALUES
    ('mariajuliasales@gmail.com', 'Java'),
    ('mariajuliasales@gmail.com', 'Groovy'),
    ('lucas.andrade@gmail.com',   'Java'),
    ('lucas.andrade@gmail.com',   'Spring Framework'),
    ('beatriz.rocha@outlook.com', 'Groovy'),
    ('beatriz.rocha@outlook.com', 'JavaScript'),
    ('gabriel.santos@dev.com',    'Python'),
    ('gabriel.santos@dev.com',    'SQL'),
    ('gabriel.santos@dev.com',    'Docker'),
    ('fernanda.souza@gmail.com',  'Docker'),
    ('fernanda.souza@gmail.com',  'Angular'),
    ('fernanda.souza@gmail.com',  'JavaScript'),
    ('sandubinha@email.com',      'Java'),
    ('sandubinha@email.com',      'Spring Framework'),
    ('sandubinha@email.com',      'Angular'),
    ('sandubinha@email.com',      'Ilustrador')
) AS data (email, competence)
JOIN person p      ON p.email = data.email
JOIN candidate c   ON c.person_id = p.id
JOIN competence co ON co.name = data.competence;

INSERT INTO person (type, name, email, description, password_hash, address_id) VALUES
    ('ENTERPRISE', 'TechSolutions Brasil',  'contato@techsolutions.com.br', 'Desenvolvimento de software sob medida', crypt('123456', gen_salt('bf')), pg_temp.new_address('01310000', 'São Paulo',      'SP')),
    ('ENTERPRISE', 'Inovação Digital LTDA', 'rh@inovacaodigital.io',        'Consultoria em transformação digital',   crypt('123456', gen_salt('bf')), pg_temp.new_address('30110010', 'Belo Horizonte', 'MG')),
    ('ENTERPRISE', 'DataAnalytics Global',  'carreiras@dataanalytics.com',  'Soluções de dados e Big Data',           crypt('123456', gen_salt('bf')), pg_temp.new_address('22041001', 'Rio de Janeiro', 'RJ')),
    ('ENTERPRISE', 'CloudScale Services',   'jobs@cloudscale.com',          'Infraestrutura multicloud',              crypt('123456', gen_salt('bf')), pg_temp.new_address('88010000', 'Florianópolis',  'SC')),
    ('ENTERPRISE', 'FrontCraft Studios',    'talent@frontcraft.dev',        'Experiências web modernas e acessíveis', crypt('123456', gen_salt('bf')), pg_temp.new_address('80010000', 'Curitiba',       'PR')),
    ('ENTERPRISE', 'Pastelsoft',            'pastelzinho.frito@pastelsoft.com.br',
     'ERPs para redes de restaurantes e distribuidoras de bebida de todo o país.',               crypt('123456', gen_salt('bf')), pg_temp.new_address('74000000', 'Goiânia',        'GO'));

INSERT INTO enterprise (person_id, cnpj)
SELECT p.id, data.cnpj
FROM (VALUES
    ('contato@techsolutions.com.br',        '11222333000181'),
    ('rh@inovacaodigital.io',               '22333444000181'),
    ('carreiras@dataanalytics.com',         '33444555000181'),
    ('jobs@cloudscale.com',                 '44555666000181'),
    ('talent@frontcraft.dev',               '55666777000181'),
    ('pastelzinho.frito@pastelsoft.com.br', '98765432000198')
) AS data (email, cnpj)
JOIN person p ON p.email = data.email;

INSERT INTO enterprise_competence (enterprise_id, competence_id)
SELECT e.id, co.id
FROM (VALUES
    ('TechSolutions Brasil',  'Java'),
    ('TechSolutions Brasil',  'Spring Framework'),
    ('Inovação Digital LTDA', 'Groovy'),
    ('Inovação Digital LTDA', 'Docker'),
    ('DataAnalytics Global',  'Python'),
    ('DataAnalytics Global',  'SQL'),
    ('CloudScale Services',   'Java'),
    ('CloudScale Services',   'Docker'),
    ('FrontCraft Studios',    'React'),
    ('FrontCraft Studios',    'JavaScript'),
    ('Pastelsoft',            'Spring Framework'),
    ('Pastelsoft',            'Angular')
) AS data (enterprise, competence)
JOIN person p      ON p.name = data.enterprise AND p.type = 'ENTERPRISE'
JOIN enterprise e  ON e.person_id = p.id
JOIN competence co ON co.name = data.competence;

INSERT INTO vacancy (enterprise_id, title, description, status, address_id)
SELECT e.id, data.title, data.description, data.status::vacancy_status,
       CASE WHEN data.city IS NULL THEN p.address_id
            ELSE pg_temp.new_address(NULL, data.city, data.state) END
FROM (VALUES
    ('TechSolutions Brasil',  'Desenvolvedor Full Stack Java/Spring', 'Desenvolvimento ponta a ponta de produtos SaaS de alta escala.',      'OPEN',   NULL,        NULL),
    ('Inovação Digital LTDA', 'Engenheiro de Software Groovy',        'Aplicações corporativas de alta performance e automação.',           'OPEN',   NULL,        NULL),
    ('DataAnalytics Global',  'Cientista de Dados',                   'Modelos preditivos, pipelines de dados e consultas SQL complexas.',  'OPEN',   NULL,        NULL),
    ('CloudScale Services',   'Especialista Cloud & DevOps',          'Infraestrutura distribuída em nuvem e conteinerização de serviços.', 'OPEN',   NULL,        NULL),
    ('FrontCraft Studios',    'Desenvolvedor Frontend React',         'Interfaces web modernas, responsivas e integradas a serviços REST.', 'OPEN',   NULL,        NULL),
    ('Pastelsoft',            'Desenvolvedor Backend Spring',         'APIs do ERP para redes de restaurantes, usando Spring Framework.',  'OPEN',   NULL,        NULL),
    ('Pastelsoft',            'Desenvolvedor Frontend Angular',       'Telas do ERP para distribuidoras de bebida, usando Angular.',        'OPEN',   'São Paulo', 'SP'),
    ('Pastelsoft',            'Estágio em Suporte ao ERP',            'Atendimento e suporte aos clientes do ERP.',                         'CLOSED', NULL,        NULL)
) AS data (enterprise, title, description, status, city, state)
JOIN person p     ON p.name = data.enterprise AND p.type = 'ENTERPRISE'
JOIN enterprise e ON e.person_id = p.id;

INSERT INTO vacancy_competence (vacancy_id, competence_id)
SELECT v.id, co.id
FROM (VALUES
    ('Desenvolvedor Full Stack Java/Spring', 'Java'),
    ('Desenvolvedor Full Stack Java/Spring', 'Spring Framework'),
    ('Engenheiro de Software Groovy',        'Groovy'),
    ('Engenheiro de Software Groovy',        'Docker'),
    ('Cientista de Dados',                   'Python'),
    ('Cientista de Dados',                   'SQL'),
    ('Especialista Cloud & DevOps',          'Java'),
    ('Especialista Cloud & DevOps',          'Docker'),
    ('Desenvolvedor Frontend React',         'React'),
    ('Desenvolvedor Frontend React',         'JavaScript'),
    ('Desenvolvedor Backend Spring',         'Java'),
    ('Desenvolvedor Backend Spring',         'Spring Framework'),
    ('Desenvolvedor Frontend Angular',       'Angular'),
    ('Desenvolvedor Frontend Angular',       'TypeScript'),
    ('Estágio em Suporte ao ERP',            'SQL')
) AS data (title, competence)
JOIN vacancy v     ON v.title = data.title
JOIN competence co ON co.name = data.competence;

INSERT INTO candidate_like (candidate_id, vacancy_id)
SELECT c.id, v.id
FROM (VALUES
    ('sandubinha@email.com',      'Desenvolvedor Backend Spring'),
    ('sandubinha@email.com',      'Desenvolvedor Full Stack Java/Spring'),
    ('lucas.andrade@gmail.com',   'Desenvolvedor Full Stack Java/Spring'),
    ('gabriel.santos@dev.com',    'Cientista de Dados'),
    ('beatriz.rocha@outlook.com', 'Engenheiro de Software Groovy')
) AS data (email, title)
JOIN person p    ON p.email = data.email
JOIN candidate c ON c.person_id = p.id
JOIN vacancy v   ON v.title = data.title;

INSERT INTO enterprise_like (enterprise_id, candidate_id)
SELECT e.id, c.id
FROM (VALUES
    ('Pastelsoft',           'sandubinha@email.com'),
    ('TechSolutions Brasil', 'lucas.andrade@gmail.com'),
    ('DataAnalytics Global', 'gabriel.santos@dev.com'),
    ('CloudScale Services',  'fernanda.souza@gmail.com')
) AS data (enterprise, email)
JOIN person pe    ON pe.name = data.enterprise AND pe.type = 'ENTERPRISE'
JOIN enterprise e ON e.person_id = pe.id
JOIN person pc    ON pc.email = data.email
JOIN candidate c  ON c.person_id = pc.id;

INSERT INTO vacancy_match (candidate_id, vacancy_id)
SELECT cl.candidate_id, cl.vacancy_id
FROM candidate_like cl
JOIN vacancy v          ON v.id = cl.vacancy_id
JOIN enterprise_like el ON el.enterprise_id = v.enterprise_id
                       AND el.candidate_id  = cl.candidate_id;

COMMIT;
