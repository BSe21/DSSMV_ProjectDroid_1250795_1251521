package com.example.bettergym;

import com.example.bettergym.exceptions.DataConsistencyException;
import com.example.bettergym.exceptions.DuplicatedDataException;
import com.example.bettergym.exceptions.InvalidDataException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Testes unitários para as exceções personalizadas da aplicação.
 */
public class ExceptionsTest {

    @Test
    public void testInvalidDataException_MetodoWhat() {
        String msg = "Erro nos dados introduzidos pelo utilizador.";
        InvalidDataException exception = new InvalidDataException(msg);

        assertNotNull(exception);
        assertEquals(msg, exception.getMessage());
        assertEquals(msg, exception.what());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    public void testDataConsistencyException_MetodoWhat() {
        String msg = "Inconsistência detetada entre DTO e documento Firestore.";
        DataConsistencyException exception = new DataConsistencyException(msg);

        assertNotNull(exception);
        assertEquals(msg, exception.getMessage());
        assertEquals(msg, exception.what());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    public void testDuplicatedDataException_MetodoWhat() {
        String msg = "Registo duplicado detetado.";
        DuplicatedDataException exception = new DuplicatedDataException(msg);

        assertNotNull(exception);
        assertEquals(msg, exception.getMessage());
        assertEquals(msg, exception.what());
        assertTrue(exception instanceof RuntimeException);
    }
}

