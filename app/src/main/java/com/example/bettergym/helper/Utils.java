package com.example.bettergym.helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {

    /**
     * Retorna "—" se o valor for nulo ou vazio, caso contrário retorna o valor.
     */
    public static String valorOuTraco(String valor) {
        return (valor == null || valor.trim().isEmpty()) ? "—" : valor;
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
     * Converte a data do formato ISO (da base de dados) para o formato DD/MM/YYYY.
     */
    public static String formatarDataRegisto(String dataIso) {
        if (dataIso == null || dataIso.trim().isEmpty()) return "—";
        try {
            SimpleDateFormat entrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            entrada.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date data = entrada.parse(dataIso);
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(data);
        } catch (Exception e) {
            return dataIso; // Se falhar a conversão, devolve o texto original
        }
    }
}