# Changelog

Todas as mudanças relevantes do Linketinder são registradas neste arquivo.

O formato segue o [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/). Toda tarefa concluída adiciona uma
entrada em `[Não lançado]`, conforme a skill `skills/feature-com-spec/SKILL.md`.

## [Não lançado]

### Adicionado

- Tela "Recrutadores" da empresa no front (`pages/recruiters.ts`), com rota `#/recruiters` e link na navbar da
  empresa. A empresa lista os próprios recrutadores numa tabela com nome, e-mail, área e descrição, cadastra novos
  (formulário com nome, e-mail, senha, área num `select`, CPF, nascimento e descrição) e remove com confirmação. Os
  erros 400 e 409 do serviço aparecem como vêm; o 404 com id vira uma mensagem genérica. CPF, nascimento e senha não
  chegam à tela, e o `id` fica só no closure do botão de remover, fora de `data-*` e `aria-*` (#21).
- Serviços de revelação após o match no front, sem tela que os use ainda. Em `services/candidates.ts`:
  `listCandidateCardsForRecruiter()` e `listCandidateCardsForEnterprise()`, sem parâmetro. O recrutador ou a empresa
  vêm da sessão, e a empresa da revelação vem deles, nunca do chamador. Lançam erro sem sessão do tipo certo, e
  "Recrutador com ID `<id>` não existe" se o recrutador da sessão não existir mais. Os cards são anônimos; nome, e-mail
  e as vagas com match (id, título e status) só aparecem com match com aquela empresa. `likedByMe` vem só da curtida
  do próprio recrutador e é sempre `false` para a empresa. Em `services/vacancies.ts`: `listOpenVacancyCards` (só
  `OPEN`, com `likedByMe`), `listLikedVacancyCards` ("Minhas curtidas", em qualquer status, com nome e e-mail da
  empresa só na vaga com match) e `listEnterpriseVacanciesForRecruiter` (todas as vagas da empresa do recrutador,
  somente leitura). Tudo é montado campo a campo, sem `enterpriseId`, CPF, nascimento, endereço, senha nem dado de
  recrutador (#21).
- Serviço de recrutadores no front, em `services/recruiters.ts`, espelhando o `RecruiterValidator` e o
  `RecruiterService` com as mesmas mensagens de erro. `createRecruiter`: valida nome, e-mail, senha (6+), CPF com
  dígitos verificadores, nascimento (não futuro, 18+, aceita quem faz 18 hoje) e área da lista (sem diferenciar
  maiúsculas), confere a empresa e o e-mail entre todas as contas ("E-mail já cadastrado") e grava sem iniciar sessão.
  `listOwnRecruiters`: só os da própria empresa. `removeRecruiter`: só da própria empresa, sem distinguir recrutador
  inexistente de recrutador de outra empresa; cada curtida dele sai pela regra de "saiu o último" (`unlikeCandidate`).
  `updateOwnProfile`: só descrição e senha, com senha vazia mantendo a atual. As funções devolvem só
  `RecruiterSummary` (`id`, nome, e-mail, área e descrição), sem CPF, nascimento nem senha. A `Collection` do
  `storage.ts` ganha `update(id, changes)`, que altera o item no lugar, e o `removeWhere` passa a ser exportado pelo
  `storage.ts`, resolvendo a duplicação em `auth.ts` e `likes.ts`. O plano ganha a regra geral de trocar os erros 404
  com id por uma mensagem genérica em todas as telas (#21).
- Regras de curtida e match no front, em `services/likes.ts`, espelhando o `CandidateLikeService` e o
  `RecruiterLikeService` com as mesmas mensagens de erro. `likeVacancy`/`unlikeVacancy` (candidato): só curte vaga
  `OPEN`, cria o match se a empresa já curte o candidato e a descurtida apaga só o match daquela vaga.
  `likeCandidate`/`unlikeCandidate` (recrutador): bloqueia a autocurtida por CPF (só dígitos); na primeira curtida da
  empresa grava o `enterpriseLike` e cria matches só com as vagas `OPEN` dela que o candidato curtiu; quando sai o
  último recrutador da empresa, apaga o `enterpriseLike` e os matches do candidato com todas as vagas dela. Tudo
  idempotente, sem devolver dados. Ainda sem tela que use o serviço. A spec (B14) passa a dizer que uma curtida
  repetida do candidato também completa o par se o match ainda não existir; o plano registra a mensagem genérica no
  lugar do 404 com id nas telas (T30, T34) e a dívida técnica do `removeWhere` duplicado (T27) (#21).
- Exclusão de contas em cascata no front, espelhando o `ON DELETE CASCADE` do banco em `auth.ts#deleteLoggedAccount`:
  a empresa apaga também os recrutadores dela, as curtidas desses recrutadores, os `enterpriseLikes` dela, as curtidas
  em vagas dela e os matches dessas vagas; o candidato apaga também as curtidas que fez, as `recruiterLikes` e
  `enterpriseLikes` que recebeu e os matches dele. A conta de recrutador continua sendo recusada (#21).
- Login e guard do recrutador no front: recrutadores entram em `allAccounts` (login por e-mail sem diferenciar
  maiúsculas e levado a `#/recruiter-candidates`), com `getLoggedRecruiter` no `auth.ts` e `requireLoggedRecruiter` no
  `guards.ts`. O guard de empresa barra o recrutador (ex.: `#/my-vacancies` redireciona para o login). E-mails de
  recrutadores passam a contar em `isEmailInUse`. `deleteLoggedAccount` lança erro para a conta de recrutador, sem
  deslogar, porque quem o remove é a empresa (#21).
- Armazenamento e seed do front para o recrutador: coleções `recruiters`, `candidateLikes`, `recruiterLikes`,
  `enterpriseLikes` e `matches` no `storage.ts`, com `DATA_FORMAT_VERSION` = `5` (dados antigos no navegador são
  recriados). O `seed.ts` copia os 5 recrutadores da migration 003 (`recruiter-1`…`recruiter-5`, senha `123456`), as 5
  curtidas de candidatos e as 4 curtidas de empresas do `seed.sql`, as 4 curtidas de recrutadores do backfill e os 3
  matches (Sandubinha × Backend Spring, Lucas × Full Stack e Gabriel × Cientista de Dados) (#21).
- Tipos e rotas do recrutador no front: `RECRUITER` em `PersonType`; `Recruiter` (sem endereço e sem competências) em
  `Account`; `RECRUITMENT_AREAS`; `CandidateLike`, `RecruiterLike`, `EnterpriseLike` e `Match`, com `id`; e os tipos
  para `pages/` `RecruiterSummary`, `CandidateCard` (nome, e-mail e vagas com match só em `reveal`), `VacancyCard`
  (`likedByMe` opcional) e `LikedVacancyCard` (nome e e-mail da empresa só com match), sem CPF, nascimento, senha,
  endereço, CNPJ nem `enterpriseId`. Rotas `#/recruiters`, `#/recruiter-candidates`, `#/recruiter-vacancies` e
  `#/recruiter-profile`, que caem na home enquanto as telas não existem; o recrutador entra em
  `#/recruiter-candidates`. Navbar do recrutador com "Candidatos" e "Vagas da empresa", sem "Excluir conta" (#21).
- Vagas da empresa para o recrutador, somente leitura: `RecruiterController#listEnterpriseVacancies` devolve
  `VacancyAnonymousResponse` com todas as vagas da empresa do recrutador, em qualquer status, sem dados da empresa e sem
  rota na API. Recrutador inexistente responde 404. Novo `RecruiterService#enterpriseIdOf`. O `RecruiterController`
  passa a receber o `VacancyService` (obrigatório), montado em `Application#createControllers`. Nenhum método de criar,
  editar ou excluir vaga recebe recrutador (#21).
- "Minhas curtidas" do candidato no back-end: `LikeController#listLikedVacancies` devolve `VacancyMatchResponse` com as
  vagas curtidas em qualquer status, em ordem crescente de id, com descrição, cidade/UF (endereço da vaga), status e
  competências. Nome e e-mail da empresa só nas vagas com match; sem `enterpriseId` nem CNPJ. Candidato inexistente
  responde 404. Nova consulta `LikeRepository#likedVacanciesOf` (uma só, sem colunas de `person`; competências numa
  subconsulta `ARRAY`), os records `LikedVacancy` e `LikedVacancyView` e `MatchQueryService#listLikedVacancies`, que
  busca a empresa uma vez por empresa distinta e só para vagas com match. Sobrecarga
  `MatchMapper#toVacancyMatchResponse(LikedVacancyView)`. O `MatchQueryService` passa a receber o `LikeRepository` (#21).
- `LikeController` e `MatchQueryService`: o controller expõe `candidateLikes`/`candidateUnlikes` e
  `recruiterLikes`/`recruiterUnlikes` (delegam ao `CandidateLikeService` e ao `RecruiterLikeService`) e as listas de
  revelação `listCandidatesForRecruiter` e `listCandidatesForEnterprise`, sem rota na API. As listas trazem só os
  candidatos com match com a empresa, revelados (nome, e-mail e só as vagas com match), em ordem crescente de id;
  candidato sem match, mesmo com curtida unilateral, fica fora e continua só na listagem anônima. Empresa ou recrutador
  inexistente responde 404. O `MatchQueryService` lê com duas consultas (vagas com match e candidatos), sem transação
  nem trava. Novos `MatchRepository#matchedVacanciesByEnterprise` (sem colunas de `person`),
  `CandidateRepository#findAllByIds` (só acréscimo ao `CandidateDAO`), o record `CandidateMatches` e a sobrecarga
  `MatchMapper#toCandidateMatchResponse(CandidateMatches)`. `Application#createControllers` passa a montar o
  `CandidateLikeService`, o `MatchQueryService` e o `LikeController` (#21).
- `RecruiterController` e rota `POST /api/recruiters`: a empresa cadastra um recrutador enviando `enterpriseId`
  (inteiro, obrigatório) e os dados do recrutador. Responde 201 com `Location` e `RecruiterResponse` (sem CPF,
  nascimento, idade nem senha); 400 para dados inválidos, 404 para empresa inexistente e 409 para e-mail ou CPF
  duplicado. O controller também expõe `updateOwnProfile`, `removeByEnterprise` e `listByEnterprise`, sem rota na API.
  `RecruiterService#listByEnterprise` devolve só os recrutadores da empresa (404 se ela não existir).
  `Application#createControllers` passa a montar `RecruiterDAO`, `LikeDAO`, `MatchDAO`, `RecruiterLikeService` e
  `RecruiterService` (#21).
- `MatchedVacancyResponse`, `CandidateMatchResponse`, `VacancyMatchResponse` e `MatchMapper`: respostas de revelação
  montadas campo a campo. O candidato traz os dados anônimos e, só com match, nome, e-mail e as vagas da empresa com
  match (id, título, status). A vaga traz os dados anônimos com status e, só com match, nome e e-mail da empresa, sem
  `enterpriseId`. Nenhuma tem CPF, nascimento, idade, endereço, senha nem CNPJ (#21).
- `RecruiterRequest`, `RecruiterResponse` e `RecruiterMapper`: o request traz nome, e-mail, senha, descrição, CPF,
  nascimento e área (convertida por `RecruitmentArea.fromName`; área inválida vira `null`); a resposta tem só `id`,
  nome, e-mail, área e descrição, sem CPF, nascimento, idade nem senha, inclusive no cadastro (#21).
- `RecruiterService#removeByEnterprise`: remoção do recrutador pela própria empresa, numa transação que trava a
  empresa. Para cada candidato curtido pelo recrutador, apaga a `recruiter_like` e aplica a regra de "saiu o último"
  (`RecruiterLikeService#releaseRecruiterLike`); depois apaga `recruiter` e `person`. Responde 404 "Recrutador com ID
  `<id>` não existe" tanto para id inexistente quanto para recrutador de outra empresa, sem apagar nada. O construtor do
  `RecruiterService` passa a receber `ConnectionManager`, `LikeRepository` e `RecruiterLikeService` (#21).
- `RecruiterLikeService#unlike`: descurtida idempotente do recrutador, numa transação que trava a empresa. Apaga a
  `recruiter_like` e, se nenhum outro recrutador da empresa curte o candidato, apaga o `enterprise_like` e os matches
  do candidato com todas as vagas da empresa. Responde 404 se o recrutador ou a empresa não existem; candidato
  inexistente não muda nada. A regra de "saiu o último" fica em `releaseRecruiterLike`, para ser reaproveitada na
  remoção do recrutador (#21).
- `RecruiterLikeService`: curtida do recrutador em candidato, numa transação que trava a empresa e mantém a invariante
  `enterprise_like` ⇔ `recruiter_like`: confere se a empresa já curtia o candidato antes de gravar a curtida e, na
  primeira curtida da empresa, grava o `enterprise_like` e cria o match só com as vagas `OPEN` que o candidato curtiu.
  Bloqueia a autocurtida pelo CPF (400, sem o CPF na mensagem) e devolve 404 para recrutador ou candidato inexistente.
  `MatchDAO#likedVacanciesOfEnterprise` passa a travar as vagas lidas com `FOR SHARE OF v` (#21).
- `CandidateLikeService`: curtida do candidato em vaga, numa transação que trava a empresa e relê o status da vaga com
  `FOR SHARE` (só vagas `OPEN`; 404 para candidato ou vaga inexistente), cria o match quando a empresa já curte o
  candidato, e descurtida idempotente em qualquer status. `LikeRepository` ganha `lockVacancyStatus` (#21).
- Repositório de match (`MatchRepository`/`MatchDAO`) com as vagas curtidas da empresa e seus status (o filtro `OPEN`
  fica no serviço), criação idempotente e remoção dos matches do candidato com a empresa (#21).
- Repositório de curtidas (`LikeRepository`/`LikeDAO`) com trava da empresa e operações idempotentes (#21).
- `RecruiterService`: cadastro do recrutador pela empresa (valida, confere a empresa e o e-mail entre todas as pessoas
  antes de gravar, sem comparar o CPF com o de candidatos) e edição da própria descrição e senha (senha vazia mantém a
  atual; só a senha nova é validada), devolvendo o recrutador sem a senha.
- `RecruiterRepository` e `RecruiterDAO`: cadastro do recrutador (`person` sem endereço + `recruiter` na mesma
  transação), busca por empresa, e-mail único entre todas as pessoas (sem diferenciar maiúsculas), atualização de
  descrição e senha e remoção, sem nunca selecionar `password_hash`; `recruiter_cpf_key` traduzido para
  "CPF já cadastrado".
- `RecruiterValidator`: nome, e-mail, senha (opcional na atualização), CPF, data de nascimento (não futura, 18+)
  e área da lista fixa, sem exigir endereço nem competências.
- Modelo `Recruiter` (sem endereço nem competências, sem `toString` com dados pessoais) e enum `RecruitmentArea`
  com as 7 áreas e busca pelo nome sem diferenciar maiúsculas; valor `RECRUITER` em `PersonType`.
- Migration `003_insert_example_recruiters.sql`: 5 recrutadores de exemplo (senha `123456`, sem endereço) e backfill
  das curtidas de empresa já existentes com a curtida do recrutador indicado, para que toda `enterprise_like` tenha uma
  `recruiter_like` correspondente.
- Migration `002_create_recruiter.sql`: cria `recruitment_area` (com as 7 áreas), `recruiter` e `recruiter_like`, e
  torna `person.address_id` opcional só para o recrutador, com o `CHECK person_address_required_check`.
- Migration `001_add_recruiter_person_type.sql`, a primeira de `Database/migrations/`: adiciona o valor `RECRUITER`
  ao ENUM `person_type`, sozinha e fora de transação, para que a próxima migration possa usá-lo.
- Plano de implementação da feature Recrutador (`docs/specs/recrutador/plano.md`).
- Spec da feature Recrutador (`docs/specs/recrutador/spec.md`), com modelo de dados, anonimato por perfil e decisões
  tomadas.
- `CHANGELOG.md`, para registrar as mudanças de cada tarefa.
- Skill `feature-com-spec` adaptada ao projeto: specs em `docs/specs/<feature>/`, checklist de anonimato e entrada no
  CHANGELOG a cada tarefa.
- `AGENTS.md` com o contexto do projeto para agentes de IA: domínio, regras de curtida e match, Princípio do Anonimato
  e banco de dados.

### Modificado

- Plano do Recrutador: na T28, `listCandidateCards(enterpriseId, recruiterId?)` vira `listCandidateCardsForRecruiter()`
  e `listCandidateCardsForEnterprise()`, que leem a sessão; a T30 passa a usar `listCandidateCardsForRecruiter()` (#21).
- Plano do Recrutador: a T27 passa a incluir `data/storage.ts`, com `update(id, changes)` na `Collection`, que altera o
  item no lugar, e o `updateOwnProfile` passa a usar esse `update` (#21).
- Front: o `register` de `auth.ts` e o cadastro público (`pages/register.ts`) passam a aceitar só candidato e empresa
  (`SelfRegisteredType`). É só um ajuste de tipo, sem mudar o comportamento: o recrutador é cadastrado pela empresa (#21).
- Spec e plano do Recrutador: a T22 inclui `auth.ts` e `register.ts` (só tipo); na T27, o `createRecruiter` é uma
  função própria, que não usa o `register`; na T35, o `register.ts` não muda de comportamento; a spec 6.2 ganha a linha
  de `pages/register.ts` (#21).
- Spec do Recrutador: casos de borda B18 (descurtida por recrutador inexistente → 404) e B19 (descurtida de candidato
  inexistente → nada muda, como B8) (#21).
- `CandidateLikeService` deixa de ignorar o retorno do `lockEnterprise`: se a empresa da vaga não existe mais, responde
  404 "Vaga com ID `<id>` não existe", sem revelar a empresa (#21).
- Spec e plano do Recrutador: os 404 passam a usar o formato padrão do projeto, "`<Entidade>` com ID `<id>` não
  existe" (`EntityNotFoundException`), em vez de mensagens próprias; a edição de perfil de um recrutador inexistente
  responde 404.
- `Database/linketinder.dbml`: valor `RECRUITER` em `person_type`, `person.address_id` opcional com o
  `CHECK person_address_required_check`, tabelas `recruitment_area`, `recruiter` e `recruiter_like` e nota da
  invariante `enterprise_like` ⇔ `recruiter_like`, garantida pelo serviço.
- `AGENTS.md`: a seção 4.2 passa a documentar o reset oficial do banco (`dropdb` + `createdb` + `schema.sql` +
  `seed.sql` + migrations) e avisa que reaplicar só o `schema.sql` sobre um banco migrado não é suportado. A seção 10
  passa a apontar para a spec aprovada do Recrutador.

### Corrigido

- Front: o login e a checagem de e-mail duplicado (`isEmailInUse`) passam a ignorar espaços nas pontas, além de
  maiúsculas, ao comparar e-mails (`auth.ts#normalizeEmail`) (#21).

## [0.1.0] - 2026-11-04

### Adicionado

- **API REST:** cadastro de candidatos, empresas e vagas via `POST`, com respostas em JSON, documentado em
  `docs/api.md`.
- **Arquitetura do back-end:** organização em camadas (view/API, controller, service, repository, DAO), com tratamento
  de erros de domínio e documentação técnica em `docs/`.
- **Persistência:** back-end integrado ao PostgreSQL via JDBC, com CRUD de candidatos, empresas, vagas e competências.
- **Endereço:** cadastro de endereço para pessoas e vagas, com validação de CEP e cidade/UF.
- **Banco de dados:** schema PostgreSQL com pessoas, competências, vagas, curtidas e matches, além de dados de exemplo,
  consultas de teste e modelo em DBML.
- **Front-end:** SPA em TypeScript com login, cadastro, listagem de vagas e de candidatos anônimos e gestão das vagas
  da empresa, com dados guardados no navegador.
- **Testes:** testes unitários com Spock e validações com mensagens de erro detalhadas.
- **Vagas:** cadastro e listagem de vagas com status.
- **Anonimato:** listagem de candidatos sem dados pessoais.
- **Console:** menu no terminal para cadastrar e listar candidatos e empresas.
- **Projeto:** estrutura inicial em Groovy com Gradle e README.

[Não lançado]: https://github.com/mariajuliasales/Linketinder-Project/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/mariajuliasales/Linketinder-Project/releases/tag/v0.1.0
