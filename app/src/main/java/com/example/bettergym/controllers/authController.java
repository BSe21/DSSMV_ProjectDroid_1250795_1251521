package com.example.bettergym.controllers;

import android.util.Patterns;

import com.example.bettergym.api.SupabaseHelper; // Ajusta o package se o teu SupabaseHelper estiver noutro sítio
import com.example.bettergym.exceptions.DuplicatedDataException;
import com.example.bettergym.exceptions.InvalidDataException;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
public class authController {
    // Interface para comunicar o sucesso ou erro de volta à Activity
    public interface AuthCallback {
        void onSucesso(String mensagem);
        void onErro(String erro);
    }

    public void registarNovaConta(String nome, String username, String dataNascimento,
                                  String telefone, String email, String password,
                                  String confirmarPassword, AuthCallback callback) {

        // 1. Validações a lançar as exceções do professor (como são RuntimeException, não precisam de throws obrigatório)
        if (nome.isEmpty() || username.isEmpty() || dataNascimento.isEmpty() || telefone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            throw new InvalidDataException("Por favor, preenche todos os campos obrigatórios.");
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            throw new InvalidDataException("O formato do email está incorreto.");
        }
        if (telefone.length() < 9) {
            throw new InvalidDataException("O número de telemóvel deve ter pelo menos 9 dígitos.");
        }
        if (password.length() < 6) {
            throw new InvalidDataException("A password tem de ter pelo menos 6 caracteres.");
        }
        if (!password.equals(confirmarPassword)) {
            throw new InvalidDataException("As passwords não coincidem.");
        }

        // 2. Pedido HTTP para o Supabase Auth (Sign Up)
        try {
            JSONObject dados = new JSONObject();
            dados.put("email", email);
            dados.put("password", password);

            JSONObject metadados = new JSONObject();
            metadados.put("nome_completo", nome);
            metadados.put("nome_utilizador", username);
            metadados.put("data_nascimento", dataNascimento);
            metadados.put("numero_telemovel", telefone);
            dados.put("data_nascimento", metadados);

            SupabaseHelper.fazerPedido("POST", "auth/v1/signup", dados, new SupabaseHelper.ApiCallback() {
                @Override
                public void onSuccess(int statusCode, String responseJson) {
                    try {
                        JSONObject resposta = new JSONObject(responseJson);

                        String userId = resposta.has("user")
                                ? resposta.getJSONObject("user").getString("id")
                                : resposta.getString("id");

                        // Se o Auth tiver sucesso, criamos o perfil na tabela pública
                        criarPerfilNaTabela(userId, nome, username, dataNascimento, telefone, callback);

                    } catch (Exception e) {
                        callback.onErro("Erro ao processar a resposta do servidor.");
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    String msg = errorMessage;
                    if (errorMessage.contains("user_already_exists") || errorMessage.contains("already registered")) {
                        msg = "Já existe uma conta registada com este email.";
                    } else if (errorMessage.contains("weak_password")) {
                        msg = "A password é demasiado fraca.";
                    } else if (errorMessage.startsWith("Falha de ligação")) {
                        msg = "Sem ligação à internet. Verifica a rede.";
                    }
                    callback.onErro(msg);
                }
            });

        } catch (Exception e) {
            callback.onErro("Erro interno ao preparar os dados de registo.");
        }
    }

    // 3. Passo interno: Insere o perfil na tabela "perfis" do Supabase
    private void criarPerfilNaTabela(String userId, String nome, String username,
                                     String dataNascimento, String telefone, AuthCallback callback) {
        try {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            iso.setTimeZone(TimeZone.getTimeZone("UTC"));

            JSONObject perfil = new JSONObject();
            perfil.put("id", userId);
            perfil.put("nome_completo", nome);
            perfil.put("username", username);
            perfil.put("data_nascimento", dataNascimento);
            perfil.put("telemovel", telefone);
            perfil.put("data_registo", iso.format(new Date()));

            SupabaseHelper.fazerPedido("POST", "rest/v1/perfis", perfil, new SupabaseHelper.ApiCallback() {
                @Override
                public void onSuccess(int statusCode, String responseJson) {
                    callback.onSucesso("Conta criada com sucesso! Podes fazer login.");
                }

                @Override
                public void onFailure(String errorMessage) {
                    callback.onSucesso("Conta criada, mas houve um erro ao guardar o perfil. Tenta fazer login.");
                }
            });

        } catch (Exception e) {
            callback.onErro("Erro ao estruturar os dados do perfil.");
        }
    }
    public void fazerLogin(String email, String password, AuthCallback callback) {
        // Validações básicas com as exceções do professor se quisesses, ou validação simples
        if (email.isEmpty() || password.isEmpty()) {
            callback.onErro("Preenche o email e a password!");
            return;
        }

        try {
            JSONObject dadosLogin = new JSONObject();
            dadosLogin.put("email", email);
            dadosLogin.put("password", password);

            SupabaseHelper.fazerPedido("POST", "auth/v1/token?grant_type=password", dadosLogin, new SupabaseHelper.ApiCallback() {
                @Override
                public void onSuccess(int statusCode, String responseJson) {
                    try {
                        JSONObject resposta = new JSONObject(responseJson);
                        String token = resposta.getString("access_token");
                        String userId = resposta.getJSONObject("user").getString("id");

                        // Devolvemos o token e o userId através do callback (podes adaptar o callback se precisares)
                        callback.onSucesso(token + "|" + userId);

                    } catch (Exception e) {
                        callback.onErro("Erro ao processar a resposta do servidor.");
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    callback.onErro("Email ou palavra-passe incorretos!");
                }
            });

        } catch (Exception e) {
            callback.onErro("Erro interno ao preparar o login.");
        }
    }

}

