# Linketinder

Desenvolvido por **Maria Julia Sales**.

O **Linketinder** é um "LinkedIn + Tinder" para vagas de emprego: conecta candidatos e empresas pelas competências técnicas, de forma anônima. O candidato vê as vagas sem saber qual empresa as publicou, e a empresa vê os candidatos sem dados pessoais, só formação, descrição e competências.
A identidade dos dois lados só é revelada depois de um match.

## Partes do projeto

| Parte | Pasta | O que é |
|-------|-------|---------|
| **Backend** | [`Linketinder/`](Linketinder/) | Aplicação em **Groovy** com menu no terminal e **API REST** (Tomcat + Servlets), regras de negócio, validações, testes unitários e acesso ao banco por JDBC |
| **Frontend** | [`Frontend/`](Frontend/) | Aplicação web em **TypeScript + Vite + Bootstrap**, com cadastro, login, listagens anônimas e gráfico de competências |
| **Banco de dados** | [`Database/`](Database/) | Modelagem (DER) e SQL do banco **PostgreSQL**: tabelas, curtidas, match e consultas de teste |

> O backend salva os dados no PostgreSQL. O frontend ainda não se comunica com o backend.

O projeto segue o padrão **MVC**: o menu do terminal e o frontend são a View, os controllers do backend recebem os
pedidos, e o Model reúne entidades, services e persistência.

## Como executar

### Pré-requisitos

* **PostgreSQL** rodando na porta 5432
* **Java JDK 21** (o Gradle não precisa ser instalado, o projeto usa o Gradle Wrapper)
* **Node.js** 20.19+ ou 22.12+

### 1. Banco de dados

```bash
git clone https://github.com/mariajuliasales/Linketinder-Project.git
cd Linketinder-Project/Database
export PGPASSWORD=suasenha  

createdb -h localhost -U postgres linketinder                          # cria o banco (só na primeira vez)
psql -h localhost -U postgres -d linketinder -f schema.sql             # cria as tabelas
psql -h localhost -U postgres -d linketinder -f seed.sql               # insere os dados de exemplo
psql -h localhost -U postgres -d linketinder -P pager=off -f queries.sql  # roda as consultas de teste
```

### 2. Backend

```bash
cd Linketinder-Project/Linketinder

./gradlew build   # compila e roda os testes (não precisa do banco)
./gradlew run     # abre o menu no terminal (precisa do banco rodando)
./gradlew runApi  # sobe a API REST em http://localhost:8080/api (precisa do banco rodando)
./gradlew test    # só os testes
```

Por padrão, a aplicação conecta em `jdbc:postgresql://localhost:5432/linketinder` com usuário e senha
`postgres`. Para usar outros valores, defina as variáveis de ambiente antes do `run`:

```bash
export DB_TYPE=postgresql
export DB_URL=jdbc:postgresql://localhost:5432/linketinder
export DB_USER=postgres
export DB_PASSWORD=sua_senha
```

No Windows, use `gradlew.bat` no lugar de `./gradlew`.

#### API REST

| Método | Rota | O que faz |
|--------|------|-----------|
| `POST` | `/api/candidates` | Cadastra um candidato |
| `POST` | `/api/enterprises` | Cadastra uma empresa |
| `POST` | `/api/vacancies` | Cria uma vaga (a empresa vai no campo `enterpriseId`) |

Os detalhes, exemplos com `curl` e o tutorial de como a API foi feita sem frameworks estão em
[docs/api.md](docs/api.md).

### 3. Frontend

```bash
cd Linketinder-Project/Frontend

npm install       # instala as dependências
npm run dev       # http://localhost:5173
```

**Contas de teste** (senha `123456`): candidato `mariajuliasales@gmail.com` e empresa `contato@techsolutions.com.br`.
