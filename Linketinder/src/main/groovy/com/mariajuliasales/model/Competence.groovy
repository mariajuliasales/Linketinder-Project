package com.mariajuliasales.model

class Competence {

    Integer id
    String name

    Competence(String name) {
        this.name = name?.trim()
    }

    Competence(Integer id, String name) {
        this.id = id
        this.name = name?.trim()
    }

    @Override
    boolean equals(Object other) {
        other instanceof Competence && name?.equalsIgnoreCase(((Competence) other).name)
    }

    @Override
    int hashCode() {
        name?.toLowerCase()?.hashCode() ?: 0
    }

    @Override
    String toString() {
        name
    }
}