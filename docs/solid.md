# Refatoração com SOLID

Nesta etapa, o backend do Linketinder foi refatorado com base nos cinco princípios SOLID. O comportamento da aplicação
não mudou. O que mudou foi a forma como as classes dependem umas das outras. Antes, cada camada dependia de classes
concretas da camada de baixo. Agora as camadas dependem de interfaces recebidas pelo construtor, e só o `Main` decide
quais implementações usar.

## Principais correções

| Princípio | Problema | Correção |
|-----------|----------|----------|
| **S**: responsabilidade única | `DatabaseConnection` lia a configuração e gerenciava conexões, `CompetenceDAO` cuidava das competências e das tabelas de ligação, `View` imprimia na tela e formatava os dados | Cada responsabilidade ganhou uma classe: `DatabaseConfig` e `DatabaseConnection`, `CompetenceDAO` e `CompetenceLinkDAO`, `ConsoleView` e `ResponseFormatter` |
| **O**: aberto/fechado | Os services de candidato, empresa e vaga repetiam o mesmo fluxo. O `MainMenu` recebia os quatro submenus fixos no construtor | `CrudService<T>` concentra o fluxo, e as regras extras entram pelo método `checkBusinessRules`. O `MainMenu` recebe uma lista de menus, e um menu novo entra sem modificar a classe |
| **L**: substituição de Liskov | O `CompetenceService` era diferente dos outros services (`create(String)`, `update(int, String)`) | Ele passou a seguir o mesmo contrato (`create(Competence)`, `update(Competence)`). Todos os repositórios seguem o mesmo contrato (`null` quando não encontram, `false` quando não excluem) e podem ser trocados entre si, inclusive por mocks |
| **I**: segregação de interface | O `CompetenceForm` dependia do service de competências inteiro só para listar. O `VacancyMenu` dependia do service de empresas inteiro só para buscar uma por ID | Interfaces pequenas: `CompetenceCatalog` (só `findAll`) e `EntityFinder<T>` (só `findById`). Cada repositório acrescenta ao CRUD apenas o que sua entidade precisa |
| **D**: inversão de dependência | Os services dependiam dos DAOs concretos e chamavam validadores estáticos. Os DAOs dependiam da classe de conexão. O menu imprimia direto no terminal | Interfaces `CandidateRepository`, `EnterpriseRepository`, `VacancyRepository`, `CompetenceRepository`, `Validator<T>` e `ConnectionManager`, todas injetadas pelo construtor. O `Main` virou o único ponto que liga as implementações concretas |

## Como a refatoração melhorou o código

- **Testes desacoplados:** os testes mockavam DAOs concretos, o que exigia as bibliotecas `byte-buddy` e `objenesis`.
  Agora os testes mockam interfaces, e as duas bibliotecas foram removidas do `build.gradle`.
- **Mais coisas testáveis:** o menu passou a escrever num `PrintStream` injetado, então os testes conferem as mensagens
  exibidas. A configuração do banco é testada sem depender das variáveis de ambiente da máquina. Os testes foram de
  239 para 245, todos passando.
- **Mudanças:** trocar a forma de configurar o banco, o texto exibido ou a tabela de ligação de competências
  afeta uma única classe.
- **Fácil de estender:** um novo service, validador, submenu ou forma de persistência entra criando uma classe nova,
  sem modificar as que já funcionam.