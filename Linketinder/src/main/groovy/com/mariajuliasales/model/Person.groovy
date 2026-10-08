package com.mariajuliasales.model

interface Person {

    int getId()

    String getName()

    String getEmail()

    String getPassword()

    Address getAddress()

    String getDescription()

    List<Competence> getCompetences()

    def addCompetence(Competence competence)

    String viewProfileAnonymous()
}
