package com.example.bettergym.service;

import com.example.bettergym.dto.Mapper;
import com.example.bettergym.dto.PlaceGymDTO;
import com.example.bettergym.handler.JsonHandler;
import com.example.bettergym.handler.NetworkHandler;
import com.example.bettergym.model.Ginasio;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlacesService {

    private static final String GOOGLE_PLACES_API_KEY = "AIzaSyCKax_upBuMHtXzfmrAE6ItVybCaS-c-ug"; // A tua chave
    private static final String URL = "https://places.googleapis.com/v1/places:searchNearby";

    public interface OnGymsLoadedListener {
        // Chamado pela worker thread; a View encaminha as atualizações para a UI Thread.
        default void onProgress(int percentagem) {}
        void onSuccess(List<Ginasio> ginasios);
        void onError(Exception e);
    }

    public void pesquisarGinasiosProximos(LatLng posicao, OnGymsLoadedListener listener) {
        new Thread(() -> {
            try {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Goog-Api-Key", GOOGLE_PLACES_API_KEY);
                // MUDANÇA AQUI: Usar regularOpeningHours em vez de currentOpeningHours para evitar o 503
                headers.put("X-Goog-FieldMask", "places.id,places.displayName,places.location,places.formattedAddress,places.rating,places.userRatingCount,places.regularOpeningHours.openNow,places.photos");

                String jsonInputString = "{"
                        + "\"includedTypes\": [\"gym\"],"
                        + "\"maxResultCount\": 20,"
                        + "\"locationRestriction\": {"
                        + "  \"circle\": {"
                        + "    \"center\": {"
                        + "      \"latitude\": " + posicao.latitude + ","
                        + "      \"longitude\": " + posicao.longitude
                        + "    },"
                        + "    \"radius\": 5000.0"
                        + "  }"
                        + "}"
                        + "}";

                String respostaJson = NetworkHandler.post(URL, jsonInputString, headers);
                listener.onProgress(70);
                List<PlaceGymDTO> dtos = JsonHandler.deSerializeJson2ListPlaceGymDTO(respostaJson);
                List<Ginasio> ginasios = Mapper.toModelList(dtos);
                listener.onProgress(85);

                listener.onSuccess(ginasios);

            } catch (Exception e) {
                android.util.Log.e("PLACES_ERRO", "Erro no serviço: " + e.getMessage());
                listener.onError(e);
            }
        }, "BetterGym-Places").start();
    }

    /**
     * Constrói o endpoint Place Photos (New) a partir do nome devolvido pelo Nearby Search.
     */
    public static String obterUrlFoto(String referenciaFoto) {
        if (referenciaFoto == null || referenciaFoto.isEmpty()) return null;
        return "https://places.googleapis.com/v1/" + referenciaFoto
                + "/media?maxWidthPx=1200&key=" + GOOGLE_PLACES_API_KEY;
    }
}
