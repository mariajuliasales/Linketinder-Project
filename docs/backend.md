# Backend (Groovy)


Aplicação de terminal com o CRUD de candidatos, empresas, vagas e competências. Os dados são salvos no
banco [**PostgreSQL**](database.md) por meio de JDBC.


## Tecnologias

* **Linguagem:** Groovy 4
* **Build:** Gradle com Gradle Wrapper
* **Banco:** PostgreSQL acessado por JDBC
* **Testes:** Spock Framework 2.3 
* **Conceitos:** orientação a objetos (interface, classe abstrata e herança), camada de serviços, DAO, DTOs,
  mappers, enums, records, transações e expressões regulares

## Arquitetura

| Camada | Classes | Responsabilidade |
|--------|---------|------------------|
| **Menu** | `Menu`, `MainMenu`, `CandidateMenu`, `EnterpriseMenu`, `VacancyMenu`, `CompetenceMenu`, `MenuOption`, `ConsoleInput`, `AddressForm`, `CompetenceForm`, `View` | Interação com o usuário no terminal: um menu por assunto, leitura do teclado, formulários reutilizáveis e formatação da saída |
| **Service** | `CandidateService`, `EnterpriseService`, `VacancyService`, `CompetenceService` | Regras de negócio: valida, chama o DAO e trata registro inexistente ou duplicado |
| **Validator** | `PersonValidator`, `CandidateValidator`, `EnterpriseValidator`, `VacancyValidator`, `CompetenceValidator`, `Validation` | Regras de validação de cada entidade, usadas pelos service |
| **Exception** | `BusinessException`, `ValidationException`, `EntityNotFoundException`, `DuplicateEntityException`, `EntityInUseException` | Erros de negócio, cada um correspondendo a um status HTTP (400, 404 e 409) |
| **DAO** | `DatabaseConnection`, `SqlExecutor`, `SqlExceptionTranslator`, `PersonDAO`, `CandidateDAO`, `EnterpriseDAO`, `VacancyDAO`, `CompetenceDAO`, `AddressDAO`, `CompetenceLink` | Conexão com o banco, execução do SQL e tradução dos erros do banco |
| **Model** | `Person`, `AbstractPerson`, `PersonType`, `Address`, `Candidate`, `Enterprise`, `Vacancy`, `Competence`, `VacancyStatus` | Entidades do domínio: só dados e regras, sem formatação de texto |
| **DTO** | `CandidateRequest`, `EnterpriseRequest`, `VacancyRequest`, `AddressRequest`, `CandidateResponse`, `CandidateAnonymousResponse`, `EnterpriseResponse`, `EnterpriseAnonymousResponse`, `VacancyAnonymousResponse`, `AddressResponse` | Records de entrada e saída. Os anônimos não têm dados que identifiquem a pessoa ou a empresa, e nenhuma resposta tem senha |
| **Mapper** | `CandidateMapper`, `EnterpriseMapper`, `VacancyMapper`, `AddressMapper`, `CompetenceMapper` | Converte entre DTOs e entidades (`toEntity`, `toResponse`, `toAnonymousResponse`) |
| **Util** | `CpfValidator`, `CnpjValidator`, `EmailValidator`, `PasswordValidator`, `AddressValidator`, `CheckDigit`, `DocumentNormalizer` | Validação de CPF, CNPJ, e-mail, senha, CEP e UF, e remoção de máscara dos documentos |

O `Main` monta as dependências (`DatabaseConnection` → DAOs → serviços → menus) e inicia o `MainMenu`.

## Integração com o banco (JDBC)

- **`DatabaseConnection`** abre as conexões com `DriverManager` e oferece dois métodos:
  `withConnection` (abre, usa e fecha a conexão) e `withTransaction` (faz `commit` se tudo der certo e
  `rollback` se algo falhar).
- **`SqlExecutor`** concentra o código JDBC repetitivo (`insertReturningId`, `update`, `query`, `queryFirst`):
  prepara o `PreparedStatement`, que evita SQL injection, liga os parâmetros, executa e fecha os recursos.
- **Um DAO por tabela principal**, com `create`, `findAll`, `findById`, `update` e `delete`.
- **Candidato e empresa** gravam em várias tabelas (`address` → `person` → `candidate`/`enterprise` → competências)
  dentro de **uma transação**: ou tudo é salvo, ou nada. O fluxo do CRUD é escrito uma vez em `PersonDAO`
  (padrão *Template Method*); `CandidateDAO` e `EnterpriseDAO` informam só o que é próprio de cada um
  (tabela, `PersonType`, SELECT, leitura da linha e colunas específicas).
- **Relação N:N com competências:** `CompetenceDAO.replaceLinks` grava as ligações em `candidate_competence`,
  `enterprise_competence` e `vacancy_competence`, escolhidas pelo enum `CompetenceLink`. Uma competência digitada
  que ainda não existe é criada na hora (ex.: "Ilustrador"), e "java" é reconhecida como "Java".
