# Linketinder-Project

O **Linketinder** é uma aplicação de terminal desenvolvida em Groovy para conectar candidatos a vagas de emprego oferecidas por empresas, simplificando o processo de recrutamento através do alinhamento de competências técnicas.

---

## Tecnologias e Conceitos Utilizados

* **Linguagem:** Groovy (JDK 17+)
* **Build Tool:** Gradle
* **Arquitetura:** Camada de Serviços (`CandidateService`, `EnterpriseService`), Modelo de Dados (`Candidate`, `Enterprise`) e Repositório Simulado (`Database`)
* **Validações:** Regras customizadas com Expressões Regulares (Regex) e utilitários (`ValidadorUtil`)
* **Tratamento de Dados:** Enums (`Competence`) e manipulação de listas

---

## Funcionalidades

- [x] **Cadastro de Candidatos:** Validação de CPF, e-mail, idade e mapeamento de competências técnicas.
- [x] **Cadastro de Empresas:** Validação de CNPJ, e-mail e país.
- [x] **Persistência:** Garantia de criação única e vinculação correta de IDs aos registros.
- [x] **Menu Interativo:** Navegação contínua no terminal.

---

## Estrutura do Projeto

```text
Linketinder/
├── src/
│   └── main/
│       └── groovy/
│           └── com/
│               └── mariajuliasales/
│                   ├── menu/
│                   │   ├── View.groovy
│                   │   ├── Menu.groovy
│                   ├── model/
│                   │   ├── Candidate.groovy
│                   │   ├── Enterprise.groovy
│                   │   └── Competence.groovy
│                   │   └── Person.groovy
│                   │   └── PersonAbstract.groovy
│                   ├── service/
│                   │   ├── CandidateService.groovy
│                   │   └── EnterpriseService.groovy
│                   ├── util/
│                   │   └── ValidateUtil.groovy
│                   ├── repository/
│                   │   └── Database.groovy
│                   └── Main.groovy
├── build.gradle
├── .gitignore
└── README.md
```
---

## Como Executar o Projeto

### Pré-requisitos
* **Java JDK 17** ou superior instalado
* **Gradle** 

### Passos para Execução

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/mariajuliasales/Linketinder-Project.git
   ```
2. **Acesse o diretório do projeto:**
   ```bash
   cd Linketinder-Project/Linketinder
   ```

4. **Compile e builde:**
   ```bash
    gradle build
   ```
   
6. **Execute no terminal:**
   ```bash
   gradle run
   ```



   
