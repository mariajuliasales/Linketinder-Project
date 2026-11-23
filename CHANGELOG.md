# Changelog

Todas as mudanças relevantes do Linketinder são registradas neste arquivo.

O formato segue o [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/). Toda tarefa concluída adiciona uma
entrada em `[Não lançado]`, conforme a skill `skills/feature-com-spec/SKILL.md`.

## [Não lançado]

### Adicionado

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

- Spec e plano do Recrutador: os 404 passam a usar o formato padrão do projeto, "`<Entidade>` com ID `<id>` não
  existe" (`EntityNotFoundException`), em vez de mensagens próprias; a edição de perfil de um recrutador inexistente
  responde 404.
- `Database/linketinder.dbml`: valor `RECRUITER` em `person_type`, `person.address_id` opcional com o
  `CHECK person_address_required_check`, tabelas `recruitment_area`, `recruiter` e `recruiter_like` e nota da
  invariante `enterprise_like` ⇔ `recruiter_like`, garantida pelo serviço.
- `AGENTS.md`: a seção 4.2 passa a documentar o reset oficial do banco (`dropdb` + `createdb` + `schema.sql` +
  `seed.sql` + migrations) e avisa que reaplicar só o `schema.sql` sobre um banco migrado não é suportado. A seção 10
  passa a apontar para a spec aprovada do Recrutador.

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
