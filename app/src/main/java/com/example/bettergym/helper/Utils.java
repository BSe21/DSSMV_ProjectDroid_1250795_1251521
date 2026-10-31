package com.example.bettergym.helper;

import java.util.Locale;

public class Utils {

    /**
     * Retorna o valor padrão se o texto for nulo ou vazio.
     */
    public static String valorOuPadrao(String valor, String padrao) {
        return (valor == null || valor.trim().isEmpty()) ? padrao : valor;
    }

    /**
     * Retorna "—" se o valor for nulo ou vazio, caso contrário retorna o valor.
     */
    public static String valorOuTraco(String valor) {
        return valorOuPadrao(valor, "—");
    }

    /**
     * Extrai as iniciais do primeiro e último nome. Ex: "João Silva" -> "JS"
     */
    public static String iniciais(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.trim().isEmpty()) return "?";
        String[] partes = nomeCompleto.trim().split("\\s+");
        String primeira = partes[0].substring(0, 1);
        if (partes.length == 1) return primeira.toUpperCase(Locale.ROOT);
        String ultima = partes[partes.length - 1].substring(0, 1);
        return (primeira + ultima).toUpperCase(Locale.ROOT);
    }

    /**
     * Converte data ISO para exibição no perfil "DD/MM/YYYY" através do DateHelper.
     */
    public static String formatarDataRegisto(String dataIso) {
        return DateHelper.formatarDataSimples(dataIso);
    }

    /**
     * Formata a classificação e número de avaliações do ginásio (ex: "★ 4.5 (120 avaliações)").
     */
    public static String formatarRating(double rating, int numAvaliacoes) {
        if (numAvaliacoes > 0) {
            return String.format(Locale.getDefault(), "★ %.1f (%d avaliações)", rating, numAvaliacoes);
        }
        return "Ainda sem avaliações públicas";
    }
}
