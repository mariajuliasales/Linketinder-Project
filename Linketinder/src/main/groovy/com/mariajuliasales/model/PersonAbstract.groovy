package com.mariajuliasales.model

abstract class PersonAbstract implements Person {

     int id
     String name
     String email
     String password
     String description
     Address address
     List<Competence> competences = []

     def addCompetence(Competence competence) {
         if (!competences.contains(competence)) {
             competences.add(competence)
         }
     }

}
