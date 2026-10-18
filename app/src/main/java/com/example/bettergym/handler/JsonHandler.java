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
            double rating = jsonChildNode.optDouble("rating", 0.0); // Default to 0.0 if not present

            // Apagas as linhas do rating e do address

            // Passas apenas os 4 parâmetros que o teu DTO pede
            list.add(new PlaceGymDTO(id, name, lat, lng, rating));
        }
        return list;
    }
}