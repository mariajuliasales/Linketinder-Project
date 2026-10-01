# Linketinder
Desenvolvido por **Maria Julia Sales**.

O **Linketinder** é um "LinkedIn + Tinder" para vagas de emprego: conecta candidatos e empresas pelas
**competências técnicas**, de forma **anônima**. O candidato vê as vagas sem saber qual empresa as publicou. A empresa vê os candidatos sem nome nem dados pessoais, só formação, descrição e competências.
A identidade dos dois lados só seria revelada depois de um *match*.

O projeto tem duas partes independentes:

| Parte | Pasta | O que é |
|-------|-------|---------|
| **Backend** | [`Linketinder/`](Linketinder/) | Aplicação de terminal em **Groovy**, com menu interativo, regras de negócio, validações e testes unitários |
| **Frontend** | [`Frontend/`](Frontend/) | Aplicação web em **TypeScript + Vite + Bootstrap**, com cadastro, login, listagens anônimas e gráfico de competências |

> Nesta etapa, frontend e backend ainda não se comunicam: cada um tem os próprios dados de exemplo.


---

## Sumário

- [Backend (Groovy)](#backend-groovy)
  - [Tecnologias](#tecnologias)
  - [Arquitetura](#arquitetura)
  - [Modelo de domínio](#modelo-de-domínio)
  - [Funcionalidades do menu](#funcionalidades-do-menu)
  - [Validações](#validações)
  - [Testes](#testes)
  - [Estrutura do backend](#estrutura-do-backend)
  - [Como executar o backend](#como-executar-o-backend)
- [Frontend (TypeScript)](#frontend-typescript)
  - [Tecnologias](#tecnologias-1)
  - [Telas](#telas)
  - [Funcionalidades](#funcionalidades)
  - [Anonimato](#anonimato)
  - [Persistência](#persistência)
  - [Estrutura do frontend](#estrutura-do-frontend)
  - [Como executar o frontend](#como-executar-o-frontend)

---

## Backend (Groovy)

Aplicação de terminal que cadastra e lista candidatos, empresas e vagas. Os dados ficam em memória
enquanto o programa está aberto, e a aplicação já começa com 6 candidatos, 6 empresas e 6 vagas de exemplo.

### Tecnologias

* **Linguagem:** Groovy 5 (Java JDK 17+)
* **Build:** Gradle (com Gradle Wrapper)
* **Testes:** Spock Framework 2.4 sobre JUnit Platform
* **Conceitos:** orientação a objetos (interface, classe abstrata e herança), camada de serviços, DTOs,
  mappers, enums, records e expressões regulares

### Arquitetura

O código é separado em camadas, cada uma com uma responsabilidade:

| Camada | Classes | Responsabilidade |
|--------|---------|------------------|
| **Menu** | `Menu`, `View` | Interação com o usuário no terminal |
| **Service** | `CandidateService`, `EnterpriseService`, `VacancyService` | Regras de negócio e validações antes de salvar |
| **Repository** | `Database` | Repositório simulado em memória |
| **Model** | `Person`, `PersonAbstract`, `Candidate`, `Enterprise`, `Vacancy`, `Competence`, `VacancyStatus` | Entidades do domínio |
| **DTO** | `CandidateRequest`, `CandidateResponse`, `CandidateAnonymousResponse` | Objetos de entrada e saída de dados do candidato  |
| **Mapper** | `CandidateMapper` | Converte entre DTOs e a entidade `Candidate` |
| **Util** | `ValidateUtil` | Validação de CPF, CNPJ e e-mail |

O `Main` monta as dependências (`Database` → serviços → `Menu`) e inicia o menu.

### Modelo de domínio

```text
Person (interface)
   └── PersonAbstract (classe abstrata: id, name, email, state, cep, description, competences)
          ├── Candidate  (+ cpf, age)
          └── Enterprise (+ cnpj, country)

Vacancy (id, title, description, competences, status, enterprise)
Competence (enum):    PYTHON, JAVA, SPRING_FRAMEWORK, ANGULAR, GROOVY, JAVASCRIPT, SQL, DOCKER, KAFKA, REACT
VacancyStatus (enum): OPEN, CLOSED, PAUSED
```

Cada pessoa sabe exibir o próprio **perfil anônimo** (`viewProfileAnonymous`), e cada vaga sabe exibir
sua versão anônima (`viewVacancyAnonymous`), sem os dados da empresa.

### Funcionalidades do menu

```text
Bem vindo(a) ao LinkeTinder!
1. Listar todos os candidatos.
2. Listar todas as empresas.
3. Listar todas as vagas.
4. Criar um novo candidato.
5. Criar uma nova empresa.
6. Criar uma nova vaga.
7. Sair do programa.
```

- **Listar candidatos:** mostra o perfil anônimo de cada candidato (`CandidateAnonymousResponse`: estado,
  CEP, descrição e competências — sem nome, e-mail, CPF ou idade).
- **Listar empresas:** mostra o perfil anônimo de cada empresa.
- **Listar vagas:** mostra título, descrição e competências de cada vaga.
- **Criar candidato / empresa:** pede os dados no terminal, valida e salva. As competências são digitadas
  separadas por vírgula (ex.: `java, spring framework`). Os nomes não reconhecidos são ignorados com aviso.
- **Criar vaga:** pede título, descrição, competências e o ID da empresa dona da vaga.

### Validações

| Onde | Regra |
|------|-------|
| `CandidateService` | Candidato não pode ser nulo; **CPF** e **e-mail** precisam ser válidos |
| `EnterpriseService` | Empresa não pode ser nula; **CNPJ** e **e-mail** precisam ser válidos |
| `VacancyService` | A vaga precisa de **título**, de **ao menos uma competência** válida e de uma **empresa**; a busca por ID exige ID positivo e existente |
| `ValidateUtil` | **CPF:** formato com ou sem pontuação, rejeita dígitos todos iguais e confere os dígitos verificadores. **CNPJ:** aceita também o formato **alfanumérico** e confere os dígitos verificadores. **E-mail:** validado por expressão regular |

Quando uma regra é violada, o serviço lança `IllegalArgumentException` com uma mensagem explicando o erro,
e o menu mostra essa mensagem sem encerrar o programa.

### Testes

Os serviços têm testes unitários com **Spock** (`src/test/groovy/.../service/`), incluindo testes
parametrizados com várias entradas inválidas:

| Especificação | O que testa |
|---------------|-------------|
| `CandidateServiceSpec` | Criação válida, candidato nulo, CPFs inválidos, e-mails inválidos, listagem e busca por ID |
| `EnterpriseServiceSpec` | Criação válida, CNPJs inválidos, e-mails inválidos, listagem e busca por ID |
| `VacancyServiceSpec` | Criação válida, empresa nula, título vazio, competências inválidas, listagem (com e sem vagas) e busca por ID (válido, inválido e inexistente) |

São **49 casos de teste** no total, todos passando.

### Estrutura do backend

```text
Linketinder/
├── build.gradle
├── settings.gradle
├── gradlew / gradlew.bat
└── src/
    ├── main/groovy/com/mariajuliasales/
    │   ├── Main.groovy
    │   ├── menu/         Menu.groovy, View.groovy
    │   ├── service/      CandidateService.groovy, EnterpriseService.groovy, VacancyService.groovy
    │   ├── repository/   Database.groovy
    │   ├── model/        Person.groovy, PersonAbstract.groovy, Candidate.groovy, Enterprise.groovy,
    │   │                 Vacancy.groovy, Competence.groovy, VacancyStatus.groovy
    │   ├── dto/
    │   │   ├── request/  CandidateRequest.groovy
    │   │   └── response/ CandidateResponse.groovy, CandidateAnonymousResponse.groovy
    │   ├── mapper/       CandidateMapper.groovy
    │   └── util/         ValidateUtil.groovy
    └── test/groovy/com/mariajuliasales/service/
        ├── CandidateServiceSpec.groovy
        ├── EnterpriseServiceSpec.groovy
        └── VacancyServiceSpec.groovy
```

### Como executar o backend

**Pré-requisito:** Java JDK 17 ou superior. Não é preciso instalar o Gradle, pois o projeto usa o Gradle Wrapper.

```bash
git clone https://github.com/mariajuliasales/Linketinder-Project.git
cd Linketinder-Project/Linketinder

./gradlew build   # compila e roda os testes
./gradlew run     # abre o menu no terminal
./gradlew test    # só os testes
```

No Windows, use `gradlew.bat` no lugar de `./gradlew`.

---

## Frontend (TypeScript)

Aplicação web de página única em que candidatos e empresas se cadastram, entram com e-mail e senha
e veem uns aos outros de forma anônima.

### Tecnologias

* **Linguagem:** TypeScript 6
* **Build e servidor de desenvolvimento:** Vite 8
* **Estilo:** Bootstrap 5.3
* **APIs do navegador:** DOM e LocalStorage 

### Telas

| Endereço | Tela | Quem acessa |
|----------|------|-------------|
| `#/home` | **Página inicial:** apresentação, números da plataforma, "Como funciona" e chamadas para cadastro | Todos |
| `#/register` | **Cadastro:** um só formulário, com a escolha "Sou candidato" ou "Sou empresa" | Todos |
| `#/login` | **Login** com e-mail e senha | Todos |
| `#/vacancies` | **Vagas:** todas as vagas abertas, sem dados da empresa | Candidato logado |
| `#/candidates` | **Candidatos:** tabela de candidatos anônimos e gráfico de candidatos por competência | Empresa logada |
| `#/my-vacancies` | **Minhas vagas:** publicar e excluir as vagas da empresa | Empresa logada |

Quem tenta abrir uma página restrita sem estar logado (ou logado com o tipo errado) é levado ao login.

### Funcionalidades

- **Cadastro (Create):**
  - campos comuns: nome, e-mail, senha, endereço (CEP, cidade, UF e país), descrição opcional e competências;
  - candidato informa também CPF, data de nascimento (mínimo de 16 anos) e formação; empresa informa o CNPJ;
  - validação com Bootstrap (campos obrigatórios, formato de CPF, CNPJ e CEP, ao menos uma competência);
  - o e-mail não pode se repetir entre candidatos e empresas;
  - após o cadastro, o usuário já entra logado.
- **Login e logout:** com e-mail e senha; após entrar, o candidato vai para as vagas e a empresa para os candidatos.
- **Vagas (candidato):** cards com título, local, descrição e competências; as competências que o
  candidato tem aparecem em **verde**.
- **Candidatos (empresa):** tabela com "Candidato #N", formação, descrição e competências.
- **Gráfico de competências (empresa):** barras horizontais com quantos candidatos têm cada competência.
- **Janelas de informação ao passar o mouse** (ou ao navegar com Tab):
  - sobre um **candidato** na tabela: formação, descrição, competências e quantas delas a empresa busca;
  - sobre uma **vaga**: detalhes e quantas competências pedidas o candidato tem;
  - sobre uma **barra do gráfico**: quantos candidatos têm aquela competência e o percentual do total;
  - nome e contato aparecem como "🔒 revelados após o match".
- **Minhas vagas (Create e Delete):** a empresa publica vagas (título, descrição, cidade, UF e
  competências) e exclui as próprias vagas.
- **Excluir conta (Delete):** candidato e empresa podem excluir a própria conta; ao excluir uma empresa,
  as vagas dela também são excluídas. Toda exclusão pede confirmação.

**Contas de teste** (senha `123456` para todas):

| Tipo | E-mail |
|------|--------|
| Candidato | `mariajuliasales@gmail.com` |
| Empresa | `contato@techsolutions.com.br` |

### Anonimato

O anonimato é garantido **pelo tipo**, não só pela tela. As páginas de listagem recebem apenas versões
reduzidas dos dados, criadas campo a campo em `services.ts`:

- `CandidateAnonymous` → só `training`, `description` e `competencies`.
  Nome, e-mail, CPF, data de nascimento e endereço não chegam à tela da empresa.
- `VacancyAnonymous` → só `title`, `description`, `city`, `state` e `competencies`.
  O vínculo com a empresa (`enterpriseId`) não chega à tela do candidato.


### Persistência

Os dados ficam no **LocalStorage** do navegador. Na primeira visita, a aplicação grava os dados de exemplo
(5 candidatos, 5 empresas e 5 vagas).

> As senhas ainda são guardadas **sem criptografia**, porque é só uma demonstração do frontend.

### Estrutura do frontend

```text
Frontend/
├── index.html
├── package.json
├── tsconfig.json
└── src/
    ├── main.ts           # rotas por hash e navbar
    ├── types.ts          # tipos do domínio e listas fixas (competências e UFs)
    ├── seed.ts           # dados de exemplo
    ├── storage.ts        # único acesso ao LocalStorage (dados e sessão)
    ├── services.ts       # cadastro, login, exclusões, vagas, listagens anônimas e contagens
    ├── dom.ts            # helpers de DOM e janela de informações
    ├── style.css         # ajustes visuais sobre o Bootstrap
    └── pages/
        ├── home.ts
        ├── register.ts
        ├── login.ts
        ├── vacancies.ts
        ├── candidates.ts     # tabela e gráfico SVG
        └── myVacancies.ts
```

O fluxo é sempre `pages` → `services` → `storage`.

### Como executar o frontend

**Pré-requisito:** Node.js 20.19+ ou 22.12+ (exigência do Vite 8).

```bash
cd Linketinder-Project/Frontend

npm install       # instala as dependências
npm run dev       # servidor de desenvolvimento em http://localhost:5173
```

---
