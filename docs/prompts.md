# Prompts usados com agentes de IA

## Parte 1 — Preparação do repositório

### Etapa 1: AGENTS.md
- **Objetivo:** criar o contexto principal para agentes de IA: visão geral, stack, estrutura, como executar e testar,
  banco, domínio, regras de negócio, Princípio do Anonimato, convenções, restrições e limitações conhecidas.
- **Prompt:**
  ```text
  # Tarefa: gerar o AGENTS.md do Linketinder

  Você vai explorar este repositório e criar um arquivo `AGENTS.md` na raiz.
  Ele será o contexto principal de qualquer agente de IA que trabalhe neste
  projeto. Escreva tudo em português do Brasil.

  ## Regras desta tarefa
  - NÃO altere, crie ou apague nenhum arquivo além do `AGENTS.md`.
  - Primeiro explore e depois escreva. Leia a estrutura de pastas, o build
    (Gradle/package.json), os modelos, os services, a persistência, os scripts SQL,
    os testes e o front-end.
  - Não invente. Se algo não estiver claro no código, escreva `⚠️ A CONFIRMAR:`
    e explique a dúvida. Prefiro uma lacuna marcada a uma regra inventada.
  - Para cada regra de negócio que você descrever, cite o arquivo (e o método,
    quando houver) de onde a tirou, no formato `(fonte: caminho/Arquivo.groovy#metodo)`.
  - Seja objetivo: listas e tabelas curtas, sem texto genérico.

  ## Antes de escrever, me mostre um resumo da exploração
  1. Camadas encontradas (banco, back-end Groovy, front-end TypeScript) e se
     elas estão de fato integradas entre si ou se alguma está isolada.
  2. Entidades encontradas e onde estão definidas.
  3. Como a curtida e o match estão implementados hoje.
  4. Que dados de candidato e empresa aparecem em cada tela/listagem.
  5. Comandos de build, execução e teste que você identificou.
  Espere minha confirmação antes de gerar o arquivo.

  ## Estrutura obrigatória do AGENTS.md

  1. **Visão geral**: o que é o Linketinder e qual é o seu objetivo, em poucas linhas.
  2. **Stack**: linguagens, versões, frameworks, banco, bibliotecas de teste.
  3. **Estrutura do repositório**: árvore resumida com o papel de cada pasta/pacote.
  4. **Como executar e testar**: pré-requisitos, subir o banco, rodar o back-end,
     rodar o front-end, rodar os testes. Os comandos devem ser exatos.
  5. **Banco de dados**:
     - onde ficam os scripts SQL e a ordem em que devem ser aplicados
     - como aplicá-los (comando exato)
     - regra: toda mudança de schema vira um NOVO script versionado; scripts já
       existentes nunca são editados.
  6. **Domínio**: as entidades Candidato, Empresa, Vaga, Competência, Curtida e
     Match, com os atributos principais de cada uma, e uma seção com os
     relacionamentos e suas cardinalidades (pode ser um diagrama Mermaid).
  7. **Regras de negócio centrais**:
     - quem pode curtir quem (candidato → vaga? empresa → candidato?)
     - quando exatamente o match acontece
     - o que muda no sistema depois do match.
  8. **🔒 Princípio do Anonimato (RESTRIÇÃO INVIOLÁVEL)**:
     - uma tabela "Dado × Visível antes do match × Visível depois do match"
       para o candidato e para a empresa
     - uma frase explícita do tipo: "Nenhuma feature, endpoint, consulta, DTO,
       log ou tela pode expor dados identificáveis antes do match. Em caso de
       dúvida, o dado NÃO é exibido."
     - lugares onde o anonimato costuma vazar e precisa ser checado: consultas SQL
       com JOIN, serialização de objetos inteiros, toString/logs, respostas da
       API e componentes do front-end.
     - dados que nunca são exibidos para outro usuário, nem depois do match
       (ex.: CPF, senha).
  9. **Convenções de código**: nomes, organização em pacotes, padrão de
     services/DAOs, tratamento de exceções, padrão de testes. Tire isso do código
     que já existe.
  10. **Restrições para agentes**:
      - não quebrar o Princípio do Anonimato
      - não alterar scripts SQL já existentes
      - toda regra nova precisa de testes
      - não adicionar dependências sem justificar
      - mudanças pequenas e revisáveis
  11. **Fluxo de trabalho com IA**:
      - features novas seguem a skill `skills/feature-com-spec/SKILL.md`
      - as specs ficam em `docs/specs/<nome-da-feature>/`
      - toda tarefa atualiza o `CHANGELOG.md` (padrão Keep a Changelog, na seção
        [Não lançado])
      - se esses arquivos ainda não existirem, cite-os mesmo assim, porque serão
        criados nas próximas etapas.

  ## Fora do escopo
  Não descreva nem defina regras da feature Recrutador. Ela será definida em
  uma spec própria.

  ## Ao final
  Liste todos os itens `⚠️ A CONFIRMAR` para eu revisar.
  ```

### Etapa 2: skill feature-com-spec
- **Objetivo:** definir o fluxo que o agente segue em toda feature nova: AGENTS.md, perguntas de esclarecimento,
  spec, plano, implementação uma tarefa por vez e validação, com os modelos de `spec.md` e `plano.md`.
- **Prompt:**
  ```text
  # Tarefa: adaptar a skill feature-com-spec ao Linketinder

  O arquivo `skills/feature-com-spec/SKILL.md` foi trazido do meu Projeto
  Introdutório. Adapte essa skill ao Linketinder.

  ## Regras desta tarefa
  - Altere SOMENTE `skills/feature-com-spec/SKILL.md`. Não crie o CHANGELOG.md nem
    a pasta docs/specs; isso é feito em outra etapa.
  - Mantenha a estrutura, as etapas e o estilo da skill original. Mude só o
    necessário e o que for pedido abaixo.
  - Use o `AGENTS.md` como fonte de verdade para regras do projeto. Não duplique
    conteúdo dele na skill: faça referência às seções.
  - Escreva em português do Brasil.

  ## Antes de editar
  Leia a skill original e o AGENTS.md e me mostre:
  1. as etapas atuais da skill, em resumo
  2. o que você pretende mudar em cada etapa e por quê
  3. o que precisa ser removido por não fazer sentido no Linketinder
  Espere minha aprovação.

  ## Adaptações obrigatórias

  1. **Local das specs:** toda feature fica em `docs/specs/<nome-da-feature>/`
     (nome em kebab-case, ex.: `docs/specs/recrutador/`). Mantenha os arquivos que a
     skill original já gera (spec, plano, tarefas etc.), agora nessa pasta.

  2. **Spec antes do código:**
     - a spec define as regras de negócio, e dúvidas viram `⚠️ A CONFIRMAR`
     - nenhum código é escrito antes de eu aprovar a spec
     - a spec tem uma seção obrigatória "Impacto no Princípio do Anonimato", que
       diz o que fica visível antes e depois do match para cada novo dado ou entidade.

  3. **Plano por camada:** o plano descreve a feature em cada camada (banco,
     back-end Groovy, front-end TypeScript) e registra quais camadas estão
     integradas e quais estão isoladas, com a decisão tomada para cada uma.

  4. **Execução por tarefa:** tarefas pequenas e revisáveis, uma de cada vez. Cada
     tarefa termina com testes passando (`./gradlew test` e, no front, os testes
     quando existirem) e espera minha revisão antes da próxima.

  5. **CHANGELOG em toda tarefa:** cada tarefa concluída adiciona uma entrada no
     `CHANGELOG.md`, na seção `[Não lançado]`, em uma das categorias Adicionado,
     Modificado, Corrigido ou Removido. A entrada é curta e diz o quê e por quê.

  6. **Checklist de validação.** Além dos itens da skill original, inclua:

     Princípio do Anonimato
     - [ ] Dados de terceiros antes do match saem apenas por DTOs `*AnonymousResponse`
           ou tipos anônimos do front, montados campo a campo
     - [ ] Nenhuma entidade é serializada inteira (ex.: `Vacancy.enterprise`)
     - [ ] Consultas com JOIN não levam dados identificáveis até a resposta
     - [ ] Senha, CPF, CNPJ, data de nascimento, idade e endereço completo nunca
           aparecem para outro usuário
     - [ ] Depois do match, só os dados permitidos no AGENTS.md são revelados
     - [ ] Há testes que verificam a AUSÊNCIA de dados sensíveis nas respostas
     - [ ] Logs e mensagens de erro não expõem dados pessoais

     Banco
     - [ ] Mudança de schema em uma migration nova e idempotente em
           `Database/migrations/`
     - [ ] Nenhum script existente foi editado
     - [ ] `linketinder.dbml` atualizado
     - [ ] DER regenerado (manual, ao final da feature)

     Processo
     - [ ] Entrada no CHANGELOG.md
     - [ ] Spec, plano e tarefas atualizados se algo mudou durante a implementação
     - [ ] Limitações novas registradas no AGENTS.md, se houver

  ## Ao final
  Mostre o diff da skill e liste os pontos da skill original que você removeu ou
  alterou de forma significativa.
  ```

### Etapa 3: CHANGELOG.md
- **Objetivo:** criar o `CHANGELOG.md` no padrão Keep a Changelog, com a seção `[Não lançado]` que toda tarefa
  atualiza.
- **Prompt:**
  ```text
  Leia o arquivo AGENTS.md e SKILL.md para saber sobre o projeto.

  # Tarefa: criar o CHANGELOG.md do Linketinder

  Crie um `CHANGELOG.md` na raiz do projeto, seguindo o padrão Keep a Changelog
  (https://keepachangelog.com/pt-BR/1.1.0/).

  ## Regras desta tarefa
  - Crie SOMENTE o `CHANGELOG.md`. Não altere nenhum outro arquivo.
  - Escreva em português do Brasil.
  - Não invente histórico. Tudo o que for registrado sobre o passado precisa vir
    do `git log` ou dos arquivos do repositório.

  ## Formato
  - Cabeçalho curto: o que é o arquivo, que ele segue o Keep a Changelog e que
    toda tarefa concluída adiciona uma entrada em [Não lançado] (conforme a skill
    `skills/feature-com-spec/SKILL.md`).
  - Seção `## [Não lançado]` no topo.
  - Mudanças agrupadas em `### Adicionado`, `### Modificado`, `### Corrigido` e
    `### Removido`. Inclua só as categorias que tiverem itens.
  - Cada entrada é uma linha curta que diz o quê e, quando não for óbvio, por quê.
  - Ordem: o mais recente no topo.

  ## Conteúdo
  1. Em `[Não lançado] › Adicionado`, registre os artefatos da Parte 1:
     - AGENTS.md, com o contexto do projeto para agentes de IA (domínio, regras de
       curtida e match, Princípio do Anonimato, banco)
     - skill `feature-com-spec` adaptada (specs em `docs/specs/<feature>/`, checklist
       de anonimato, CHANGELOG por tarefa)
     - o próprio CHANGELOG.md
  2. Estado anterior do projeto: leia o `git log` e proponha uma seção que resuma o
     que já existia (banco, back-end com console e API, front-end isolado),
     agrupada por tema, e não um item por commit. Use a data do último commit
     (`9f64be3`). Se não houver versões/tags no repositório, proponha um nome para
     essa seção e marque como ⚠️ A CONFIRMAR.
  3. Não registre limitações nem bugs conhecidos aqui; eles ficam no AGENTS.md. O
     CHANGELOG registra apenas mudanças.

  ## Antes de criar
  Mostre o resumo do `git log` que você pretende usar no item 2 e o nome proposto
  para a seção. Espere minha aprovação.
  ```

---

## Parte 2 — Feature Recrutador

### Etapa 4: issue e branch
- **Objetivo:** abrir a issue da feature Recrutador e criar o branch de trabalho.
- **Prompt:**
  ```text
  Vamos começar a Parte 2, Etapa 4 (Issue e branch) da feature Recrutador do Linketinder.

  ## Contexto
  - Leia o `AGENTS.md` inteiro antes de tudo, principalmente: Princípio do Anonimato, Restrições para agentes e Fluxo de trabalho com IA.
  - Nesta etapa NÃO escreva código, spec nem plano. Só a branch e a issue.
  - Use o MCP do GitHub para as operações no GitHub (issue, labels, projeto). Use o git local para a branch.
  - Repositório: mariajuliasales/Linketinder-Project

  ## Tarefa 1 — Atualizar a branch `feat/recruiter`
  A branch `feat/recruiter` já existe, mas está desatualizada: ela não tem o merge do PR #20 (AGENTS.md, skill `feature-com-spec` e CHANGELOG.md), que está na `dev`.
  1. Rode `git fetch origin` e mostre o `git status` e o `git log --oneline -5` da `feat/recruiter` e da `origin/dev`.
  2. Confirme que a `feat/recruiter` não tem commits próprios que a `dev` não tenha (`git log origin/dev..feat/recruiter`).
  3. Se não tiver, atualize com `git merge --ff-only origin/dev`. Se tiver commits próprios ou houver conflito, PARE e me explique antes de fazer qualquer coisa. Não use rebase, reset nem force push.
  4. Confirme que `AGENTS.md`, `CHANGELOG.md` e `skills/feature-com-spec/SKILL.md` estão na branch.
  5. Faça o push só depois que eu aprovar.

  ## Tarefa 2 — Escrever a issue (rascunho)
  Escreva o rascunho da issue em português, com:
  - **Título:** `feat: Recrutador`, ou algo curto e claro
  - **Contexto:** o problema que a feature resolve, em 2 ou 3 frases
  - **Descrição da feature:** o que o Recrutador faz, com base só no enunciado acima. Não invente regras.
  - **Perguntas em aberto:** as perguntas da seção "A feature: o Recrutador", sem respostas. Elas serão decididas na spec (Etapa 5) e algumas validadas com o Tutor.
  - **Restrição:** o Princípio do Anonimato (AGENTS.md › seção 8) continua inviolável. Nenhum dado identificável aparece antes do match.
  - **Entregáveis (checklist):**
    - [ ] Spec em `docs/specs/recrutador/spec.md` (aprovada e validada com o Tutor)
    - [ ] Plano em `docs/specs/recrutador/plano.md`
    - [ ] Implementação incremental (banco → back-end com testes Spock → front-end)
    - [ ] Validação dos critérios de aceite e do anonimato
    - [ ] Pull Request com `Closes #<número desta issue>`
  - **Branch:** `feat/recruiter`

  Mostre o rascunho e PARE para eu revisar. Não crie a issue antes da minha aprovação.

  ## Tarefa 3 — Criar a issue e colocar no kanban (depois da aprovação)
  1. Crie a issue pelo MCP do GitHub, com a label `enhancement` (ou `feature`, se existir). Se a label não existir, me avise em vez de criar.
  2. Atribua a issue a mim (mariajuliasales).
  3. Adicione a issue ao projeto (kanban) do repositório, na coluna "Todo" ou "In Progress". Se o MCP não tiver ferramenta para GitHub Projects, me avise e sugira o comando equivalente com `gh project item-add`. Não tente outros caminhos sozinho.
  4. Me responda com o número e o link da issue. Vou usar esse número no `Closes #N` do PR.

  ## Ao final
  Liste o que foi feito, o que ficou pendente e qualquer ponto em que você precisou parar.
  ```

### Etapa 5: spec
- **Objetivo:** escrever a spec da feature Recrutador (regras de negócio, impacto no anonimato, critérios de aceite,
  impacto no que já existe e fora do escopo) e aprová-la antes de qualquer código.
- **Prompt:**
  ```text
  Vamos para a Parte 2, Etapa 5: a especificação da feature Recrutador.

  ## Como trabalhar
  - Use a skill `skills/feature-com-spec/SKILL.md`. Esta etapa cobre só as Etapas 1, 2 e 3 dela (AGENTS.md → perguntas → spec). Não faça plano nem código.
  - Nome da feature: `recrutador`. Arquivo: `docs/specs/recrutador/spec.md`. Branch: `feat/recruiter`.
  - Issue: #21
  - A descrição da feature e as 5 perguntas do enunciado estão na issue acima. Leia por lá, pelo MCP do GitHub.
  - O `AGENTS.md` (item 8 das Restrições) diz que o Recrutador terá spec própria. As regras dele saem desta spec, não do AGENTS.md. As regras que já existem (anonimato, curtida, match, convenções e banco) continuam valendo.

  ## Passo 1 — Explorar antes de perguntar
  Leia o `AGENTS.md` inteiro e explore o código que a feature toca: `Database/schema.sql`, `seed.sql`, `linketinder.dbml`, os models `Person`/`AbstractPerson`/`Candidate`/`Enterprise`, `PersonDAO`, `PersonType`, os validators, os mappers anônimos, `ApiMain` e, no front, `types.ts`, `services/auth.ts`, `pages/guards.ts` e `data/seed.ts`.

  Me conte, antes das perguntas, o que encontrar que muda a feature. Pelo menos estes pontos:
  - Curtida e match **só existem no banco**. Não há código no back-end nem no front (AGENTS.md › seção 7). A feature precisa implementar curtida/match ou só a parte do recrutador?
  - `enterprise_like` liga empresa → candidato. Como a curtida do recrutador se encaixa nisso sem quebrar as regras da seção 7.2?
  - Como o recrutador entra no modelo `person`/`PersonType` (hoje é candidato **ou** empresa, com CHECK e FK composta).
  - Login e autenticação: o back-end não tem login, e o front guarda a senha em texto (seção 12).

  ## Passo 2 — Perguntas (pare depois)
  Siga a Etapa 2 da skill: de 5 a 10 perguntas numeradas, cada uma com uma **sugestão de resposta** e o motivo. Elas precisam cobrir obrigatoriamente as 5 perguntas do enunciado:
  1. **Vínculo:** como o recrutador se associa à empresa; quem cadastra e remove; o que acontece com as curtidas dele se for removido.
  2. **Curtidas e match:** a curtida do recrutador conta como curtida da empresa? Dois recrutadores da mesma empresa curtindo o mesmo candidato; um deles remove a curtida; o candidato curtiu vagas de áreas diferentes.
  3. **Escopo de atuação:** todas as vagas da empresa ou só as da área dele; se pode criar ou editar vagas; se a vaga passa a ter área.
  4. **Anonimato:** o que o recrutador vê do candidato antes e depois do match; se o candidato vê algum dado do recrutador depois do match; o que nunca é exibido.
  5. **Validações:** e-mail (único entre todas as pessoas?), CPF (único? pode repetir o de um candidato?), data de nascimento (idade mínima?), senha, e se a área é lista fixa ou texto livre.

  Use as outras perguntas para: escopo das camadas (banco, back-end e front isolado), migration, endpoints da API, menu do console, dependências e o que fica fora do escopo.

  Marque com **(resposta obrigatória)** as perguntas de anonimato e de regra de negócio nova. Explique como trato as respostas, como manda a skill. Depois **pare e espere minhas respostas**.

  ## Passo 3 — Spec (depois das minhas respostas)
  Escreva `docs/specs/recrutador/spec.md` com o modelo da seção 5 da skill, mais estas seções obrigatórias do enunciado:

  - **Modelo de dados:** entidades novas, campos (tipo, obrigatório, único), relacionamentos e cardinalidade, um diagrama mermaid `erDiagram` com o que muda e a migration prevista (`Database/migrations/NNN_...sql`, idempotente, sem editar scripts existentes).
  - **Anonimato:** seção dedicada, com uma tabela **por perfil** dizendo o que cada um enxerga **antes** e **depois** do match:
    - recrutador vendo candidato;
    - candidato vendo recrutador (e vaga/empresa);
    - empresa vendo os próprios recrutadores;
    - recrutador vendo outros recrutadores;
    - dados do recrutador que **nunca** são exibidos.

    Inclua também os pontos de vazamento da seção 8.5 do AGENTS.md que a feature toca e os critérios de aceite que verificam a **ausência** de dados.
  - **Decisões tomadas:** uma tabela com as 5 perguntas do enunciado → decisão → justificativa → **Discutida com o Tutor? (sim/não)**. Deixe a coluna do Tutor como ⚠️ A CONFIRMAR até eu informar.

  Também é obrigatório: contexto e objetivo, regras de negócio com casos de borda, critérios de aceite numerados (CA01…) no formato Dado/Quando/Então, impacto no que já existe, fora do escopo, checklist da feature (AGENTS.md › seção 11) e a lista "Pendências para validar com o Tutor".

  Status: **aguardando aprovação**. Depois de escrever, **pare** e me mostre:
  - o resumo das decisões;
  - as pendências para o Tutor;
  - os bugs ou limitações encontrados.

  ## Passo 4 — Aprovação e commit (só quando eu pedir)
  Depois que eu validar com o Tutor e aprovar:
  1. Mude o status para **aprovada**, troque cada ⚠️ A CONFIRMAR pela decisão e preencha a coluna "Discutida com o Tutor".
  2. Adicione a entrada no `CHANGELOG.md` em `[Não lançado] › Adicionado`.
  3. Faça o commit com `docs(recrutador): add approved spec for recruiter feature` e `Refs #<NÚMERO DA ISSUE>` no corpo. Commite só a spec e o CHANGELOG.
  4. Não faça push sem eu pedir.
  ```

### Etapa 6: plano
- **Objetivo:** quebrar a spec em tarefas pequenas, na ordem banco → back-end com testes → front-end → validação
  final, cada uma com arquivos, critérios de aceite, validação, anonimato, dependências e commit sugerido.
- **Prompt:**
  ```text
  Vamos para a Parte 2, Etapa 6: o plano de implementação da feature Recrutador.

  ## Como trabalhar
  - Use a skill `skills/feature-com-spec/SKILL.md`, só na etapa de plano. Não escreva código nesta etapa.
  - Fonte da verdade: `docs/specs/recrutador/spec.md` (status: aprovada). Não invente regra que não esteja na spec. Se faltar algo para planejar, PARE e me pergunte. Não preencha a lacuna sozinho.
  - Leia também o `AGENTS.md` (convenções, banco, anonimato na seção 8 e checklist da seção 11).
  - Arquivo: `docs/specs/recrutador/plano.md`. Branch: `feat/recruiter`. Issue: #21.

  ## Passo 1 — Conferir antes de planejar
  Antes do plano, me mostre em poucas linhas:
  - o que já existe no código e o que precisa ser criado do zero (curtida e match ainda não existem no back-end);
  - quais bugs/limitações B1–B17 da spec afetam a ordem das tarefas;
  - dúvidas que a spec não responde. Se houver alguma, PARE aqui.

  ## Passo 2 — Escrever o plano
  Ordem das fases: **banco de dados → back-end com testes → front-end → validação final**.

  Cada tarefa deve ser pequena (um commit, revisável em poucos minutos) e seguir este formato:

  ### T01 — <título curto>
  - **Camada:** banco | back-end | front-end
  - **O que será feito:** 2 a 4 linhas
  - **Arquivos envolvidos:** caminhos reais (criar/alterar)
  - **Critérios de aceite cobertos:** CAxx da spec
  - **Como validar:** comando ou passo concreto (ex.: `psql ... -c "\d recruiter"`, `./gradlew test --tests '*RecruiterValidatorSpec'`, fluxo manual na tela X)
  - **Anonimato:** o que verificar nesta tarefa, ou "não se aplica"
  - **Depende de:** Txx
  - **Commit sugerido:** `tipo(recrutador): ...`
  - [ ] Concluída

  Regras do plano:
  - **Banco:** migrations novas em `Database/migrations/NNN_...sql`, idempotentes, sem editar scripts existentes. O `ALTER TYPE ... ADD VALUE` fica em migration própria, fora de transação. Inclua uma tarefa para atualizar `seed.sql` e `linketinder.dbml` e trate o risco de reset pelo `schema.sql`.
  - **Back-end:** testes Spock em cada tarefa de regra de negócio (validações, vínculo, curtida, match, autocurtida, vaga OPEN). O teste entra na mesma tarefa da regra, não numa tarefa separada no fim. Os mappers anônimos e os endpoints ganham testes que verificam a **ausência** de dados identificáveis antes do match.
  - **Front-end:** `types.ts`, `services/auth.ts`, `pages/guards.ts`, `data/seed.ts` e as telas novas, uma tela por tarefa.
  - **Última fase:** conferir os CAs um a um, regressão dos fluxos antigos (cadastro de candidato e empresa, vagas, curtidas, match) e varredura de anonimato em todas as telas e respostas da API.
  - Cada tarefa inclui atualizar o `CHANGELOG.md` e marcar o checkbox no plano.

  No fim do arquivo, inclua:
  - uma tabela **CA → tarefas** mostrando que todo CA da spec está coberto;
  - os riscos e a ordem de mitigação.

  Status do plano: **aguardando aprovação**.

  ## Passo 3 — Parar para revisão
  Depois de escrever, PARE e me mostre:
  - a lista de tarefas (número, título, camada);
  - os CAs sem cobertura, se houver;
  - tarefas que você acha grandes demais para um commit.

  ## Passo 4 — Commit (só quando eu aprovar)
  1. Mude o status para **aprovado**.
  2. No `CHANGELOG.md`, em [Não lançado] › Adicionado: "Plano de implementação da feature Recrutador (`docs/specs/recrutador/plano.md`)."
  3. Commit só do plano e do CHANGELOG: `docs(recrutador): add implementation plan for recruiter feature`, com `Refs #21` no corpo.
  4. Não faça push. Me mostre o `git show --stat`.
  ```

### Etapa 7: implementação

#### Fase 1 — Banco de dados

### Tarefa T01: Pasta de migrations e valor `RECRUITER` no ENUM
- **Objetivo:** criar `Database/migrations/` e a primeira migration, só com o novo valor `RECRUITER` em
  `person_type`, provando que o laço de migrations pode ser repetido.
- **Prompt:**
  ```text
  Implemente a tarefa T01 do plano docs/specs/recrutador/plano.md (issue #21, branch feat/recruiter).

  Antes de codar:
  1. Leia AGENTS.md, docs/specs/recrutador/spec.md, o plano inteiro e a skill do projeto.
  2. Confira se todas as tarefas em "Depende de" de T01 estão marcadas como concluídas. Se alguma não estiver, pare e me avise.
  3. Leia os arquivos existentes que a tarefa toca ou imita (ex.: CompetenceLinkDAO, PersonDAO, specs Spock vizinhas, services do front) e siga os mesmos padrões.

  Regras:
  - Altere SOMENTE os arquivos listados em "Arquivos envolvidos" de T01. Se precisar de outro arquivo, pare e me explique o motivo antes.
  - NÃO altere ainda o CHANGELOG.md nem o checkbox da tarefa: isso fica para depois da minha revisão.
  - Não toque em Database/schema.sql, seed.sql, queries.sql, main/view/, nem no comportamento de PersonDAO, PersonValidator, CandidateDAO e EnterpriseDAO.
  - Regra de negócio no back-end vem com o teste Spock na mesma tarefa.
  - Se a spec estiver incompleta, ambígua ou em conflito com o plano, PARE. Não invente a regra: descreva a lacuna e proponha o texto de atualização da spec. Só volto ao código depois de aprovar a spec.
  - Não faça commit.

  Depois de implementar:
  - Rode tudo o que está em "Como validar" de T01 que você consegue executar, mais `./gradlew test` (e `npm run build` se a tarefa for de front).
  - O que depende de mim (navegador, DevTools, dbdiagram.io, DER) fica num roteiro passo a passo para eu executar.

  Responda com:
  1. Resumo do que mudou, arquivo por arquivo.
  2. Comandos rodados e o resultado de cada um (passou/falhou, com o trecho relevante da saída).
  3. Checagem do anonimato conforme o item "Anonimato" de T01: o que você conferiu e como (ex.: nenhum SELECT com password_hash, DTO sem cpf/birthDate, nada de id em data-*/aria-*, nenhum println/log novo).
  4. Critérios de aceite cobertos (CAxx) e qualquer cobertura parcial.
  5. Verificações manuais pendentes para mim.
  6. Dúvidas ou desvios em relação ao plano.
  ```

### Tarefa T02: AGENTS.md: reset do banco e Recrutador no escopo
- **Objetivo:** documentar o reset oficial do banco (`dropdb` + `createdb` + scripts base + migrations) e trocar
  "fora do escopo" pela referência à spec aprovada.
- **Prompt:**
  ```text
  Implemente a tarefa T02 do plano docs/specs/recrutador/plano.md (issue #21, branch feat/recruiter).

  Antes de codar:
  1. Leia AGENTS.md, docs/specs/recrutador/spec.md, o plano inteiro e a skill do projeto.
  2. Confira se todas as tarefas em "Depende de" de T02 estão marcadas como concluídas. Se alguma não estiver, pare e me avise.
  3. Leia os arquivos existentes que a tarefa toca ou imita (ex.: CompetenceLinkDAO, PersonDAO, specs Spock vizinhas, services do front) e siga os mesmos padrões.

  Regras:
  - Altere SOMENTE os arquivos listados em "Arquivos envolvidos" de T02. Se precisar de outro arquivo, pare e me explique o motivo antes.
  - NÃO altere ainda o CHANGELOG.md nem o checkbox da tarefa: isso fica para depois da minha revisão.
  - Não toque em Database/schema.sql, seed.sql, queries.sql, main/view/, nem no comportamento de PersonDAO, PersonValidator, CandidateDAO e EnterpriseDAO.
  - Regra de negócio no back-end vem com o teste Spock na mesma tarefa.
  - Se a spec estiver incompleta, ambígua ou em conflito com o plano, PARE. Não invente a regra: descreva a lacuna e proponha o texto de atualização da spec. Só volto ao código depois de aprovar a spec.
  - Não faça commit.

  Depois de implementar:
  - Rode tudo o que está em "Como validar" de T02 que você consegue executar, mais `./gradlew test` (e `npm run build` se a tarefa for de front).
  - O que depende de mim (navegador, DevTools, dbdiagram.io, DER) fica num roteiro passo a passo para eu executar.

  Responda com:
  1. Resumo do que mudou, arquivo por arquivo.
  2. Comandos rodados e o resultado de cada um (passou/falhou, com o trecho relevante da saída).
  3. Checagem do anonimato conforme o item "Anonimato" de T02: o que você conferiu e como (ex.: nenhum SELECT com password_hash, DTO sem cpf/birthDate, nada de id em data-*/aria-*, nenhum println/log novo).
  4. Critérios de aceite cobertos (CAxx) e qualquer cobertura parcial.
  5. Verificações manuais pendentes para mim.
  6. Dúvidas ou desvios em relação ao plano.
  ```

### Tarefa T03: Migration de recrutador, área e curtida do recrutador
- **Objetivo:** criar `recruitment_area` (7 áreas), tornar `person.address_id` opcional com o
  `person_address_required_check`, e criar `recruiter` e `recruiter_like`, tudo idempotente.
- **Prompt:**
  ```text
  Implemente a tarefa T03 do plano docs/specs/recrutador/plano.md (issue #21, branch feat/recruiter).

  Antes de codar:
  1. Leia AGENTS.md, docs/specs/recrutador/spec.md, o plano inteiro e a skill do projeto.
  2. Confira se todas as tarefas em "Depende de" de T03 estão marcadas como concluídas. Se alguma não estiver, pare e me avise.
  3. Leia os arquivos existentes que a tarefa toca ou imita (ex.: CompetenceLinkDAO, PersonDAO, specs Spock vizinhas, services do front) e siga os mesmos padrões.

  Regras:
  - Altere SOMENTE os arquivos listados em "Arquivos envolvidos" de T03. Se precisar de outro arquivo, pare e me explique o motivo antes.
  - NÃO altere ainda o CHANGELOG.md nem o checkbox da tarefa: isso fica para depois da minha revisão.
  - Não toque em Database/schema.sql, seed.sql, queries.sql, main/view/, nem no comportamento de PersonDAO, PersonValidator, CandidateDAO e EnterpriseDAO.
  - Regra de negócio no back-end vem com o teste Spock na mesma tarefa.
  - Se a spec estiver incompleta, ambígua ou em conflito com o plano, PARE. Não invente a regra: descreva a lacuna e proponha o texto de atualização da spec. Só volto ao código depois de aprovar a spec.
  - Não faça commit.

  Depois de implementar:
  - Rode tudo o que está em "Como validar" de T03 que você consegue executar, mais `./gradlew test` (e `npm run build` se a tarefa for de front).
  - O que depende de mim (navegador, DevTools, dbdiagram.io, DER) fica num roteiro passo a passo para eu executar.

  Responda com:
  1. Resumo do que mudou, arquivo por arquivo.
  2. Comandos rodados e o resultado de cada um (passou/falhou, com o trecho relevante da saída).
  3. Checagem do anonimato conforme o item "Anonimato" de T03: o que você conferiu e como (ex.: nenhum SELECT com password_hash, DTO sem cpf/birthDate, nada de id em data-*/aria-*, nenhum println/log novo).
  4. Critérios de aceite cobertos (CAxx) e qualquer cobertura parcial.
  5. Verificações manuais pendentes para mim.
  6. Dúvidas ou desvios em relação ao plano.
  ```

### Tarefa T04: Recrutadores de exemplo e backfill das curtidas
- **Objetivo:** inserir os 5 recrutadores de exemplo e criar a `recruiter_like` para cada `enterprise_like` órfã.
- **Prompt:**
  ```text
  Implemente a tarefa T04 do plano docs/specs/recrutador/plano.md (issue #21, branch feat/recruiter).

  Antes de codar:
  1. Leia AGENTS.md, docs/specs/recrutador/spec.md, o plano inteiro e a skill do projeto.
  2. Confira se todas as tarefas em "Depende de" de T04 estão marcadas como concluídas. Se alguma não estiver, pare e me avise.
  3. Leia os arquivos existentes que a tarefa toca ou imita (ex.: CompetenceLinkDAO, PersonDAO, specs Spock vizinhas, services do front) e siga os mesmos padrões.

  Regras:
  - Altere SOMENTE os arquivos listados em "Arquivos envolvidos" de T04. Se precisar de outro arquivo, pare e me explique o motivo antes.
  - NÃO altere ainda o CHANGELOG.md nem o checkbox da tarefa: isso fica para depois da minha revisão.
  - Não toque em Database/schema.sql, seed.sql, queries.sql, main/view/, nem no comportamento de PersonDAO, PersonValidator, CandidateDAO e EnterpriseDAO.
  - Regra de negócio no back-end vem com o teste Spock na mesma tarefa.
  - Se a spec estiver incompleta, ambígua ou em conflito com o plano, PARE. Não invente a regra: descreva a lacuna e proponha o texto de atualização da spec. Só volto ao código depois de aprovar a spec.
  - Não faça commit.

  Depois de implementar:
  - Rode tudo o que está em "Como validar" de T04 que você consegue executar, mais `./gradlew test` (e `npm run build` se a tarefa for de front).
  - O que depende de mim (navegador, DevTools, dbdiagram.io, DER) fica num roteiro passo a passo para eu executar.

  Responda com:
  1. Resumo do que mudou, arquivo por arquivo.
  2. Comandos rodados e o resultado de cada um (passou/falhou, com o trecho relevante da saída).
  3. Checagem do anonimato conforme o item "Anonimato" de T04: o que você conferiu e como (ex.: nenhum SELECT com password_hash, DTO sem cpf/birthDate, nada de id em data-*/aria-*, nenhum println/log novo).
  4. Critérios de aceite cobertos (CAxx) e qualquer cobertura parcial.
  5. Verificações manuais pendentes para mim.
  6. Dúvidas ou desvios em relação ao plano.
  ```

### Tarefa T05: Modelo DBML atualizado
- **Objetivo:** refletir no DBML o valor `RECRUITER`, o `address_id` opcional, as tabelas novas e a nota da
  invariante `enterprise_like` ⇔ `recruiter_like`.
- **Prompt:**
  ```text
  Implemente a tarefa T05 do plano docs/specs/recrutador/plano.md (issue #21, branch feat/recruiter).

  Antes de codar:
  1. Leia AGENTS.md, docs/specs/recrutador/spec.md, o plano inteiro e a skill do projeto.
  2. Confira se todas as tarefas em "Depende de" de T05 estão marcadas como concluídas. Se alguma não estiver, pare e me avise.
  3. Leia os arquivos existentes que a tarefa toca ou imita (ex.: CompetenceLinkDAO, PersonDAO, specs Spock vizinhas, services do front) e siga os mesmos padrões.

  Regras:
  - Altere SOMENTE os arquivos listados em "Arquivos envolvidos" de T05. Se precisar de outro arquivo, pare e me explique o motivo antes.
  - NÃO altere ainda o CHANGELOG.md nem o checkbox da tarefa: isso fica para depois da minha revisão.
  - Não toque em Database/schema.sql, seed.sql, queries.sql, main/view/, nem no comportamento de PersonDAO, PersonValidator, CandidateDAO e EnterpriseDAO.
  - Regra de negócio no back-end vem com o teste Spock na mesma tarefa.
  - Se a spec estiver incompleta, ambígua ou em conflito com o plano, PARE. Não invente a regra: descreva a lacuna e proponha o texto de atualização da spec. Só volto ao código depois de aprovar a spec.
  - Não faça commit.

  Depois de implementar:
  - Rode tudo o que está em "Como validar" de T05 que você consegue executar, mais `./gradlew test` (e `npm run build` se a tarefa for de front).
  - O que depende de mim (navegador, DevTools, dbdiagram.io, DER) fica num roteiro passo a passo para eu executar.

  Responda com:
  1. Resumo do que mudou, arquivo por arquivo.
  2. Comandos rodados e o resultado de cada um (passou/falhou, com o trecho relevante da saída).
  3. Checagem do anonimato conforme o item "Anonimato" de T05: o que você conferiu e como (ex.: nenhum SELECT com password_hash, DTO sem cpf/birthDate, nada de id em data-*/aria-*, nenhum println/log novo).
  4. Critérios de aceite cobertos (CAxx) e qualquer cobertura parcial.
  5. Verificações manuais pendentes para mim.
  6. Dúvidas ou desvios em relação ao plano.
  ```

#### Fase 2 — Back-end com testes

### Tarefa T06: Modelo do recrutador e área de recrutamento
- **Objetivo:** criar o modelo `Recruiter`, o enum `RecruitmentArea` e o valor `RECRUITER` em `PersonType`.
- **Prompt:**
  ```text
  Implemente a tarefa T06 do plano docs/specs/recrutador/plano.md (issue #21, branch feat/recruiter).

  Antes de codar:
  1. Leia AGENTS.md, docs/specs/recrutador/spec.md, o plano inteiro e a skill do projeto.
  2. Confira se todas as tarefas em "Depende de" de T06 estão marcadas como concluídas. Se alguma não estiver, pare e me avise.
  3. Leia os arquivos existentes que a tarefa toca ou imita (ex.: CompetenceLinkDAO, PersonDAO, specs Spock vizinhas, services do front) e siga os mesmos padrões.

  Regras:
  - Altere SOMENTE os arquivos listados em "Arquivos envolvidos" de T06. Se precisar de outro arquivo, pare e me explique o motivo antes.
  - NÃO altere ainda o CHANGELOG.md nem o checkbox da tarefa: isso fica para depois da minha revisão.
  - Não toque em Database/schema.sql, seed.sql, queries.sql, main/view/, nem no comportamento de PersonDAO, PersonValidator, CandidateDAO e EnterpriseDAO.
  - Regra de negócio no back-end vem com o teste Spock na mesma tarefa.
  - Se a spec estiver incompleta, ambígua ou em conflito com o plano, PARE. Não invente a regra: descreva a lacuna e proponha o texto de atualização da spec. Só volto ao código depois de aprovar a spec.
  - Não faça commit.

  Depois de implementar:
  - Rode tudo o que está em "Como validar" de T06 que você consegue executar, mais `./gradlew test` (e `npm run build` se a tarefa for de front).
  - O que depende de mim (navegador, DevTools, dbdiagram.io, DER) fica num roteiro passo a passo para eu executar.

  Responda com:
  1. Resumo do que mudou, arquivo por arquivo.
  2. Comandos rodados e o resultado de cada um (passou/falhou, com o trecho relevante da saída).
  3. Checagem do anonimato conforme o item "Anonimato" de T06: o que você conferiu e como (ex.: nenhum SELECT com password_hash, DTO sem cpf/birthDate, nada de id em data-*/aria-*, nenhum println/log novo).
  4. Critérios de aceite cobertos (CAxx) e qualquer cobertura parcial.
  5. Verificações manuais pendentes para mim.
  6. Dúvidas ou desvios em relação ao plano.
  ```

### Tarefa T07: Validação do recrutador
- **Objetivo:** criar o `RecruiterValidator` com as regras de campos da spec.
- **Prompt:**
  ```text
  Implemente a tarefa T07 do plano docs/specs/recrutador/plano.md (issue #21, branch feat/recruiter).

  Antes de codar:
  1. Leia AGENTS.md, docs/specs/recrutador/spec.md, o plano inteiro e a skill do projeto.
  2. Confira se todas as tarefas em "Depende de" de T07 estão marcadas como concluídas. Se alguma não estiver, pare e me avise.
  3. Leia os arquivos existentes que a tarefa toca ou imita (ex.: CompetenceLinkDAO, PersonDAO, specs Spock vizinhas, services do front) e siga os mesmos padrões.

  Regras:
  - Altere SOMENTE os arquivos listados em "Arquivos envolvidos" de T07. Se precisar de outro arquivo, pare e me explique o motivo antes.
  - NÃO altere ainda o CHANGELOG.md nem o checkbox da tarefa: isso fica para depois da minha revisão.
  - Não toque em Database/schema.sql, seed.sql, queries.sql, main/view/, nem no comportamento de PersonDAO, PersonValidator, CandidateDAO e EnterpriseDAO.
  - Regra de negócio no back-end vem com o teste Spock na mesma tarefa.
  - Se a spec estiver incompleta, ambígua ou em conflito com o plano, PARE. Não invente a regra: descreva a lacuna e proponha o texto de atualização da spec. Só volto ao código depois de aprovar a spec.
  - Não faça commit.

  Depois de implementar:
  - Rode tudo o que está em "Como validar" de T07 que você consegue executar, mais `./gradlew test` (e `npm run build` se a tarefa for de front).
  - O que depende de mim (navegador, DevTools, dbdiagram.io, DER) fica num roteiro passo a passo para eu executar.

  Responda com:
  1. Resumo do que mudou, arquivo por arquivo.
  2. Comandos rodados e o resultado de cada um (passou/falhou, com o trecho relevante da saída).
  3. Checagem do anonimato conforme o item "Anonimato" de T07: o que você conferiu e como (ex.: nenhum SELECT com password_hash, DTO sem cpf/birthDate, nada de id em data-*/aria-*, nenhum println/log novo).
  4. Critérios de aceite cobertos (CAxx) e qualquer cobertura parcial.
  5. Verificações manuais pendentes para mim.
  6. Dúvidas ou desvios em relação ao plano.
  ```

### Tarefa T08: Persistência do recrutador
- **Objetivo:** criar o repositório e o DAO JDBC do recrutador e traduzir o `recruiter_cpf_key` no
  `SqlExceptionTranslator`.
- **Prompt:**
  ```text
  Implemente a tarefa T08 do plano docs/specs/recrutador/plano.md (issue #21, branch feat/recruiter).

  Antes de codar:
  1. Leia AGENTS.md, docs/specs/recrutador/spec.md, o plano inteiro e a skill do projeto.
  2. Confira se todas as tarefas em "Depende de" de T08 estão marcadas como concluídas. Se alguma não estiver, pare e me avise.
  3. Leia os arquivos existentes que a tarefa toca ou imita (ex.: CompetenceLinkDAO, PersonDAO, specs Spock vizinhas, services do front) e siga os mesmos padrões.

  Regras:
  - Altere SOMENTE os arquivos listados em "Arquivos envolvidos" de T08. Se precisar de outro arquivo, pare e me explique o motivo antes.
  - NÃO altere ainda o CHANGELOG.md nem o checkbox da tarefa: isso fica para depois da minha revisão.
  - Não toque em Database/schema.sql, seed.sql, queries.sql, main/view/, nem no comportamento de PersonDAO, PersonValidator, CandidateDAO e EnterpriseDAO.
  - Regra de negócio no back-end vem com o teste Spock na mesma tarefa.
  - Se a spec estiver incompleta, ambígua ou em conflito com o plano, PARE. Não invente a regra: descreva a lacuna e proponha o texto de atualização da spec. Só volto ao código depois de aprovar a spec.
  - Não faça commit.

  Depois de implementar:
  - Rode tudo o que está em "Como validar" de T08 que você consegue executar, mais `./gradlew test` (e `npm run build` se a tarefa for de front).
  - O que depende de mim (navegador, DevTools, dbdiagram.io, DER) fica num roteiro passo a passo para eu executar.

  Responda com:
  1. Resumo do que mudou, arquivo por arquivo.
  2. Comandos rodados e o resultado de cada um (passou/falhou, com o trecho relevante da saída).
  3. Checagem do anonimato conforme o item "Anonimato" de T08: o que você conferiu e como (ex.: nenhum SELECT com password_hash, DTO sem cpf/birthDate, nada de id em data-*/aria-*, nenhum println/log novo).
  4. Critérios de aceite cobertos (CAxx) e qualquer cobertura parcial.
  5. Verificações manuais pendentes para mim.
  6. Dúvidas ou desvios em relação ao plano.
  ```

### Tarefa T09: Serviço do recrutador: cadastro e edição
- **Objetivo:** criar o `RecruiterService` com o cadastro pela empresa e a edição do próprio perfil (descrição e
  senha).
- **Prompt:**
  ```text
  Implemente a tarefa T09 do plano docs/specs/recrutador/plano.md (issue #21, branch feat/recruiter).

  Antes de codar:
  1. Leia AGENTS.md, docs/specs/recrutador/spec.md, o plano inteiro e a skill do projeto.
  2. Confira se todas as tarefas em "Depende de" de T09 estão marcadas como concluídas. Se alguma não estiver, pare e me avise.
  3. Leia os arquivos existentes que a tarefa toca ou imita (ex.: CompetenceLinkDAO, PersonDAO, specs Spock vizinhas, services do front) e siga os mesmos padrões.

  Regras:
  - Altere SOMENTE os arquivos listados em "Arquivos envolvidos" de T09. Se precisar de outro arquivo, pare e me explique o motivo antes.
  - NÃO altere ainda o CHANGELOG.md nem o checkbox da tarefa: isso fica para depois da minha revisão.
  - Não toque em Database/schema.sql, seed.sql, queries.sql, main/view/, nem no comportamento de PersonDAO, PersonValidator, CandidateDAO e EnterpriseDAO.
  - Regra de negócio no back-end vem com o teste Spock na mesma tarefa.
  - Se a spec estiver incompleta, ambígua ou em conflito com o plano, PARE. Não invente a regra: descreva a lacuna e proponha o texto de atualização da spec. Só volto ao código depois de aprovar a spec.
  - Não faça commit.

  Depois de implementar:
  - Rode tudo o que está em "Como validar" de T09 que você consegue executar, mais `./gradlew test` (e `npm run build` se a tarefa for de front).
  - O que depende de mim (navegador, DevTools, dbdiagram.io, DER) fica num roteiro passo a passo para eu executar.

  Responda com:
  1. Resumo do que mudou, arquivo por arquivo.
  2. Comandos rodados e o resultado de cada um (passou/falhou, com o trecho relevante da saída).
  3. Checagem do anonimato conforme o item "Anonimato" de T09: o que você conferiu e como (ex.: nenhum SELECT com password_hash, DTO sem cpf/birthDate, nada de id em data-*/aria-*, nenhum println/log novo).
  4. Critérios de aceite cobertos (CAxx) e qualquer cobertura parcial.
  5. Verificações manuais pendentes para mim.
  6. Dúvidas ou desvios em relação ao plano.
  ```

### Tarefa T10: Repositório de curtidas
- **Objetivo:** criar a interface `LikeRepository` e o `LikeDAO`, com métodos que recebem a `Connection`.
- **Prompt:**
  ```text
  Implemente a T10 de docs/specs/recrutador/plano.md.

  Antes: leia AGENTS.md, a spec, o plano e a skill do projeto. Confira se as dependências da T10 estão concluídas. Siga os padrões dos arquivos vizinhos.

  Siga as "Regras para todas as tarefas" do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test`. O que for manual vira um roteiro para mim.

  Responda com: 1) mudanças por arquivo; 2) comandos e resultados; 3) checagem do anonimato; 4) CAs cobertos ou parciais; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T11: Repositório de match
- **Objetivo:** criar a interface `MatchRepository` e o `MatchDAO`, com métodos que recebem a `Connection`.
- **Prompt:**
  ```text
  Faça a tarefa T11
  ```

### Tarefa T12: Curtida do candidato
- **Objetivo:** criar o `CandidateLikeService` (`like`/`unlike`), com curtida só em vaga `OPEN` e match quando a
  empresa já curtiu o candidato.
- **Prompt:**
  ```text
  Implemente a T12 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T12 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T12.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T13: Curtida do recrutador
- **Objetivo:** criar `RecruiterLikeService.like`, numa transação, mantendo a invariante `enterprise_like` ⇔
  `recruiter_like` e criando os matches com as vagas `OPEN` curtidas pelo candidato.
- **Prompt:**
  ```text
  Implemente a T13 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T13 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T13.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T14: Descurtida do recrutador
- **Objetivo:** criar `RecruiterLikeService.unlike`: quando o último recrutador da empresa descurte, apagar o
  `enterprise_like` e os matches do candidato com as vagas da empresa.
- **Prompt:**
  ```text
  Implemente a T14 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T14 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T14.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T15: Remoção do recrutador pela empresa
- **Objetivo:** criar `RecruiterService.removeByEnterprise`, que remove o recrutador e recalcula as curtidas da
  empresa na mesma transação.
- **Prompt:**
  ```text
  Implemente a T15 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T15 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T15.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T16: DTOs e mapper do recrutador
- **Objetivo:** criar `RecruiterRequest`, `RecruiterResponse` (sem CPF, nascimento, idade e senha) e o
  `RecruiterMapper`.
- **Prompt:**
  ```text
  Implemente a T16 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T16 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T16.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T17: DTOs e mapper de revelação
- **Objetivo:** criar os DTOs que revelam dados só depois do match e o `MatchMapper`.
- **Prompt:**
  ```text
  Implemente a T17 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T17 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T17.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T18: Controller do recrutador e rota `POST /api/recruiters`
- **Objetivo:** criar o `RecruiterController`, o `RecruiterServlet` e registrar a rota `POST /api/recruiters`.
- **Prompt:**
  ```text
  Implemente a T18 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T18 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T18.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T19: Curtidas e visão de candidatos para recrutador e empresa
- **Objetivo:** criar o `LikeController` e o `MatchQueryService`, com a lista de candidatos que revela nome e e-mail só
  depois do match.
- **Prompt:**
  ```text
  Implemente a T19 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T19 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T19.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T20: "Minhas curtidas" do candidato no back-end
- **Objetivo:** listar as vagas curtidas pelo candidato em qualquer status, revelando nome e e-mail da empresa só na
  vaga com match.
- **Prompt:**
  ```text
  Implemente a T20 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T20 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T20.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T21: Vagas da empresa para o recrutador (somente leitura)
- **Objetivo:** permitir que o recrutador liste as vagas da própria empresa, em todos os status, sem editar.
- **Prompt:**
  ```text
  Implemente a T21 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T21 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T21.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

#### Fase 3 — Front-end

### Tarefa T22: Tipos e rotas do recrutador
- **Objetivo:** acrescentar os tipos do recrutador e das curtidas no front e as rotas e a navegação do recrutador.
- **Prompt:**
  ```text
  Implemente a T22 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T22 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T22.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T23: Armazenamento e seed do front
- **Objetivo:** criar as coleções de recrutadores, curtidas e matches no `localStorage` e alinhar o seed do front às
  migrations.
- **Prompt:**
  ```text
  Implemente a T23 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T23 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T23.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T24: Login e guard do recrutador
- **Objetivo:** permitir o login do recrutador e proteger as telas dele com um guard.
- **Prompt:**
  ```text
  Implemente a T24 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T24 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T24.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T25: Exclusão de contas em cascata no front
- **Objetivo:** espelhar no front o `ON DELETE CASCADE` do banco ao excluir contas (recrutadores, curtidas e matches).
- **Prompt:**
  ```text
  Implemente a T25 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T25 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T25.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T26: Regras de curtida e match no front
- **Objetivo:** reimplementar no front as regras de curtida do candidato e do recrutador e a criação e remoção de
  match.
- **Prompt:**
  ```text
  Implemente a T26 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T26 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T26.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T27: Serviço de recrutadores no front
- **Objetivo:** criar o serviço de recrutadores do front (cadastro, listagem, remoção e edição do perfil).
- **Prompt:**
  ```text
  Implemente a T27 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T27 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T27.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T28: Serviços de revelação no front
- **Objetivo:** montar nos serviços os tipos revelados depois do match (`CandidateCard`, `LikedVacancyCard`).
- **Prompt:**
  ```text
  Implemente a T28 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T28 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T28.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T29: Tela "Recrutadores" da empresa
- **Objetivo:** criar a tela em que a empresa lista, cadastra e remove os próprios recrutadores.
- **Prompt:**
  ```text
  Implemente a T29 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T29 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T29.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T30: Tela de candidatos do recrutador
- **Objetivo:** criar a tela do recrutador com os candidatos anônimos, o botão curtir/descurtir e a revelação depois
  do match.
- **Prompt:**
  ```text
  Implemente a T30 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T30 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T30.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T31: Tela de vagas do recrutador (somente leitura)
- **Objetivo:** criar a tela do recrutador com todas as vagas da própria empresa e o status de cada uma, sem edição.
- **Prompt:**
  ```text
  Implemente a T31 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T31 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T31.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T32: Perfil do recrutador
- **Objetivo:** criar a tela "Meu perfil" do recrutador para editar a descrição e a senha.
- **Prompt:**
  ```text
  Implemente a T32 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T32 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T32.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T33: Candidatos da empresa com revelação
- **Objetivo:** fazer a tela de candidatos da empresa usar `CandidateCard`, sem botão de curtir e com nome e e-mail só
  depois do match.
- **Prompt:**
  ```text
  Implemente a T33 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T33 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T33.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T34: Vagas do candidato: curtir e "Minhas curtidas"
- **Objetivo:** acrescentar o botão curtir/descurtir nas vagas `OPEN` e a seção "Minhas curtidas" na tela de vagas do
  candidato.
- **Prompt:**
  ```text
  Implemente a T34 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T34 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T34.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T35: Home e cadastro público
- **Objetivo:** tirar o "em breve" do passo "Dê match" da home e conferir que o cadastro público continua só com
  candidato e empresa.
- **Prompt:**
  ```text
  Implemente a T35 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T35 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T35.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

#### Fase 4 — Validação final

### Tarefa T36: AGENTS.md e docs da API
- **Objetivo:** atualizar o `AGENTS.md` e a documentação da API com o recrutador, as curtidas e o match.
- **Prompt:**
  ```text
  Implemente a T36 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T36 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T36.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T37: Conferência dos critérios de aceite
- **Objetivo:** conferir os critérios CA01–CA52, com o teste que cobre cada um.
- **Prompt:**
  ```text
  Implemente a T37 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T37 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T37.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T38: Regressão dos fluxos antigos
- **Objetivo:** repassar os fluxos que já existiam com o banco resetado e o `localStorage` limpo.
- **Prompt:**
  ```text
  Implemente a T38 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T38 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T38.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T39: Varredura de anonimato
- **Objetivo:** conferir o Princípio do Anonimato ponto a ponto (spec 4.6).
- **Prompt:**
  ```text
  Implemente a T39 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T39 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T39.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T40: N+1 de competências no `PersonDAO`
- **Objetivo:** eliminar a consulta de competências por pessoa em `PersonDAO#findWhere`, sem mudar os métodos públicos
  nem o resultado.
- **Prompt:**
  ```text
  Implemente a T40 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T40 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T40.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T41: Exclusão da empresa remove as pessoas dos recrutadores
- **Objetivo:** ao excluir uma empresa, remover na mesma transação as `person` dos recrutadores dela, sem deixar
  registro órfão.
- **Prompt:**
  ```text
  T41 — desbloqueio: autorizado o caminho (a)

  Exceção às regras de proteção do plano, válida só para esta tarefa:
  - Adicione no `PersonDAO` o método `delete(Connection conn, int id)`. Ele usa a conexão recebida e não abre, não confirma (commit) nem fecha a transação.
  - Não altere o `delete(int)` existente, nem qualquer outro método do `PersonDAO` ou do `EnterpriseDAO`.
  - Registre essa exceção no plano, na própria T41, para ficar rastreável.

  Implementação no `EnterpriseService#delete`:
  1. Abra uma conexão e inicie a transação.
  2. Busque os ids das `person` dos recrutadores da empresa.
  3. Remova essas `person`, usando o método que já recebe a conexão (cascata para `recruiter` e `recruiter_like`).
  4. Remova a empresa com o novo `PersonDAO#delete(Connection, int)`.
  5. Faça commit. Em caso de erro, rollback e propague a exceção.

  Testes Spock:
  - CA53: empresa com R1 excluída → nenhuma `person` de R1 sobra, e o e-mail de R1 pode ser cadastrado de novo.
  - Rollback: se a exclusão da empresa falhar, as `person` dos recrutadores continuam existindo.
  - Empresa sem recrutadores: a exclusão funciona como antes.
  - Os testes existentes de `PersonDAO` e `EnterpriseDAO` continuam passando sem alteração.

  Entrega:
  - Lista de arquivos alterados.
  - Resultado dos testes.
  - Uma nova seção "Dúvidas e desvios".
  ```

### Tarefa T42: N+1 de vagas por empresa no `EnterpriseDAO`
- **Objetivo:** eliminar a consulta de vagas por empresa em `EnterpriseDAO#loadRelations`.
- **Prompt:**
  ```text
  Implemente a T42 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T42 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T42.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Tarefa T43: N+1 de competências por vaga no `VacancyDAO`
- **Objetivo:** eliminar a consulta de competências por vaga em `VacancyDAO#findWhere`.
- **Prompt:**
  ```text
  Implemente a T43 de docs/specs/recrutador/plano.md.

  Leitura (não leia o plano nem a spec inteiros):
  - Do plano, leia só a seção "Regras para todas as tarefas", a T43 e as tarefas de que ela depende, para conferir se estão concluídas. Use grep para achar as linhas.
  - Da spec, leia só as seções ligadas aos CAs da T43.
  - Leia AGENTS.md e só os arquivos vizinhos necessários para seguir o padrão.

  Siga as regras do plano, com estas mudanças:
  - Se precisar de arquivo fora da lista, ou se a spec tiver lacuna ou conflito, pare e proponha o texto para a spec.
  - Não altere o CHANGELOG nem o checkbox, e não faça commit.
  - Rode o "Como validar" e o `./gradlew test -q`. Mostre só as falhas e o resumo.

  Responda de forma curta: 1) mudanças por arquivo; 2) comandos e resultados; 3) anonimato; 4) CAs; 5) pendências manuais; 6) dúvidas e desvios.
  ```

### Etapa 8: validação
- **Objetivo:** validar a feature inteira antes do PR: estado do plano, testes automatizados, critérios de aceite,
  anonimato, regressão, roteiro manual no navegador, falhas e desvios.
- **Prompt:**
  ```text
  Etapa 8 — Validação da feature Recrutador

  Objetivo: comprovar que a feature cumpre a spec, sem regressões, antes do PR (Etapa 9).
  Nesta etapa você NÃO implementa nada. Se algo falhar, registre e pare. A correção vira tarefa nova no plano, com minha autorização.

  1. Estado do plano
  - Confirme que todas as tarefas do plano estão concluídas (incluindo T40–T43) ou explicitamente adiadas.
  - Liste as pendências registradas no plano/CHANGELOG que ficam para depois do PR.

  2. Testes automatizados
  - Rode `./gradlew clean test` e informe total, falhas e ignorados.
  - Rode os testes do frontend (e o `tsc --noEmit`, se não fizer parte deles).
  - Não altere testes para fazê-los passar. Se um teste falhar, registre o motivo provável e pare.

  3. Critérios de aceite, um a um
  - Monte uma tabela com TODOS os CAs da spec (CA1 até o último, incluindo o CA53) com as colunas: CA | Como foi verificado (teste Spock com arquivo:método, ou passo manual) | Resultado (OK / Falhou / Não verificável).
  - CA sem teste automatizado: verifique pelo console ou pela API no banco descartável `linketinder_etapa8`, e descreva o passo exato.
  - CA "Não verificável": explique por quê e proponha como verificar.

  4. Anonimato (AGENTS.md, seção 12)
  - Verifique, pelos mappers/DTOs e por chamadas reais à API, que:
    - candidato não vê nome, e-mail nem CNPJ da empresa antes do match (nem via recrutador);
    - empresa/recrutador não vê dados identificáveis do candidato antes do match;
    - depois do match, aparece só o que a spec permite.
  - Registre cada verificação com o endpoint/tela e o campo conferido.

  5. Regressão dos fluxos antigos
  No banco `linketinder_etapa8`, execute e registre o resultado:
  - cadastro e listagem de candidatos, empresas e vagas (console e API);
  - curtida de candidato em vaga, curtida de empresa em candidato e geração de match;
  - exclusão de empresa com e sem recrutadores (CA53: e-mail do recrutador reutilizável);
  - telas do frontend: cadastro, perfil do candidato (vagas abertas acima de "Minhas curtidas", status "Pausada"), perfil da empresa e gráfico de competências.
  Apague o banco no fim.

  6. Roteiro manual para mim
  Gere um checklist curto (no máximo 15 itens) do que EU devo testar no navegador, com passos e resultado esperado, priorizando o que você não conseguiu verificar sozinho.

  7. Registro
  - Salve o relatório como `validacao.md` na mesma pasta da spec da feature.
  - Acrescente no CHANGELOG uma linha na entrada da feature: "Validação concluída", com link para o relatório.
  - Não faça commit. Eu reviso antes.

  Entrega:
  - Resumo: testes (back/front), CAs OK/Falhou/Não verificável, anonimato, regressão.
  - Caminho do relatório.
  - Seção "Dúvidas e desvios".
  ```
