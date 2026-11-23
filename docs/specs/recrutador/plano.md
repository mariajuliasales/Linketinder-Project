# Plano de implementação: Recrutador

- **Spec:** [spec.md](spec.md) (aprovada)
- **Issue:** [#21](https://github.com/mariajuliasales/Linketinder-Project/issues/21)
- **Branch:** `feat/recruiter`
- **Status:** aprovado
- **Ordem:** banco de dados → back-end com testes → front-end → validação final. Dentro de cada fase, da base para as
  regras que dependem dela.

---

## Plano por camada

| Camada | Integrada ou isolada? | Decisão para esta feature |
|---|---|---|
| Banco (PostgreSQL) | integrada com o back-end | 3 migrations novas (`001`, `002`, `003`) e `linketinder.dbml`. `schema.sql`, `seed.sql` e `queries.sql` **não** mudam (CA49) |
| Back-end (Groovy) | integrada com o banco | Recrutador, curtidas e match em model, repository, DAO, service, validator, DTO, mapper e controller. Só uma rota nova: `POST /api/recruiters` |
| Front-end (TypeScript) | isolada (`localStorage`) | Mesmas regras reimplementadas em `services/`, coleções novas, `DATA_FORMAT_VERSION` = `5`, seed alinhado às migrations e telas novas |

### Banco
- `001_add_recruiter_person_type.sql`: só o `ALTER TYPE person_type ADD VALUE IF NOT EXISTS 'RECRUITER'`, **sem**
  `BEGIN/COMMIT`.
- `002_create_recruiter.sql`: `recruitment_area` com as 7 áreas, `person.address_id` opcional com o `CHECK
  person_address_required_check`, `recruiter` (com `recruiter_cpf_key`) e `recruiter_like`, com índices.
- `003_insert_example_recruiters.sql`: os 5 recrutadores de exemplo (tabela abaixo) e o backfill das `enterprise_like`
  órfãs.
- **Risco de reset:** reaplicar só o `schema.sql` sobre um banco migrado não é suportado (spec 3.3). O reset oficial
  (`dropdb` + `createdb` + scripts base + migrations) entra no AGENTS.md em T02, e é o comando usado na validação de
  todas as tarefas de banco.

**Recrutadores de exemplo** (senha `123456` via `crypt`, sem endereço, CPFs válidos e diferentes dos candidatos do seed)

| Empresa | Nome | E-mail | CPF | Nascimento | Área | Backfill |
|---|---|---|---|---|---|---|
| Pastelsoft | Carla Mendes Prado | `carla.prado@pastelsoft.com.br` | `71460258371` | 1990-04-12 | TI | curte Sandubinha |
| TechSolutions Brasil | Rafael Teixeira Lopes | `rafael.lopes@techsolutions.com.br` | `30195847601` | 1988-09-03 | TI | curte Lucas |
| TechSolutions Brasil | Juliana Freitas Moura | `juliana.moura@techsolutions.com.br` | `84621593005` | 1993-02-27 | RH | — (2º recrutador) |
| DataAnalytics Global | Paulo Henrique Ramos | `paulo.ramos@dataanalytics.com` | `59037481205` | 1985-11-18 | Segurança da Informação | curte Gabriel |
| CloudScale Services | Tatiane Rocha Batista | `tatiane.batista@cloudscale.com` | `26384910551` | 1996-06-08 | Operações | curte Fernanda |

O "primeiro recrutador de exemplo de E" do backfill é escolhido pelo e-mail (coluna "Backfill"), não pelo `id`. Isso
deixa a escolha explícita e estável.

### Back-end Groovy
- **Transação:** os services de curtida e de remoção recebem o `ConnectionManager` e abrem `withTransaction`. Os
  repositórios de curtida e match têm métodos pequenos que recebem a `Connection` (mesmo padrão do `CompetenceLinkDAO`).
  Nos testes, um `Stub(ConnectionManager)` executa o closure com uma `Connection` falsa, e os repositórios são `Mock()`.
  Assim, B1–B17 são verificados com `1 *` / `0 *`, sem banco. O `DatabaseConnectionSpec` testa o `rollback` com uma
  `Connection` `Mock()`, não contra o PostgreSQL. Não há teste automático do rollback no banco real (AGENTS.md 12).
- **Concorrência (spec 6.6):** toda operação que lê ou muda `enterprise_like` começa com
  `SELECT id FROM enterprise WHERE id = ? FOR UPDATE` na empresa E envolvida. Essa trava serializa as curtidas e
  descurtidas de recrutadores de E, as curtidas de candidatos em vagas de E e a remoção de recrutador. O teste confere a
  **ordem**: a trava é a primeira chamada da transação.
  - A trava de E **não** protege o status da vaga: `VacancyDAO#update` e `#delete` não travam E. Por isso, quem
    depende do status `OPEN` relê a vaga com `FOR SHARE` dentro da transação, depois de travar E.
  - **Ordem das travas: sempre enterprise antes de vacancy. Nenhum fluxo pode travar vacancy e depois enterprise.**
- **`RecruiterDAO`** não estende o `PersonDAO`. Ele tem SQL próprio para `person` (com `address_id` nulo e sem
  competências) e reaproveita só o `SqlExecutor`. As consultas nunca selecionam `password_hash`.
- **Área:** enum `RecruitmentArea` no back-end, espelhando a tabela, com o nome de exibição (`TI`, `RH`,
  `Segurança da Informação`…) e a busca sem diferenciar maiúsculas. O DAO resolve o `area_id` pelo nome.

### Front-end TypeScript
- `types.ts`: `RECRUITER` em `PersonType`, `Recruiter`, `RECRUITMENT_AREAS` e os tipos de curtida e match. Tipos para
  `pages/`: `RecruiterSummary`, `CandidateCard` (anônimo ou revelado), `VacancyCard` e `LikedVacancyCard`.
- `storage.ts`: coleções `recruiters`, `candidateLikes`, `recruiterLikes`, `enterpriseLikes` e `matches`, com
  `DATA_FORMAT_VERSION` = `5`.
- `services/likes.ts`: a mesma regra do back-end (invariante, match só com vaga `OPEN`, autocurtida bloqueada).
- Telas novas, uma por tarefa: recrutadores da empresa, candidatos do recrutador, vagas do recrutador e perfil do
  recrutador. Telas alteradas: candidatos da empresa, vagas do candidato (com "Minhas curtidas") e home.
- Na tela, o `id` é usado só em closures de eventos. Ele nunca é renderizado nem colocado em `data-*`/`aria-*`.

---

## Regras para todas as tarefas

- Caminhos abreviados (são os caminhos reais):
  - `main/` = `Linketinder/src/main/groovy/com/mariajuliasales/`
  - `test/` = `Linketinder/src/test/groovy/com/mariajuliasales/`
  - `front/` = `Frontend/src/`
- Comandos:
  - Banco, a partir de `Database/`, com `export PGPASSWORD=...`. **Reset oficial** (chamado de `reset+migrar` abaixo):
    ```bash
    dropdb -h localhost -U postgres --if-exists linketinder && createdb -h localhost -U postgres linketinder
    psql -h localhost -U postgres -d linketinder -v ON_ERROR_STOP=1 -f schema.sql
    psql -h localhost -U postgres -d linketinder -v ON_ERROR_STOP=1 -f seed.sql
    for f in migrations/*.sql; do psql -h localhost -U postgres -d linketinder -v ON_ERROR_STOP=1 -f "$f"; done
    ```
  - Back-end: `./gradlew test` em `Linketinder/`.
  - Front: `npm run build` em `Frontend/`.
- **Toda tarefa:**
  - altera só os arquivos listados, mais o `CHANGELOG.md` (entrada em `[Não lançado]`) e o checkbox da tarefa neste
    plano;
  - termina com `./gradlew test` passando; tarefas de front terminam também com `npm run build` passando e com a
    verificação manual descrita;
  - é um commit, com a mensagem sugerida e `Refs #21` no corpo. Quem faz o commit é a desenvolvedora.
- **Toda tarefa de regra de negócio no back-end** traz o teste Spock junto, na mesma tarefa.
- **Erros 404 no front:** os serviços do front (`services/likes.ts`, `services/recruiters.ts`…) mantêm o texto do
  back-end ("`<Entidade>` com ID `<id>` não existe"), mas **toda tela** troca essa mensagem por uma genérica, sem o id
  interno (AGENTS.md 8). As mensagens 400 e 409 são exibidas como vêm do serviço.
- Nenhuma tarefa altera `Database/schema.sql`, `Database/seed.sql`, `Database/queries.sql`, os menus do console
  (`main/view/`) nem o comportamento de `PersonDAO`, `PersonValidator`, `CandidateDAO` e `EnterpriseDAO`.
- `Database/DER-Linketinder.png` é regenerado **manualmente pela desenvolvedora**. O agente deixa esse item aberto.

## Antes de começar

A spec (com a seção "Minhas curtidas") e este plano são commitados antes de qualquer código.

---

## Fase 1 — Banco de dados

### T01 — Pasta de migrations e valor `RECRUITER` no ENUM
- **Camada:** banco
- **O que será feito:** cria `Database/migrations/` e a primeira migration, só com
  `ALTER TYPE person_type ADD VALUE IF NOT EXISTS 'RECRUITER';`, sem `BEGIN/COMMIT`. Prova que o laço de migrations do
  AGENTS.md funciona e pode ser repetido. É a Tarefa 1 da skill: não há dependência nova a preparar.
- **Arquivos envolvidos:** criar `Database/migrations/001_add_recruiter_person_type.sql`
- **Critérios de aceite cobertos:** CA01, CA02 (parte do ENUM)
- **Como validar:** `reset+migrar`, depois rodar de novo só o laço `for f in migrations/*.sql`; os dois sem erro.
  `psql ... -c "SELECT enum_range(NULL::person_type)"` → `{CANDIDATE,ENTERPRISE,RECRUITER}`.
- **Anonimato:** não se aplica.
- **Depende de:** —
- **Commit sugerido:** `feat(recrutador): add RECRUITER value to person_type enum`
- [x] Concluída

### T02 — AGENTS.md: reset do banco e Recrutador no escopo
- **Camada:** banco (documentação)
- **O que será feito:** na seção 4.2, troca o fluxo de reset pelo oficial (`dropdb` + `createdb` + `schema.sql` +
  `seed.sql` + migrations) e avisa que reaplicar só o `schema.sql` sobre um banco migrado não é suportado. No item 8 da
  seção 10, troca "fora do escopo" por uma referência à spec aprovada `docs/specs/recrutador/spec.md`.
- **Arquivos envolvidos:** alterar `AGENTS.md` (só seção 4.2 e item 8 da seção 10)
- **Critérios de aceite cobertos:** nenhum diretamente; sustenta CA01/CA02 (comando de reset) e destrava as tarefas
  seguintes
- **Como validar:** copiar e rodar o bloco da seção 4.2 sem alterações → termina sem erro. `git diff AGENTS.md` mostra
  só as duas seções.
- **Anonimato:** não se aplica.
- **Depende de:** T01
- **Commit sugerido:** `docs(recrutador): document official database reset and recruiter scope in AGENTS.md`
- [x] Concluída

### T03 — Migration de recrutador, área e curtida do recrutador
- **Camada:** banco
- **O que será feito:** `recruitment_area` com as 7 áreas (`ON CONFLICT (name) DO NOTHING`); `person.address_id DROP NOT
  NULL`; `CHECK person_address_required_check` dentro de um `DO $$` que consulta `pg_constraint`; `recruiter` com a FK
  composta, o `CHECK`, `recruiter_cpf_key`, a FK de empresa `ON DELETE CASCADE` e a FK de área `ON DELETE RESTRICT`;
  `recruiter_like` com PK composta; índices em `recruiter(enterprise_id)` e `recruiter_like(candidate_id)`. Tudo com
  `IF NOT EXISTS`.
- **Arquivos envolvidos:** criar `Database/migrations/002_create_recruiter.sql`
- **Critérios de aceite cobertos:** CA01, CA02, CA03
- **Como validar:**
  - `reset+migrar` e depois o laço de novo → sem erro;
  - `psql ... -c "\d recruiter"`, `-c "\d recruiter_like"` e `-c "SELECT count(*) FROM recruitment_area"` → 7;
  - CA03, dentro de `BEGIN; ... ROLLBACK;`: `INSERT` em `person` do tipo `CANDIDATE` sem `address_id` → erro do
    `person_address_required_check`; do tipo `RECRUITER` sem `address_id` → aceito.
- **Anonimato:** não se aplica (só estrutura).
- **Depende de:** T01, T02
- **Commit sugerido:** `feat(recrutador): create recruiter, recruitment_area and recruiter_like tables`
- [x] Concluída

### T04 — Recrutadores de exemplo e backfill das curtidas
- **Camada:** banco
- **O que será feito:** insere os 5 recrutadores da tabela "Recrutadores de exemplo" (`person` com `ON CONFLICT (email)
  DO NOTHING`, `recruiter` com `WHERE NOT EXISTS`). Faz o backfill: para cada `enterprise_like(E, C)` sem nenhuma
  `recruiter_like` de recrutador de E, cria a `recruiter_like` do recrutador indicado na coluna "Backfill".
- **Arquivos envolvidos:** criar `Database/migrations/003_insert_example_recruiters.sql`
- **Critérios de aceite cobertos:** CA01, CA02, CA04
- **Como validar:**
  - `reset+migrar`, guardar `SELECT count(*)` de `recruitment_area`, `recruiter`, `recruiter_like` e
    `enterprise_like`, rodar o laço de novo e comparar → contagens iguais (CA02);
  - CA04, nos dois sentidos, as duas consultas devem voltar vazias:
    `SELECT * FROM enterprise_like el WHERE NOT EXISTS (SELECT 1 FROM recruiter_like rl JOIN recruiter r ON r.id = rl.recruiter_id WHERE r.enterprise_id = el.enterprise_id AND rl.candidate_id = el.candidate_id)`
    e a inversa, partindo de `recruiter_like`;
  - login de exemplo: `SELECT password_hash = crypt('123456', password_hash) FROM person WHERE email = 'rafael.lopes@techsolutions.com.br'` → `t`.
- **Anonimato:** o script não seleciona nem imprime dados (sem `RAISE NOTICE` com e-mail ou CPF).
- **Depende de:** T03
- **Commit sugerido:** `feat(recrutador): add example recruiters and backfill recruiter likes`
- [x] Concluída

### T05 — Modelo DBML atualizado
- **Camada:** banco
- **O que será feito:** acrescenta no `linketinder.dbml` o valor `RECRUITER` no enum, `address_id` opcional com nota do
  `CHECK`, as tabelas `recruitment_area`, `recruiter` e `recruiter_like`, as referências e a nota da invariante
  `enterprise_like` ⇔ `recruiter_like` ("garantida pelo serviço"). O `seed.sql` **não** muda.
- **Arquivos envolvidos:** alterar `Database/linketinder.dbml`
- **Critérios de aceite cobertos:** CA05
- **Como validar:** comparar o dbml com `\d recruiter`, `\d recruiter_like`, `\d recruitment_area` e `\d person`;
  colar o dbml em dbdiagram.io e ver que ele renderiza sem erro. Deixar sinalizado: **DER-Linketinder.png a regenerar
  pela desenvolvedora**.
- **Anonimato:** não se aplica.
- **Depende de:** T04
- **Commit sugerido:** `docs(recrutador): add recruiter tables to DBML model`
- [x] Concluída

---

## Fase 2 — Back-end com testes

### T06 — Modelo do recrutador e área de recrutamento
- **Camada:** back-end
- **O que será feito:** `RECRUITER` em `PersonType`; enum `RecruitmentArea` com nome de exibição e
  `fromName(String)` sem diferenciar maiúsculas (`ti` → `TI`, `Jurídico` → `null`); model `Recruiter` (estende
  `AbstractPerson`, com `enterpriseId`, `area`, `cpf`, `birthDate` e `getAge`, sem endereço nem competências);
  `TestData.validRecruiter(changes)`.
- **Arquivos envolvidos:** alterar `main/model/PersonType.groovy`, `test/fixture/TestData.groovy`; criar
  `main/model/RecruitmentArea.groovy`, `main/model/Recruiter.groovy`, `test/model/RecruitmentAreaSpec.groovy`,
  `test/model/RecruiterSpec.groovy`
- **Critérios de aceite cobertos:** base de CA10 e CA11
- **Como validar:** `./gradlew test --tests '*RecruitmentAreaSpec' --tests '*RecruiterSpec'` e depois `./gradlew test`.
- **Anonimato:** `Recruiter` não ganha `toString` com campos (sem `@ToString`/`@Canonical`), para não vazar CPF ou senha
  em log.
- **Depende de:** T05
- **Commit sugerido:** `feat(recrutador): add Recruiter model and RecruitmentArea enum`
- [x] Concluída

### T07 — Validação do recrutador
- **Camada:** back-end
- **O que será feito:** `RecruiterValidator implements Validator<Recruiter>`:
  - nome obrigatório; e-mail válido (`EmailValidator`);
  - senha obrigatória no cadastro e opcional na atualização, com 6+ caracteres (`PasswordValidator`);
  - CPF válido (`CpfValidator`);
  - nascimento obrigatório, não futuro e 18+ (aceita quem faz 18 hoje, recusa quem faz amanhã);
  - área obrigatória e da lista: senão, "Área de recrutamento inválida".

  Não valida endereço nem competências.
- **Arquivos envolvidos:** criar `main/service/validator/RecruiterValidator.groovy`,
  `test/service/validator/RecruiterValidatorSpec.groovy`
- **Critérios de aceite cobertos:** CA08 (400), CA10, CA11, CA12, CA17 (senha na atualização)
- **Como validar:** `./gradlew test --tests '*RecruiterValidatorSpec'`, com casos para cada regra, incluindo os limites
  "18 anos hoje" e "18 anos amanhã" e as áreas `ti` e `Jurídico`.
- **Anonimato:** as mensagens de erro não repetem o valor recebido (CPF, e-mail).
- **Depende de:** T06
- **Commit sugerido:** `feat(recrutador): validate recruiter fields`
- [x] Concluída

### T08 — Persistência do recrutador
- **Camada:** back-end
- **O que será feito:**
  - `RecruiterRepository`: `create`, `findById`, `findByIdAndEnterprise`, `findAllByEnterprise`, `isEmailInUse` com
    `lower()` contra **todas** as pessoas, `updateProfile` (descrição e senha opcional) e `delete(Connection, id)`;
  - `RecruiterDAO` com SQL próprio (`person` sem endereço + `recruiter` na mesma transação; `area_id` resolvido pelo
    nome). As consultas nunca selecionam `password_hash`;
  - `SqlExceptionTranslator`: `recruiter_cpf_key` → "CPF já cadastrado".
- **Arquivos envolvidos:** criar `main/repository/RecruiterRepository.groovy`, `main/dao/RecruiterDAO.groovy`; alterar
  `main/dao/SqlExceptionTranslator.groovy`, `test/dao/SqlExceptionTranslatorSpec.groovy`
- **Critérios de aceite cobertos:** CA08 (409), CA06 (persistência)
- **Como validar:** `./gradlew test --tests '*SqlExceptionTranslatorSpec'`. O DAO não tem teste automático (não há testes
  de integração, AGENTS.md 12). Validação manual: as consultas do DAO rodadas no `psql` contra o banco de T04.
- **Anonimato:** conferir que nenhum `SELECT` traz `password_hash` e que `findAllByEnterprise` filtra por
  `enterprise_id`.
- **Depende de:** T07
- **Commit sugerido:** `feat(recrutador): add recruiter repository and JDBC DAO`
- [x] Concluída

### T09 — Serviço do recrutador: cadastro e edição
- **Camada:** back-end
- **O que será feito:** `RecruiterService`:
  - `create(enterpriseId, recruiter)`: valida, depois confere a empresa (404 "Empresa com ID `<id>` não existe") e o e-mail
    (`isEmailInUse` → 409 "E-mail já cadastrado"), e só então grava. **Não** confere o CPF contra candidatos;
  - `updateOwnProfile(recruiterId, description, password)`: senha vazia mantém a atual; senha curta → 400; recrutador
    inexistente → 404 "Recrutador com ID `<id>` não existe".
- **Arquivos envolvidos:** criar `main/service/RecruiterService.groovy`, `test/service/RecruiterServiceSpec.groovy`
- **Critérios de aceite cobertos:** CA06, CA07, CA09, CA13, CA17
- **Como validar:** `./gradlew test --tests '*RecruiterServiceSpec'`, cobrindo:
  - e-mail `Ana@X.com` em uso → 409 e `0 * repository.create(_)`;
  - CPF igual ao de um candidato → aceito;
  - empresa inexistente → 404 e nada salvo;
  - edição com senha vazia, curta e válida;
  - edição de recrutador inexistente → 404 (no carregamento e quando `updateProfile` devolve `false`).
- **Anonimato:** a mensagem de 409 é a genérica "E-mail já cadastrado" e não diz o tipo da pessoa que já usa o e-mail
  (limitação aceita na spec 6.6, sem piorá-la).
- **Depende de:** T08
- **Commit sugerido:** `feat(recrutador): add recruiter service with create and profile update`
- [x] Concluída

### T10 — Repositório de curtidas
- **Camada:** back-end
- **O que será feito:** interface `LikeRepository` e `LikeDAO`, com métodos pequenos que recebem a `Connection`:
  - `lockEnterprise` (`SELECT ... FOR UPDATE`);
  - `candidateLikeExists`, `insertCandidateLike`, `deleteCandidateLike`;
  - `insertRecruiterLike`, `deleteRecruiterLike`, `recruiterLikeExists`, `otherRecruiterOfEnterpriseLikes`,
    `candidatesLikedBy(recruiterId)`;
  - `enterpriseLikeExists`, `insertEnterpriseLike`, `deleteEnterpriseLike`.

  Os `INSERT` usam `ON CONFLICT DO NOTHING`. Nenhuma regra fica no SQL.
- **Arquivos envolvidos:** criar `main/repository/LikeRepository.groovy`, `main/dao/LikeDAO.groovy`
- **Critérios de aceite cobertos:** base de CA22–CA35
- **Como validar:** `./gradlew build` compila. As consultas são rodadas à mão no `psql` contra o banco de T04 (ex.:
  `otherRecruiterOfEnterpriseLikes` devolve `true` para Juliana e Lucas, porque Rafael já curte Lucas, e `false` para
  Rafael e Lucas antes de Juliana curtir Lucas).
- **Anonimato:** os métodos devolvem só ids e `boolean`, nunca nome, e-mail, CPF ou CNPJ.
- **Depende de:** T05
- **Commit sugerido:** `feat(recrutador): add like repository`
- [x] Concluída

### T11 — Repositório de match
- **Camada:** back-end
- **O que será feito:** interface `MatchRepository` e `MatchDAO`, com métodos que recebem a `Connection`:
  `likedVacanciesOfEnterprise(candidateId, enterpriseId)` (id e status das vagas de E que C curtiu, em qualquer
  status; o serviço filtra `OPEN`), `insertMatch` (`ON CONFLICT DO NOTHING`) e
  `deleteMatchesOfCandidateWithEnterprise`. Nenhuma regra fica no SQL.
- **Arquivos envolvidos:** criar `main/repository/MatchRepository.groovy`, `main/dao/MatchDAO.groovy`
- **Critérios de aceite cobertos:** base de CA22–CA35
- **Como validar:** `./gradlew build` compila. Consulta rodada à mão no `psql` contra o banco de T04:
  `likedVacanciesOfEnterprise` para Sandubinha e Pastelsoft devolve a vaga curtida com status `OPEN` (numa transação
  desfeita: curtir também a vaga `CLOSED` da Pastelsoft e pausar a `OPEN` → a lista traz as duas, com os status
  `CLOSED` e `PAUSED`).
- **Anonimato:** os métodos devolvem só ids e status, nunca nome, e-mail, CPF ou CNPJ.
- **Depende de:** T05
- **Commit sugerido:** `feat(recrutador): add match repository`
- [x] Concluída

### T12 — Curtida do candidato
- **Camada:** back-end
- **O que será feito:** `CandidateLikeService` com `like(candidateId, vacancyId)` e `unlike(candidateId, vacancyId)`,
  numa transação. Na curtida, a ordem é:
  - antes da transação: candidato existe (`CandidateRepository`, 404 "Candidato com ID `<id>` não existe") → carrega a
    vaga só para descobrir E (404 "Vaga com ID `<id>` não existe");
  - na transação: trava E (primeira chamada) → relê o status da vaga com `FOR SHARE` (`lockVacancyStatus`): existe
    (404 "Vaga com ID `<id>` não existe") e está `OPEN` (400 "Só é possível curtir vagas abertas") → grava a curtida
    (idempotente) → se `enterprise_like(E, C)` existe, cria o match.

  O status é conferido só na releitura: conferir fora da transação deixaria uma janela em que a vaga é pausada ou
  fechada antes da trava. Novo método `lockVacancyStatus(connection, vacancyId)` no `LikeRepository`/`LikeDAO`
  (`SELECT status FROM vacancy WHERE id = ? FOR SHARE`; `null` se a vaga não existe). Ordem das travas: sempre
  enterprise antes de vacancy. A descurtida funciona em qualquer status; o match cai pela FK.
- **Arquivos envolvidos:** criar `main/service/CandidateLikeService.groovy`, `test/service/CandidateLikeServiceSpec.groovy`;
  alterar `main/repository/LikeRepository.groovy`, `main/dao/LikeDAO.groovy` (`lockVacancyStatus`) e
  `test/service/VacancyServiceSpec.groovy` (B17)
- **Critérios de aceite cobertos:** CA22 (B6, B11), CA23, CA24, CA25 (B12), CA26 (B17)
- **Como validar:** `./gradlew test --tests '*CandidateLikeServiceSpec'`:
  - `PAUSED`/`CLOSED` → 400 e `0 * insertCandidateLike`;
  - vaga `OPEN` na primeira leitura e `CLOSED` no `lockVacancyStatus` → 400, nada salvo;
  - candidato inexistente → 404 "Candidato com ID `<id>` não existe", nada salvo;
  - ordem: `lockEnterprise` → `lockVacancyStatus` → `insertCandidateLike`;
  - sem `enterprise_like` → `0 * insertMatch`; com `enterprise_like` → `1 * insertMatch`;
  - curtir de novo e descurtir sem curtida → sem erro;
  - descurtir não chama `deleteEnterpriseLike` (B12);
  - B17: um teste no `VacancyServiceSpec` que confere que `update` de status não usa `MatchRepository`;
  - manual, com duas sessões `psql` contra um banco descartável:
    - sessão A: `BEGIN;` → `SELECT id FROM enterprise WHERE id = <E> FOR UPDATE;` →
      `SELECT status FROM vacancy WHERE id = <V> FOR SHARE;`;
    - sessão B: `UPDATE vacancy SET status = 'CLOSED' WHERE id = <V>;` precisa ficar **bloqueada**;
    - sessão A: `COMMIT;` (ou `ROLLBACK;`) → o `UPDATE` da sessão B termina.
- **Anonimato:** não se aplica (sem saída para terceiros).
- **Depende de:** T10, T11
- **Commit sugerido:** `feat(recrutador): add candidate like service with match on open vacancies`
- [x] Concluída

### T13 — Curtida do recrutador
- **Camada:** back-end
- **O que será feito:** `RecruiterLikeService.like(recruiterId, candidateId)`, numa transação. A ordem é:
  - antes da transação: recrutador existe (404 "Recrutador com ID `<id>` não existe") → candidato existe (404
    "Candidato com ID `<id>` não existe") → mesmo CPF, comparando só os dígitos (400 "Você não pode curtir o seu
    próprio perfil de candidato.");
  - na transação: trava E (se `lockEnterprise` indica que E não existe, 404 "Recrutador com ID `<id>` não existe",
    porque o recrutador cai junto em cascata) → confere `enterpriseLikeExists(E, C)` **antes** de gravar a curtida do
    recrutador atual → grava a `recruiter_like` (idempotente) → se E ainda não curtia C (primeira de E), grava
    `enterprise_like` e cria o match com cada vaga **`OPEN`** de E que C curtiu.

  **Consistência com T12:** o `CandidateLikeService` também deixa de ignorar o retorno do `lockEnterprise`: se E não
  existe, 404 "Vaga com ID `<id>` não existe" (a vaga cai junto em cascata; a mensagem não revela E).

  **Nota (concorrência):** a trava de E não protege o status das vagas (`VacancyDAO#update` não trava E). Por isso o
  `likedVacanciesOfEnterprise` do `MatchDAO` ganha `FOR SHARE OF v`, implementado nesta tarefa: as vagas lidas não
  mudam de status nem somem até o fim da transação. Ordem das travas: sempre enterprise antes de vacancy. Nenhum
  fluxo pode travar vacancy e depois enterprise. O Javadoc do `MatchRepository` registra que os métodos são chamados
  dentro da transação, com E já travada.
- **Arquivos envolvidos:** criar `main/service/RecruiterLikeService.groovy`, `test/service/RecruiterLikeServiceSpec.groovy`;
  alterar `main/dao/MatchDAO.groovy` (`FOR SHARE OF v`), `main/repository/MatchRepository.groovy` (Javadoc),
  `main/service/CandidateLikeService.groovy` e `test/service/CandidateLikeServiceSpec.groovy` (404 quando E não
  existe no `lockEnterprise`)
- **Critérios de aceite cobertos:** CA27 (B1), CA28 (B2), CA29 (B3), CA31 (B7, B10), CA32 (B9), CA33 (B13), CA34 (B14),
  CA35 (B16)
- **Como validar:** `./gradlew test --tests '*RecruiterLikeServiceSpec'`, com um caso por B1, B2, B3, B7, B9, B10, B13,
  B14 e B16, mais:
  - primeira curtida de E: `enterpriseLikeExists` antes de `insertRecruiterLike` → cria os matches;
  - `lockEnterprise` devolve `false` → 404, nada salvo (também no `CandidateLikeServiceSpec`, com a mensagem da vaga);
  - recrutador inexistente → 404 "Recrutador com ID `<id>` não existe";
  - B9: CPF com e sem máscara, e `0 *` em todo `insert`;
  - B14: `insertMatch` só com V2. O filtro `OPEN` é do serviço, com o `likedVacanciesOfEnterprise` mockado:
    - o mock devolve uma vaga `OPEN` → o match é criado;
    - o mock devolve só vagas que não estão `OPEN` → nenhum match é criado;
    - o mock devolve vagas `OPEN` e não `OPEN` misturadas → o match é criado só com a `OPEN`;
  - B16: o segundo `insertMatch` lança exceção. O teste monta o serviço com um `DatabaseConnection` de verdade sobre
    uma `Connection` `Mock()` e confere `0 * connection.commit()` e `1 * connection.rollback()`. Continua sendo teste
    com mock: o rollback **no PostgreSQL** não é testado, porque o projeto não tem testes de integração com o banco
    (AGENTS.md 12; limitação registrada na tabela "Critérios de aceite × tarefas").
- **Anonimato:** a mensagem de B9 não traz o CPF.
- **Depende de:** T12
- **Commit sugerido:** `feat(recrutador): add recruiter like service keeping enterprise_like invariant`
- [x] Concluída

### T14 — Descurtida do recrutador
- **Camada:** back-end
- **O que será feito:** `RecruiterLikeService.unlike(recruiterId, candidateId)`: trava E → apaga a `recruiter_like` →
  se nenhum outro recrutador de E curte C, apaga o `enterprise_like(E, C)` e os matches de C com todas as vagas de E.
  A lógica de "saiu o último" fica num método reaproveitado em T15.
- **Arquivos envolvidos:** alterar `main/service/RecruiterLikeService.groovy`, `test/service/RecruiterLikeServiceSpec.groovy`
  e `docs/specs/recrutador/spec.md` (casos de borda B18 e B19, aprovados durante a tarefa)
- **Critérios de aceite cobertos:** CA30 (B4, B5), CA31 (B8, B18, B19)
- **Como validar:** `./gradlew test --tests '*RecruiterLikeServiceSpec'`:
  - B4: `0 * deleteEnterpriseLike`;
  - B5: `1 * deleteEnterpriseLike` e `1 * deleteMatchesOfCandidateWithEnterprise`;
  - B8: sem erro e sem `delete`;
  - B18: recrutador inexistente → 404 "Recrutador com ID `<id>` não existe", nada apagado;
  - B19: candidato inexistente → sem erro e sem `delete` (igual a B8);
  - `lockEnterprise` devolve `false` → 404 "Recrutador com ID `<id>` não existe", nada apagado (igual a T13);
  - falha ao apagar os matches → `0 * connection.commit()` e `1 * connection.rollback()`.
- **Anonimato:** não se aplica.
- **Depende de:** T13
- **Commit sugerido:** `feat(recrutador): remove enterprise like and matches when last recruiter unlikes`
- [x] Concluída

### T15 — Remoção do recrutador pela empresa
- **Camada:** back-end
- **O que será feito:** `RecruiterService.removeByEnterprise(enterpriseId, recruiterId)`, numa transação: busca com
  `findByIdAndEnterprise` (404 "Recrutador com ID `<id>` não existe" quando é de outra empresa ou não existe) → trava E → para
  cada C curtido por ele, apaga a `recruiter_like` e aplica a regra de "saiu o último" (T14) → apaga `recruiter` e
  `person`.
- **Arquivos envolvidos:** alterar `main/service/RecruiterService.groovy`, `test/service/RecruiterServiceSpec.groovy`
- **Critérios de aceite cobertos:** CA14, CA15 (B15), CA16
- **Como validar:** `./gradlew test --tests '*RecruiterServiceSpec'`:
  - outra empresa → 404 e `0 * delete`;
  - R1 único a curtir C → apaga o `enterprise_like` e os matches de C;
  - R1 e R2 curtem C → `0 * deleteEnterpriseLike`;
  - R1 único a curtir C e também curtindo D, que R2 curte → só os de C somem.
- **Anonimato:** o 404 não distingue "não existe" de "é de outra empresa".
- **Depende de:** T14
- **Commit sugerido:** `feat(recrutador): remove recruiter and recompute enterprise likes`
- [x] Concluída

### T16 — DTOs e mapper do recrutador
- **Camada:** back-end
- **O que será feito:** `RecruiterRequest` (nome, e-mail, senha, descrição, CPF, nascimento, área) e
  `RecruiterResponse` (id, nome, e-mail, área, descrição). `RecruiterMapper` com `toEntity` e `toResponse`, campo a
  campo, tratando `null`.
- **Arquivos envolvidos:** criar `main/dto/request/RecruiterRequest.groovy`, `main/dto/response/RecruiterResponse.groovy`,
  `main/mapper/RecruiterMapper.groovy`, `test/mapper/RecruiterMapperSpec.groovy`
- **Critérios de aceite cobertos:** CA44, CA46 (back)
- **Como validar:** `./gradlew test --tests '*RecruiterMapperSpec'`: os componentes do record são exatamente
  `[id, name, email, area, description]`, sem `cpf`, `birthDate`, `age` nem `password`.
- **Anonimato:** a verificação é o próprio teste de **ausência** de campos.
- **Depende de:** T06
- **Commit sugerido:** `feat(recrutador): add recruiter DTOs and mapper without sensitive fields`
- [x] Concluída

### T17 — DTOs e mapper de revelação
- **Camada:** back-end
- **O que será feito:**
  - `MatchedVacancyResponse` (id, título, status);
  - `CandidateMatchResponse`: dados anônimos + `name`, `email` e `matchedVacancies`, preenchidos só com match;
  - `VacancyMatchResponse`: dados anônimos da vaga + `status` + `enterpriseName` e `enterpriseEmail`, preenchidos só com
    match, **sem** `enterpriseId`.

  `MatchMapper` monta tudo campo a campo.
- **Arquivos envolvidos:** criar `main/dto/response/MatchedVacancyResponse.groovy`,
  `main/dto/response/CandidateMatchResponse.groovy`, `main/dto/response/VacancyMatchResponse.groovy`,
  `main/mapper/MatchMapper.groovy`, `test/mapper/MatchMapperSpec.groovy`
- **Critérios de aceite cobertos:** CA40, CA41, CA42, CA52 (back)
- **Como validar:** `./gradlew test --tests '*MatchMapperSpec'`:
  - sem match, `name`, `email`, `matchedVacancies`, `enterpriseName` e `enterpriseEmail` são `null` ou vazios;
  - com ou sem match, os records não têm componentes `cpf`, `birthDate`, `age`, `address`, `password`, `cnpj` nem
    `enterpriseId`;
  - com match em V1, só V1 aparece em `matchedVacancies`.
- **Anonimato:** testes de **ausência** antes e depois do match.
- **Depende de:** T06
- **Commit sugerido:** `feat(recrutador): add match reveal DTOs and mapper`
- [x] Concluída

### T18 — Controller do recrutador e rota `POST /api/recruiters`
- **Camada:** back-end
- **O que será feito:** `RecruiterController` com `create(enterpriseId, request)`, `updateOwnProfile`,
  `removeByEnterprise` e `listByEnterprise`, devolvendo só `RecruiterResponse`. `RecruiterServlet#doPost`: lê o JSON,
  exige `enterpriseId` inteiro, chama `create` e responde 201 com `Location` e `RecruiterResponse`. Os erros saem pelo
  `JsonResponse.sendError` (400/404/409). Registro em `Controllers`, `Application#createControllers` e `ApiMain`.
  Registrar também `LikeDAO` e `MatchDAO` no `Application.groovy`.
  `RecruiterService#listByEnterprise(enterpriseId)`: 404 "Empresa com ID `<id>` não existe" se a empresa não existir;
  senão, `recruiterRepository.findAllByEnterprise(enterpriseId)`.
- **Arquivos envolvidos:** criar `main/controller/RecruiterController.groovy`,
  `test/controller/RecruiterControllerSpec.groovy`, `main/api/RecruiterServlet.groovy`,
  `test/api/RecruiterServletSpec.groovy`; alterar `main/controller/Controllers.groovy`, `main/Application.groovy`,
  `main/ApiMain.groovy`; alterar `main/service/RecruiterService.groovy`, `test/service/RecruiterServiceSpec.groovy`
  (método `listByEnterprise`)
- **Critérios de aceite cobertos:** CA06, CA07, CA08, CA13, CA14, CA17, CA44, CA46
- **Como validar:**
  - `./gradlew test --tests '*RecruiterControllerSpec' --tests '*RecruiterServletSpec'`. No servlet, com `Stub()` de
    request e response: 201 + `Location`; o corpo JSON não contém as chaves `cpf`, `birthDate`, `age` nem `password`;
    `enterpriseId` ausente → 400;
  - `--tests '*RecruiterServiceSpec'`: a listagem devolve só os recrutadores da empresa; empresa inexistente → 404;
  - manual: `./gradlew runApi` + `curl -X POST localhost:8080/api/recruiters ...` contra o banco de T04 (201, depois o
    mesmo e-mail em maiúsculas → 409, e `enterpriseId` 9999 → 404).
- **Anonimato:** o controller devolve `RecruiterResponse`, nunca `Recruiter`; o teste do servlet verifica a
  **ausência** dos campos no JSON serializado. Nenhum log novo.
- **Depende de:** T09, T15, T16
- **Commit sugerido:** `feat(recrutador): add recruiter controller and POST /api/recruiters endpoint`
- [x] Concluída

### T19 — Curtidas e visão de candidatos para recrutador e empresa
- **Camada:** back-end
- **O que será feito:**
  - `LikeController` com `candidateLikes`/`candidateUnlikes` (delegam ao `CandidateLikeService`) e
    `recruiterLikes`/`recruiterUnlikes` (delegam ao `RecruiterLikeService`), mais `listCandidatesForRecruiter(recruiterId)`
    e `listCandidatesForEnterprise(enterpriseId)` → `List<CandidateMatchResponse>`, que delegam ao novo
    `MatchQueryService`. O controller monta a resposta com `MatchMapper.toCandidateMatchResponse(CandidateMatches)`.
  - **As duas listas são de revelação:** trazem só os candidatos com ao menos um `vacancy_match` com vaga de E, já
    revelados. Candidato sem match com E fica fora delas e continua só em `CandidateController.findAll`
    (`CandidateAnonymousResponse`). Empresa sem matches → lista vazia.
  - `MatchQueryService` (AGENTS.md › 9.1: controller → service → repository) recebe `ConnectionManager`,
    `CandidateRepository`, `EnterpriseRepository`, `RecruiterRepository` e `MatchRepository`:
    - `candidatesWithMatchesForEnterprise(enterpriseId)` → 404 se a empresa não existe;
    - `candidatesWithMatchesForRecruiter(recruiterId)` → 404 se o recrutador não existe (resolvido pelo
      `RecruiterRepository`, sem `findById` público no `RecruiterService`), depois usa a empresa dele;
    - devolve `List<CandidateMatches>`, candidatos por `id` crescente e as vagas de cada um por `id` crescente;
    - leitura com `withConnection`, sem transação e sem trava;
    - sem consulta dentro de laço no código da T19: as vagas com match saem numa consulta e os candidatos em outra.
      O `loadRelations` por candidato do `PersonDAO#findWhere` continua (limitação conhecida, ver T40).
    - A T20 (`listLikedVacancies`) estende este mesmo service.
  - `main/model/CandidateMatches.groovy`: `record CandidateMatches(Candidate candidate, List<Vacancy> vacancies)`,
    imutável (cópia imutável da lista de vagas).
  - Nova consulta no `MatchRepository`: `matchedVacanciesByEnterprise(Connection, enterpriseId)` → vagas com match de E,
    agrupadas por `candidate_id` (id, título e status), via JOIN de `vacancy_match` com `vacancy`, sem colunas de
    `person`, com `ORDER BY vm.candidate_id, v.id`.
  - Novo método `findAllByIds(Collection<Integer>)` no `CandidateRepository`/`CandidateDAO`: `WHERE candidate.id IN
    (?, …)`, um `?` por id, ordem por `candidate.id`; coleção vazia → `[]` sem ir ao banco. **O `CandidateDAO` foi
    alterado nesta tarefa com autorização explícita da desenvolvedora, só por acréscimo**, sem mudar o que já existe.
    A consulta `IN (...)` **não tem teste com banco** (AGENTS.md 12); o comportamento é coberto no
    `MatchQueryServiceSpec` com o repositório mockado, inclusive ids vazios.
  - `MatchMapper` ganha a sobrecarga `toCandidateMatchResponse(CandidateMatches)`, que repassa ao método atual
    (o `MatchMapperSpec` não muda). Service e controller usam só a versão nova.
  - Registro em `Controllers` e `Application`.
- **Arquivos envolvidos:**
  - criar: `main/controller/LikeController.groovy`, `test/controller/LikeControllerSpec.groovy`,
    `main/service/MatchQueryService.groovy`, `test/service/MatchQueryServiceSpec.groovy`,
    `main/model/CandidateMatches.groovy`;
  - alterar: `main/repository/MatchRepository.groovy`, `main/dao/MatchDAO.groovy`,
    `main/repository/CandidateRepository.groovy`, `main/dao/CandidateDAO.groovy` (só acréscimo do `findAllByIds`),
    `main/mapper/MatchMapper.groovy` (sobrecarga), `main/controller/Controllers.groovy`, `main/Application.groovy`.
- **Critérios de aceite cobertos:** CA37, CA38, CA40, CA41
  - CA40: ausência do candidato sem match na lista de revelação (inclusive com curtida unilateral), mais o
    `CandidateAnonymousResponse` que já existe;
  - CA37 e CA41: lista de revelação com só as vagas com match;
  - CA38: mesma lista, pela rota da empresa.
- **Como validar:** `./gradlew test --tests '*LikeControllerSpec' --tests '*MatchQueryServiceSpec'`:
  - candidato sem match com E não aparece na lista de revelação (continua só em `CandidateController.findAll`,
    anônimo), inclusive com curtida unilateral (evidência do CA40);
  - com match só em V1 → só V1;
  - match com E1 não revela para E2;
  - curtida sem match não aparece;
  - ordenação por id, empresa sem matches → lista vazia, empresa ou recrutador inexistente → 404, recrutador resolve
    para a própria empresa, ids vazios não chamam `findAllByIds`.
- **Anonimato:** a consulta de vagas com match não seleciona colunas de `person`; nome e e-mail vêm do `Candidate`
  carregado só para quem tem match.
- **Depende de:** T14, T17
- **Commit sugerido:** `feat(recrutador): add like controller and candidate reveal for recruiters and enterprises`
- [x] Concluída

### T20 — "Minhas curtidas" do candidato no back-end
- **Camada:** back-end
- **O que será feito:** `LikeController.listLikedVacancies(candidateId)` → `List<VacancyMatchResponse>` com as vagas
  curtidas em qualquer status, e nome e e-mail da empresa só nas com match.
  - Nova consulta `LikeRepository.likedVacanciesOf(connection, candidateId)` → `List<LikedVacancy>`, em ordem crescente
    de id da vaga (`ORDER BY` no SQL). O SQL faz `candidate_like JOIN vacancy JOIN address LEFT JOIN vacancy_match` e
    lê só dados da vaga: `vacancy` (id, título, descrição, status, `enterprise_id`), cidade/UF de `address` pela
    `v.address_id` (endereço da vaga; `address_id` é `NOT NULL`, por isso `JOIN`), as competências da vaga numa
    subconsulta `ARRAY(...)` ordenada por nome, e a flag de match. Nenhuma coluna de `person`. Uma consulta só.
  - O `java.sql.Array` é convertido com `getArray()` para `List<String>` (o tipo que o `VacancyMatchResponse` usa);
    sem competências → lista vazia, nunca `null`. O `LikedVacancy` guarda uma cópia imutável.
  - `LikedVacancy` é um `record` em `main/model/` com id, título, descrição, cidade, UF, status, competências,
    `enterpriseId` e `matched`. Não carrega nome, e-mail nem CNPJ.
  - `LikedVacancyView(LikedVacancy vacancy, String enterpriseName, String enterpriseEmail)` é um `record` em
    `main/model/`; nome e e-mail são `null` sem match. É o retorno do service: a entidade `Enterprise` (com CNPJ) não
    sai do service.
  - `MatchQueryService.listLikedVacancies(candidateId)` → `List<LikedVacancyView>`: candidato inexistente → 404; busca
    as vagas curtidas com uma consulta; para as com `matched = true`, busca nome/e-mail da empresa pelo
    `EnterpriseRepository`, uma vez por empresa distinta (não por vaga). Vagas sem match nunca disparam leitura de dados
    da empresa. A ordem vem do `ORDER BY` e é mantida na lista. Não usa `VacancyRepository`/`VacancyDAO`.
  - O `MatchMapper` (sobrecarga `toVacancyMatchResponse(LikedVacancyView)`, como na T19) preenche todos os campos de
    vaga do DTO e copia nome/e-mail da empresa só quando `matched` é verdadeiro. O DTO não tem `enterpriseId` nem
    `cnpj`. Nenhum campo de vaga fica nulo por causa do SQL.
  - **Sem teste com banco:** o SQL do `likedVacanciesOf` (incluindo a subconsulta `ARRAY` e a conversão do
    `java.sql.Array`) não tem teste automatizado com PostgreSQL (AGENTS.md, seção 12). O mapeamento é coberto no
    `MatchQueryServiceSpec` com o repositório mockado, incluindo vaga sem competências.
  - O construtor do `MatchQueryService` passa a receber só o `LikeRepository` a mais; em
    `Application.groovy#createControllers`, a única mudança é passar o `likeDAO`.
- **Arquivos envolvidos:** alterar `main/controller/LikeController.groovy`, `test/controller/LikeControllerSpec.groovy`,
  `main/repository/LikeRepository.groovy`, `main/dao/LikeDAO.groovy`, `main/service/MatchQueryService.groovy`,
  `test/service/MatchQueryServiceSpec.groovy`, `main/mapper/MatchMapper.groovy`, `main/Application.groovy` (só passar o
  `likeDAO`); criar `main/model/LikedVacancy.groovy` e `main/model/LikedVacancyView.groovy`
- **Critérios de aceite cobertos:** CA39, CA42, CA52 (back)
- **Como validar:** `./gradlew test --tests '*LikeControllerSpec' --tests '*MatchQueryServiceSpec'`: V1 com match tem
  `enterpriseName`/`enterpriseEmail`; V2 sem match (`PAUSED`) aparece com status e **sem** esses campos; nenhum item tem
  `enterpriseId` ou `cnpj`; candidato inexistente → 404; o repositório de empresa não é chamado para vagas sem match;
  duas vagas com match da mesma empresa geram uma só busca da empresa; descrição, cidade, UF e competências chegam ao
  DTO; vaga sem competências tem lista vazia.
- **Anonimato:** o SQL não lê colunas de `person`; `LikedVacancy` não carrega dados da empresa; nome e e-mail só são
  lidos e copiados nas vagas com match; a entidade `Enterprise` não sai do service; CNPJ e `enterpriseId` nunca vão
  para o DTO.
- **Depende de:** T19
- **Commit sugerido:** `feat(recrutador): list candidate liked vacancies with reveal after match`
- [x] Concluída

### T21 — Vagas da empresa para o recrutador (somente leitura)
- **Camada:** back-end
- **O que será feito:** `RecruiterService#enterpriseIdOf(recruiterId)`: 404 "Recrutador com ID `<id>` não existe" se o
  recrutador não existir; senão, devolve `recruiter.enterpriseId`. `RecruiterController.listEnterpriseVacancies(recruiterId)`
  → `List<VacancyAnonymousResponse>` com todas as vagas da empresa do recrutador, em qualquer status, via
  `VacancyService#findByEnterprise` (que usa o `VacancyRepository.findByEnterprise` já existente) e
  `VacancyMapper.toAnonymousResponses`. O controller passa a receber também o `VacancyService`, obrigatório no
  construtor. Nenhum método de criar, editar ou excluir vaga recebe recrutador.
- **Arquivos envolvidos:** alterar `main/controller/RecruiterController.groovy`,
  `test/controller/RecruiterControllerSpec.groovy`, `main/Application.groovy`, `main/service/RecruiterService.groovy`,
  `test/service/RecruiterServiceSpec.groovy` (método `enterpriseIdOf`), `test/api/RecruiterServletSpec.groovy` (só
  passa um `VacancyService` com `Stub(VacancyRepository)` na criação do controller, sem mudar nenhum caso de teste)
- **Critérios de aceite cobertos:** CA19, CA20 (back), CA21
- **Como validar:** `./gradlew test --tests '*RecruiterControllerSpec'`: vagas `OPEN`, `PAUSED` e `CLOSED` de E1
  aparecem; nenhuma de E2.
- **Anonimato:** a resposta é `VacancyAnonymousResponse`, sem dados da empresa.
- **Depende de:** T18
- **Commit sugerido:** `feat(recrutador): list enterprise vacancies read-only for recruiter`
- [x] Concluída

---

## Fase 3 — Front-end

### T22 — Tipos e rotas do recrutador
- **Camada:** front-end
- **O que será feito:**
  - `types.ts`: `RECRUITER` em `PersonType`; `Recruiter` (sem endereço e sem competências) em `Account`;
    `RECRUITMENT_AREAS`; tipos `CandidateLike`, `RecruiterLike`, `EnterpriseLike` e `Match`; tipos para `pages/`
    `RecruiterSummary`, `CandidateCard`, `VacancyCard` e `LikedVacancyCard`;
  - `navigation.ts`: rotas novas e `HOME_BY_TYPE.RECRUITER`;
  - `ui/navbar.ts`: links do recrutador, sem "Excluir conta" para ele;
  - ajustes só de tipo, para o build passar: o `register` de `auth.ts` aceita só `Candidate | Enterprise`, porque quem
    cadastra o recrutador é a empresa (T27); `pages/register.ts` usa `SelfRegisteredType` (`'CANDIDATE' | 'ENTERPRISE'`,
    em `types.ts`), sem mudar o comportamento.

  Enquanto as telas não existem, as rotas novas caem na home.
- **Arquivos envolvidos:** alterar `front/types.ts`, `front/navigation.ts`, `front/ui/navbar.ts`,
  `front/services/auth.ts`, `front/pages/register.ts`
- **Critérios de aceite cobertos:** base de CA48 e CA51
- **Como validar:** `npm run build`. Manual: com conta de candidato e de empresa, a navbar continua igual.
- **Anonimato:** `CandidateCard`, `VacancyCard` e `LikedVacancyCard` não têm `cpf`, `birthDate`, `password`,
  `address`, `cnpj` nem `enterpriseId`. `RecruiterSummary` não tem `cpf`, `birthDate` nem `password`.
- **Depende de:** T21
- **Commit sugerido:** `feat(recrutador): add recruiter types and routes to frontend`
- [x] Concluída

### T23 — Armazenamento e seed do front
- **Camada:** front-end
- **O que será feito:** coleções `recruiters`, `candidateLikes`, `recruiterLikes`, `enterpriseLikes` e `matches` no
  `storage.ts`, com `DATA_FORMAT_VERSION` = `5`. `seed.ts` com os 5 recrutadores de T04 (`recruiter-1`…`recruiter-5`) e
  as curtidas e matches do `seed.sql` + backfill:
  - 5 `candidateLikes`;
  - 4 `enterpriseLikes`;
  - 4 `recruiterLikes`;
  - 3 `matches`: Sandubinha × Backend Spring, Lucas × Full Stack e Gabriel × Cientista de Dados.
- **Arquivos envolvidos:** alterar `front/data/storage.ts`, `front/data/seed.ts`
- **Critérios de aceite cobertos:** CA50
- **Como validar:** `npm run build`. Manual: abrir o app com dados antigos no navegador → o `localStorage` é recriado
  com versão `5` e as coleções novas; conferir as contagens no DevTools contra o banco de T04.
- **Anonimato:** dados completos no `localStorage` são limitação aceita (spec 6.6). Nada muda nas telas nesta tarefa.
- **Depende de:** T22
- **Commit sugerido:** `feat(recrutador): add recruiters, likes and matches to frontend storage and seed`
- [x] Concluída

### T24 — Login e guard do recrutador
- **Camada:** front-end
- **O que será feito:** em `auth.ts`, recrutadores entram em `allAccounts` (login por e-mail sem diferenciar
  maiúsculas) e ganham `getLoggedRecruiter`. Em `guards.ts`, `requireLoggedRecruiter`. `deleteLoggedAccount` recusa
  explicitamente a conta de recrutador (lança erro, sem deslogar), porque ele não exclui a própria conta.
- **Arquivos envolvidos:** alterar `front/services/auth.ts`, `front/pages/guards.ts`
- **Critérios de aceite cobertos:** CA51, CA20 (guard de empresa barra recrutador)
- **Como validar:** `npm run build`. Manual: login com `rafael.lopes@techsolutions.com.br` / `123456` cai na home do
  recrutador; abrir `#/my-vacancies` logado como recrutador → redireciona para o login.
- **Anonimato:** não se aplica (sem tela nova).
- **Depende de:** T23
- **Commit sugerido:** `feat(recrutador): support recruiter login and guard in frontend`
- [x] Concluída

### T25 — Exclusão de contas em cascata no front
- **Camada:** front-end
- **O que será feito:** em `auth.ts#deleteLoggedAccount`, espelhando o `ON DELETE CASCADE` do banco:
  - a empresa apaga também os recrutadores dela, as curtidas desses recrutadores, os `enterpriseLikes` dela, as
    curtidas em vagas dela e os matches dessas vagas. Isso inclui os matches que vieram das curtidas dos recrutadores,
    porque todo match de um recrutador é com uma vaga da empresa dele;
  - o candidato apaga também as curtidas que fez, as `recruiterLikes`/`enterpriseLikes` que recebeu e os matches dele;
  - a recusa explícita para a conta de recrutador, feita na T24, continua.
- **Arquivos envolvidos:** alterar `front/services/auth.ts`
- **Critérios de aceite cobertos:** nenhum CA direto (spec 6.1, `auth.ts`); sustenta CA50 (dados coerentes)
- **Como validar:** `npm run build`. Manual: excluir a Pastelsoft → somem Carla, a curtida dela em Sandubinha, o
  `enterpriseLike` e o match de Sandubinha no `localStorage`; excluir Lucas → somem as curtidas e o match dele.
- **Anonimato:** não se aplica (sem tela nova).
- **Depende de:** T24
- **Commit sugerido:** `feat(recrutador): cascade account deletion to recruiters, likes and matches in frontend`
- [x] Concluída

### T26 — Regras de curtida e match no front
- **Camada:** front-end
- **O que será feito:** `services/likes.ts` com `likeVacancy`/`unlikeVacancy` (candidato) e
  `likeCandidate`/`unlikeCandidate` (recrutador), espelhando T12–T14:
  - só vaga `OPEN`;
  - match só com vaga `OPEN` na primeira curtida de E;
  - invariante `enterpriseLikes` ⇔ `recruiterLikes`;
  - autocurtida por CPF bloqueada;
  - idempotência;
  - a descurtida do candidato apaga o match.

  Os erros são devolvidos com as mesmas mensagens do back-end.
- **Arquivos envolvidos:** criar `front/services/likes.ts`
- **Critérios de aceite cobertos:** CA22, CA24, CA25, CA27, CA30, CA32, CA34 (front)
- **Como validar:** `npm run build`. Ainda não há tela que use o serviço; o roteiro manual de B1, B4, B5, B9 e B14 é
  feito pelas telas em T30 e T34.
- **Anonimato:** o serviço não devolve dados de outros recrutadores; só `boolean`/mensagem de erro.
- **Depende de:** T23
- **Commit sugerido:** `feat(recrutador): add like and match rules to frontend services`
- [x] Concluída

### T27 — Serviço de recrutadores no front
- **Camada:** front-end
- **O que será feito:** `services/recruiters.ts`:
  - `createRecruiter(enterprise, data)`, com as mesmas validações de T07 e o e-mail único entre todas as contas. É uma
    função própria: não usa o `register` de `auth.ts`, que só cadastra candidato e empresa e inicia a sessão;
  - `removeRecruiter(enterprise, id)`: só da própria empresa, com a regra de "saiu o último" de T26;
  - `listOwnRecruiters(enterprise)` → `RecruiterSummary[]`, montado por desestruturação;
  - `updateOwnProfile(recruiter, description, password)`, que grava com o `update` novo da `Collection`;
  - `data/storage.ts`: `Collection` ganha `update(id, changes)`, que altera o item no lugar e mantém a ordem da lista
    (nada de `remove` seguido de `add`);
  - **dívida técnica de T26 (resolvida nesta tarefa):** o `removeWhere`, que estava duplicado em `services/auth.ts` e
    `services/likes.ts`, passou a ser exportado por `data/storage.ts` e os dois serviços o importam de lá. O
    `recruiters.ts` não precisou dele: a remoção usa o `unlikeCandidate` de T26 e o `remove` da `Collection`.
- **Arquivos envolvidos:** criar `front/services/recruiters.ts`; alterar `front/data/storage.ts`,
  `front/services/auth.ts` e `front/services/likes.ts` (só a troca do `removeWhere`)
- **Critérios de aceite cobertos:** CA06, CA07, CA10, CA11, CA12, CA15, CA16, CA17, CA46 (front)
- **Como validar:** `npm run build`. O fluxo manual é feito em T29 e T32.
- **Anonimato:** `listOwnRecruiters` devolve só `{ id, name, email, area, description }`.
- **Depende de:** T26
- **Commit sugerido:** `feat(recrutador): add recruiter service to frontend`
- [x] Concluída

### T28 — Serviços de revelação no front
- **Camada:** front-end
- **O que será feito:**
  - `services/candidates.ts`: `listCandidateCardsForRecruiter()` e `listCandidateCardsForEnterprise()` →
    `CandidateCard[]`. A empresa vem do recrutador logado ou da empresa logada, nunca de um parâmetro. Lança erro se
    não houver recrutador ou empresa logada, ou se o recrutador da sessão não existir mais. Os cards são anônimos;
    nome, e-mail e vagas com match só aparecem se houver match com essa empresa. `likedByMe` vem só da curtida do
    próprio recrutador e é sempre `false` para a empresa;
  - `services/vacancies.ts`: `listOpenVacancyCards(candidate)` (com `likedByMe`), `listLikedVacancyCards(candidate)`
    (qualquer status, empresa só com match) e `listEnterpriseVacanciesForRecruiter(recruiter)`.
- **Arquivos envolvidos:** alterar `front/services/candidates.ts`, `front/services/vacancies.ts`
- **Critérios de aceite cobertos:** CA19, CA21, CA37, CA38, CA39, CA40, CA42, CA52 (front)
- **Como validar:** `npm run build`. O fluxo manual é feito nas tarefas de tela.
- **Anonimato:** tudo montado por desestruturação, campo a campo; nada de `enterpriseId`, CPF, nascimento ou endereço
  nos tipos devolvidos; nenhum dado de recrutador nos tipos do candidato.
- **Depende de:** T26
- **Commit sugerido:** `feat(recrutador): add reveal-after-match services to frontend`
- [x] Concluída

### T29 — Tela "Recrutadores" da empresa
- **Camada:** front-end
- **O que será feito:** página nova para a empresa listar, cadastrar (formulário com os campos da spec e a área num
  `select`) e remover os próprios recrutadores, com confirmação. Rota e link na navbar da empresa.
- **Arquivos envolvidos:** criar `front/pages/recruiters.ts`; alterar `front/router.ts`, `front/navigation.ts`,
  `front/ui/navbar.ts`
- **Critérios de aceite cobertos:** CA06, CA07, CA14, CA15, CA16, CA46
- **Como validar:** `npm run build`. Manual como TechSolutions:
  - vê só Rafael e Juliana;
  - cadastra um recrutador; e-mail repetido com maiúsculas e menor de 18 anos dão erro;
  - como Juliana, curtir Lucas (no seed, só Rafael curte Lucas);
  - remove Rafael com Juliana ainda curtindo → o match de Lucas continua;
  - inspecionar o DOM: nenhum CPF ou nascimento em texto, `data-*` ou `aria-*`.
- **Anonimato:** a tabela mostra só nome, e-mail, área e descrição; o `id` fica só no closure do botão de remover.
- **Depende de:** T27
- **Commit sugerido:** `feat(recrutador): add enterprise recruiters page`
- [x] Concluída

### T30 — Tela de candidatos do recrutador
- **Camada:** front-end
- **O que será feito:** página do recrutador com os candidatos: "Candidato #N" anônimo, botão curtir/descurtir (só da
  própria curtida) e, com match, nome, e-mail e a lista das vagas de E com match. Mensagem de erro da autocurtida.
  Os dados vêm de `listCandidateCardsForRecruiter()`, que lê o recrutador da sessão (T28).
- **Erros 404:** a tela troca por uma mensagem genérica, sem o id interno (ver "Regras para todas as tarefas").
- **Junção das listas:** feita no serviço (T28). Cada `CandidateCard` traz `reveal` quando há match; a tela só escolhe
  entre anônimo e revelado.
- **Nome da empresa:** `getOwnEnterpriseName(recruiter)` em `services/recruiters.ts` devolve só o nome da empresa do
  recrutador (spec 4.4), sem alterar os tipos dos cards.
- **Arquivos envolvidos:** criar `front/pages/recruiterCandidates.ts`; alterar `front/router.ts`,
  `front/services/recruiters.ts`
- **Critérios de aceite cobertos:** CA27–CA34 (front), CA37, CA40, CA45 e "A tela do recrutador exibe o nome da empresa
  (spec 4.4)"
- **Testes:** o front não tem testes automatizados (AGENTS.md 9.5). A garantia "sem match, nada de nome ou e-mail no
  retorno" é testada no back-end (`MatchMapperSpec`, `MatchQueryServiceSpec`, `LikeControllerSpec`); no front, ela e o
  nome da empresa são conferidos pelo roteiro manual abaixo.
- **Como validar:** `npm run build`. Manual como Juliana (TechSolutions):
  - o cabeçalho mostra "Recrutando para TechSolutions Brasil";
  - Lucas aparece revelado, com só "Full Stack" nas vagas com match;
  - Sandubinha (curtiu a Full Stack, sem match) aparece anônimo, sem indicar a curtida;
  - curtir Sandubinha → revela;
  - curtir Lucas (no seed, só Rafael curte Lucas);
  - descurtir Lucas com Rafael ainda curtindo → continua revelado.

  Inspecionar o hover card e os atributos. No console do DevTools, conferir o retorno do serviço: card sem match não
  tem a chave `reveal` nem `name`, `email`, `cpf`, `birthDate`, `address` ou `password`. Em `pages/recruiterCandidates.ts`,
  conferir que a escolha entre anônimo e revelado depende só de `candidate.reveal`.
- **Anonimato:** nada de quem mais curtiu, nem de outros recrutadores; antes do match, nenhum nome ou e-mail no DOM.
- **Depende de:** T28
- **Commit sugerido:** `feat(recrutador): add recruiter candidates page with like and reveal`
- [x] Concluída

### T31 — Tela de vagas do recrutador (somente leitura)
- **Camada:** front-end
- **O que será feito:** página do recrutador com todas as vagas da própria empresa e o status de cada uma, sem botões de
  criar, editar ou excluir, e com o nome da empresa como contexto.
- **Arquivos envolvidos:** criar `front/pages/recruiterVacancies.ts`; alterar `front/router.ts`
- **Critérios de aceite cobertos:** CA19, CA20, CA21
- **Como validar:** `npm run build`. Manual como Carla (Pastelsoft): vê as 3 vagas da Pastelsoft, incluindo a `CLOSED`,
  e nenhuma de outra empresa; não há ação de vaga em nenhuma tela do recrutador.
- **Anonimato:** sem CNPJ e sem vagas de outras empresas.
- **Depende de:** T28
- **Commit sugerido:** `feat(recrutador): add read-only enterprise vacancies page for recruiter`
- [x] Concluída

### T32 — Perfil do recrutador
- **Camada:** front-end
- **O que será feito:** página "Meu perfil" do recrutador para editar a descrição e a senha (vazia mantém a atual; curta
  dá erro). Sem opção de excluir conta.
- **Arquivos envolvidos:** criar `front/pages/recruiterProfile.ts`; alterar `front/router.ts`, `front/ui/navbar.ts`
- **Critérios de aceite cobertos:** CA17
- **Como validar:** `npm run build`. Manual: mudar a descrição, sair e entrar de novo → mantida; senha com 3
  caracteres → erro; senha vazia → login com a anterior funciona.
- **Anonimato:** a tela mostra só os dados do próprio recrutador, sem CPF nem nascimento.
- **Depende de:** T27
- **Commit sugerido:** `feat(recrutador): add recruiter profile page`
- [x] Concluída

### T33 — Candidatos da empresa com revelação
- **Camada:** front-end
- **O que será feito:** `pages/candidates.ts` passa a usar `CandidateCard`: sem botão de curtir; nome e e-mail só com
  match com a empresa.
- **Junção das listas:** feita no serviço (T28); a tela só escolhe entre anônimo e revelado por `candidate.reveal`.
- **Fora do escopo:** a lista das vagas de E com match não é exibida à empresa, porque o CA38 cobre só nome e e-mail.
  A spec 2.4 ("Empresa E vendo C: a mesma regra do recrutador") a inclui; a decisão 4 (spec 8) a atribui só ao
  recrutador. Pendente de decisão da desenvolvedora.
- **Arquivos envolvidos:** alterar `front/pages/candidates.ts`
- **Critérios de aceite cobertos:** CA36, CA38
- **Como validar:** `npm run build`. Manual como `contato@techsolutions.com.br`: Lucas revelado, os demais anônimos; sem
  botão de curtir; como Juliana, curtir Lucas (no seed, só Rafael curte Lucas); depois que Rafael e Juliana descurtem
  Lucas, ele volta a "Candidato #N".
- **Anonimato:** o gráfico e o hover card continuam sem dados pessoais.
- **Depende de:** T28
- **Commit sugerido:** `feat(recrutador): reveal matched candidates on enterprise candidates page`
- [x] Concluída

### T34 — Vagas do candidato: curtir e "Minhas curtidas"
- **Camada:** front-end
- **O que será feito:** `pages/vacancies.ts`: botão curtir/descurtir nas vagas `OPEN`; seção "Minhas curtidas" com as
  vagas curtidas em qualquer status, com o status, o botão de descurtir e, só com match, nome e e-mail da empresa.
- **Erros 404:** a tela troca por uma mensagem genérica, sem o id interno (ver "Regras para todas as tarefas").
- **Arquivos envolvidos:** alterar `front/pages/vacancies.ts`
- **Critérios de aceite cobertos:** CA22, CA23, CA24, CA25, CA26, CA39, CA42, CA43, CA52
- **Como validar:** `npm run build`. Manual como Sandubinha:
  - "Backend Spring" mostra Pastelsoft;
  - "Full Stack" aparece sem empresa;
  - "Vagas abertas" aparece acima de "Minhas curtidas";
  - pausar a "Full Stack" como TechSolutions → ela some da lista principal e continua em "Minhas curtidas", onde o
    status aparece como *Pausada* (`PAUSED` no backend), sem empresa, e é possível descurtir;
  - candidato sem curtidas → mensagem de lista vazia;
  - inspecionar o DOM: nenhum dado de recrutador nem indicação de ter sido curtido.
- **Anonimato:** a vaga sem match não tem nome, e-mail, CNPJ nem `enterpriseId` na tela ou nos atributos.
- **Depende de:** T28
- **Commit sugerido:** `feat(recrutador): add vacancy likes and "minhas curtidas" section for candidates`
- [x] Concluída

### T35 — Home e cadastro público
- **Camada:** front-end
- **O que será feito:** tira o "em breve" do passo "Dê match" da home. Confere que o cadastro público continua só com
  candidato e empresa, sem mudar o comportamento de `register.ts` (o ajuste de tipo foi feito na T22).
- **Arquivos envolvidos:** alterar `front/pages/home.ts`
- **Critérios de aceite cobertos:** CA18, CA51
- **Como validar:** `npm run build`. Manual: a home sem o selo; `#/register` só com as duas opções; login de cada um dos
  5 recrutadores de exemplo funciona.
- **Anonimato:** não se aplica.
- **Depende de:** T34
- **Commit sugerido:** `feat(recrutador): enable match step on home page`
- [x] Concluída

---

## Fase 4 — Validação final

### T36 — AGENTS.md e docs da API
- **Camada:** todas (documentação)
- **O que será feito:** atualiza o AGENTS.md:
  - seção 5.1, item 1: ressalva de que o `schema.sql` não deve ser reaplicado sozinho sobre banco migrado (usar o reset
    oficial da 4.2), com o texto: "1. `Database/schema.sql`: base. Começa com `DROP TABLE ... CASCADE`, então
    **reaplicá-lo apaga todos os dados**. Sobre um banco que já recebeu migrations, não o reaplique sozinho: use o reset
    oficial da seção 4.2.";
  - seção 6: recrutador, área e curtidas no domínio;
  - seção 7: estado atual de curtida e match;
  - seção 8: tabelas de anonimato do recrutador;
  - seção 11: a skill e o CHANGELOG já existem;
  - seção 12: limitações novas da spec 6.6, e também: as rotas e controllers que recebem `recruiterId`/`candidateId`
    (T19, T20, T21) confiam no id informado; não há autenticação na API.

  Em `docs/api.md`, documenta o `POST /api/recruiters`.
- **Arquivos envolvidos:** alterar `AGENTS.md`, `docs/api.md`
- **Critérios de aceite cobertos:** checklist da spec (seção 9)
- **Como validar:** reler as seções contra a spec; o exemplo de `curl` do `docs/api.md` funciona contra o banco de T04.
- **Anonimato:** as tabelas novas do AGENTS.md batem com a spec, seção 4.
- **Depende de:** T35
- **Commit sugerido:** `docs(recrutador): update AGENTS.md and API docs for recruiter feature`
- [ ] Concluída

### T37 — Conferência dos critérios de aceite
- **Camada:** todas
- **O que será feito:** tabela "Critério | Atendido? | Teste que cobre | Observação" para CA01–CA52 (skill, Etapa 6),
  registrada neste plano. Roda `reset+migrar` duas vezes, `./gradlew build`, `npm run build` e
  `git diff main -- Database/schema.sql Database/seed.sql Database/queries.sql`.
- **Arquivos envolvidos:** alterar `docs/specs/recrutador/plano.md`
- **Critérios de aceite cobertos:** CA01–CA52, em especial CA47, CA48, CA49 e CA50
- **Como validar:** a tabela não tem "não"; os itens "parcial" estão justificados. O diff dos scripts base é vazio.
- **Anonimato:** ver T39.
- **Depende de:** T36
- **Commit sugerido:** `docs(recrutador): record acceptance criteria verification`
- [ ] Concluída

### T38 — Regressão dos fluxos antigos
- **Camada:** todas
- **O que será feito:** com o banco resetado e o `localStorage` limpo, repassa:
  - cadastro de candidato e de empresa (front e `POST /api/candidates`, `/api/enterprises`);
  - publicação, pausa e exclusão de vaga (front e `POST /api/vacancies`);
  - login das contas antigas e exclusão de conta;
  - curtidas e matches do seed (3 matches);
  - menu do console (`./gradlew run`) sem mudanças.

  O resultado é registrado no plano.
- **Arquivos envolvidos:** alterar `docs/specs/recrutador/plano.md`
- **Critérios de aceite cobertos:** CA47, CA48 (regressão)
- **Como validar:** roteiro acima executado; falhas viram tarefa nova no fim do plano (skill, Etapa 6).
- **Anonimato:** as telas antigas continuam com "Candidato #N" e vagas sem empresa.
- **Depende de:** T37
- **Commit sugerido:** `docs(recrutador): record regression check of existing flows`
- [ ] Concluída

### T39 — Varredura de anonimato
- **Camada:** todas
- **O que será feito:** checklist "Princípio do Anonimato" da skill (Etapa 6), ponto a ponto da spec 4.6:
  - JOINs novos sem `password_hash` nem CNPJ/CPF desnecessários;
  - nenhuma entidade serializada;
  - nenhum `println`/log novo;
  - JSON do `POST /api/recruiters` sem CPF ou nascimento;
  - em cada tela nova ou alterada (como candidato, empresa e recrutador, antes e depois do match), DOM, hover cards e
    `aria-*`/`data-*` conferidos no DevTools;
  - candidato sem nenhum dado de recrutador.

  Deixa aberto o item "DER regenerado (manual)".
- **Arquivos envolvidos:** alterar `docs/specs/recrutador/plano.md`
- **Critérios de aceite cobertos:** CA40, CA41, CA42, CA43, CA44, CA45, CA46, CA52
- **Como validar:** `grep -rn "println\|password_hash\|SELECT \*" Linketinder/src/main` nos arquivos novos; checklist
  todo marcado, menos o DER.
- **Anonimato:** é o objetivo da tarefa.
- **Depende de:** T38
- **Commit sugerido:** `docs(recrutador): record anonymity sweep`
- [ ] Concluída

### T40 — N+1 de competências no `PersonDAO`
- **Camada:** back-end (fora do escopo da feature Recrutador; não bloqueia as outras tarefas)
- **O que será feito:** eliminar a consulta de competências por pessoa em `PersonDAO#findWhere`
  (`Linketinder/src/main/groovy/com/mariajuliasales/dao/PersonDAO.groovy:104-111`, `loadRelations` dentro do laço),
  carregando as competências de todas as pessoas da listagem numa única consulta. Registrada na T19, onde o
  `findAllByIds` herda esse N+1.
- **Arquivos envolvidos:** a definir na própria tarefa (`PersonDAO`, `CompetenceLinkDAO` e specs). Exige autorização,
  porque o plano protege o comportamento do `PersonDAO`.
- **Critérios de aceite cobertos:** nenhum (melhoria de desempenho)
- **Como validar:** `./gradlew test`; listagens de candidatos e empresas com as mesmas competências de antes.
- **Anonimato:** não se aplica (só carrega competências, que já são públicas).
- **Depende de:** T19
- **Commit sugerido:** `perf(dao): load person competences in a single query`
- [ ] Concluída

---

## Critérios de aceite × tarefas

| Critério | Tarefas |
|---|---|
| CA01 | T01, T03, T04, T37 |
| CA02 | T01, T03, T04, T37 |
| CA03 | T03 |
| CA04 | T04 |
| CA05 | T05 |
| CA06 | T08, T09, T18, T27, T29 |
| CA07 | T09, T18, T27, T29 |
| CA08 | T07, T08, T18 |
| CA09 | T09 |
| CA10 | T06, T07, T27 |
| CA11 | T06, T07, T27 |
| CA12 | T07, T27 |
| CA13 | T09, T18 |
| CA14 | T15, T18, T29 |
| CA15 | T15, T27, T29 |
| CA16 | T15, T27, T29 |
| CA17 | T07, T09, T18, T27, T32 |
| CA18 | T35 |
| CA19 | T21, T28, T31 |
| CA20 | T21, T24, T31 |
| CA21 | T21, T28, T31 |
| CA22 | T12, T26, T34 |
| CA23 | T12, T34 |
| CA24 | T12, T26, T34 |
| CA25 | T12, T26, T34 |
| CA26 | T12, T34 |
| CA27 | T13, T26, T30 |
| CA28 | T13, T30 |
| CA29 | T13, T30 |
| CA30 | T14, T26, T30 |
| CA31 | T13, T14, T30 |
| CA32 | T13, T26, T30 |
| CA33 | T13, T30 |
| CA34 | T13, T26, T30 |
| CA35 | T13 (só com mock: `DatabaseConnection` real sobre `Connection` `Mock()`; rollback no PostgreSQL sem teste automático, porque não há testes de integração com o banco) |
| CA36 | T33 |
| CA37 | T19, T28, T30 |
| CA38 | T19, T28, T33 |
| CA39 | T20, T28, T34 |
| CA40 | T17, T19, T28, T30, T39 |
| CA41 | T17, T19, T39 |
| CA42 | T17, T20, T28, T34, T39 |
| CA43 | T34, T39 |
| CA44 | T16, T18, T39 |
| CA45 | T30, T39 |
| CA46 | T16, T18, T27, T29, T39 |
| CA47 | T37, T38 |
| CA48 | T37, T38 |
| CA49 | T37 |
| CA50 | T23, T37 |
| CA51 | T22, T24, T35 |
| CA52 | T17, T20, T28, T34, T39 |

Todos os critérios CA01–CA52 estão cobertos.

---

## Riscos e ordem de mitigação

| # | Risco | Mitigação | Quando |
|---|---|---|---|
| 1 | `ALTER TYPE ... ADD VALUE` dentro de transação quebra a 002 | 001 sozinha, sem `BEGIN/COMMIT`; laço de migrations rodado duas vezes | T01, T03 |
| 2 | Reaplicar só o `schema.sql` sobre banco migrado deixa `recruiter` inconsistente | Reset oficial com `dropdb`/`createdb` documentado e usado em toda validação | T02 |
| 3 | Migration não idempotente (CA02) ou backfill ressuscitando curtidas | `IF NOT EXISTS`, `ON CONFLICT`, `DO $$` com `pg_constraint`; backfill só de órfãos; contagens comparadas | T03, T04 |
| 4 | Lógica de match no SQL, sem teste possível sem banco | DAOs só com métodos pequenos; regra nos services com `Mock()` e `1 *`/`0 *` para B1–B17 | T10–T15 |
| 5 | Invariante `enterprise_like` ⇔ `recruiter_like` quebrada por concorrência | `SELECT ... FOR UPDATE` em `enterprise` como primeira chamada da transação, com ordem conferida em teste | T12–T15 |
| 6 | Rollback parcial em B16 | Teste com `DatabaseConnection` real sobre `Connection` `Mock()` confere `rollback` sem `commit`. Rollback no PostgreSQL sem teste automático (limitação) | T13 |
| 7 | DTO ou JSON vazando CPF, nascimento, senha, CNPJ ou `enterpriseId` | Testes de **ausência** nos `*MapperSpec` e no `RecruiterServletSpec` | T16, T17, T18 |
| 8 | `Vacancy.enterprise` (com CNPJ) chegando ao DTO do candidato | Mapper copia só nome e e-mail, só com match; teste de ausência | T20 |
| 9 | Front divergente do back (regras duplicadas à mão) | `services/likes.ts` espelha T12–T14 caso a caso; roteiro manual B1–B17 nas telas | T26, T30, T34 |
| 10 | Tela vazando dado em hover card ou `data-*`/`aria-*` | `pages/` recebem só os tipos de cartão; inspeção do DOM em cada tela e varredura final | T29–T34, T39 |
| 11 | Seed do front desalinhado (CA50) | Valores copiados da tabela de recrutadores e do `seed.sql`; contagens conferidas | T23, T37 |
| 12 | Regressão nos fluxos antigos (cadastro, vagas, login) | `PersonDAO` e afins intocados; roteiro de regressão | T38 |
