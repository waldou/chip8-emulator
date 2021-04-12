package com.waldou.chip8.chipset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class InputTest {
    private Input input;

    @BeforeEach
    void setUp() {
        input = new Input();
    }

    @Test
    void shouldPressKey() {
        assertFalse(input.isKeyPressed(0x0));

        input.press(0x0);

        assertTrue(input.isKeyPressed(0x0));
    }

    @Test
    void shouldReleaseKey() {
        assertFalse(input.isKeyPressed(0x0));

        input.press(0x0);
        assertTrue(input.isKeyPressed(0x0));

        input.release(0x0);
        assertFalse(input.isKeyPressed(0x0));
    }
}