- **Relação 1:N empresa → vagas:** cada `Vacancy` guarda a sua `Enterprise`, e `VacancyDAO.findByEnterprise`
  lista as vagas de uma empresa. Excluir a empresa exclui as vagas dela (cascade no banco).
- **Senha** gravada como hash bcrypt pelo próprio PostgreSQL (`crypt(?, gen_salt('bf'))`).
- **Erros do banco** são traduzidos pelo `SqlExceptionTranslator`: e-mail, CPF, CNPJ ou competência repetidos viram
  `DuplicateEntityException` ("E-mail já cadastrado"...), e excluir algo em uso vira `EntityInUseException`.

## Modelo de domínio

```text
Person (interface: contrato de leitura usado por PersonValidator e PersonDAO)
   └── AbstractPerson (classe abstrata: id, name, email, password, description, address, competences)
          ├── Candidate  (+ cpf, birthDate, training; a idade é calculada)
          └── Enterprise (+ cnpj, vacancies; addVacancy/removeVacancy mantêm os dois lados da relação)

Address (id, cep, street, number, complement, neighborhood, city, state, country) — endereço de pessoas e vagas
Vacancy (id, title, description, competences, status, address, enterprise)
Competence (id, name) — lista pré-definida no banco + competências digitadas pelos usuários
PersonType (enum): CANDIDATE, ENTERPRISE
VacancyStatus (enum): OPEN, CLOSED, PAUSED
```

Os models não formatam texto. Para exibir, o menu converte a entidade em um DTO de resposta com o mapper
(ex.: `CandidateMapper.toAnonymousResponse`) e o `View` monta o texto do terminal.

## Funcionalidades do menu

```text
Bem vindo(a) ao LinkeTinder!
1. Candidatos       → listar (anônimo), ver perfil completo, cadastrar, atualizar, excluir
2. Empresas         → listar, ver dados completos, cadastrar, atualizar, excluir (com as vagas)
3. Vagas            → listar todas, listar as de uma empresa, cadastrar, atualizar, excluir
4. Competências     → listar, cadastrar, renomear, excluir
0. Sair do programa.
```

- **Listar candidatos:** mostra só formação, descrição e competências (`CandidateAnonymousResponse`), sem nome,
  e-mail, CPF ou endereço.
- **Cadastrar:** pede os dados no terminal, valida e salva no banco. As competências disponíveis são mostradas, e
  o usuário digita as suas separadas por vírgula, podendo escrever competências novas.
- **Atualizar:** mostra o valor atual de cada campo entre colchetes; **Enter mantém o valor**. A senha só muda
  se uma nova for digitada.
- **Excluir competência:** o banco recusa se ela estiver em uso por algum candidato, empresa ou vaga.

## Validações

