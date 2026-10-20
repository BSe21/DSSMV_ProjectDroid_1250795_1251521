package com.example.bettergym.handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.bettergym.dto.PlaceGymDTO;

import java.util.ArrayList;
import java.util.List;

public class JsonHandler {

    public static List<PlaceGymDTO> deSerializeJson2ListPlaceGymDTO(String resp) throws JSONException {
        List<PlaceGymDTO> list = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(resp);

        if (!jsonObject.has("places")) return list;

        JSONArray jsonResponse = jsonObject.getJSONArray("places");

        for(int i = 0; i < jsonResponse.length(); i++){
            JSONObject jsonChildNode = jsonResponse.getJSONObject(i);

            String id = jsonChildNode.optString("id", "");
            String name = jsonChildNode.has("displayName") ? jsonChildNode.getJSONObject("displayName").optString("text", "Sem Nome") : "Sem Nome";

            double lat = 0.0, lng = 0.0;
            if (jsonChildNode.has("location")) {
                lat = jsonChildNode.getJSONObject("location").optDouble("latitude", 0.0);
                lng = jsonChildNode.getJSONObject("location").optDouble("longitude", 0.0);
            }

            double rating = jsonChildNode.optDouble("rating", 0.0);
            String morada = jsonChildNode.optString("formattedAddress", "");
            int numeroAvaliacoes = jsonChildNode.optInt("userRatingCount", 0);
            String referenciaFoto = "";
            if (jsonChildNode.has("photos") && jsonChildNode.getJSONArray("photos").length() > 0) {
                referenciaFoto = jsonChildNode.getJSONArray("photos").getJSONObject(0).optString("name", "");
            }

            // --- MUDANÇA AQUI: Navegar pelas "caixas" corretas da Google ---
            boolean isOpenNow = false;
            if (jsonChildNode.has("regularOpeningHours")) {
                isOpenNow = jsonChildNode.getJSONObject("regularOpeningHours").optBoolean("openNow", false);
            } else if (jsonChildNode.has("currentOpeningHours")) {
                isOpenNow = jsonChildNode.getJSONObject("currentOpeningHours").optBoolean("openNow", false);
            }
            // ----------------------------------------------------------------

            list.add(new PlaceGymDTO(id, name, lat, lng, rating, isOpenNow, morada, numeroAvaliacoes, referenciaFoto));
        }
        return list;
    }
}
