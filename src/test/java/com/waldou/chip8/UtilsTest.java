package com.waldou.chip8;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilsTest {
    @Test
    void shouldReturnFileBytes() throws IOException {
        Mockito.mockStatic(Files.class);
        when(Files.readAllBytes(any(Path.class))).thenReturn("hello".getBytes());

        byte[] bytes = Utils.readFile("dummy.txt");

        assertEquals("hello", new String(bytes));
    }

    @Test
    void shouldReturnTrueWhenNullString() {
        assertTrue(Utils.isEmpty(null));
    }

    @Test
    void shouldReturnTrueWhenEmptyString() {
        assertTrue(Utils.isEmpty(""));
    }

    @Test
    void shouldReturnFalseWhenNotEmptyString() {
        assertFalse(Utils.isEmpty("hello"));
    }
}