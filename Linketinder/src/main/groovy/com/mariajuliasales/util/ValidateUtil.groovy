package com.mariajuliasales.util

class ValidateUtil {

    private ValidateUtil() {
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
        String cpfRegex = /^\d{3}\.?\d{3}\.?\d{3}-?\d{2}$/

        if (!(cpf ==~ cpfRegex)) {
            return false
        }

        String numbers = cpf.replaceAll(/\D/, "")


        if (numbers.toSet().size() == 1) {
            return false
        }

        try {
            int d1 = calculateDigit(numbers.substring(0, 9), 10)
            int d2 = calculateDigit(numbers.substring(0, 9) + d1, 11)

            return numbers.endsWith("${d1}${d2}")
        } catch (Exception e) {
            return false
        }

    }

    static boolean isValidCnpj(String cnpj) {

        final int[] PESOS_D1 = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]
        final int[] PESOS_D2 = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]
        if (!cnpj?.trim()) {
            return false
        }
        String cnpjRegex = /^[A-Za-z0-9]{2}\.?[A-Za-z0-9]{3}\.?[A-Za-z0-9]{3}\/?[A-Za-z0-9]{4}-?\d{2}$/

        if (!(cnpj ==~ cnpjRegex)) {
            return false
        }

        String cleaned = cnpj.replaceAll(/[.\-\/]/, "").toUpperCase()

        if (cleaned.length() != 14) {
            return false
        }

        if (cleaned.toSet().size() == 1) {
            return false
        }

        try {
            String base = cleaned.substring(0, 12)

            int d1 = calculateCnpjDigit(base, PESOS_D1)
            int d2 = calculateCnpjDigit(base + d1, PESOS_D2)

            return cleaned.endsWith("${d1}${d2}")
        } catch (Exception e) {
            return false
        }

    }


    private static int calculateDigit(String base, int weight) {
        int sum = 0
        for (int i = 0; i < base.length(); i++) {
            sum += Character.getNumericValue(base.charAt(i)) * weight--
        }
        int remainder = sum % 11
        return remainder < 2 ? 0 : 11 - remainder
    }

    private static int calculateCnpjDigit(String base, int[] pesos) {
        int soma = 0
        base.eachWithIndex { ch, idx ->
            int valor = (ch as char) - 48
            soma += valor * pesos[idx]
        }
        int resto = soma % 11
        return resto < 2 ? 0 : 11 - resto
    }
}
