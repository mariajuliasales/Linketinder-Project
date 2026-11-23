---
name: feature-com-spec
description: Implementa uma feature do Linketinder seguindo o fluxo AGENTS.md → spec → plano → implementação → validação, com revisão da pessoa entre cada etapa, uma tarefa e um commit por vez. Os documentos ficam em docs/specs/<nome-da-feature>/, toda spec avalia o impacto no Princípio do Anonimato e toda tarefa registra uma entrada no CHANGELOG.md. Use quando pedirem para implementar uma feature de forma planejada, com spec e plano antes do código.
---

# Feature com spec

## 1. Objetivo

Implementar uma feature do Linketinder seguindo o fluxo **spec → plano → implementação**.

Cada etapa gera um documento ou uma mudança pequena, e a pessoa revisa antes de seguir. Nenhum código é escrito antes de a spec ser aprovada.

O `AGENTS.md` da raiz é a **fonte de verdade** das regras do projeto. Esta skill não repete essas regras: ela aponta para as seções do `AGENTS.md` pelo nome (ex.: "AGENTS.md › Princípio do Anonimato").

## 2. Entradas

- **Descrição da feature:** o que deve ser feito, em poucas frases.
- **Nome da feature:** em kebab-case (ex.: `recrutador`). Ele define a pasta `docs/specs/<nome-da-feature>/`.

Se faltar alguma das duas, pergunte antes de começar.

## 3. Passo a passo

Em todas as etapas:
- Use só informações encontradas no projeto ou dadas pela pessoa. Quando não tiver certeza, escreva **⚠️ A CONFIRMAR** e liste esses itens no fim da resposta.
- Ao terminar a etapa, pare e espere a revisão. Não avance sozinho para a próxima.

### Etapa 1 – AGENTS.md

Leia o `AGENTS.md` inteiro antes de qualquer outra coisa. Preste atenção especial a:
- **AGENTS.md › Integração entre as camadas** (front-end isolado);
- **AGENTS.md › Princípio do Anonimato**;
- **AGENTS.md › Restrições para agentes**;
- **AGENTS.md › Limitações e violações conhecidas**;
- **AGENTS.md › Pontos a confirmar**.

Se o `AGENTS.md` não existir, pare e avise a pessoa. Não crie um.

Se a feature exigir mudar o `AGENTS.md`, isso vira uma tarefa do plano (Etapa 4). Não edite o arquivo nesta etapa.

**A pessoa revisa:**
- se algo do `AGENTS.md` está desatualizado em relação ao código;
- os itens ⚠️ A CONFIRMAR do `AGENTS.md` que afetam a feature.

### Etapa 2 – Perguntas de esclarecimento

**O agente gera:** de 5 a 10 perguntas numeradas sobre a feature, cada uma com uma **sugestão de resposta**. Cubra:
- o que entra e o que fica fora do escopo;
- os comportamentos e casos de borda mais importantes;
- **regras de negócio novas** (quem pode fazer o quê, e quando);
- **Princípio do Anonimato:** que dados novos aparecem para outro usuário, e o que fica visível antes e depois do match;
- **camadas afetadas:** banco, back-end Groovy e front-end TypeScript (que está isolado), e o que muda em cada uma;
- se há mudança de schema;
- dependências novas e mudanças no build;
- metas (desempenho, cobertura etc.), se houver;
- como entregar no Git (branch, commits, idioma).

Antes de perguntar, explore o código. Se encontrar algo que muda a feature (por exemplo, um bug ou uma limitação), conte na pergunta.

Marque cada pergunta sobre o **Princípio do Anonimato** ou sobre **regras de negócio novas** com **(resposta obrigatória)**.

**A pessoa revisa:** responde às perguntas. Diga na mensagem como as respostas são tratadas:
- **Pergunta comum sem resposta:** a sugestão é aceita.
- **Pergunta com (resposta obrigatória) sem resposta:** a sugestão **não** é aceita. O item vira ⚠️ A CONFIRMAR na spec, e a spec não pode ser aprovada enquanto ele estiver em aberto.

### Etapa 3 – Spec

**O agente gera:** o arquivo `docs/specs/<nome-da-feature>/spec.md`, usando o modelo da seção 5, com:
- contexto e objetivo;
- regras de negócio por parte do sistema, com comportamentos esperados e casos de borda;
- **impacto no Princípio do Anonimato** (obrigatório): para cada dado ou entidade nova, o que fica visível para outro usuário antes e depois do match. A tabela segue **AGENTS.md › Princípio do Anonimato**. Se a feature não expõe nenhum dado novo, diga isso explicitamente;
- critérios de aceite numerados (CA01, CA02...) no formato Dado / Quando / Então, incluindo critérios que verificam a **ausência** de dados proibidos;
- impacto no que já existe;
- o que fica fora do escopo;
- **pendências para validar com o Tutor:** todos os ⚠️ A CONFIRMAR da spec reunidos numa lista.

