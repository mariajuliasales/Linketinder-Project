# Validação: Recrutador (Etapa 8)

- **Spec:** [spec.md](spec.md) · **Plano:** [plano.md](plano.md) · **Issue:** #21 · **Branch:** `feat/recruiter`
- **Data:** 2026-11-23 · **Commit validado:** `cdade9e`
- **Banco:** descartável `linketinder_etapa8` (`reset+migrar`, removido no fim)
- **Resultado:** ❌ **não está pronta para o PR.** 45 CAs OK, **2 falharam (CA37, CA38)** e 6 não puderam ser verificados
  nesta sessão (só front ou bloqueados pela falha).

Legenda: `test/` = `Linketinder/src/test/groovy/com/mariajuliasales/`. "Real" = chamada contra o PostgreSQL
(`linketinder_etapa8`), pela API (`./gradlew runApi`) ou, para o que não tem rota (spec 6.4), por um script descartável
que monta `Application#createControllers` e chama os controllers reais. O script ficou fora do repositório.

---

## 1. Estado do plano

- T01–T43: todas marcadas `[x] Concluída`, incluindo T40, T41, T42 e T43. Nenhuma tarefa adiada.
- Pendências registradas que ficam para depois do PR:
  1. **T33, decisão em aberto:** a tela da empresa não mostra a lista de vagas com match. A spec 2.4 ("Empresa E vendo
     C: a mesma regra do recrutador") e o AGENTS.md 7.3 incluem a lista; a decisão 4 (spec 8) e o CA38 não. Hoje o
     back-end devolve `matchedVacancies` para a empresa (`listCandidatesForEnterprise`) e o front não mostra.
  2. Limitações aceitas (AGENTS.md 12 e spec 6.6): API sem autenticação, enumeração de e-mail no cadastro de
     recrutador, invariante `enterprise_like` ⇔ `recruiter_like` só no serviço, sem testes de integração com o banco,
     e-mail com maiúsculas para candidato e empresa, senha em texto no front.

## 2. Testes automatizados

| Suíte | Comando | Resultado |
|---|---|---|
| Back-end | `./gradlew clean test` | ✅ 58 specs, **512 testes, 0 falhas, 0 erros, 0 ignorados** |
| Front-end | não tem testes (AGENTS.md 9.5) | — |
| Front-end | `npx tsc --noEmit` | ✅ sem erros |
| Front-end | `npm run build` | ✅ passou |

Todos os testes Spock citados na tabela da seção 3 existem e passaram (conferido nos XML de `build/test-results`).

## 3. Critérios de aceite

| CA | Como foi verificado | Resultado |
|---|---|---|
| CA01 | Real: `reset+migrar`. Migrations 001–003 sem erro; `recruitment_area` com 7 áreas (TI, RH, Marketing, Segurança da Informação, Financeiro, Comercial, Operações), `recruiter` e `recruiter_like` existem | OK |
| CA02 | Real: laço de migrations reaplicado. Contagens (áreas, recrutadores, `recruiter_like`, `enterprise_like`, `candidate_like`, matches, pessoas) = 7, 5, 4, 4, 5, 3, 17 antes e depois | OK |
| CA03 | Real: `INSERT` em `person` sem `address_id`, em transação desfeita. `CANDIDATE` e `ENTERPRISE` → erro `person_address_required_check`; `RECRUITER` → aceito | OK |
| CA04 | Real: consulta de `enterprise_like` sem `recruiter_like` e vice-versa = 0 e 0, no banco recém-migrado **e** ao fim de todos os cenários | OK |
| CA05 | Leitura do `Database/linketinder.dbml`: `RECRUITER`, `address_id` opcional com o `CHECK`, `recruitment_area`, `recruiter`, `recruiter_like` e as `Ref` novas | OK |
| CA06 | `test/api/RecruiterServletSpec` "a valid JSON should save the recruiter…(CA06)". Real: `POST /api/recruiters` → 201 `{id, name, email, area, description}` | OK |
| CA07 | `test/service/RecruiterServiceSpec` "create should fail with 409 when the email is already in use, ignoring case". Real: e-mail de candidata (`MariaJuliaSales@Gmail.com`), de empresa (`CONTATO@…`) e de recrutador (`Bruna.Costa@…`) → 409 "E-mail já cadastrado"; nenhuma `person` salva | OK |
| CA08 | `RecruiterServletSpec` "an invalid CPF should answer 400 and a repeated CPF should answer 409 (CA08)". Real: `111.111.111-11` → 400 "CPF inválido"; CPF de outro recrutador sem máscara → 409 "CPF já cadastrado" (via `recruiter_cpf_key` no PostgreSQL), sem `person` salva | OK |
| CA09 | `RecruiterServiceSpec` "create should accept the CPF of an existing candidate". Real: CPF da candidata 6 → 201 | OK |
| CA10 | `test/service/validator/RecruiterValidatorSpec` (futuro, 18 hoje, menor de 18). Real: `2027-01-01` → 400; `2008-11-24` → 400 "O recrutador deve ter pelo menos 18 anos"; `2008-11-23` → 201 | OK |
| CA11 | `RecruiterValidatorSpec` "area '#name' outside the list or empty…". Real: `Jurídico` e `""` → 400 "Área de recrutamento inválida"; `ti` → 201 com área `TI` | OK |
| CA12 | `RecruiterValidatorSpec` "recruiter with invalid #field…". Real: nome vazio → 400 "Nome é obrigatório"; senha `12345` → 400 "A senha deve ter pelo menos 6 caracteres" | OK |
| CA13 | `RecruiterServletSpec` "a missing enterprise should answer 404…(CA13)". Real: `enterpriseId` 999 → 404 "Empresa com ID 999 não existe" | OK |
| CA14 | `RecruiterServiceSpec` "removeByEnterprise should fail with 404…(CA14)". Real: E2 remove R3 (de E1) → 404 "Recrutador com ID 3 não existe", igual a R999; R3 continua | OK |
| CA15 | `RecruiterServiceSpec` "removeByEnterprise of the only recruiter…(CA15, B15)". Real: E1 remove R3, único a curtir C5 → somem `recruiter_like`, `enterprise_like(1, 5)`, o match (5, 1) e a `person` de R3 | OK |
| CA16 | `RecruiterServiceSpec` "…keep the enterprise like and the matches…(CA16)". Real: R2 e R3 curtem C5 (match V1), só R2 curte C2 (match V1). E1 remove R2 → C5 mantém `enterprise_like` e match; C2 perde os dois (borda da 2.1) | OK |
| CA17 | `RecruiterServiceSpec` "update own profile…" (3 testes). Real (controller): senha vazia mantém `123456` (`crypt` confere) e salva a descrição; `senhaNova1` passa a valer; `123` → 400; R999 → 404. Sem rota na API, como previsto | OK |
| CA18 | Código: `pages/register.ts` só tem "Sou candidato" e "Sou empresa". Sem navegador nesta sessão | Não verificável (roteiro item 1) |
| CA19 | `test/controller/RecruiterControllerSpec` "listEnterpriseVacancies…(CA19, CA21)". Real: R1 (Pastelsoft) vê V6 `OPEN`, V7 `OPEN`, V8 `CLOSED`; R2 (E1) vê V1, V9 `PAUSED`, V10, V12 | OK |
| CA20 | `RecruiterControllerSpec` "no vacancy create, update or delete operation should receive a recruiter (CA20)". Código: `pages/myVacancies.ts` usa `requireLoggedEnterprise`; as telas do recrutador não têm ação de vaga | OK (navegador no roteiro item 7) |
| CA21 | `RecruiterControllerSpec` "(CA19, CA21)". Real: nenhuma vaga de outra empresa nas listas de R1 e R2 | OK |
| CA22 | `test/service/CandidateLikeServiceSpec` "(B6)" e "(B11)". Real: C3 curte V1, V9, V10 sem recrutador de E1 → só `candidate_like`; com E1 curtindo C3, C3 curte V12 → curtida + match (3, 12) | OK |
| CA23 | `CandidateLikeServiceSpec` "like should fail with 400…PAUSED/CLOSED". Real: V8 `CLOSED` e V11 `PAUSED` → 400 "Só é possível curtir vagas abertas"; nada salvo | OK |
| CA24 | `CandidateLikeServiceSpec` "…already likes…", "…did not like…". Real: curtir V1 de novo e descurtir V7 não curtida → sem erro, estado igual | OK |
| CA25 | `CandidateLikeServiceSpec` "(B12)". Real: C3 descurte V1 → match (3, 1) some pela cascata do banco; `enterprise_like(1, 3)` e matches (3, 9), (3, 12) continuam | OK |
| CA26 | `test/service/VacancyServiceSpec` "(B17)". Real: V9 (com match) pausada → curtida e match continuam | OK |
| CA27 | `test/service/RecruiterLikeServiceSpec` "(B1)". Real: C3 curtiu V1, V9 (E1, `OPEN`) e V2 (E2). R2 curte C3 → `recruiter_like(2, 3)`, `enterprise_like(1, 3)`, matches (3, 1) e (3, 9); nada com V2 | OK |
| CA28 | `RecruiterLikeServiceSpec` "(B2)". Real (indireto): C5 sem curtida em E1 antes de curtir V1; ver CA33 | OK |
| CA29 | `RecruiterLikeServiceSpec` "(B3)". Real: R3 curte C3 (já curtido por R2) → só `recruiter_like(3, 3)`; `enterprise_like` e matches iguais | OK |
| CA30 | `RecruiterLikeServiceSpec` "(B4)" e "(B5)". Real: R2 descurte C3 → só some `recruiter_like(2, 3)`; R3 descurte → somem `enterprise_like(1, 3)` e todos os matches de C3 com E1 | OK |
| CA31 | `RecruiterLikeServiceSpec` "(B7)", "(B8)", "(B10)", "(B18)", "(B19)". Real: curtir de novo e descurtir não curtido → sem erro; C999 → 404 "Candidato com ID 999 não existe"; R999 → 404 "Recrutador com ID 999 não existe"; descurtir C999 → nada muda | OK |
| CA32 | `RecruiterLikeServiceSpec` "(B9)". Real: R8 (CPF da C6) curte C6 → 400 "Você não pode curtir o seu próprio perfil de candidato."; estado de C6 igual | OK |
| CA33 | `RecruiterLikeServiceSpec` "(B13)". Real: C5 curte V1; R3 (área RH) curte C5 → match (5, 1) | OK |
| CA34 | `RecruiterLikeServiceSpec` "(B14)". Real: V10 pausada antes da primeira curtida de E1 em C3 → sem match (3, 10) e `candidate_like(3, 10)` mantida; V10 reaberta → match não criado | OK |
| CA35 | `RecruiterLikeServiceSpec` "(B16)". Real: trigger temporário no banco descartável que falha no `INSERT` em `vacancy_match` para C1. R2 curte C1 → `PSQLException`; nenhuma `recruiter_like`, `enterprise_like` ou match salvo (rollback real no PostgreSQL) | OK |
| CA36 | Código: `pages/candidates.ts` não tem botão de curtir. Sem navegador | Não verificável (roteiro item 4) |
| CA37 | `test/controller/LikeControllerSpec` "recruiter should see the matched candidate revealed…" passa (com mock). **Real: `LikeController#listCandidatesForRecruiter(2)` lança `GroovyCastException` sempre que a empresa tem ao menos um match** (seção 7, F1). Front isolado não verificado nesta sessão | **Falhou** (back) |
| CA38 | `LikeControllerSpec` "enterprise should see the matched candidate revealed" passa (com mock). **Real: `listCandidatesForEnterprise(1)` lança a mesma exceção** (F1) | **Falhou** (back) |
| CA39 | `LikeControllerSpec` "candidate liked vacancies should reveal…". Real: "Minhas curtidas" de C3 traz nome e e-mail da TechSolutions só em V1, V9, V12 (com match); depois de desfeitos os matches, todas voltam com `enterpriseName`/`enterpriseEmail` nulos | OK |
| CA40 | `test/mapper/MatchMapperSpec` "candidate without match should have only anonymous data"; `test/service/MatchQueryServiceSpec` "candidate without match…should not appear". Real: listagem anônima (`CandidateController#findAll`) com só `id`, `training`, `description`, `competences` | OK |
| CA41 | `MatchMapperSpec` "candidate response should never contain sensitive data…"; `LikeControllerSpec` "reveal response should not carry sensitive fields". A chamada real que traria o candidato revelado cai em F1 | Não verificável (bloqueado por F1) |
| CA42 | `MatchMapperSpec` "vacancy without match should not reveal the enterprise". Real: V2 e V10 (sem match) com `enterpriseName`/`enterpriseEmail` nulos; o JSON não tem `enterpriseId` nem CNPJ | OK |
| CA43 | Código: `pages/vacancies.ts` não usa dado de recrutador. Back: nenhuma resposta ao candidato tem campo de recrutador (real). Tela sem navegador | Não verificável (roteiro itens 2 e 3) |
| CA44 | `test/mapper/RecruiterMapperSpec` "response should not contain sensitive fields"; `RecruiterServletSpec` "(CA44)". Real: JSON do 201 só com `id`, `name`, `email`, `area`, `description` | OK |
| CA45 | Código: telas do recrutador não listam recrutadores. Sem navegador | Não verificável (roteiro item 6) |
| CA46 | `RecruiterServiceSpec` e `RecruiterControllerSpec` "listByEnterprise…(CA46)". Real: E1 vê só R2, R3, R6, R9, com `id`, `name`, `email`, `area`, `description` | OK (tela no roteiro item 8) |
| CA47 | `./gradlew clean test`: 512 testes, 0 falhas, sem PostgreSQL | OK |
| CA48 | `npm run build` | OK |
| CA49 | `git diff main -- Database/schema.sql Database/seed.sql Database/queries.sql` vazio | OK |
| CA50 | `Frontend/src/data/seed.ts` comparado com o banco migrado: 5 recrutadores (mesmos dados), 5 `candidate_like`, 4 `enterprise_like`, 4 `recruiter_like`, 3 matches iguais; `DATA_FORMAT_VERSION = '5'` | OK |
| CA51 | Código: `services/auth.ts` inclui os recrutadores no login. Real: `crypt('123456')` confere para os recrutadores de exemplo. Tela sem navegador | Não verificável (roteiro item 5) |
| CA52 | `MatchQueryServiceSpec` "liked vacancies should keep every status…". Real: com V1 (match) e V2 (sem match, pausada), "Minhas curtidas" traz as duas com o status de cada uma, empresa só em V1; a lista principal não traz V2. Código: em `pages/vacancies.ts`, "Vagas abertas" vem antes de "Minhas curtidas" | OK (tela no roteiro item 2) |
| CA53 | `test/service/EnterpriseServiceSpec` "(CA53)" (3 testes, inclui rollback). Real: E1 excluída com R6 e R9 → 0 `person` `RECRUITER` sem `recruiter`, `person` de `bruna.costa@exemplo.com` apagada (e-mail livre de novo), 0 `enterprise_like` e matches de E1; E5 (sem recrutadores) excluída normalmente | OK |

**Totais:** 45 OK · 2 Falhou (CA37, CA38) · 6 Não verificável (CA18, CA36, CA41, CA43, CA45, CA51).

Os "Não verificável" de front dependem de navegador, que esta sessão não tem (só Firefox, sem automação). Como
verificar: roteiro da seção 6. O CA41 volta a ser verificável depois da correção de F1, repetindo a chamada real de
`listCandidatesForRecruiter` com um candidato com match.

## 4. Anonimato

| Perfil e ponto | Onde foi conferido | Campo conferido | Resultado |
|---|---|---|---|
| Candidato, vaga sem match | `LikeController#listLikedVacancies(3)` (real) | `enterpriseName`, `enterpriseEmail` nulos em V2 e V10; sem `enterpriseId`, CNPJ ou dado de recrutador | ✅ |
| Candidato, vaga com match | idem | só nome e e-mail da empresa em V1, V9, V12; nenhum campo de recrutador | ✅ |
| Candidato, fim do match | idem, depois de CA30 | nome e e-mail voltam a nulos | ✅ |
| Candidato, vagas de terceiros | `POST /api/vacancies` e `RecruiterController#listEnterpriseVacancies` (real) | `VacancyAnonymousResponse`: só id, título, descrição, cidade/UF, status, competências | ✅ |
| Candidato, via recrutador | Todas as respostas ao candidato (acima) | nenhum dado de recrutador nem indicação de curtida | ✅ |
| Empresa/recrutador, candidato sem match | `CandidateController#findAll` (real); `MatchMapperSpec` | só `id`, `training`, `description`, `competences`; sem nome, e-mail, CPF, nascimento, idade, endereço | ✅ |
| Empresa/recrutador, candidato com match | `listCandidatesForRecruiter`/`ForEnterprise` | **não verificável por chamada real (F1)**. Spock (`MatchMapperSpec`, `LikeControllerSpec`) confirma só nome, e-mail e vagas com match (id, título, status) | ⚠️ |
| Empresa, próprios recrutadores | `RecruiterController#listByEnterprise(1)` (real) | sem CPF, nascimento, idade, senha | ✅ |
| Cadastro de recrutador | `POST /api/recruiters` (real) | 201 sem CPF, nascimento, idade, senha | ✅ |
| Erros | Respostas reais de 400/404/409 | não repetem CPF nem e-mail; 404 de recrutador de outra empresa igual ao de id inexistente. 409 de e-mail é a limitação aceita | ✅ |
| Console | `./gradlew run` equivalente (real) | listagens anônimas iguais às da `main`; recrutadores não aparecem; `main/view/` sem diff | ✅ |

A F1 não vaza dados: a chamada falha antes de montar a resposta.

## 5. Regressão dos fluxos antigos

| Fluxo | Como | Resultado |
|---|---|---|
| Cadastro de candidato (API) | `POST /api/candidates` | ✅ 201 (senha em bcrypt, `crypt` confere, resposta sem senha); 409 e-mail; 409 CPF; 400 CPF inválido |
| Cadastro de empresa (API) | `POST /api/enterprises` | ✅ 201; 409 e-mail (mesma caixa); 409 CNPJ. `NOVA.ENT@x.com` foi aceito sobre `nova.ent@x.com`: bug conhecido (AGENTS.md 12), não é regressão |
| Cadastro de vaga (API) | `POST /api/vacancies` | ✅ 201 `OPEN` e `PAUSED`; 404 empresa inexistente; 400 sem competência |
| Cadastro e listagem (console) | Menu com entrada por arquivo | ✅ candidato e empresa cadastrados; "Listar candidatos", "Listar empresas" e "Listar todas as vagas" anônimos e corretos |
| Curtida do candidato em vaga | Controller (real) | ✅ CA22–CA25 |
| "Curtida da empresa" em candidato | Controller (real) | ✅ agora feita por recrutador (spec 2.3; a empresa não curte mais). `enterprise_like` e matches gerados como antes (CA27) |
| Geração de match | Controller (real) | ✅ CA22 (B11), CA27, CA33 |
| Exclusão de empresa com recrutadores | `EnterpriseController#delete(1)` (real) | ✅ CA53 |
| Exclusão de empresa sem recrutadores | `EnterpriseController#delete(5)` (real) | ✅ |
| Telas do front (cadastro, vagas do candidato, perfil da empresa, gráfico) | Código + `npm run build` | ⚠️ sem navegador: "Vagas abertas" antes de "Minhas curtidas", rótulo "Pausada", cadastro só com candidato/empresa e gráfico em `pages/candidates.ts` conferidos no código. Tela no roteiro |

Banco `linketinder_etapa8` apagado no fim (`dropdb`, conferido em `pg_database`). API encerrada.

## 6. Roteiro manual (navegador)

Antes: `npm run dev`, `localStorage` limpo, senha `123456`.

1. **Cadastro (CA18):** abra `#/register`. Esperado: só "Sou candidato" e "Sou empresa". Cadastre um candidato e
   uma empresa novos e faça login com eles.
2. **Candidata `mariajuliasales@gmail.com` (CA52):** curta "Desenvolvedor Full Stack". Esperado: "Vagas abertas" em
   cima e "Minhas curtidas" logo abaixo, com a vaga e o selo *Aberta*.
3. **Pausada (CA52, CA43):** como `contato@techsolutions.com.br`, pause a Full Stack; volte como a candidata.
   Esperado: a vaga sai de "Vagas abertas", fica em "Minhas curtidas" com *Pausada*, pode ser descurtida e não mostra
   empresa nem recrutador (DOM, hover card e `aria-*`).
4. **Empresa TechSolutions, Candidatos (CA36, CA38):** Esperado: sem botão de curtir; Lucas revelado (nome, e-mail,
   selo "Match"); os demais como "Candidato #N"; gráfico de competências aparece sem dados pessoais.
5. **Login do recrutador (CA51):** `rafael.lopes@techsolutions.com.br`. Esperado: cai em `#/recruiter-candidates`,
   com o nome da empresa no cabeçalho.
6. **Recrutador, Candidatos (CA37, CA45):** Esperado: Lucas revelado com só a vaga com match; os demais anônimos;
   nenhuma menção a Juliana ou a outros recrutadores.
7. **Recrutador sem gestão de vagas (CA19, CA20):** abra "Vagas da empresa" (todas, com status, sem botões) e depois
   digite `#/my-vacancies`. Esperado: redirecionado.
8. **Recrutadores da empresa (CA46):** como TechSolutions, abra `#/recruiters`. Esperado: só Rafael e Juliana, com
   nome, e-mail, área e descrição; sem CPF nem nascimento no DOM e nos atributos.
9. **Match pela curtida do recrutador (CA27, CA37):** como Juliana, curta o candidato da Maria Julia (ela curtiu a
   Full Stack, reaberta). Esperado: ela aparece revelada; como a candidata, a Full Stack mostra nome e e-mail da
   TechSolutions.
10. **Fim do match (CA30, CA37, CA39):** como Juliana, descurta. Esperado: a candidata volta a "Candidato #N" e a vaga
    volta sem empresa.
11. **Remoção de recrutador (CA15):** como TechSolutions, remova o Rafael. Esperado: Lucas volta a "Candidato #N" e o
    login do Rafael passa a falhar.
12. **Perfil do recrutador (CA17):** como Juliana, edite a descrição com a senha vazia, saia e entre com `123456`;
    tente a senha `123`. Esperado: login funciona; senha curta mostra erro.
13. **Exclusão de empresa (CA53, front):** cadastre um recrutador na empresa criada no item 1 e exclua a conta da
    empresa. Esperado: o login do recrutador falha e o e-mail pode ser usado de novo.

## 7. Falhas encontradas

**F1 — Revelação de candidatos quebra no banco real (CA37, CA38).**

- **Onde:** [`CandidateDAO.groovy:51`](../../../Linketinder/src/main/groovy/com/mariajuliasales/dao/CandidateDAO.groovy#L51),
  `List<Integer> distinctIds = ids.toUnique()`, chamado por `MatchQueryService.groovy:91` com
  `vacanciesByCandidate.keySet()`.
- **Causa provável:** o argumento é um `Set` (`LinkedHashSet`). `Set#toUnique()` devolve outro `Set`, e a atribuição a
  `List<Integer>` falha em Groovy dinâmico: `GroovyCastException: Cannot cast object '[2, 3, 5]' with class
  'java.util.LinkedHashSet' to class 'java.util.List'`.
- **Quando acontece:** em toda chamada de `listCandidatesForRecruiter` ou `listCandidatesForEnterprise` em que a
  empresa tem pelo menos um match. Sem match, o serviço retorna antes e não falha.
- **Por que os testes não pegaram:** `MatchQueryServiceSpec` e `LikeControllerSpec` usam `Mock()` do
  `CandidateRepository`, e o `CandidateDAO#findAllByIds` não tem teste próprio. Introduzido em `748df3d`.
- **Impacto:** só o back-end. Não há rota na API e o front é isolado, então a tela do front não é afetada. Nenhum dado
  vaza.
- **Correção proposta (nova tarefa T44, aguarda autorização):** converter para lista antes de deduplicar, por exemplo
  `List<Integer> distinctIds = ids.toList().unique()` (ou `new ArrayList<>(ids).unique()`), e criar um
  `CandidateDAOSpec` que chame `findAllByIds` com um `Set`. Depois, repetir CA37, CA38 e CA41 com chamada real.

## 8. Dúvidas e desvios

- **"Se algo falhar, registre e pare":** não corrigi a F1. Terminei as verificações que não dependiam dela (banco,
  API, console, anonimato), para que o relatório mostre todo o estado de uma vez. Não criei a T44 no plano: ela
  depende da sua autorização.
- **Verificação real sem rota:** curtidas, revelação, remoção e edição de recrutador não têm rota na API nem menu no
  console (spec 6.4, 7). Usei um script Groovy descartável, fora do repositório, que monta os controllers reais por
  `Application#createControllers` e aponta para o `linketinder_etapa8`.
- **CA35 com trigger:** para provocar uma falha real do PostgreSQL ao gravar o match, criei e depois removi um
  trigger só no banco descartável. Nenhum script do repositório foi alterado.
- **Senha do banco:** usei o padrão do projeto (`postgres`/`postgres`, `DatabaseConfig#fromEnvironment`).
- **Dados alterados pelos cenários:** ao pausar ou reabrir vagas pelo controller, a descrição e a cidade de V2, V9 e
  V10 foram trocadas pelos valores do script ("Desc", Recife/PE). É efeito do cenário, não do sistema.
- **CNPJ repetido no 1º teste de empresa:** usei um CNPJ que já estava no seed. O 409 estava correto; repeti com outro.
- **CHANGELOG:** não há uma "entrada da feature" única. Acrescentei a linha no topo de `[Não lançado] › Adicionado`,
  com o texto pedido e o aviso das falhas, porque "Validação concluída" sozinho daria a entender que tudo passou.
