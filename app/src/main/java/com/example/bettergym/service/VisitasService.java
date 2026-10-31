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

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public interface EscritaCallback {
        void onSucesso();
        void onErro(Exception erro);
    }

    public interface VisitasCallback {
        void onResultado(List<Visita> visitas);
        void onErro(Exception erro);
    }

    private String uidAtual() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            throw new InvalidDataException("Inicia sessão para registar uma visita.");
        }
        return user.getUid();
    }

    // -------------------------------------------------------------------
    // REGRAS DE NEGÓCIO DA VISITA (Responsabilidade da camada Service)
    // -------------------------------------------------------------------

    /**
     * Valida se o ginásio cumpre todos os requisitos para registo de visita:
     * 1. Utilizador autenticado
     * 2. Ginásio com dados válidos
     * 3. Ginásio aberto no momento do registo
     */
    public void validarRegrasVisita(Ginasio ginasio) throws InvalidDataException {
        uidAtual(); // Garante utilizador autenticado

        if (ginasio == null || ginasio.getId() == null || ginasio.getId().trim().isEmpty()) {
            throw new InvalidDataException("Ginásio inválido para registar visita.");
        }

        if (!ginasio.isAbertoAgora()) {
            throw new InvalidDataException("Não é possível registar visita: o ginásio encontra-se fechado de momento.");
        }
    }

    /**
     * Valida o conteúdo descodificado do código QR contra os dados do ginásio.
     */
    public void validarQrCodeGinasio(Ginasio ginasio, String qrCodeLido) throws InvalidDataException {
        validarRegrasVisita(ginasio);

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

    // -------------------------------------------------------------------
    // REGISTAR UMA NOVA VISITA POR QR CODE
    // Atualiza o array historico_ginasios_visitados do documento perfis/{uid}
    // -------------------------------------------------------------------
    public void registarVisita(Ginasio ginasio, EscritaCallback callback) {
        new Thread(() -> {
            try {
                // Aplicação da regra de negócio no Service antes de qualquer chamada de rede
                validarRegrasVisita(ginasio);

                String uid = uidAtual();

                // 1. Obter perfil atual do utilizador para carregar o histórico existente
                String urlGet = BuildConfig.BASE_URL + "perfis/" + uid;
                String respostaJson = NetworkHandler.get(urlGet, null);

                PerfilDTO perfilDTO = JsonHandler.deSerializeFirebaseJson2PerfilDTO(respostaJson);
                List<VisitaDTO> historico = new ArrayList<>();
                if (perfilDTO != null && perfilDTO.getHistorico_ginasios_visitados() != null) {
                    historico.addAll(perfilDTO.getHistorico_ginasios_visitados());
                }

                // 2. Adicionar nova visita com timestamp ISO 8601 UTC através do DateHelper
                String dataAtual = DateHelper.obterDataAtualIso();

                String nome = (ginasio.getNome() != null && !ginasio.getNome().trim().isEmpty())
                        ? ginasio.getNome().trim() : "Ginásio Visitado";
                Visita novaVisita = new Visita(ginasio.getId(), dataAtual, nome);
                historico.add(Mapper.toDTO(novaVisita));

                // 3. Serializar o novo array com o JsonHandler
                String jsonBody = JsonHandler.serializeHistoricoVisitasFirebaseJson(historico);

                // 4. Executar PATCH com updateMask para não sobrepor outros campos do perfil
                String urlPatch = BuildConfig.BASE_URL + "perfis/" + uid + "?updateMask.fieldPaths=historico_ginasios_visitados";
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

    // -------------------------------------------------------------------
    // OBTER LISTA DE TODAS AS VISITAS DO UTILIZADOR ATUAL
    // -------------------------------------------------------------------
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

                // Ordenar por data mais recente primeiro
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