A spec define as regras de negócio. Toda dúvida vira ⚠️ A CONFIRMAR, no ponto do texto em que aparece e na lista de pendências.

A spec descreve o comportamento **atual** do código e o comportamento **desejado** da feature. Se encontrar um bug fora do escopo, registre como "bug conhecido" e deixe a correção em "Fora do escopo".

**A pessoa revisa:**
- se os critérios de aceite descrevem o que ela quer;
- a tabela de impacto no anonimato;
- as pendências para validar com o Tutor;
- os bugs e comportamentos estranhos encontrados.

A spec só pode ser aprovada quando a lista "Pendências para validar com o Tutor" estiver **vazia** ou com **cada item decidido**. Quando estiver tudo certo, a pessoa aprova. Então:
- mude o status da spec para **aprovada**;
- troque cada ⚠️ A CONFIRMAR resolvido pela decisão tomada;
- registre a decisão na lista de pendências.

### Etapa 4 – Plano

**O agente gera:** o arquivo `docs/specs/<nome-da-feature>/plano.md`, usando o modelo da seção 5:
- um **plano por camada**: o que muda no banco, no back-end Groovy e no front-end TypeScript;
- uma tabela com cada camada, se ela está **integrada** ou **isolada** (ver **AGENTS.md › Integração entre as camadas**) e a decisão tomada para a feature naquela camada;
- tarefas pequenas e numeradas, cada uma feita e revisada em poucos minutos, com a camada que tocam;
- **Tarefa 1:** preparar o ambiente (dependências, configuração) e provar que funciona com um exemplo mínimo;
- se o `AGENTS.md` precisar mudar por causa da feature, uma tarefa logo depois da Tarefa 1;
- mudança de schema em tarefa própria, com migration nova (ver **AGENTS.md › Banco de dados**);
- tarefas organizadas da parte mais simples para a mais complexa;
- **última tarefa:** verificação final com o checklist da Etapa 6 e, se fizer sentido, um teste manual feito pela pessoa;
- uma tabela no final relacionando **cada** critério de aceite às tarefas que o cobrem.

Confira que todo critério da spec aparece na tabela.

**A pessoa revisa:**
- o tamanho das tarefas (dividir as grandes);
- a ordem das tarefas;
- a decisão para cada camada;
- se todo critério está coberto.

### Etapa 5 – Implementação (uma tarefa por vez)

A pessoa pede uma tarefa pelo número. Para cada tarefa:

**O agente faz:**
1. Lê de novo o `AGENTS.md`, a spec e o plano, porque eles podem ter mudado.
2. Confere o estado do repositório (`git status`, último commit).
3. Implementa **só** essa tarefa e altera **só** os arquivos listados nela.
4. Roda a validação da tarefa e também a suíte completa de testes:
   - `./gradlew test` (em `Linketinder/`) deve passar;
   - os testes do front-end, quando existirem;
   - `npm run build` (em `Frontend/`), se a tarefa tocar no front.

   Os comandos exatos estão em **AGENTS.md › Como executar e testar**.
5. Adiciona uma entrada no `CHANGELOG.md`, na seção `[Não lançado]`, em **Adicionado**, **Modificado**, **Corrigido** ou **Removido**. A entrada é curta e diz o quê e por quê.
6. Responde com:
   - os arquivos criados ou alterados;
   - um resumo do que cada teste ou mudança verifica, com o critério de aceite correspondente;
   - o comando de validação e o resultado obtido;
   - a entrada adicionada no CHANGELOG;
   - uma sugestão de mensagem de commit.

Depois disso, para e espera a revisão. Não começa a próxima tarefa.

**O agente para e pergunta quando:**
- precisa de uma dependência ou mudança no build que não foi aprovada (explique o motivo e mostre a solução testada, se possível);
- precisaria alterar código fora do escopo;
- precisaria editar um script SQL existente;
- encontra um comportamento que contradiz a spec;
- encontra qualquer risco ao Princípio do Anonimato que a spec não previu.

**A pessoa revisa:**
- roda a validação;
- confere o código;
- marca a tarefa como `[x]` no plano;
- faz o commit da tarefa.

### Etapa 6 – Validação

**O agente gera:** uma verificação dos critérios de aceite, sem alterar arquivos:
- uma tabela com as colunas **Critério**, **Atendido?** (sim / não / parcial), **Teste que cobre** (arquivo e método) e **Observação**;
- a lista de casos de borda da spec que não têm teste;
- o checklist abaixo, com cada item marcado e uma observação quando não estiver atendido.

