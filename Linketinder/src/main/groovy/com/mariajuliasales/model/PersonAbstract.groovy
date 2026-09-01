package com.mariajuliasales.model

 abstract class PersonAbstract implements Person {

     int id
     String name
     String email
     String state
     String cep
     String description
     List<Competence> competences = []

     PersonAbstract(int id, String name, String email, String state, String cep, String description, List<Competence> competences) {
         this.id = id
         this.name = name
         this.email = email
         this.state = state
         this.cep = cep
         this.description = description
         this.competences = competences
     }

     def addCompetence(Competence competence) {
         if (!competences.contains(competence)) {
             competences.add(competence)
         }
     }

 }
