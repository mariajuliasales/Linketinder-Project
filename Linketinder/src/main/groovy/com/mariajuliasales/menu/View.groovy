package com.mariajuliasales.menu

class View {

    View(){}

    static void showMenu() {
        println ""
        println "Bem vindo(a) ao LinkeTinder!"
        println "Escolha uma opção:"
        println "1. Candidatos"
        println "2. Empresas"
        println "3. Vagas"
        println "4. Competências"
        println "0. Sair do programa."
    }

    static void showSubmenu(String title, List<String> options) {
        println ""
        println "--- ${title} ---"
        options.eachWithIndex { String option, int index -> println "${index + 1}. ${option}" }
        println "0. Voltar"
    }

}