**Princípio do Anonimato**
- [ ] Dados de terceiros antes do match saem apenas por DTOs `*AnonymousResponse` ou tipos anônimos do front, montados campo a campo
- [ ] Nenhuma entidade é serializada inteira (ex.: `Vacancy.enterprise`)
- [ ] Consultas com JOIN não levam dados identificáveis até a resposta
- [ ] Senha, CPF, CNPJ, data de nascimento, idade e endereço completo nunca aparecem para outro usuário
- [ ] Depois do match, só os dados permitidos no AGENTS.md são revelados
- [ ] Há testes que verificam a AUSÊNCIA de dados sensíveis nas respostas
- [ ] Logs e mensagens de erro não expõem dados pessoais

**Banco**
- [ ] Mudança de schema em uma migration nova e idempotente em `Database/migrations/`
- [ ] Nenhum script existente foi editado
- [ ] `linketinder.dbml` atualizado
- [ ] DER regenerado (manual, ao final da feature)

**Processo**
- [ ] Entrada no CHANGELOG.md
- [ ] Spec, plano e tarefas atualizados se algo mudou durante a implementação
- [ ] Limitações novas registradas no AGENTS.md, se houver

O item "DER regenerado" é feito manualmente pela desenvolvedora. O agente o deixa em aberto e o sinaliza.

**A pessoa revisa:** os itens "não" e "parcial", os casos de borda sem teste e os itens do checklist em aberto.

Se faltar algo, o ciclo se repete:
1. Novos critérios na spec, continuando a numeração.
2. Uma nova tarefa no fim do plano, com a tabela atualizada.
3. Implementação da tarefa.
4. Validação de novo.

## 4. Regras

- **Não escrever código antes da spec aprovada.** Antes disso, só leitura, perguntas e documentos.
- **Não aprovar spec com pendência em aberto.** "Pendências para validar com o Tutor" precisa estar vazia ou com cada item decidido.
- **Princípio do Anonimato é inviolável.** Siga **AGENTS.md › Princípio do Anonimato**. Na dúvida, o dado não é exibido e a dúvida vira ⚠️ A CONFIRMAR.
- **Implementar uma tarefa por vez.** Só a tarefa pedida, mesmo que a próxima pareça simples.
- **Toda tarefa termina com testes passando e uma entrada no CHANGELOG.md.**
- **Fazer um commit por tarefa.** O commit inclui a marcação `[x]` da tarefa no plano e a entrada do CHANGELOG. O agente só faz commit quando a pessoa pedir.
- **Não alterar nada fora do escopo.** Só os arquivos listados na tarefa. Código de produção, build, dependências e dados locais só com autorização (ver **AGENTS.md › Restrições para agentes**).
- **Nunca editar scripts SQL existentes.** Mudança de schema é sempre uma migration nova (ver **AGENTS.md › Banco de dados**).
- **Se a spec estiver errada, atualizar a spec antes do código.** Depois, atualizar o plano. Só então implementar.
- **Bug encontrado fora do escopo:** não corrigir. Registrar na spec como "bug conhecido" e, se for o caso, escrever um teste do comportamento correto marcado como pendente (`@PendingFeature` do Spock), com um comentário explicando o bug.
- **Relatar com honestidade:** se um teste falhar, mostrar a saída. Se uma validação não foi feita, dizer que não foi feita.

## 5. Modelos

### docs/specs/<nome-da-feature>/spec.md

