package com.example.bettergym.service;

import com.example.bettergym.BuildConfig;
import com.example.bettergym.dto.Mapper;
import com.example.bettergym.dto.PerfilDTO;
import com.example.bettergym.helper.ImageHelper;
import com.example.bettergym.model.Perfil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.bettergym.handler.JsonHandler;
import com.example.bettergym.handler.NetworkHandler;

public class PerfilService {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();



    public interface PerfilCallback {
        void onSuccess(Perfil perfil);
        void onError(Exception e);
    }

    public interface ContagemCallback {
        void onResultado(long contagem);
    }

    public interface FotoCallback {
        void onSuccess(String fotoUrl);
        void onError(Exception e);
    }

    public String getCurrentUserId() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return null;
    }

    public void fazerLogout() {
        mAuth.signOut();
    }

    public void carregarPerfil(String uid, PerfilCallback callback) {
        new Thread(() -> {
            try {
                String url = BuildConfig.BASE_URL + "perfis/" + uid;
                String respostaJson = NetworkHandler.get(url, null);

                PerfilDTO dto = JsonHandler.deSerializeFirebaseJson2PerfilDTO(respostaJson);

                if (dto != null) {
                    Perfil perfil = Mapper.toModel(uid, dto);
                    callback.onSuccess(perfil);
                } else {
                    callback.onError(new Exception("Perfil não encontrado ou vazio."));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public void contarEstatistica(String colecao, String uid, ContagemCallback callback) {
        db.collection(colecao).whereEqualTo("user_id", uid)
                .count().get(AggregateSource.SERVER)
                .addOnSuccessListener(resultado -> callback.onResultado(resultado.getCount()))
                .addOnFailureListener(e -> callback.onResultado(0));
    }

    public void atualizarFotoPerfil(String uid, byte[] fotoBytes, FotoCallback callback) {
        if (uid == null || uid.isEmpty() || fotoBytes == null || fotoBytes.length == 0) {
            callback.onError(new Exception("Dados de imagem inválidos."));
            return;
        }

        String fotoUrl = ImageHelper.converterParaBase64(fotoBytes);
        db.collection("perfis").document(uid)
                .update("foto_url", fotoUrl)
                .addOnSuccessListener(aVoid -> callback.onSuccess(fotoUrl))
                .addOnFailureListener(callback::onError);
    }
}