package com.example.bettergym.helper;

public final class PasswordValidator {
    private PasswordValidator() {}

    public static String validarAlteracao(String atual, String nova, String confirmacao) {
        if (atual == null || atual.isEmpty() || nova == null || nova.isEmpty()
                || confirmacao == null || confirmacao.isEmpty()) {
            return "Preenche os três campos.";
        }
        if (nova.trim().isEmpty() || nova.length() < 6) {
            return "A nova palavra-passe deve ter pelo menos 6 caracteres e não pode conter espaços.";
        }
        if (!nova.equals(confirmacao)) {
            return "A confirmação não coincide com a nova palavra-passe.";
        }
        if (nova.equals(atual)) {
            return "A nova palavra-passe deve ser diferente da atual.";
        }
        return null;
    }
}
