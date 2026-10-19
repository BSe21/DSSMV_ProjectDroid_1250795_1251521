package com.example.bettergym.handler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class NetworkHandler {

    /**
     * Faz um pedido HTTP POST genérico (Usado pela API da Google Places).
     */
    public static String post(String urlString, String jsonBody, Map<String, String> headers) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();

        conexao.setRequestMethod("POST");
        conexao.setConnectTimeout(10000);
        conexao.setReadTimeout(10000);
        conexao.setDoOutput(true);

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                conexao.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        if (jsonBody != null) {
            try (OutputStream os = conexao.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        int responseCode = conexao.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Erro na ligação HTTP. Código: " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conexao.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder resposta = new StringBuilder();
        String linha;

        while ((linha = reader.readLine()) != null) {
            resposta.append(linha);
        }

        reader.close();
        conexao.disconnect();

        return resposta.toString();
    }

    /**
     * Faz o download de uma imagem através de um URL e devolve um Bitmap (Usado no Perfil).
     */
    public static Bitmap downloadBitmap(String urlFoto) throws Exception {
        URL url = new URL(urlFoto);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Tempos de espera um pouco maiores para imagens pesadas
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);

        try (InputStream in = conn.getInputStream()) {
            return BitmapFactory.decodeStream(in);
        } finally {
            conn.disconnect();
        }
    }
}