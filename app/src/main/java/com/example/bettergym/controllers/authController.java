package com.example.bettergym.controllers;

import android.util.Patterns;

import com.example.bettergym.exceptions.InvalidDataException;
import com.example.bettergym.model.Perfil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class authController {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Interface para comunicar o sucesso ou erro de volta à Activity
    public interface AuthCallback {
        void onSucesso(String mensagem);
        void onErro(String erro);
    }

    public void registarNovaConta(String nome, String username, String dataNascimento,
                                  String telefone, String email, String password,
                                  String confirmarPassword, AuthCallback callback) {

        // 1. Validações mantidas com as exceções do professor
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

        // 2. Criar utilizador com Firebase Auth (substitui o pedido HTTP do Supabase)
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            // 3. Se o Auth tiver sucesso, criamos o perfil na coleção "perfis" do Firestore
                            criarPerfilNaTabela(userId, nome, username, dataNascimento, telefone, email, callback);
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "";
                        String msg = "Não foi possível criar a conta.";
                        if (errorMessage != null && (errorMessage.contains("already in use") || errorMessage.contains("address is already in use"))) {
                            msg = "Já existe uma conta registada com este email.";
                        } else if (errorMessage != null && errorMessage.contains("weak password")) {
                            msg = "A password é demasiado fraca.";
                        }
                        callback.onErro(msg);
                    }
                });
    }

    // 4. Passo interno: Insere o perfil na coleção "perfis" do Firestore usando o model que criámos
    private void criarPerfilNaTabela(String userId, String nome, String username,
                                     String dataNascimento, String telefone, String email, AuthCallback callback) {
        try {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            iso.setTimeZone(TimeZone.getTimeZone("UTC"));

            // Usamos a classe Perfil que criámos anteriormente na pasta models
            Perfil perfil = new Perfil(
                    userId,
                    nome,
                    username,
                    email,
                    dataNascimento,
                    telefone,

                    "", // foto_url vazia por agora
                    iso.format(new Date())
            );

            db.collection("perfis").document(userId)
                    .set(perfil)
                    .addOnSuccessListener(aVoid -> callback.onSucesso("Conta criada com sucesso! Podes fazer login."))
                    .addOnFailureListener(e -> callback.onSucesso("Conta criada, mas houve um erro ao guardar o perfil. Tenta fazer login."));

        } catch (Exception e) {
            callback.onErro("Erro ao estruturar os dados do perfil.");
        }
    }

    public void fazerLogin(String email, String password, AuthCallback callback) {
        if (email.isEmpty() || password.isEmpty()) {
            callback.onErro("Preenche o email e a password!");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Devolvemos o UID do utilizador no sucesso
                            callback.onSucesso(user.getUid());
                        }
                    } else {
                        callback.onErro("Email ou palavra-passe incorretos!");
                    }
                });
    }
}