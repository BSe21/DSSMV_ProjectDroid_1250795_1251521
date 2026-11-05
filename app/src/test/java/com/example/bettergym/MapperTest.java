package com.example.bettergym;

import com.example.bettergym.dto.Mapper;
import com.example.bettergym.dto.PerfilDTO;
import com.example.bettergym.dto.PlaceGymDTO;
import com.example.bettergym.dto.VisitaDTO;
import com.example.bettergym.exceptions.DataConsistencyException;
import com.example.bettergym.model.Ginasio;
import com.example.bettergym.model.Perfil;
import com.example.bettergym.model.Visita;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Testes unitários para o Mapper entre DTOs e modelos.
 */
public class MapperTest {

    @Test
    public void testToModelPlaceGymDTO_MapeamentoCompleto() {
        PlaceGymDTO dto = new PlaceGymDTO(
                "place_abc",
                "Solinca Dragão",
                41.161,
                -8.583,
                4.5,
                true,
                "Via Futebol Clube do Porto",
                120,
                "ref_foto_123"
        );
        dto.setFotos(Arrays.asList("foto1.jpg", "foto2.jpg"));

        Ginasio model = Mapper.toModel(dto);

        assertNotNull(model);
        assertEquals("place_abc", model.getId());
        assertEquals("Solinca Dragão", model.getNome());
        assertEquals(41.161, model.getLatitude(), 0.0001);
        assertEquals(-8.583, model.getLongitude(), 0.0001);
        assertEquals(4.5, model.getRating(), 0.0001);
        assertTrue(model.isAbertoAgora());
        assertEquals("Via Futebol Clube do Porto", model.getMorada());
        assertEquals(120, model.getNumeroAvaliacoes());
        assertEquals("ref_foto_123", model.getReferenciaFoto());
        assertEquals(2, model.getFotos().size());
    }

    @Test
    public void testToModelPlaceGymDTO_NuloRetornaNulo() {
        assertNull(Mapper.toModel((PlaceGymDTO) null));
    }

    @Test
    public void testToModelPlaceGymDTO_IdEmFalta_LancaDataConsistencyException() {
        PlaceGymDTO dtoSemId = new PlaceGymDTO();
        dtoSemId.setNome("Ginásio Sem ID");

        try {
            Mapper.toModel(dtoSemId);
            fail("Deveria lançar DataConsistencyException para DTO sem ID");
        } catch (DataConsistencyException e) {
            assertNotNull(e.what());
            assertTrue(e.what().contains("ID em falta"));
        }
    }

    @Test
    public void testToDTOPlaceGym_MapeamentoCompleto() {
        Ginasio model = new Ginasio(
                "place_xyz",
                "Fitness Hut Trindade",
                41.151,
                -8.609,
                4.2,
                false,
                "Rua de Camões",
                85,
                "foto_hut"
        );

        PlaceGymDTO dto = Mapper.toDTO(model);

        assertNotNull(dto);
        assertEquals("place_xyz", dto.getId());
        assertEquals("Fitness Hut Trindade", dto.getNome());
        assertFalse(dto.isAbertoAgora());
        assertEquals("Rua de Camões", dto.getMorada());
    }

    @Test
    public void testToModelPerfilDTO_MapeamentoComHistorico() {
        PerfilDTO dto = new PerfilDTO();
        dto.setNome_completo("Bruno Saraiva");
        dto.setUsername("brunosaraiva");
        dto.setEmail("bruno@isep.ipp.pt");
        dto.setData_registo("2026-10-01T12:00:00.000Z");

        List<VisitaDTO> historico = new ArrayList<>();
        historico.add(new VisitaDTO("gym_1", "2026-11-01T10:00:00.000Z", "Ginásio 1"));
        historico.add(new VisitaDTO("gym_2", "2026-11-02T15:00:00.000Z", "Ginásio 2"));
        dto.setHistorico_ginasios_visitados(historico);

        Perfil model = Mapper.toModel("user_1250795", dto);

        assertNotNull(model);
        assertEquals("user_1250795", model.getId());
        assertEquals("Bruno Saraiva", model.getNome_completo());
        assertEquals("brunosaraiva", model.getUsername());
        assertNotNull(model.getHistorico_ginasios_visitados());
        assertEquals(2, model.getHistorico_ginasios_visitados().size());
        assertEquals("Ginásio 1", model.getHistorico_ginasios_visitados().get(0).getNome_ginasio());
    }

    @Test
    public void testToModelPerfilDTO_IdNulo_LancaDataConsistencyException() {
        PerfilDTO dto = new PerfilDTO();
        dto.setNome_completo("Tomás Pereira");

        try {
            Mapper.toModel(null, dto);
            fail("Deveria lançar DataConsistencyException para ID de perfil nulo");
        } catch (DataConsistencyException e) {
            assertNotNull(e.what());
        }
    }

    @Test
    public void testToModelVisitaDTO_MapeamentoCompleto() {
        VisitaDTO dto = new VisitaDTO("place_123", "2026-11-05T09:00:00.000Z", "Holmes Place Porto");

        Visita model = Mapper.toModel(dto);

        assertNotNull(model);
        assertEquals("place_123", model.getGinasio_id());
        assertEquals("2026-11-05T09:00:00.000Z", model.getData_visita());
        assertEquals("Holmes Place Porto", model.getNome_ginasio());
    }

    @Test
    public void testToModelVisitaDTO_SemGinasioId_LancaDataConsistencyException() {
        VisitaDTO dtoInvalido = new VisitaDTO(null, "2026-11-05T09:00:00.000Z", "Ginásio");

        try {
            Mapper.toModel(dtoInvalido);
            fail("Deveria lançar DataConsistencyException para visita sem ID de ginásio");
        } catch (DataConsistencyException e) {
            assertNotNull(e.what());
        }
    }

    @Test
    public void testToModelList_FiltragemInteligenteDeLocais() {
        List<PlaceGymDTO> dtos = new ArrayList<>();
        dtos.add(new PlaceGymDTO("1", "Fitness Up Antas", 41.0, -8.0, 4.5, true));
        dtos.add(new PlaceGymDTO("2", "Crossfit Invicta", 41.1, -8.1, 4.7, true));
        dtos.add(new PlaceGymDTO("3", "Estádio do Dragão", 41.2, -8.2, 4.9, false)); // Deve ser filtrado fora
        dtos.add(new PlaceGymDTO("4", "Piscina Municipal da Constituição", 41.3, -8.3, 4.0, true)); // Filtrado fora
        dtos.add(new PlaceGymDTO("5", "Solinca Classic Gym", 41.4, -8.4, 4.4, true));

        List<Ginasio> resultado = Mapper.toModelList(dtos);

        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        assertEquals("Fitness Up Antas", resultado.get(0).getNome());
        assertEquals("Crossfit Invicta", resultado.get(1).getNome());
        assertEquals("Solinca Classic Gym", resultado.get(2).getNome());
    }
}

