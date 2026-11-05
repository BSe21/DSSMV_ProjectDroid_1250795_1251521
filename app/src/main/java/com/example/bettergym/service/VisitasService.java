package com.example.bettergym.service;

import com.example.bettergym.BuildConfig;
import com.example.bettergym.dto.Mapper;
import com.example.bettergym.dto.PerfilDTO;
import com.example.bettergym.dto.VisitaDTO;
import com.example.bettergym.exceptions.InvalidDataException;
import com.example.bettergym.handler.JsonHandler;
import com.example.bettergym.handler.NetworkHandler;
import com.example.bettergym.helper.DateHelper;
import com.example.bettergym.model.Ginasio;
import com.example.bettergym.model.Visita;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisitasService {

    private final FirebaseAuth auth;

    public VisitasService() {
        FirebaseAuth instancia = null;
        try {
            instancia = FirebaseAuth.getInstance();
        } catch (Exception ignored) {
        }
        this.auth = instancia;
    }

    public VisitasService(FirebaseAuth auth) {
        this.auth = auth;
    }

    public interface EscritaCallback {
        void onSucesso();

        void onErro(Exception erro);
    }

    public interface VisitasCallback {
        void onResultado(List<Visita> visitas);

        void onErro(Exception erro);
    }

    private String uidAtual() {
        if (auth == null) {
            throw new InvalidDataException("Inicia sessão para registar uma visita.");
        }
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            throw new InvalidDataException("Inicia sessão para registar uma visita.");
        }
        return user.getUid();
    }

    public void validarRegrasGinasio(Ginasio ginasio) throws InvalidDataException {
        if (ginasio == null || ginasio.getId() == null || ginasio.getId().trim().isEmpty()) {
            throw new InvalidDataException("Ginásio inválido para registar visita.");
        }

        if (!ginasio.isAbertoAgora()) {
            throw new InvalidDataException("Não é possível registar visita: o ginásio encontra-se fechado de momento.");
        }
    }

    public void validarRegrasVisita(Ginasio ginasio) throws InvalidDataException {
        if (auth != null) {
            uidAtual();
        }
        validarRegrasGinasio(ginasio);
    }

    public void validarQrCodeGinasio(Ginasio ginasio, String qrCodeLido) throws InvalidDataException {
        validarRegrasGinasio(ginasio);

        if (qrCodeLido == null || qrCodeLido.trim().isEmpty()) {
            throw new InvalidDataException("Código QR vazio ou ilegível.");
        }

        String texto = qrCodeLido.trim();
        String gymId = ginasio.getId().trim();
        String gymNome = ginasio.getNome() != null ? ginasio.getNome().trim() : "";

        boolean valido = texto.equalsIgnoreCase(gymId)
                || texto.contains(gymId)
                || (gymId.contains(texto) && !texto.isEmpty())
                || (!gymNome.isEmpty() && texto.toLowerCase().contains(gymNome.toLowerCase()));

        if (!valido) {
            throw new InvalidDataException("Código QR inválido. Não corresponde a este ginásio.");
        }
    }

    public void registarVisita(Ginasio ginasio, EscritaCallback callback) {
        new Thread(() -> {
            try {
                validarRegrasVisita(ginasio);

                String uid = uidAtual();

                String urlGet = BuildConfig.BASE_URL + "perfis/" + uid;
                String respostaJson = NetworkHandler.get(urlGet, null);

                PerfilDTO perfilDTO = JsonHandler.deSerializeFirebaseJson2PerfilDTO(respostaJson);
                List<VisitaDTO> historico = new ArrayList<>();
                if (perfilDTO != null && perfilDTO.getHistorico_ginasios_visitados() != null) {
                    historico.addAll(perfilDTO.getHistorico_ginasios_visitados());
                }

                String dataAtual = DateHelper.obterDataAtualIso();

                String nome = (ginasio.getNome() != null && !ginasio.getNome().trim().isEmpty())
                        ? ginasio.getNome().trim()
                        : "Ginásio Visitado";
                Visita novaVisita = new Visita(ginasio.getId(), dataAtual, nome);
                historico.add(Mapper.toDTO(novaVisita));

                String jsonBody = JsonHandler.serializeHistoricoVisitasFirebaseJson(historico);

                String urlPatch = BuildConfig.BASE_URL + "perfis/" + uid
                        + "?updateMask.fieldPaths=historico_ginasios_visitados";
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                NetworkHandler.patch(urlPatch, jsonBody, headers);

                callback.onSucesso();
            } catch (Exception e) {
                callback.onErro(e);
            }
        }).start();
    }

    public void registarVisita(String ginasioId, String nomeGinasio, EscritaCallback callback) {
        Ginasio g = new Ginasio();
        g.setId(ginasioId);
        g.setNome(nomeGinasio);
        g.setAbertoAgora(true);
        registarVisita(g, callback);
    }

    public void registarVisita(String ginasioId, EscritaCallback callback) {
        registarVisita(ginasioId, "", callback);
    }

    public void obterVisitas(VisitasCallback callback) {
        new Thread(() -> {
            try {
                String uid = uidAtual();
                String url = BuildConfig.BASE_URL + "perfis/" + uid;
                String respostaJson = NetworkHandler.get(url, null);

                PerfilDTO perfilDTO = JsonHandler.deSerializeFirebaseJson2PerfilDTO(respostaJson);
                List<Visita> visitas = new ArrayList<>();

                if (perfilDTO != null && perfilDTO.getHistorico_ginasios_visitados() != null) {
                    for (VisitaDTO vDto : perfilDTO.getHistorico_ginasios_visitados()) {
                        Visita v = Mapper.toModel(vDto);
                        if (v != null) {
                            visitas.add(v);
                        }
                    }
                }

                visitas.sort((a, b) -> {
                    String dataA = a.getData_visita() != null ? a.getData_visita() : "";
                    String dataB = b.getData_visita() != null ? b.getData_visita() : "";
                    return dataB.compareTo(dataA);
                });

                callback.onResultado(visitas);
            } catch (Exception e) {
                callback.onErro(e);
            }
        }).start();
    }
}
