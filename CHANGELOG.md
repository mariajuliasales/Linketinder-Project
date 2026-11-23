# Changelog

Todas as mudanças relevantes do Linketinder são registradas neste arquivo.

O formato segue o [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/). Toda tarefa concluída adiciona uma
entrada em `[Não lançado]`, conforme a skill `skills/feature-com-spec/SKILL.md`.

## [Não lançado]

### Adicionado

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
