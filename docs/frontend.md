# Frontend


Aplicação web em que candidatos e empresas se cadastram, entram com e-mail e senha e veem uns aos outros de forma anônima. Ainda não se comunica com o backend, os dados ficam no navegador.

## Tecnologias

* **Linguagem:** TypeScript 6
* **Build e servidor de desenvolvimento:** Vite 8
* **Estilo:** Bootstrap 5.3
* **APIs do navegador:** DOM e LocalStorage

## Telas

| Endereço | Tela | Quem acessa |
|----------|------|-------------|
| `#/home` | **Página inicial:** apresentação, números da plataforma, "Como funciona" e chamadas para cadastro | Todos |
| `#/register` | **Cadastro:** um só formulário, com a escolha "Sou candidato" ou "Sou empresa" | Todos |
| `#/login` | **Login** com e-mail e senha | Todos |
| `#/vacancies` | **Vagas:** todas as vagas abertas, sem dados da empresa | Candidato logado |
| `#/candidates` | **Candidatos:** tabela de candidatos anônimos e gráfico de candidatos por competência | Empresa logada |
| `#/my-vacancies` | **Minhas vagas:** publicar e excluir as vagas da empresa | Empresa logada |

Quem tenta abrir uma página restrita sem estar logado ou com o tipo errado é levado ao login.

## Funcionalidades

- **Cadastro:**
  - campos comuns: nome, e-mail, senha, endereço (CEP, cidade, UF e país), descrição opcional e competências;
  - candidato informa também CPF, data de nascimento (mínimo de 16 anos) e formação; empresa informa o CNPJ;
  - validação com Bootstrap (campos obrigatórios, formato de CPF, CNPJ e CEP, ao menos uma competência);
  - o e-mail não pode se repetir entre candidatos e empresas;
  - após o cadastro, o usuário já entra logado.
- **Login e logout:** com e-mail e senha. Após entrar, o candidato vai para as vagas e a empresa para os candidatos.
- **Vagas (candidato):** cards com título, local, descrição e competências. As competências que o
  candidato tem aparecem em verde.
- **Candidatos (empresa):** tabela com "Candidato #N", formação, descrição e competências.
- **Gráfico de competências (empresa):** barras horizontais com quantos candidatos têm cada competência.
- **Janelas de informação ao passar o mouse** (ou com Tab):
  - sobre um **candidato** na tabela: formação, descrição, competências e quantas delas a empresa busca;
  - sobre uma **vaga**: detalhes e quantas competências pedidas o candidato tem;
  - sobre uma **barra do gráfico**: quantos candidatos têm aquela competência e o percentual do total;
  - nome e contato aparecem como "🔒 revelados após o match".
- **Minhas vagas:** a empresa publica vagas (título, descrição, cidade, UF e competências) e exclui as próprias vagas.
- **Excluir conta:** candidato e empresa podem excluir a própria conta. Ao excluir uma empresa,
  as vagas dela também são excluídas. Toda exclusão pede confirmação.

## Anonimato

O anonimato é garantido pelo tipo, não só pela tela. As páginas de listagem recebem apenas versões
reduzidas dos dados, criadas campo a campo em `services/candidates.ts` e `services/vacancies.ts`:

- `CandidateAnonymous` → só `training`, `description` e `competences`.
  Nome, e-mail, CPF, data de nascimento e endereço não chegam à tela da empresa.
- `VacancyAnonymous` → só `title`, `description`, `city`, `state` e `competences`.
  O vínculo com a empresa (`enterpriseId`) não chega à tela do candidato.

## Persistência

Os dados ficam no LocalStorage do navegador. Na primeira visita, a aplicação grava os dados de exemplo,
os mesmos do `Database/seed.sql` (6 candidatos, 6 empresas e 8 vagas, uma delas encerrada), com CPFs e CNPJs válidos.
Quando o formato dos dados muda, `DATA_FORMAT_VERSION` é incrementada e os dados antigos são substituídos pelo seed.


## Estrutura de pastas

```text
Frontend/
├── index.html
├── package.json
├── tsconfig.json
└── src/
    ├── main.ts           # inicializa o armazenamento e o roteador
    ├── router.ts         # associa cada rota à sua página e redesenha a tela
    ├── navigation.ts     # rotas (ROUTES), página inicial por tipo de conta e navegação
    ├── types.ts          # tipos do domínio e listas fixas (competências e UFs)
    ├── style.css         # ajustes visuais sobre o Bootstrap
    ├── data/
    │   ├── seed.ts       # dados de exemplo (iguais aos do banco)
    │   └── storage.ts    # único acesso ao LocalStorage: coleções genéricas e sessão
    ├── services/
    │   ├── auth.ts       # cadastro, login, logout, conta logada e exclusão de conta
    │   ├── vacancies.ts  # publicar, excluir e listar vagas (próprias ou abertas e anônimas)
    │   ├── candidates.ts # listagem anônima e ranking de competências
    │   ├── stats.ts      # números da página inicial
    │   └── matching.ts   # quantas competências pedidas a pessoa tem
    ├── ui/
    │   ├── dom.ts        # criar elementos, buscar elementos obrigatórios e cards
    │   ├── hoverCard.ts  # janela de informações ao passar o mouse ou focar
    │   ├── badges.ts     # badges de competência (verde quando combina) e aviso de "revelado após o match"
    │   ├── forms.ts      # leitura do formulário, select de UF, checkboxes de competências e validação
    │   ├── barChart.ts   # gráfico de barras em SVG, genérico
    │   └── navbar.ts     # barra de navegação
    └── pages/
        ├── guards.ts     # redireciona ao login quem não tem acesso à página
        ├── home.ts
        ├── register.ts
        ├── login.ts
        ├── vacancies.ts
        ├── candidates.ts
        └── myVacancies.ts
```