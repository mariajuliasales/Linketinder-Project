package com.mariajuliasales.model

enum Competence {

    PYTHON,
    JAVA,
    SPRING_FRAMEWORK,
    ANGULAR,
    GROOVY,
    JAVASCRIPT,
    SQL,
    DOCKER,
    KAFKA,
    REACT

    @Override
    String toString() {
        name().replace('_', ' ')
    }
}