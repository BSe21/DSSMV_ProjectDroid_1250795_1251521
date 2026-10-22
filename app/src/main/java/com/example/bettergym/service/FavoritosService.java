package com.example.bettergym.service;

import com.example.bettergym.model.Favorito;
import com.example.bettergym.model.Ginasio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.TimeZone;

public class FavoritosService {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface EstadoCallback {
        void onResultado(boolean favorito);
        void onErro(Exception erro);
    }

    public interface ListaCallback {
        void onResultado(List<Favorito> favoritos);
        void onErro(Exception erro);
    }

    public interface EscritaCallback {
        void onSucesso();
        void onErro(Exception erro);
    }

    private String uidAtual() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) throw new IllegalStateException("Inicia sessão para consultar os favoritos.");
        return user.getUid();
    }

    private DocumentReference referencia(String uid, String ginasioId) {
        if (ginasioId == null || ginasioId.isEmpty() || ginasioId.contains("/")) {
            throw new IllegalArgumentException("Ginásio inválido.");
        }
        // Um documento por utilizador/ginásio evita favoritos duplicados.
        return db.collection("favoritos").document(uid + "_" + ginasioId);
    }

    public ListenerRegistration observarEstado(String ginasioId, EstadoCallback callback) {
        try {
            String uid = uidAtual();
            return referencia(uid, ginasioId).addSnapshotListener((doc, erro) -> {
                if (auth.getCurrentUser() == null || !uid.equals(auth.getCurrentUser().getUid())) return;
                if (erro != null) callback.onErro(erro);
                else if (doc != null) callback.onResultado(doc.exists());
            });
        } catch (Exception e) {
            callback.onErro(e);
            return null;
        }
    }

    public ListenerRegistration observarLista(ListaCallback callback) {
        try {
            String uid = uidAtual();
            return db.collection("favoritos").whereEqualTo("user_id", uid)
                    .addSnapshotListener((snapshot, erro) -> {
                        if (auth.getCurrentUser() == null || !uid.equals(auth.getCurrentUser().getUid())) return;
                        if (erro != null) {
                            callback.onErro(erro);
                            return;
                        }
                        if (snapshot == null) return;
                        List<Favorito> favoritos = new ArrayList<>();
                        try {
                            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                                Favorito favorito = doc.toObject(Favorito.class);
                                if (favorito != null && favorito.getGinasio() != null) favoritos.add(favorito);
                            }
                            favoritos.sort((a, b) -> {
                                String dataA = a.getData_adicionado() == null ? "" : a.getData_adicionado();
                                String dataB = b.getData_adicionado() == null ? "" : b.getData_adicionado();
                                return dataB.compareTo(dataA);
                            });
                            callback.onResultado(favoritos);
                        } catch (Exception e) {
                            callback.onErro(e);
                        }
                    });
        } catch (Exception e) {
            callback.onErro(e);
            return null;
        }
    }

    public void guardar(Ginasio ginasio, EscritaCallback callback) {
        try {
            String uid = uidAtual();
            DocumentReference ref = referencia(uid, ginasio.getId());
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            iso.setTimeZone(TimeZone.getTimeZone("UTC"));
            Favorito favorito = new Favorito(ref.getId(), uid, ginasio.getId(), iso.format(new Date()));
            favorito.setGinasio(ginasio);
            ref.set(favorito).addOnSuccessListener(ignored -> callback.onSucesso())
                    .addOnFailureListener(callback::onErro);
        } catch (Exception e) {
            callback.onErro(e);
        }
    }

    public void remover(String ginasioId, EscritaCallback callback) {
        try {
            referencia(uidAtual(), ginasioId).delete()
                    .addOnSuccessListener(ignored -> callback.onSucesso())
                    .addOnFailureListener(callback::onErro);
        } catch (Exception e) {
            callback.onErro(e);
        }
    }
}
