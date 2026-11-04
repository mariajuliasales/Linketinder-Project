# API REST

## Endpoints

Base: `http://localhost:8080/api`

| Método | Rota | O que faz |
|--------|------|-----------|
| `POST` | `/api/candidates` | Cadastra um candidato |
| `POST` | `/api/enterprises` | Cadastra uma empresa |
| `POST` | `/api/vacancies` | Cria uma vaga para a empresa informada em `enterpriseId` |


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

## Arquivos

| Arquivo | Papel |
|---------|-------|
| `ApiMain` | Sobe o Tomcat e liga cada rota ao seu Servlet |
| `api/CandidateServlet`, `EnterpriseServlet`, `VacancyServlet` | Recebem o `POST`, montam o DTO e chamam o controller |
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