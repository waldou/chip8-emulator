package com.waldou.chip8.chipset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RAMTest {
    private static final byte[] FAKE_PROGRAM = {(byte) 0xFA, (byte) 0xCE, (byte) 0xDA, (byte) 0xF8};

    private RAM ram;

    @BeforeEach
    void setUp() {
        ram = new RAM(FAKE_PROGRAM);
    }

    @Test
    void shouldThrowExceptionWhenNullProgram() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new RAM(null);
        });

        String expectedMessage = "Program to load cannot be null";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void shouldThrowExceptionWhenHugeProgram() {
        byte[] hugeProgram = new byte[6000];

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new RAM(hugeProgram);
        });

        String expectedMessage = "Program failed to load";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void shouldReadOpcode() {
        short expected = (short) 0xFACE;
        short actual = ram.readOpcode((short) 0x200);
        assertEquals(expected, actual);
    }

    @Test
    void shouldReadByte() {
        byte expected = (byte) 0xDA;
        byte actual = ram.readByte((short) 0x202);
        assertEquals(expected, actual);
    }

    @Test
    void shouldWriteByte() {
        byte expected = (byte) 0x88;

        ram.writeByte((short) 0x203, (byte) 0x88);

        byte actual = ram.readByte((short) 0x203);
        assertEquals(expected, actual);
    }

    @Test
    void shouldHaveCorrectStartMemoryAddress() {
        assertEquals(0x200, ram.getStartMemoryAddress());
    }

    @Test
    void shouldNotFillMemoryBelowStart() {
        for (int i = 0; i < 0x200; i++) {
            byte bytes = ram.readByte((short) i);
            assertEquals(0x00, bytes);
        }
    }
}
