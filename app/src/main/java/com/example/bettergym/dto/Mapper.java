package com.example.bettergym.dto;

import com.example.bettergym.model.Ginasio;
import com.example.bettergym.model.Perfil;
import java.util.ArrayList;
import java.util.List;

public class Mapper {

    public static Ginasio toModel(PlaceGymDTO dto) {
        if (dto == null) return null;
        return new Ginasio(
                dto.getId(),
                dto.getNome(),
                dto.getLatitude(),
                dto.getLongitude(),
                dto.getRating(),
                dto.isAbertoAgora(),
                dto.getMorada(),
                dto.getNumeroAvaliacoes(),
                dto.getReferenciaFoto()
        );
    }

    public static List<Ginasio> toModelList(List<PlaceGymDTO> dtos) {
        List<Ginasio> models = new ArrayList<>();
        for (PlaceGymDTO dto : dtos) {
            models.add(toModel(dto));
        }
        return models;
    }



    public static Perfil toModel(String id, PerfilDTO dto) {
        if (dto == null) return null;

        // Passar os dados EXATAMENTE na ordem do construtor:
        // id, nome, username, email, nascimento, telemovel, foto, registo
        return new Perfil(
                id,
                dto.getNome_completo(),
                dto.getUsername(),
                dto.getEmail(),
                dto.getData_nascimento(),
                dto.getNumero_telemovel(),
                dto.getFoto_url(),
                dto.getData_registo()
        );
    }

}
