package com.example.bettergym.service;

import com.example.bettergym.BuildConfig;
import com.example.bettergym.dto.FavoritoDTO;
import com.example.bettergym.dto.Mapper;
import com.example.bettergym.exceptions.InvalidDataException;
import com.example.bettergym.handler.JsonHandler;
import com.example.bettergym.handler.NetworkHandler;
import com.example.bettergym.model.Favorito;
import com.example.bettergym.model.Ginasio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class FavoritosService {
    // Mantemos apenas a autenticação, o Firestore desaparece daqui!
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

    // -------------------------------------------------------------------
    // 1. VERIFICAR SE É FAVORITO (GET Simples)
    // Era o antigo 'observarEstado'
    // -------------------------------------------------------------------
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
                    // Tenta ler o documento. Se não der erro, existe (200 OK)!
                    NetworkHandler.get(url, null);
                    callback.onResultado(true);
                } catch (Exception e) {
                    // Se o erro for 404 Not Found, significa que o favorito não existe
                    if (e.getMessage() != null && e.getMessage().contains("404")) {
                        callback.onResultado(false);
                    } else {
                        throw e; // Outro erro qualquer (ex: sem internet)
                    }
                }
            } catch (Exception e) {
                callback.onErro(e);
            }
        }).start();
    }

    // -------------------------------------------------------------------
    // 2. OBTER LISTA DE FAVORITOS (POST com StructuredQuery)
    // Era o antigo 'observarLista'
    // -------------------------------------------------------------------
    public void obterLista(ListaCallback callback) {
        new Thread(() -> {
            try {
                String uid = uidAtual();
                // Endpoint para fazer queries (pesquisas avançadas)
                String url = BuildConfig.BASE_URL.replace("/documents/", "/documents:runQuery");

                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                // A Query: "Vai à coleção 'favoritos' onde 'user_id' == UID"
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

                // 1. O Carteiro (NetworkHandler)
                String respostaJson = NetworkHandler.post(url, jsonBody, headers);

                // 2. O Desempacotador (JsonHandler) - Transforma em DTOs
                List<FavoritoDTO> listaDtos = JsonHandler.deSerializeFirebaseJson2ListFavoritoDTO(respostaJson);

                // 3. O Organizador (Mapper) - Transforma em Modelos
                List<Favorito> favoritos = new ArrayList<>();
                for (FavoritoDTO dto : listaDtos) {
                    Favorito f = Mapper.toModel(dto.getId(), dto);
                    // Como combinámos, já não guardamos o modelo gigante do ginásio no firebase,
                    // por isso se tiveres lá os dados básicos (ID e Nome), chegam para desenhar o ecrã.
                    if (f != null) {
                        favoritos.add(f);
                    }
                }

                // 4. Ordenar por data
                favoritos.sort((a, b) -> {
                    String dataA = a.getData_adicionado() == null ? "" : a.getData_adicionado();
                    String dataB = b.getData_adicionado() == null ? "" : b.getData_adicionado();
                    return dataB.compareTo(dataA); // Mais recentes primeiro
                });

                // Devolvemos à view
                callback.onResultado(favoritos);

            } catch (Exception e) {
                callback.onErro(e);
            }
        }).start();
    }

    // -------------------------------------------------------------------
    // 3. GUARDAR NOVO FAVORITO (PATCH)
    // -------------------------------------------------------------------
    public void guardar(Ginasio ginasio, EscritaCallback callback) {
        new Thread(() -> {
            try {
                if (ginasio == null || ginasio.getId() == null || ginasio.getId().trim().isEmpty()) {
                    throw new InvalidDataException("Ginásio inválido para guardar.");
                }
                String uid = uidAtual();
                String docId = uid + "_" + ginasio.getId();
                String url = BuildConfig.BASE_URL + "favoritos/" + docId;

                SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                iso.setTimeZone(TimeZone.getTimeZone("UTC"));
                String dataAtual = iso.format(new Date());

                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                // Montar o JSON com os dados que combinámos guardar (ID, Data e o Nome para não estar a ir buscar à Google Places de propósito)
                String jsonBody = "{"
                        + "\"fields\": {"
                        + "  \"user_id\": { \"stringValue\": \"" + uid + "\" },"
                        + "  \"ginasio_id\": { \"stringValue\": \"" + ginasio.getId() + "\" },"
                        + "  \"data_adicionado\": { \"stringValue\": \"" + dataAtual + "\" },"
                        + "  \"nome\": { \"stringValue\": \"" + ginasio.getNome() + "\" }"
                        + "}"
                        + "}";

                // Usamos PATCH para criar ou atualizar o documento sem apagar o resto
                NetworkHandler.patch(url, jsonBody, headers);

                callback.onSucesso();
            } catch (Exception e) {
                callback.onErro(e);
            }
        }).start();
    }

    // -------------------------------------------------------------------
    // 4. REMOVER FAVORITO (DELETE)
    // -------------------------------------------------------------------
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