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
            throw new Exception("Erro na ligação HTTP POST. Código: " + responseCode);
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

    public static String get(String urlString, Map<String, String> headers) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();

        conexao.setRequestMethod("GET");
        conexao.setRequestProperty("Accept", "application/json");
        conexao.setConnectTimeout(10000);
        conexao.setReadTimeout(10000);

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                conexao.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        int responseCode = conexao.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Erro na ligação HTTP GET. Código: " + responseCode);
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

    public static String patch(String urlString, String jsonBody, Map<String, String> headers) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();

        conexao.setRequestMethod("POST");
        conexao.setRequestProperty("X-HTTP-Method-Override", "PATCH");
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
            throw new Exception("Erro na ligação HTTP PATCH. Código: " + responseCode);
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

    public static String delete(String urlString, Map<String, String> headers) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();

        conexao.setRequestMethod("DELETE");
        conexao.setConnectTimeout(10000);
        conexao.setReadTimeout(10000);

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                conexao.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        int responseCode = conexao.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new Exception("Erro na ligação HTTP DELETE. Código: " + responseCode);
        }

        StringBuilder resposta = new StringBuilder();
        InputStream is = conexao.getInputStream();
        if (is != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String linha;
            while ((linha = reader.readLine()) != null) {
                resposta.append(linha);
            }
            reader.close();
        }

        conexao.disconnect();
        return resposta.toString();
    }

    public static Bitmap downloadBitmap(String urlFoto) throws Exception {
        URL url = new URL(urlFoto);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);

        try (InputStream in = conn.getInputStream()) {
            return BitmapFactory.decodeStream(in);
        } finally {
            conn.disconnect();
        }
    }
}