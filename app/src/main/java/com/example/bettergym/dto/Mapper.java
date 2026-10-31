package com.example.bettergym.dto;

import com.example.bettergym.exceptions.DataConsistencyException;
import com.example.bettergym.model.Ginasio;
import com.example.bettergym.model.Favorito;
import com.example.bettergym.model.Perfil;
import com.example.bettergym.model.Avaliacao;
import com.example.bettergym.model.Visita;
import java.util.ArrayList;
import java.util.List;

public class Mapper {

    public static Ginasio toModel(PlaceGymDTO dto) {
        if (dto == null) return null;
        if (dto.getId() == null || dto.getId().trim().isEmpty()) {
            throw new DataConsistencyException("Inconsistência nos dados do ginásio: ID em falta.");
        }

        // 1. Criamos a variável 'ginasio' primeiro
        Ginasio ginasio = new Ginasio(
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

        if (dto.getAvaliacoes() != null) {
            List<Avaliacao> listaAvaliacoesModel = new ArrayList<>();
            for (AvaliacaoDTO avaliacaoDTO : dto.getAvaliacoes()) {
                listaAvaliacoesModel.add(toModel(avaliacaoDTO));
            }
            ginasio.setAvaliacoes(listaAvaliacoesModel);
        }

        if (dto.getFotos() != null) {
            ginasio.setFotos(dto.getFotos());
        }

        return ginasio;
    }

    public static List<Ginasio> toModelList(List<PlaceGymDTO> dtos) {
        List<Ginasio> models = new ArrayList<>();
        if (dtos == null) return models;
        for (PlaceGymDTO dto : dtos) {
            if (isGinasioValido(dto)) {
                models.add(toModel(dto));
            }
        }
        return models;
    }

    private static boolean isGinasioValido(PlaceGymDTO dto) {
        if (dto == null || dto.getNome() == null) return false;
        String nome = dto.getNome().toLowerCase().trim();

        // Se tiver palavras diretamente associadas a ginásios/fitness, aceita sempre
        if (nome.contains("ginásio") || nome.contains("ginasio") || nome.contains("gym") ||
                nome.contains("fitness") || nome.contains("crossfit") || nome.contains("studio") ||
                nome.contains("training") || nome.contains("workout")) {
            // Exceto se for calistenia de rua / parque público
            if (nome.contains("street workout")) return false;
            return true;
        }

        // Excluir recintos desportivos que não são ginásios convencionais para frequentar
        if (nome.contains("estádio") || nome.contains("estadio") ||
                nome.contains("futebol") || nome.contains("basquetebol") ||
                nome.contains("piscina") || nome.contains("polidesportivo") ||
                nome.contains("parque infantil") || nome.contains("street workout")) {
            return false;
        }

        return true;
    }

    public static Perfil toModel(String id, PerfilDTO dto) {
        if (dto == null) return null;
        if (id == null || id.trim().isEmpty()) {
            throw new DataConsistencyException("Inconsistência nos dados do perfil: ID de utilizador em falta.");
        }

        List<Visita> historicoModel = new ArrayList<>();
        if (dto.getHistorico_ginasios_visitados() != null) {
            for (VisitaDTO vDto : dto.getHistorico_ginasios_visitados()) {
                Visita v = toModel(vDto);
                if (v != null) {
                    historicoModel.add(v);
                }
            }
        }

        return new Perfil(
                id,
                dto.getNome_completo(),
                dto.getUsername(),
                dto.getEmail(),
                dto.getData_nascimento(),
                dto.getNumero_telemovel(),
                dto.getFoto_url(),
                dto.getData_registo(),
                historicoModel
        );
    }

    public static Favorito toModel(String id, FavoritoDTO dto) {
        if (dto == null) return null;
        if (id == null || id.trim().isEmpty()) {
            throw new DataConsistencyException("Inconsistência nos dados do favorito: ID em falta.");
        }
        Favorito favorito = new Favorito(id, dto.getUser_id(), dto.getGinasio_id(),
                dto.getData_adicionado());
        favorito.setGinasio(toModel(dto.getGinasio()));
        return favorito;
    }

    public static PlaceGymDTO toDTO(Ginasio ginasio) {
        if (ginasio == null) return null;
        PlaceGymDTO dto = new PlaceGymDTO(ginasio.getId(), ginasio.getNome(), ginasio.getLatitude(),
                ginasio.getLongitude(), ginasio.getRating(), ginasio.isAbertoAgora(),
                ginasio.getMorada(), ginasio.getNumeroAvaliacoes(), ginasio.getReferenciaFoto());
        dto.setFotos(ginasio.getFotos());
        return dto;
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

    public static AvaliacaoDTO toDTO(Avaliacao avaliacao) {
        if (avaliacao == null) return null;
        AvaliacaoDTO dto = new AvaliacaoDTO();
        dto.setId(avaliacao.getId());
        dto.setUser_id(avaliacao.getUser_id());
        dto.setGinasio_id(avaliacao.getGinasio_id());
        dto.setNota(avaliacao.getNota());
        dto.setDescricao(avaliacao.getDescricao());
        dto.setData_avaliacao(avaliacao.getData_avaliacao());
        return dto;
    }

    public static Avaliacao toModel(AvaliacaoDTO dto) {
        if (dto == null) return null;

        return new Avaliacao(
                dto.getId(),
                dto.getUser_id(),
                dto.getGinasio_id(),
                dto.getNota(),
                dto.getDescricao(),
                dto.getData_avaliacao()
        );
    }

    public static Visita toModel(VisitaDTO dto) {
        if (dto == null) return null;
        if (dto.getGinasio_id() == null || dto.getGinasio_id().trim().isEmpty()) {
            throw new DataConsistencyException("Inconsistência nos dados da visita: ID do ginásio em falta.");
        }
        return new Visita(dto.getGinasio_id(), dto.getData_visita(), dto.getNome_ginasio());
    }

    public static VisitaDTO toDTO(Visita visita) {
        if (visita == null) return null;
        return new VisitaDTO(visita.getGinasio_id(), visita.getData_visita(), visita.getNome_ginasio());
    }
}
