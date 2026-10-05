package com.example.bettergym;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;



public class SupabaseHelper {


    public interface ApiCallback {
        void onSuccess(int statusCode, String responseJson);
        void onFailure(String errorMessage);
    }

    //  A Função Central
    public static void fazerPedido(String metodo, String endpoint, JSONObject corpo, ApiCallback callback) {


        Handler mainHandler = new Handler(Looper.getMainLooper());

        // Abre a autoestrada secundária
        new Thread(() -> {
            try {

                URL url = new URL("https://dpttoebwicfftlferdzy.supabase.co/" + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();


                conn.setRequestMethod(metodo);
                conn.setRequestProperty("apikey", "sb_publishable_FKPpCOPMeoZEq_xMKlcZjw_CKCr5D9r");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                // Se houver um corpo de dados (ex: POST do Login) e não for um GET, enviamos o pacote
                if (corpo != null && (metodo.equals("POST") || metodo.equals("PUT") || metodo.equals("PATCH"))) {
                    conn.setDoOutput(true);
                    OutputStream os = conn.getOutputStream();
                    os.write(corpo.toString().getBytes("UTF-8"));
                    os.close();
                }

                // Espera pela resposta da Base de Dados
                int codigoResposta = conn.getResponseCode();

                // Lê o que o Supabase nos disse (seja sucesso ou erro)
                InputStream in;
                if (codigoResposta >= 200 && codigoResposta < 300) {
                    in = conn.getInputStream();
                } else {
                    in = conn.getErrorStream();
                }

                String respostaFinal = "";
                if (in != null) {
                    Scanner scanner = new Scanner(in);
                    scanner.useDelimiter("\\A");
                    respostaFinal = scanner.hasNext() ? scanner.next() : "";
                    scanner.close();
                }

                final String res = respostaFinal;

                // Manda a resposta pelo "Telefone" de volta para o teu ecrã
                if (codigoResposta >= 200 && codigoResposta < 300) {
                    mainHandler.post(() -> callback.onSuccess(codigoResposta, res));
                } else {
                    mainHandler.post(() -> callback.onFailure("Erro " + codigoResposta + ": " + res));
                }

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onFailure("Falha de ligação: " + e.getMessage()));
            }
        }).start(); // Inicia a Thread
    }
}