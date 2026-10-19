package com.example.bettergym.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

//Funções de formatação dos dados do perfil para apresentação no ecrã
public final class FormatadorPerfil {

    private static final String VALOR_VAZIO = "—";
    private static final String FORMATO_ISO = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String FORMATO_APRESENTACAO = "dd/MM/yyyy";

    private FormatadorPerfil() {
    }

    //Devolve o valor recebido, ou um traço se estiver vazio.
    public static String valorOuTraco(String valor) {
        return (valor == null || valor.trim().isEmpty()) ? VALOR_VAZIO : valor;
    }

    //Calcula as iniciais de um nome: primeira letra do primeiro e do último nome.
    public static String calcularIniciais(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.trim().isEmpty()) {
            return "?";
        }
        String[] partes = nomeCompleto.trim().split("\\s+");
        String primeira = partes[0].substring(0, 1);
        if (partes.length == 1) {
            return primeira.toUpperCase(Locale.ROOT);
        }
        String ultima = partes[partes.length - 1].substring(0, 1);
        return (primeira + ultima).toUpperCase(Locale.ROOT);
    }

    //Devolve o nome de utilizador com "@" à frente.
    public static String formatarNomeUtilizador(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "";
        }
        return "@" + username;
    }

    //Converte a data de registo ("2026-10-15T19:45:45Z") para "15/10/2026".
    public static String formatarDataRegisto(String dataIso) {
        if (dataIso == null || dataIso.trim().isEmpty()) {
            return VALOR_VAZIO;
        }
        try {
            SimpleDateFormat entrada = new SimpleDateFormat(FORMATO_ISO, Locale.ROOT);
            entrada.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date data = entrada.parse(dataIso);

            SimpleDateFormat saida = new SimpleDateFormat(FORMATO_APRESENTACAO, Locale.ROOT);
            saida.setTimeZone(TimeZone.getTimeZone("UTC"));
            return saida.format(data);
        } catch (Exception e) {
            return dataIso;
        }
    }
}

