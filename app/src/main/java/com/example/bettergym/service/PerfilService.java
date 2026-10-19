package com.example.bettergym.service;

import com.example.bettergym.dto.Mapper;
import com.example.bettergym.dto.PerfilDTO;
import com.example.bettergym.model.Perfil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;

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
        db.collection("perfis").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // 1. Firebase carrega os dados brutos para o DTO
                        PerfilDTO dto = doc.toObject(PerfilDTO.class);

                        // 2. O Mapper traduz o DTO para o teu Modelo (passando o uid como ID!)
                        Perfil perfil = Mapper.toModel(uid, dto);

                        // 3. Devolvemos o Modelo pronto a usar na UI
                        callback.onSuccess(perfil);
                    } else {
                        callback.onError(new Exception("Perfil não encontrado."));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    // Estatísticas (Mantém-se igual pois devolve apenas um número)
    public void contarEstatistica(String colecao, String uid, ContagemCallback callback) {
        db.collection(colecao).whereEqualTo("user_id", uid)
                .count().get(AggregateSource.SERVER)
                .addOnSuccessListener(resultado -> callback.onResultado(resultado.getCount()))
                .addOnFailureListener(e -> callback.onResultado(0));
    }
}