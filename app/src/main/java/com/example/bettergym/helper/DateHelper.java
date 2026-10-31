package com.example.bettergym.helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateHelper {

    private static final String ISO_FORMAT_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String ISO_FORMAT_SECS = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * Devolve a data e hora atual no formato ISO 8601 UTC (ex: "2026-10-31T15:30:00.000Z")
     * para gravação uniforme e consistente na REST API do Firestore.
     */
    public static String obterDataAtualIso() {
        SimpleDateFormat iso = new SimpleDateFormat(ISO_FORMAT_MILLIS, Locale.US);
        iso.setTimeZone(TimeZone.getTimeZone("UTC"));
        return iso.format(new Date());
    }

    /**
     * Converte data ISO para exibição no formato simples: "DD/MM/YYYY".
     */
    public static String formatarDataSimples(String dataIso) {
        Date data = converterIsoParaDate(dataIso);
        if (data != null) {
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(data);
        }
        return (dataIso == null || dataIso.trim().isEmpty()) ? "—" : dataIso;
    }

    /**
     * Converte data ISO para exibição com data e hora: "DD/MM/YYYY às HH:mm".
     */
    public static String formatarDataHora(String dataIso) {
        Date data = converterIsoParaDate(dataIso);
        if (data != null) {
            SimpleDateFormat saida = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());
            return saida.format(data);
        }
        return (dataIso == null || dataIso.trim().isEmpty()) ? "Data desconhecida" : dataIso;
    }

    /**
     * Formata a data ISO para apresentação amigável no histórico de visitas: "Visitado a DD/MM/YYYY às HH:mm".
     */
    public static String formatarVisita(String dataIso) {
        Date data = converterIsoParaDate(dataIso);
        if (data != null) {
            SimpleDateFormat saida = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());
            return "Visitado a " + saida.format(data);
        }
        return (dataIso == null || dataIso.trim().isEmpty()) ? "Data desconhecida" : "Visitado a " + dataIso;
    }

    /**
     * Faz o parsing robusto de datas ISO com ou sem milissegundos.
     */
    public static Date converterIsoParaDate(String dataIso) {
        if (dataIso == null || dataIso.trim().isEmpty()) {
            return null;
        }

        try {
            SimpleDateFormat entradaMillis = new SimpleDateFormat(ISO_FORMAT_MILLIS, Locale.US);
            entradaMillis.setTimeZone(TimeZone.getTimeZone("UTC"));
            return entradaMillis.parse(dataIso);
        } catch (Exception ignored) {
            try {
                SimpleDateFormat entradaSecs = new SimpleDateFormat(ISO_FORMAT_SECS, Locale.US);
                entradaSecs.setTimeZone(TimeZone.getTimeZone("UTC"));
                return entradaSecs.parse(dataIso);
            } catch (Exception e) {
                return null;
            }
        }
    }
}

