package com.example.bettergym.helper;

import java.util.Locale;

public class Utils {

    public static String valorOuPadrao(String valor, String padrao) {
        return (valor == null || valor.trim().isEmpty()) ? padrao : valor;
    }

    public static String valorOuTraco(String valor) {
        return valorOuPadrao(valor, "—");
    }

    public static String iniciais(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.trim().isEmpty()) return "?";
        String[] partes = nomeCompleto.trim().split("\\s+");
        String primeira = partes[0].substring(0, 1);
        if (partes.length == 1) return primeira.toUpperCase(Locale.ROOT);
        String ultima = partes[partes.length - 1].substring(0, 1);
        return (primeira + ultima).toUpperCase(Locale.ROOT);
    }

    public static String formatarDataRegisto(String dataIso) {
        return DateHelper.formatarDataSimples(dataIso);
    }

    public static String formatarRating(double rating, int numAvaliacoes) {
        if (numAvaliacoes > 0) {
            return String.format(Locale.getDefault(), "★ %.1f (%d avaliações)", rating, numAvaliacoes);
        }
        return "Ainda sem avaliações públicas";
    }
}
