package com.example.bettergym.service;

import com.example.bettergym.BuildConfig;
import com.example.bettergym.dto.Mapper;
import com.example.bettergym.dto.PerfilDTO;
import com.example.bettergym.model.Perfil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;
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

    public String getCurrentUserId() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return null;
    }

    public void fazerLogout() {
        mAuth.signOut();
    }

    // AQUI ESTÁ O FLUXO DO FIREBASE!
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

    // Estatísticas (Mantém-se igual pois devolve apenas um número)
    public void contarEstatistica(String colecao, String uid, ContagemCallback callback) {
        db.collection(colecao).whereEqualTo("user_id", uid)
                .count().get(AggregateSource.SERVER)
                .addOnSuccessListener(resultado -> callback.onResultado(resultado.getCount()))
                .addOnFailureListener(e -> callback.onResultado(0));
    }
}