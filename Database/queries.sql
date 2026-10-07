\echo '\n== 1. Candidatos e suas competências (candidato N:N competência)'
SELECT p.name                                      AS candidato,
       string_agg(co.name, ', ' ORDER BY co.name)  AS competencias
FROM candidate c
JOIN person p                ON p.id = c.person_id
JOIN candidate_competence cc ON cc.candidate_id = c.id
JOIN competence co           ON co.id = cc.competence_id
GROUP BY p.name
ORDER BY p.name;

\echo '\n== 2. Vagas de cada empresa com local e competências (empresa 1:N vaga, vaga N:N competência)'
SELECT p.name                                      AS empresa,
       v.title                                     AS vaga,
       a.city || '/' || a.state                    AS local,
       string_agg(co.name, ', ' ORDER BY co.name)  AS competencias
FROM vacancy v
JOIN enterprise e          ON e.id = v.enterprise_id
JOIN person p              ON p.id = e.person_id
JOIN address a             ON a.id = v.address_id
JOIN vacancy_competence vc ON vc.vacancy_id = v.id
JOIN competence co         ON co.id = vc.competence_id
GROUP BY p.name, v.id, a.city, a.state
ORDER BY p.name, v.title;

\echo '\n== 3. O que a empresa vê dos candidatos antes do match: formação, descrição e competências'
SELECT c.training                                  AS formacao,
       p.description                               AS descricao,
       string_agg(co.name, ', ' ORDER BY co.name)  AS competencias
FROM candidate c
JOIN person p                ON p.id = c.person_id
JOIN candidate_competence cc ON cc.candidate_id = c.id
JOIN competence co           ON co.id = cc.competence_id
GROUP BY c.id, p.description
ORDER BY c.id;

\echo '\n== 4. O que o candidato vê das vagas abertas: sem dados da empresa'
SELECT v.title                                     AS vaga,
       v.description                               AS descricao,
       a.city || '/' || a.state                    AS local,
       string_agg(co.name, ', ' ORDER BY co.name)  AS competencias
FROM vacancy v
JOIN address a             ON a.id = v.address_id
JOIN vacancy_competence vc ON vc.vacancy_id = v.id
JOIN competence co         ON co.id = vc.competence_id
WHERE v.status = 'OPEN'
GROUP BY v.id, a.city, a.state
ORDER BY v.id;

\echo '\n== 5. Curtidas dos candidatos nas vagas'
SELECT p.name AS candidato, v.title AS vaga
FROM candidate_like cl
JOIN candidate c ON c.id = cl.candidate_id
JOIN person p    ON p.id = c.person_id
JOIN vacancy v   ON v.id = cl.vacancy_id
ORDER BY p.name, v.title;

\echo '\n== 6. Curtidas das empresas nos candidatos'
SELECT pe.name AS empresa, pc.name AS candidato
FROM enterprise_like el
JOIN enterprise e ON e.id = el.enterprise_id
JOIN person pe    ON pe.id = e.person_id
JOIN candidate c  ON c.id = el.candidate_id
JOIN person pc    ON pc.id = c.person_id
ORDER BY pe.name, pc.name;

\echo '\n== 7. Matches: o candidato curtiu a vaga e a empresa dona dela curtiu o candidato'
SELECT pc.name  AS candidato,
       pe.name  AS empresa,
       v.title  AS vaga,
       m.matched_at
FROM vacancy_match m
JOIN candidate c  ON c.id = m.candidate_id
JOIN person pc    ON pc.id = c.person_id
JOIN vacancy v    ON v.id = m.vacancy_id
JOIN enterprise e ON e.id = v.enterprise_id
JOIN person pe    ON pe.id = e.person_id
ORDER BY m.id;
