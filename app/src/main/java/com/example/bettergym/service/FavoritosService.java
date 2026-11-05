package com.example.bettergym.service;

import com.example.bettergym.BuildConfig;
import com.example.bettergym.dto.FavoritoDTO;
import com.example.bettergym.dto.Mapper;
import com.example.bettergym.exceptions.InvalidDataException;
import com.example.bettergym.handler.JsonHandler;
import com.example.bettergym.handler.NetworkHandler;
import com.example.bettergym.helper.DateHelper;
import com.example.bettergym.model.Favorito;
import com.example.bettergym.model.Ginasio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoritosService {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();


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
        if (user == null) throw new InvalidDataException("Inicia sessão para consultar os favoritos.");
        return user.getUid();
    }

    public void verificarEstado(String ginasioId, EstadoCallback callback) {
        new Thread(() -> {
            try {
                if (ginasioId == null || ginasioId.isEmpty() || ginasioId.contains("/")) {
                    throw new InvalidDataException("Ginásio inválido.");
                }

                String uid = uidAtual();
                String docId = uid + "_" + ginasioId;
                String url = BuildConfig.BASE_URL + "favoritos/" + docId;

                try {
                    NetworkHandler.get(url, null);
                    callback.onResultado(true);
                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().contains("404")) {
                        callback.onResultado(false);
                    } else {
                        throw e;
                    }
                }
            } catch (Exception e) {
                callback.onErro(e);
            }
        }).start();
    }

    public void obterLista(ListaCallback callback) {
        new Thread(() -> {
            try {
                String uid = uidAtual();
                String url = BuildConfig.BASE_URL.replace("/documents/", "/documents:runQuery");

                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                String jsonBody = "{"
                        + "\"structuredQuery\": {"
                        + "  \"from\": [{\"collectionId\": \"favoritos\"}],"
                        + "  \"where\": {"
                        + "    \"fieldFilter\": {"
                        + "      \"field\": {\"fieldPath\": \"user_id\"},"
                        + "      \"op\": \"EQUAL\","
                        + "      \"value\": {\"stringValue\": \"" + uid + "\"}"
                        + "    }"
                        + "  }"
                        + "}"
                        + "}";

                String respostaJson = NetworkHandler.post(url, jsonBody, headers);

                List<FavoritoDTO> listaDtos = JsonHandler.deSerializeFirebaseJson2ListFavoritoDTO(respostaJson);

                List<Favorito> favoritos = new ArrayList<>();
                for (FavoritoDTO dto : listaDtos) {
                    Favorito f = Mapper.toModel(dto.getId(), dto);
                    if (f != null) {
                        favoritos.add(f);
                    }
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
        }).start();
    }

    public void guardar(Ginasio ginasio, EscritaCallback callback) {
        new Thread(() -> {
            try {
                if (ginasio == null || ginasio.getId() == null || ginasio.getId().trim().isEmpty()) {
                    throw new InvalidDataException("Ginásio inválido para guardar.");
                }
                String uid = uidAtual();
                String docId = uid + "_" + ginasio.getId();
                String url = BuildConfig.BASE_URL + "favoritos/" + docId;

                String dataAtual = DateHelper.obterDataAtualIso();

                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                String jsonBody = "{"
                        + "\"fields\": {"
                        + "  \"user_id\": { \"stringValue\": \"" + uid + "\" },"
                        + "  \"ginasio_id\": { \"stringValue\": \"" + ginasio.getId() + "\" },"
                        + "  \"data_adicionado\": { \"stringValue\": \"" + dataAtual + "\" },"
                        + "  \"nome\": { \"stringValue\": \"" + ginasio.getNome() + "\" }"
                        + "}"
                        + "}";

                NetworkHandler.patch(url, jsonBody, headers);

                callback.onSucesso();
            } catch (Exception e) {
                callback.onErro(e);
            }
        }).start();
    }

    public void remover(String ginasioId, EscritaCallback callback) {
        new Thread(() -> {
            try {
                if (ginasioId == null || ginasioId.trim().isEmpty()) {
                    throw new InvalidDataException("Ginásio inválido para remover.");
                }
                String uid = uidAtual();
                String docId = uid + "_" + ginasioId;
                String url = BuildConfig.BASE_URL + "favoritos/" + docId;

                NetworkHandler.delete(url, null);

                callback.onSucesso();
            } catch (Exception e) {
                callback.onErro(e);
            }
        }).start();
    }
}