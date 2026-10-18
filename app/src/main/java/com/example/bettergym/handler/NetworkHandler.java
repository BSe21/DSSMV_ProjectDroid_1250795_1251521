package com.example.bettergym.handler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class NetworkHandler {

    /**
     * Faz um pedido HTTP POST genérico para qualquer API.
     */
    public static String post(String urlString, String jsonBody, Map<String, String> headers) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();

        // 1. Configurar a ligação
        conexao.setRequestMethod("POST");
        conexao.setConnectTimeout(10000); // 10 segundos de limite
        conexao.setReadTimeout(10000);
        conexao.setDoOutput(true);

        // 2. Injetar os Cabeçalhos (Headers) - Ex: API Key
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                conexao.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        // 3. Enviar o Corpo do Pedido (JSON Body)
        if (jsonBody != null) {
            try (OutputStream os = conexao.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        // 4. Verificar se a chamada teve sucesso (Código 200 OK)
        int responseCode = conexao.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Erro na ligação HTTP. Código da Google: " + responseCode);
        }

        // 5. Ler a resposta da Google e devolver como String
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
}