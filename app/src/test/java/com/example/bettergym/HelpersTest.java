package com.example.bettergym;

import com.example.bettergym.helper.DateHelper;
import com.example.bettergym.helper.PasswordValidator;
import com.example.bettergym.helper.Utils;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Testes unitários para classes auxiliares (PasswordValidator, DateHelper e Utils).
 */
public class HelpersTest {

    @Test
    public void testPasswordValidator_CamposVazios_RetornaErro() {
        assertEquals("Preenche os três campos.", PasswordValidator.validarAlteracao(null, "nova123", "nova123"));
        assertEquals("Preenche os três campos.", PasswordValidator.validarAlteracao("atual123", "", "nova123"));
        assertEquals("Preenche os três campos.", PasswordValidator.validarAlteracao("atual123", "nova123", ""));
    }

    @Test
    public void testPasswordValidator_MenosDeSeisCaracteres_RetornaErro() {
        String erro = PasswordValidator.validarAlteracao("atual123", "12345", "12345");
        assertNotNull(erro);
        assertTrue(erro.contains("pelo menos 6 caracteres"));
    }

    @Test
    public void testPasswordValidator_ConfirmacaoDiferente_RetornaErro() {
        String erro = PasswordValidator.validarAlteracao("atual123", "novapassword123", "diferente123");
        assertNotNull(erro);
        assertTrue(erro.contains("não coincide"));
    }

    @Test
    public void testPasswordValidator_NovaIgualAtual_RetornaErro() {
        String erro = PasswordValidator.validarAlteracao("mesmapassword", "mesmapassword", "mesmapassword");
        assertNotNull(erro);
        assertTrue(erro.contains("diferente da atual"));
    }

    @Test
    public void testPasswordValidator_Sucesso_RetornaNull() {
        String resultado = PasswordValidator.validarAlteracao("passwordAtual1", "novaPasswordSegura2", "novaPasswordSegura2");
        assertNull(resultado);
    }

    @Test
    public void testDateHelper_ObterDataAtualIso_FormatoValido() {
        String iso = DateHelper.obterDataAtualIso();
        assertNotNull(iso);
        assertTrue("Deve terminar em Z (UTC)", iso.endsWith("Z"));
        assertTrue("Deve conter separador T", iso.contains("T"));
    }

    @Test
    public void testDateHelper_ConverterIsoParaDate_ComEMilissegundos() {
        Date d1 = DateHelper.converterIsoParaDate("2026-11-05T14:30:00.123Z");
        assertNotNull("Deve fazer parse com milissegundos", d1);

        Date d2 = DateHelper.converterIsoParaDate("2026-11-05T14:30:00Z");
        assertNotNull("Deve fazer parse sem milissegundos", d2);
    }

    @Test
    public void testDateHelper_ConverterIsoParaDate_InvalidoRetornaNull() {
        assertNull(DateHelper.converterIsoParaDate(null));
        assertNull(DateHelper.converterIsoParaDate(""));
        assertNull(DateHelper.converterIsoParaDate("data_completamente_invalida"));
    }

    @Test
    public void testDateHelper_FormatarVisita_ComDataValidaEInvalida() {
        String formatada = DateHelper.formatarVisita("2026-11-05T10:00:00.000Z");
        assertNotNull(formatada);
        assertTrue(formatada.startsWith("Visitado a "));

        String formatadaNula = DateHelper.formatarVisita(null);
        assertEquals("Data desconhecida", formatadaNula);
    }

    @Test
    public void testUtils_Iniciais_CasosCompletos() {
        assertEquals("TP", Utils.iniciais("Tomás Pereira"));
        assertEquals("BS", Utils.iniciais("Bruno Saraiva"));
        assertEquals("CFF", "CF", Utils.iniciais("Carlos Filipe Freitas"));
        assertEquals("B", Utils.iniciais("Bruno"));
        assertEquals("?", Utils.iniciais(null));
        assertEquals("?", Utils.iniciais("   "));
    }

    @Test
    public void testUtils_ValorOuTraco_ProtecaoContraNulos() {
        assertEquals("ISEP", Utils.valorOuTraco("ISEP"));
        assertEquals("—", Utils.valorOuTraco(null));
        assertEquals("—", Utils.valorOuTraco("   "));
    }

    @Test
    public void testUtils_FormatarRating() {
        String comAvaliacoes = Utils.formatarRating(4.6, 25);
        assertTrue(comAvaliacoes.contains("4.6") || comAvaliacoes.contains("4,6"));
        assertTrue(comAvaliacoes.contains("25 avaliações"));

        String semAvaliacoes = Utils.formatarRating(0.0, 0);
        assertEquals("Ainda sem avaliações públicas", semAvaliacoes);
    }
}

