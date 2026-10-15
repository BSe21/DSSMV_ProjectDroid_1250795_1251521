package com.example.bettergym.dtos;

import com.example.bettergym.model.Ginasio;
import org.json.JSONObject;

public class PlaceGymDTO {
    private String id;
    private String nome;
    private double latitude;
    private double longitude;

    public PlaceGymDTO() {}

    // Construtor que desserializa diretamente a partir de um JSONObject da Places API (New)
    public static PlaceGymDTO fromJson(JSONObject json) {
        PlaceGymDTO dto = new PlaceGymDTO();
        try {
            dto.id = json.optString("id", "");

            if (json.has("displayName")) {
                dto.nome = json.getJSONObject("displayName").optString("text", "Ginásio");
            } else {
                dto.nome = "Ginásio";
            }

            if (json.has("location")) {
                JSONObject loc = json.getJSONObject("location");
                dto.latitude = loc.optDouble("latitude", 0.0);
                dto.longitude = loc.optDouble("longitude", 0.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dto;
    }

    // Método que converte o DTO para o teu Model de domínio
    public Ginasio toModel() {
        return new Ginasio(
                this.id,
                this.nome,
                this.id, // google_place_id
                this.latitude,
                this.longitude
        );
    }

    // Getters
    public String getId() { return id; }
    public String getNome() { return nome; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}