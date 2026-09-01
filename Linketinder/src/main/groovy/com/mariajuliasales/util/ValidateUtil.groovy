package com.mariajuliasales.util

class ValidateUtil {

    private ValidateUtil(){
        throw new IllegalStateException("Utility class")
    }

    static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false
        }
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$"
        return email.matches(emailRegex)
    }

    static boolean isValidCpf(String cpf) {
        if (cpf == null || cpf.isEmpty()) {
            return false
        }
        String cpfRegex = "^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}\$"
        return cpf.matches(cpfRegex)
    }

    static boolean isValidCnpj(String cnpj) {
        if (!cnpj?.trim()) {
            return false
        }
        String cnpjRegex = "^[A-Za-z0-9]{2}\\.?[A-Za-z0-9]{3}\\.?[A-Za-z0-9]{3}/?[A-Za-z0-9]{4}-?\\d{2}\$"
        return cnpj.matches(cnpjRegex)
    }
}
