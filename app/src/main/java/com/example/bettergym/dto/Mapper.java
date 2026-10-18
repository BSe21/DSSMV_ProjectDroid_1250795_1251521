package com.example.bettergym.dto;

import com.example.bettergym.model.Ginasio;
import java.util.ArrayList;
import java.util.List;

public class Mapper {

    public static Ginasio toModel(PlaceGymDTO dto) {
        if (dto == null) return null;
        return new Ginasio(dto.getId(), dto.getNome(),dto.getLatitude() ,dto.getLongitude(), dto.getRating());
    }

    public static List<Ginasio> toModelList(List<PlaceGymDTO> dtos) {
        List<Ginasio> models = new ArrayList<>();
        for (PlaceGymDTO dto : dtos) {
            models.add(toModel(dto));
        }
        return models;
    }
}