| Onde | Regra |
|------|-------|
| `PersonValidator` | Regras comuns a candidato e empresa: nome, e-mail, endereço (CEP, cidade/UF) e senha (6+ caracteres, obrigatória no cadastro e opcional na atualização) |
| `CandidateValidator` | Regras de `PersonValidator` + **CPF**, data de nascimento e formação |
| `EnterpriseValidator` | Regras de `PersonValidator` + **CNPJ** |
| `VacancyValidator` | **Título**, descrição, **empresa**, cidade/**UF** e **ao menos uma competência** |
| `CompetenceValidator` | Nome não vazio, com até 50 caracteres |
| `CompetenceService` | Nome de competência que ainda não exista |
| `CpfValidator` | Formato com ou sem pontuação, rejeita dígitos todos iguais e confere os dígitos verificadores |
| `CnpjValidator` | Aceita também o formato **alfanumérico**, rejeita caracteres todos iguais e confere os dígitos verificadores |
| `CheckDigit` | Cálculo do dígito verificador (módulo 11), compartilhado por CPF e CNPJ |
| `EmailValidator` | Expressão regular |
| `PasswordValidator` | Mínimo de 6 caracteres |
| `AddressValidator` | **CEP:** 8 dígitos. **UF:** 2 letras. **Local:** cidade preenchida e UF válida |

## Erros

Herdam de `BusinessException` e têm mensagens em português:

| Exceção | Quando | Status HTTP |
|---------|--------|--------------------------|
| `ValidationException` | Uma regra de validação foi violada | 400 |
| `EntityNotFoundException` | Busca, atualização ou exclusão de um ID que não existe | 404 |
| `DuplicateEntityException` | E-mail, CPF, CNPJ ou competência já cadastrados | 409 |
| `EntityInUseException` | Exclusão de um registro usado por outros | 409 |


## Testes

Os testes unitários usam Spock. Nos serviços, os DAOs são substituídos por mocks, então os testes rodam sem precisar do banco. Os dados válidos usados ficam em `fixture/TestData`.

| Especificação | O que testa |
|---------------|-------------|
| `CandidateServiceSpec`, `EnterpriseServiceSpec`, `VacancyServiceSpec` | Salvar dados válidos, não chamar o DAO com dados inválidos, listagem, busca, atualização, exclusão e ID inexistente |
| `CompetenceServiceSpec` | O mesmo, mais nome duplicado na criação e na renomeação |
| `PersonValidatorSpec`, `CandidateValidatorSpec`, `EnterpriseValidatorSpec`, `VacancyValidatorSpec`, `CompetenceValidatorSpec` | Cada regra de validação e sua mensagem |
| `CpfValidatorSpec`, `CnpjValidatorSpec`, `CheckDigitSpec`, `EmailValidatorSpec`, `PasswordValidatorSpec`, `AddressValidatorSpec`, `DocumentNormalizerSpec` | Formatos e dígitos verificadores, valores válidos, inválidos e nulos |
| `CandidateMapperSpec`, `EnterpriseMapperSpec`, `VacancyMapperSpec`, `AddressMapperSpec`, `CompetenceMapperSpec` | Conversão de request para entidade e de entidade para resposta, respostas sem senha e anônimas sem dados pessoais, status da vaga |
| `EnterpriseSpec`, `CandidateSpec`, `CompetenceSpec` | Relação empresa–vagas, cálculo da idade e igualdade de competências sem diferenciar maiúsculas |
| `SqlExecutorSpec`, `SqlExceptionTranslatorSpec` | Ligação dos parâmetros, leitura das linhas e fechamento dos recursos (com mocks de JDBC), tradução dos erros do banco |


## Estrutura de pastas

```text
Linketinder/
├── build.gradle
├── settings.gradle
├── gradlew / gradlew.bat
└── src/
    ├── main/groovy/com/mariajuliasales/
    │   ├── Main.groovy
    │   ├── menu/          Menu, MainMenu, CandidateMenu, EnterpriseMenu, VacancyMenu, CompetenceMenu,
    │   │                  MenuOption, ConsoleInput, AddressForm, CompetenceForm, EndOfInputException, View
    │   ├── service/       CandidateService, EnterpriseService, VacancyService, CompetenceService
    │   │   └── validator/ PersonValidator, CandidateValidator, EnterpriseValidator, VacancyValidator,
    │   │                  CompetenceValidator, Validation
    │   ├── exception/     BusinessException, ValidationException, EntityNotFoundException,
    │   │                  DuplicateEntityException, EntityInUseException
    │   ├── dao/           DatabaseConnection, SqlExecutor, SqlExceptionTranslator, PersonDAO, CandidateDAO,
    │   │                  EnterpriseDAO, VacancyDAO, CompetenceDAO, AddressDAO, CompetenceLink
    │   ├── model/         Person, AbstractPerson, PersonType, Address, Candidate, Enterprise, Vacancy,
    │   │                  Competence, VacancyStatus
    │   ├── dto/
    │   │   ├── request/   CandidateRequest, EnterpriseRequest, VacancyRequest, AddressRequest
    │   │   └── response/  CandidateResponse, CandidateAnonymousResponse, EnterpriseResponse,
    │   │                  EnterpriseAnonymousResponse, VacancyAnonymousResponse, AddressResponse
    │   ├── mapper/        CandidateMapper, EnterpriseMapper, VacancyMapper, AddressMapper, CompetenceMapper
    │   └── util/          CpfValidator, CnpjValidator, CheckDigit, EmailValidator, PasswordValidator,
    │                      AddressValidator, DocumentNormalizer
    └── test/groovy/com/mariajuliasales/
        ├── fixture/       TestData
        ├── dao/           SqlExecutorSpec, SqlExceptionTranslatorSpec
        ├── menu/          MenuSpec, ConsoleInputSpec, FormSpec, ViewSpec
        ├── model/         EnterpriseSpec, CandidateSpec, CompetenceSpec
        ├── mapper/        CandidateMapperSpec, EnterpriseMapperSpec, VacancyMapperSpec, AddressMapperSpec,
        │                  CompetenceMapperSpec
        ├── service/       CandidateServiceSpec, EnterpriseServiceSpec, VacancyServiceSpec, CompetenceServiceSpec
        │   └── validator/ PersonValidatorSpec, CandidateValidatorSpec, EnterpriseValidatorSpec,
        │                  VacancyValidatorSpec, CompetenceValidatorSpec
        └── util/          CpfValidatorSpec, CnpjValidatorSpec, CheckDigitSpec, EmailValidatorSpec,
                           PasswordValidatorSpec, AddressValidatorSpec, DocumentNormalizerSpec
```
