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

    private static final String GOOGLE_PLACES_API_KEY = "AIzaSyCKax_upBuMHtXzfmrAE6ItVybCaS-c-ug"; // Mantém a tua chave
    private static final String URL = "https://places.googleapis.com/v1/places:searchNearby";

    // O Callback para comunicar com a tua View (o MapaFragment)
    public interface OnGymsLoadedListener {
        void onSuccess(List<Ginasio> ginasios);
        void onError(Exception e);
    }

    public void pesquisarGinasiosProximos(LatLng posicao, OnGymsLoadedListener listener) {
        new Thread(() -> {
            try {
                // 1. Configurar os Cabeçalhos (Headers)
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Goog-Api-Key", GOOGLE_PLACES_API_KEY);
                headers.put("X-Goog-FieldMask", "places.id,places.displayName,places.location,places.formattedAddress,places.rating,places.userRatingCount,places.currentOpeningHours.openNow");

                // 2. Configurar o Corpo do Pedido (Body)
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

                // 3. Fazer o pedido HTTP usando o NetworkHandler
                String respostaJson = NetworkHandler.post(URL, jsonInputString, headers);

                // 4. Converter a String JSON em DTOs usando o JsonHandler
                List<PlaceGymDTO> dtos = JsonHandler.deSerializeJson2ListPlaceGymDTO(respostaJson);

                // 5. Mapear os DTOs para Modelos de Negócio (Ginasio) usando o Mapper
                List<Ginasio> ginasios = Mapper.toModelList(dtos);

                // 6. Devolver a lista limpa para a Interface
                listener.onSuccess(ginasios);

            } catch (Exception e) {
                android.util.Log.e("PLACES_ERRO", "Erro no serviço: " + e.getMessage());
                listener.onError(e);
            }
        }).start();
    }
}