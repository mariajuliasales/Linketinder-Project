# Refatoração com Clean Code


Nesta etapa, o Linketinder (backend, frontend e banco de dados) foi analisado e refatorado
seguindo os princípios do Clean Code: nomes autoexplicativos, regra do escoteiro, funções pequenas, DRY,
comentários só quando necessários, tratamento de erros e testagem.

A refatoração não mudou o que a aplicação faz. O menu do terminal e as telas do frontend continuam com as
mesmas funcionalidades. O que mudou foi a organização do código, que ficou mais fácil de ler, testar e evoluir.


## Resumo em números

| Item | Antes | Depois |
|------|-------|--------|
| Casos de teste do backend | 96 | **239** |
| Especificações de teste (Spock) | 5 | 30 |
| Maior classe do backend (`Menu`) | 392 linhas | dividida em 12 classes (a maior com 101 linhas) |
| `ValidateUtil` | 1 classe com 122 linhas e 7 responsabilidades | 7 classes com uma responsabilidade cada |
| `CandidateDAO` / `EnterpriseDAO` | 123 / 117 linhas, com código JDBC repetido | 78 / 75 linhas, o fluxo comum fica uma vez no `PersonDAO` |
| `CandidateService` | 85 linhas, com 10 `if/throw` seguidos | 43 linhas, validação em classe própria |
| Comentários explicativos | 16 | 2 |
| `main.ts` do frontend | 84 linhas (rotas, navbar e renderização) | 7 linhas |


## O que mudou

| Parte | Principais mudanças |
|-------|---------------------|
| **Backend: `util`** | `ValidateUtil` dividido em 7 classes |
| **Backend: `service`** | Validadores separados, exceções de negócio, nomes de métodos padronizados, mensagens em português |
| **Backend: `model`, `dto`, `mapper`** | Models sem formatação de texto, interface `Person` usada como contrato, DTOs como contrato (sem senha, anônimos sem dados pessoais), um mapper por entidade |
| **Backend: `dao`** | `SqlExecutor`; Template Method no `PersonDAO`, enums no lugar de strings, erros do banco traduzidos |
| **Backend: `menu`** | `Menu` dividido em 12 classes, `MenuOption` no lugar do `switch`, `ConsoleInput` e formulários reutilizáveis |
| **Frontend** | Pastas `data/`, `services/`, `ui/` e `pages/`, serviços separados por assunto, componentes reutilizáveis, seed alinhado ao banco |
| **Banco de dados** | Função para a senha do seed e limpeza do DBML |

Os detalhes de cada parte estão em [backend.md](backend.md), [frontend.md](frontend.md) e [database.md](database.md).

