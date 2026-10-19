package com.example.bettergym.controllers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.example.bettergym.dtos.PerfilDTO;
import com.example.bettergym.exceptions.DataConsistencyException;
import com.example.bettergym.exceptions.InvalidDataException;
import com.example.bettergym.model.Perfil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PerfilController {

    private static final String COLECAO_PERFIS = "perfis";
    private static final String COLECAO_VISITAS = "visitas";
    private static final String COLECAO_FAVORITOS = "favoritos";
    private static final String COLECAO_AVALIACOES = "avaliacoes";
    private static final String CAMPO_ID_UTILIZADOR = "user_id";

    private static final int TEMPO_LIGACAO_MS = 10000;
    private static final int TEMPO_LEITURA_MS = 15000;

    private final FirebaseAuth autenticacao = FirebaseAuth.getInstance();
    private final FirebaseFirestore baseDados = FirebaseFirestore.getInstance();
    private final Handler threadPrincipal = new Handler(Looper.getMainLooper());

    // Devolve o perfil carregado, ou o motivo do erro.
    public interface PerfilCallback {
        void onSucesso(Perfil perfil);

        void onErro(String erro);
    }

    // Devolve um total de registos (visitas, favoritos ou avaliações), ou o motivo do erro.
    public interface ContagemCallback {
        void onSucesso(long total);

        void onErro(String erro);
    }

    public interface FotoCallback {
        void onSucesso(Bitmap foto);

        void onErro(String erro);
    }

    //Indica se há um utilizador com a sessão iniciada.
    public boolean existeSessao() {
        return autenticacao.getCurrentUser() != null;
    }

    //Devolve o identificador do utilizador com a sessão iniciada.
    public String obterIdUtilizador() {
        FirebaseUser utilizador = autenticacao.getCurrentUser();
        if (utilizador == null) {
            throw new InvalidDataException("Não há nenhuma sessão iniciada.");
        }
        return utilizador.getUid();
    }

    //Carrega o perfil do utilizador a partir do Firestore.
    public void carregarPerfil(PerfilCallback callback) {
        String idUtilizador = obterIdUtilizador();

        baseDados.collection(COLECAO_PERFIS).document(idUtilizador).get()
                .addOnSuccessListener(documento -> {
                    try {
                        PerfilDTO dto = documento.toObject(PerfilDTO.class);
                        if (dto == null) {
                            throw new DataConsistencyException(
                                    "A conta existe, mas não tem perfil guardado.");
                        }
                        callback.onSucesso(dto.toModel());
                    } catch (DataConsistencyException e) {
                        callback.onErro(e.what());
                    } catch (RuntimeException e) {
                        callback.onErro("Os dados do perfil estão num formato inválido.");
                    }
                })
                .addOnFailureListener(e -> callback.onErro("Não foi possível carregar o perfil."));
    }

    public void contarVisitas(ContagemCallback callback) {
        contarRegistos(COLECAO_VISITAS, callback);
    }

    public void contarFavoritos(ContagemCallback callback) {
        contarRegistos(COLECAO_FAVORITOS, callback);
    }

    public void contarAvaliacoes(ContagemCallback callback) {
        contarRegistos(COLECAO_AVALIACOES, callback);
    }

    private void contarRegistos(String colecao, ContagemCallback callback) {
        String idUtilizador = obterIdUtilizador();

        baseDados.collection(colecao).whereEqualTo(CAMPO_ID_UTILIZADOR, idUtilizador)
                .count().get(AggregateSource.SERVER)
                .addOnSuccessListener(resultado -> callback.onSucesso(resultado.getCount()))
                .addOnFailureListener(e -> callback.onErro("Não foi possível contar os registos."));
    }

    //Descarrega a foto de perfil. O descarregamento é feito numa thread de trabalh
    // (não bloqueia a thread principal) e o resultado volta pela thread principal.
    public void carregarFoto(String urlFoto, FotoCallback callback) {
        if (urlFoto == null || !urlFoto.startsWith("http")) {
            throw new InvalidDataException("O endereço da foto não é válido.");
        }

        new Thread(() -> {
            HttpURLConnection ligacao = null;
            try {
                ligacao = (HttpURLConnection) new URL(urlFoto).openConnection();
                ligacao.setConnectTimeout(TEMPO_LIGACAO_MS);
                ligacao.setReadTimeout(TEMPO_LEITURA_MS);

                try (InputStream entrada = ligacao.getInputStream()) {
                    Bitmap foto = BitmapFactory.decodeStream(entrada);
                    if (foto == null) {
                        threadPrincipal.post(() -> callback.onErro("A imagem não pôde ser lida."));
                    } else {
                        threadPrincipal.post(() -> callback.onSucesso(foto));
                    }
                }
            } catch (Exception e) {
                threadPrincipal.post(() -> callback.onErro("Não foi possível descarregar a foto."));
            } finally {
                if (ligacao != null) {
                    ligacao.disconnect();
                }
            }
        }).start();
    }

    // Termina a sessão do utilizador no Firebase.
    public void terminarSessao() {
        autenticacao.signOut();
    }
}