```markdown
# Spec: <nome da feature>

- **Branch:** `<nome-da-branch>`
- **Status:** aguardando aprovação
- **Referência do projeto:** [AGENTS.md](../../../AGENTS.md)

---

## 1. Contexto e objetivo

### Contexto
<Situação atual e o problema que a feature resolve.>

### Objetivo
<O que a feature deve entregar, em poucos itens.>

### Como será feito
<Decisões técnicas principais: onde ficam os arquivos, convenções, ferramentas.>

---

## 2. Regras de negócio

### 2.1 <Parte do sistema>

**Comportamentos esperados**
- <comportamento>

**Casos de borda**
- <caso de borda: entrada → resultado esperado>

**Bug conhecido: <título>** (se houver)
- <o que acontece hoje>
- <como será tratado nos testes>
- A correção está fora do escopo (seção 6).

### 2.2 <Parte do sistema>
...

---

## 3. Impacto no Princípio do Anonimato

Referência: AGENTS.md › Princípio do Anonimato.

| Dado / entidade nova | Quem vê | Visível antes do match | Visível depois do match | Onde é garantido (DTO, tipo do front, consulta) |
|---|---|---|---|---|
| <dado> | <candidato / empresa / ...> | <sim / não> | <sim / não / nunca> | <arquivo> |

<Se a feature não expõe nenhum dado novo a outro usuário, escreva isso aqui explicitamente.>

**Pontos de vazamento verificados:** <consultas com JOIN, serialização, logs, respostas da API, telas do front>

---

## 4. Critérios de aceite

### <Parte do sistema>
- **CA01** — Dado <situação>, quando <ação>, então <resultado esperado>.
- **CA02** — Dado ..., quando ..., então ...

### Anonimato
- **CAxx** — Dado <um usuário sem match>, quando <consulta o dado>, então <a resposta não contém nome, e-mail nem outros dados proibidos>.

### Gerais
- **CAxx** — Dado <a feature concluída>, quando <a suíte de testes é executada>, então <todos passam>.
- **CAxx** — Dado <a feature concluída>, quando <se compara o código fora do escopo com a branch principal>, então <não há alterações>.

---

## 5. Impacto no que já existe

### Código de produção
<O que muda ou "Nenhuma alteração em <pasta>".>

### Arquivos alterados ou criados

| Arquivo | Mudança |
|---|---|
| `<caminho>` | <o que muda> |

### Banco de dados
<Migrations novas (`Database/migrations/NNN_descricao.sql`) ou "Sem mudança de schema".>

### Dependências
- <dependência e motivo>

### Riscos
- <risco e como será verificado>

### Forma de entrega
- Branch `<nome>`, um commit por tarefa do plano, mensagens em <idioma>.
- A spec é commitada antes de qualquer código.

---

## 6. Fora do escopo

- <o que esta feature não vai fazer>

---

## 7. Pendências para validar com o Tutor

Todos os ⚠️ A CONFIRMAR desta spec. A spec só é aprovada quando esta lista estiver vazia ou com cada item decidido.

| # | Pendência | Onde aparece | Decisão |
|---|---|---|---|
| 1 | ⚠️ A CONFIRMAR: <dúvida> | <seção> | <em aberto / decisão tomada> |
```

### docs/specs/<nome-da-feature>/plano.md

```markdown
# Plano de implementação: <nome da feature>

- **Spec:** [spec.md](spec.md)
- **Branch:** `<nome-da-branch>`
- **Ordem:** <critério de ordenação das tarefas>

## Plano por camada

| Camada | Integrada ou isolada? | Decisão para esta feature |
|---|---|---|
| Banco (PostgreSQL) | integrada com o back-end | <o que muda / nada> |
| Back-end (Groovy) | integrada com o banco | <o que muda / nada> |
| Front-end (TypeScript) | isolada (localStorage) | <o que muda / nada, e por quê> |

### Banco
<Migrations, tabelas e constraints.>

### Back-end Groovy
<Model, DAO, service, validator, DTO, mapper, controller, API, menus.>

### Front-end TypeScript
<Tipos, services, páginas, seed.>

## Regras para todas as tarefas

- <onde rodar os comandos>
- Nenhuma tarefa altera <pasta fora do escopo>.
- Um commit por tarefa, no formato <padrão de commit>. Ao concluir a tarefa, marque `[x]` no título dela, no mesmo commit.
- Ao final de cada tarefa, `./gradlew test` deve passar (e os testes do front, quando existirem).
- Toda tarefa adiciona uma entrada no `CHANGELOG.md`, em `[Não lançado]`.

## Antes de começar

Commitar `docs/specs/<nome-da-feature>/spec.md` e `docs/specs/<nome-da-feature>/plano.md` antes de qualquer código.

---

## [ ] Tarefa 1 – Preparar o ambiente e provar que funciona
- O que será feito:
- Camada:
- Arquivos criados ou alterados:
- Critérios de aceite relacionados:
- Como validar: (comando a rodar e resultado esperado)
- Entrada no CHANGELOG:

## [ ] Tarefa 2 – <título curto>
- O que será feito:
- Camada:
- Arquivos criados ou alterados:
- Critérios de aceite relacionados:
- Como validar:
- Entrada no CHANGELOG:

## [ ] Tarefa N – Verificação final
- O que será feito: verificar os critérios gerais e o checklist de validação (Anonimato, Banco, Processo) e, se fizer sentido, um teste manual feito pela pessoa.
- Camada: todas
- Arquivos criados ou alterados: `docs/specs/<nome-da-feature>/plano.md`, `CHANGELOG.md`
- Critérios de aceite relacionados:
- Como validar:
- Entrada no CHANGELOG:

---

## Critérios de aceite × tarefas

| Critério | Tarefas |
|---|---|
| CA01 | |
| CA02 | |
```
