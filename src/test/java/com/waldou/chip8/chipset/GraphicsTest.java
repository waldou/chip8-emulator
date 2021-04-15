package com.waldou.chip8.chipset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GraphicsTest {
    private Graphics graphics;

    @BeforeEach
    void setUp() {
        graphics = new Graphics();
    }

    @Test
    void shouldDrawLineWithoutPixelFlip() {
        boolean result = graphics.drawLine(0, 0, 0, (byte) 0xFF);
        assertFalse(result);
        for (int i = 0; i < 8; i++) {
            assertTrue(graphics.getPixel(i, 0));
        }
    }

    @Test
    void shouldDrawLineWithPixelFlip() {
        graphics.drawLine(0, 0, 0, (byte) 0xFF);
        boolean result = graphics.drawLine(0, 0, 0, (byte) 0xFF);
        assertTrue(result);
        for (int i = 0; i < 8; i++) {
            assertFalse(graphics.getPixel(i, 0));
        }
    }

    @Test
    void shouldClearScreen() {
        graphics.drawLine(0, 0, 0, (byte) 0xFF);
        graphics.clearScreen();
        for (int i = 0; i < 8; i++) {
            assertFalse(graphics.getPixel(i, 0));
        }
    }

    @Test
    void shouldHaveCorrectWidth() {
        assertEquals(64, graphics.getScreenWidth());
    }

    @Test
    void shouldHaveCorrectHeight() {
        assertEquals(32, graphics.getScreenHeight());
    }
}