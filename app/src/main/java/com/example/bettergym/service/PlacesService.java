package com.example.bettergym.service;

import com.example.bettergym.BuildConfig;
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

    private static final String URL = BuildConfig.GOOGLE_SEARCH_NEARBY_URL;

    public interface OnGymsLoadedListener {
        default void onProgress(int percentagem) {
        }

        void onSuccess(List<Ginasio> ginasios);

        void onError(Exception e);
    }

    public void pesquisarGinasiosProximos(LatLng posicao, OnGymsLoadedListener listener) {
        new Thread(() -> {
            try {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Goog-Api-Key", BuildConfig.GOOGLE_PLACES_API_KEY);
                headers.put("X-Goog-FieldMask",
                        "places.id,places.displayName,places.location,places.formattedAddress,places.regularOpeningHours.openNow");

                String jsonInputString = "{"
                        + "\"includedPrimaryTypes\": [\"fitness_center\", \"gym\"],"
                        + "\"maxResultCount\": 20,"
                        + "\"rankPreference\": \"DISTANCE\","
                        + "\"locationRestriction\": {"
                        + "  \"circle\": {"
                        + "    \"center\": {"
                        + "      \"latitude\": " + posicao.latitude + ","
                        + "      \"longitude\": " + posicao.longitude
                        + "    },"
                        + "    \"radius\": 10000.0"
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

    public static String obterUrlFoto(String referenciaFoto) {
        if (referenciaFoto == null || referenciaFoto.isEmpty())
            return null;
        return "https://places.googleapis.com/v1/" + referenciaFoto
                + "/media?maxWidthPx=1200&key=" + BuildConfig.GOOGLE_PLACES_API_KEY;
    }

    public interface OnGymDetailsLoadedListener {
        void onSuccess(Ginasio ginasio);

        void onError(Exception e);
    }

    public void obterDetalhesGinasio(String placeId, OnGymDetailsLoadedListener listener) {
        new Thread(() -> {
            try {
                String url = BuildConfig.GOOGLE_API_URL + placeId;

                Map<String, String> headers = new HashMap<>();
                headers.put("X-Goog-Api-Key", BuildConfig.GOOGLE_PLACES_API_KEY);
                headers.put("X-Goog-FieldMask",
                        "id,displayName,location,formattedAddress,rating,userRatingCount,regularOpeningHours.openNow,photos,reviews");
                headers.put("Accept-Language", "pt-PT");
                String respostaJson = NetworkHandler.get(url, headers);

                String jsonAdaptado = "{\"places\":[" + respostaJson + "]}";

                List<PlaceGymDTO> dtos = JsonHandler.deSerializeJson2ListPlaceGymDTO(jsonAdaptado);

                if (dtos != null && !dtos.isEmpty()) {
                    Ginasio ginasioReal = Mapper.toModel(dtos.get(0));
                    listener.onSuccess(ginasioReal);
                } else {
                    listener.onError(new Exception("Ginásio não encontrado."));
                }
            } catch (Exception e) {
                listener.onError(e);
            }
        }, "BetterGym-PlaceDetails").start();
    }
}
