# API REST

## Endpoints

Base: `http://localhost:8080/api`

| Método | Rota | O que faz |
|--------|------|-----------|
| `POST` | `/api/candidates` | Cadastra um candidato |
| `POST` | `/api/enterprises` | Cadastra uma empresa |
| `POST` | `/api/vacancies` | Cria uma vaga para a empresa informada em `enterpriseId` |
| `POST` | `/api/recruiters` | Cadastra um recrutador para a empresa informada em `enterpriseId` |


| Status | Quando |
|--------|--------|
| `400` | Dados inválidos, JSON malformado ou data fora do formato `AAAA-MM-DD` |
| `404` | Empresa inexistente |
| `409` | E-mail, CPF ou CNPJ já cadastrados |
| `500` | Erro inesperado |

**Exemplo de corpo (vaga):**

```json
{ "enterpriseId": 1, "title": "Dev Groovy", "description": "APIs REST",
  "city": "Recife", "state": "PE", "competences": ["Groovy"] }
```

### `POST /api/recruiters`

Só a empresa cadastra os próprios recrutadores. O recrutador não tem endereço nem competências. Não há outras rotas de
recrutador, curtida ou match: elas existem só no controller e no front isolado.

**Corpo:**

| Campo | Obrigatório | Regra |
|-------|-------------|-------|
| `enterpriseId` | sim | Número. Empresa precisa existir (senão 404) |
| `name` | sim | |
| `email` | sim | Formato válido; único entre **todas** as pessoas, sem diferenciar maiúsculas (senão 409 "E-mail já cadastrado") |
| `password` | sim | 6 ou mais caracteres |
| `description` | não | |
| `cpf` | sim | Válido, com ou sem máscara; único entre recrutadores (senão 409 "CPF já cadastrado"). Pode repetir o CPF de um candidato |
| `birthDate` | sim | `AAAA-MM-DD`, não futura, 18 anos ou mais |
| `area` | sim | TI, RH, Marketing, Segurança da Informação, Financeiro, Comercial ou Operações (sem diferenciar maiúsculas; senão 400 "Área de recrutamento inválida") |

```json
{ "enterpriseId": 1, "name": "Bruna Costa", "email": "bruna.costa@exemplo.com", "password": "123456",
  "description": "Recrutadora de tecnologia", "cpf": "418.273.650-80", "birthDate": "1992-05-10", "area": "TI" }
```

**Respostas:**

- `201` com o header `Location: /api/recruiters/{id}` e o corpo `{ "id", "name", "email", "area", "description" }`.
  **Nunca** traz `cpf`, `birthDate`, idade nem `password`, nem para a empresa que cadastrou.
- `400` dados inválidos ou `enterpriseId` ausente/não numérico.
- `404` "Empresa com ID `<id>` não existe".
- `409` "E-mail já cadastrado" ou "CPF já cadastrado".

> **Limitações:** sem autenticação, a rota confia no `enterpriseId` do corpo (como `POST /api/vacancies`). O 409 de
> e-mail revela se o e-mail já pertence a alguma pessoa (limitação aceita; ver `AGENTS.md`, seção 12).

## Arquivos

| Arquivo | Papel |
|---------|-------|
| `ApiMain` | Sobe o Tomcat e liga cada rota ao seu Servlet |
| `api/CandidateServlet`, `EnterpriseServlet`, `VacancyServlet`, `RecruiterServlet` | Recebem o `POST`, montam o DTO e chamam o controller |
| `api/JsonResponse` | Escreve a resposta em JSON e converte exceções em status HTTP |
| `Application` + `controller/Controllers` | Montam os controllers, usados pelo menu e pela API |
| `build.gradle` | Dependências `tomcat-embed-core` e `groovy-json`, e tarefa `runApi` |


## Como executar

Com o banco rodando, suba a API:

```bash
cd Linketinder
./gradlew runApi
```

Em outro terminal, faça uma requisição:

```bash
curl -i -X POST localhost:8080/api/vacancies -H 'Content-Type: application/json' \
  -d '{"enterpriseId":1,"title":"Dev Groovy","description":"APIs REST","city":"Recife","state":"PE","competences":["Groovy"]}'
```

Cadastro de recrutador (empresa 1 do seed):

```bash
curl -i -X POST localhost:8080/api/recruiters -H 'Content-Type: application/json' \
  -d '{"enterpriseId":1,"name":"Bruna Costa","email":"bruna.costa@exemplo.com","password":"123456","description":"Recrutadora de tecnologia","cpf":"418.273.650-80","birthDate":"1992-05-10","area":"TI"}'
```
