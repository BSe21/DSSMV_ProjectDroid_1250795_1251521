package com.example.bettergym.dto;

import com.example.bettergym.model.Ginasio;
import com.example.bettergym.model.Favorito;
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

    public static Favorito toModel(String id, FavoritoDTO dto) {
        if (dto == null) return null;
        Favorito favorito = new Favorito(id, dto.getUser_id(), dto.getGinasio_id(),
                dto.getData_adicionado());
        favorito.setGinasio(toModel(dto.getGinasio()));
        return favorito;
    }

    public static PlaceGymDTO toDTO(Ginasio ginasio) {
        if (ginasio == null) return null;
        return new PlaceGymDTO(ginasio.getId(), ginasio.getNome(), ginasio.getLatitude(),
                ginasio.getLongitude(), ginasio.getRating(), ginasio.isAbertoAgora(),
                ginasio.getMorada(), ginasio.getNumeroAvaliacoes(), ginasio.getReferenciaFoto());
    }

    public static FavoritoDTO toDTO(Favorito favorito) {
        if (favorito == null) return null;
        FavoritoDTO dto = new FavoritoDTO();
        dto.setId(favorito.getId());
        dto.setUser_id(favorito.getUser_id());
        dto.setGinasio_id(favorito.getGinasio_id());
        dto.setData_adicionado(favorito.getData_adicionado());
        dto.setGinasio(toDTO(favorito.getGinasio()));
        return dto;
    }

}
