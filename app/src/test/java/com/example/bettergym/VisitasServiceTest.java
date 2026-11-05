package com.example.bettergym;

import com.example.bettergym.exceptions.InvalidDataException;
import com.example.bettergym.model.Ginasio;
import com.example.bettergym.service.VisitasService;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Testes unitários para validações de visitas e códigos QR.
 */
public class VisitasServiceTest {

    private VisitasService visitasService;
    private Ginasio ginasioValido;

    @Before
    public void setUp() {
        visitasService = new VisitasService(null);
        ginasioValido = new Ginasio(
                "ChIJ_12345_TestGym",
                "Fitness Park Porto",
                41.1579,
                -8.6291,
                4.8,
                true,
                "Rua de Teste, 100",
                50,
                "photo_ref"
        );
    }

    @Test
    public void testValidarRegrasGinasio_GinasioAberto_Sucesso() {
        ginasioValido.setAbertoAgora(true);
        try {
            visitasService.validarRegrasGinasio(ginasioValido);
        } catch (Exception e) {
            fail("Não deveria lançar exceção para ginásio válido e aberto: " + e.getMessage());
        }
    }

    @Test
    public void testValidarRegrasGinasio_GinasioFechado_LancaExcecao() {
        ginasioValido.setAbertoAgora(false);
        try {
            visitasService.validarRegrasGinasio(ginasioValido);
            fail("Deveria lançar InvalidDataException quando o ginásio está fechado");
        } catch (InvalidDataException e) {
            assertNotNull(e.what());
            assertTrue(e.what().toLowerCase().contains("fechado"));
        }
    }

    @Test
    public void testValidarRegrasGinasio_GinasioNulo_LancaExcecao() {
        try {
            visitasService.validarRegrasGinasio(null);
            fail("Deveria lançar InvalidDataException para ginásio nulo");
        } catch (InvalidDataException e) {
            assertNotNull(e.what());
            assertTrue(e.what().contains("inválido"));
        }
    }

    @Test
    public void testValidarRegrasGinasio_GinasioIdVazio_LancaExcecao() {
        ginasioValido.setId("   ");
        try {
            visitasService.validarRegrasGinasio(ginasioValido);
            fail("Deveria lançar InvalidDataException para ginásio com ID vazio");
        } catch (InvalidDataException e) {
            assertNotNull(e.what());
        }
    }

    @Test
    public void testValidarQrCode_QrCodeIdExato_Sucesso() {
        ginasioValido.setAbertoAgora(true);
        String qr = "ChIJ_12345_TestGym";
        try {
            visitasService.validarQrCodeGinasio(ginasioValido, qr);
        } catch (Exception e) {
            fail("Deveria aceitar QR code igual ao ID do ginásio: " + e.getMessage());
        }
    }

    @Test
    public void testValidarQrCode_QrCodeComPrefixoCustomizado_Sucesso() {
        ginasioValido.setAbertoAgora(true);
        String qr = "BETTERGYM_CHECKIN:ChIJ_12345_TestGym";
        try {
            visitasService.validarQrCodeGinasio(ginasioValido, qr);
        } catch (Exception e) {
            fail("Deveria aceitar QR code formatado com prefixo e ID: " + e.getMessage());
        }
    }

    @Test
    public void testValidarQrCode_QrCodeComNomeDoGinasio_Sucesso() {
        ginasioValido.setAbertoAgora(true);
        String qr = "Fitness Park Porto - Entrada";
        try {
            visitasService.validarQrCodeGinasio(ginasioValido, qr);
        } catch (Exception e) {
            fail("Deveria aceitar QR code que contenha o nome do ginásio: " + e.getMessage());
        }
    }

    @Test
    public void testValidarQrCode_QrCodeDeOutroGinasio_LancaExcecao() {
        ginasioValido.setAbertoAgora(true);
        String qrOutro = "ChIJ_OUTRO_GINASIO_999";
        try {
            visitasService.validarQrCodeGinasio(ginasioValido, qrOutro);
            fail("Deveria lançar InvalidDataException ao ler QR code de outro ginásio");
        } catch (InvalidDataException e) {
            assertNotNull(e.what());
            assertTrue(e.what().contains("Não corresponde a este ginásio"));
        }
    }

    @Test
    public void testValidarQrCode_QrCodeVazioOuNulo_LancaExcecao() {
        ginasioValido.setAbertoAgora(true);
        try {
            visitasService.validarQrCodeGinasio(ginasioValido, "");
            fail("Deveria lançar InvalidDataException para QR code vazio");
        } catch (InvalidDataException e) {
            assertNotNull(e.what());
            assertTrue(e.what().contains("vazio ou ilegível"));
        }
    }

    @Test
    public void testValidarQrCode_GinasioFechado_LancaExcecaoMesmoComQrCorreto() {
        ginasioValido.setAbertoAgora(false);
        String qr = ginasioValido.getId();
        try {
            visitasService.validarQrCodeGinasio(ginasioValido, qr);
            fail("Não deve validar QR Code se o ginásio estiver fechado");
        } catch (InvalidDataException e) {
            assertNotNull(e.what());
            assertTrue(e.what().toLowerCase().contains("fechado"));
        }
    }
}

