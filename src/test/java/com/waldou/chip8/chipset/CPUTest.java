package com.waldou.chip8.chipset;

import com.waldou.chip8.Utils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CPUTest {
    private static final long CPU_CYCLE_DELTA_TIME = 5_000_000;
    private static final long TIMER_UPDATE_DELTA_TIME = 20_000_000;

    @Test
    void shouldStoreRegisterValuesInMemory() throws InterruptedException {
        RAM ram = new RAM(program(
                0x600A, // V0 = 10
                0x610B, // V1 = 11
                0xA300, // I = 0x300
                0xF155  // Store V0..V1 in memory starting at I
        ));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        runCycles(cpu, 4);

        assertEquals(0x0A, ram.readByte((short) 0x300));
        assertEquals(0x0B, ram.readByte((short) 0x301));
    }

    @Test
    void shouldClearScreen() throws InterruptedException {
        RAM ram = new RAM(program(
                0x6000, // V0 = 0
                0x6100, // V1 = 0
                0xA300, // I = 0x300
                0xD011, // Draw 1-byte sprite at V0,V1
                0x00E0  // Clear screen
        ));
        ram.writeByte((short) 0x300, (byte) 0x80);
        Graphics graphics = new Graphics();
        CPU cpu = cpu(ram, graphics, new Input(), mock(Sound.class));

        runCycles(cpu, 5);

        assertFalse(graphics.getPixel(0, 0));
    }

    @Test
    void shouldJumpToAddress() throws InterruptedException {
        RAM ram = new RAM(program(
                0x1206, // Jump to 0x206
                0x6000, // Skipped
                0x0000, // Padding
                0x6007, // V0 = 7
                0xA300, // I = 0x300
                0xF055  // Store V0 in memory starting at I
        ));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        runCycles(cpu, 4);

        assertEquals(0x07, ram.readByte((short) 0x300));
    }

    @Test
    void shouldSkipInstructionWhenRegistersAreEqual() throws InterruptedException {
        RAM ram = new RAM(program(
                0x600A, // V0 = 10
                0x610A, // V1 = 10
                0x5010, // Skip next instruction if V0 == V1
                0x6000, // V0 = 0
                0xA300, // I = 0x300
                0xF055  // Store V0 in memory starting at I
        ));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        runCycles(cpu, 5);

        assertEquals(0x0A, ram.readByte((short) 0x300));
    }

    @Test
    void shouldSkipInstructionWhenRegisterEqualsImmediateValue() throws InterruptedException {
        RAM ram = new RAM(program(
                0x600A, // V0 = 10
                0x300A, // Skip next instruction if V0 == 10
                0x6000, // V0 = 0
                0xA300, // I = 0x300
                0xF055  // Store V0 in memory starting at I
        ));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        runCycles(cpu, 4);

        assertEquals(0x0A, ram.readByte((short) 0x300));
    }

    @Test
    void shouldSkipInstructionWhenRegisterDoesNotEqualImmediateValue() throws InterruptedException {
        RAM ram = new RAM(program(
                0x600A, // V0 = 10
                0x400B, // Skip next instruction if V0 != 11
                0x6000, // V0 = 0
                0xA300, // I = 0x300
                0xF055  // Store V0 in memory starting at I
        ));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        runCycles(cpu, 4);

        assertEquals(0x0A, ram.readByte((short) 0x300));
    }

    @Test
    void shouldAddImmediateValueWithByteWraparound() throws InterruptedException {
        RAM ram = new RAM(program(
                0x60FE, // V0 = 254
                0x7005, // V0 += 5
                0xA300, // I = 0x300
                0xF055  // Store V0 in memory starting at I
        ));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        runCycles(cpu, 4);

        assertEquals(0x03, ram.readByte((short) 0x300));
    }

    @Test
    void shouldSkipInstructionWhenRegistersAreNotEqual() throws InterruptedException {
        RAM ram = new RAM(program(
                0x600A, // V0 = 10
                0x610B, // V1 = 11
                0x9010, // Skip next instruction if V0 != V1
                0x6000, // V0 = 0
                0xA300, // I = 0x300
                0xF055  // Store V0 in memory starting at I
        ));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        runCycles(cpu, 5);

        assertEquals(0x0A, ram.readByte((short) 0x300));
    }

    @Test
    void shouldJumpToAddressPlusV0() throws InterruptedException {
        RAM ram = new RAM(program(
                0x6004, // V0 = 4
                0xB204, // Jump to 0x204 + V0
                0x6000, // Skipped
                0x0000, // Padding
                0x6009, // V0 = 9
                0xA300, // I = 0x300
                0xF055  // Store V0 in memory starting at I
        ));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        runCycles(cpu, 5);

        assertEquals(0x09, ram.readByte((short) 0x300));
    }

    @Test
    void shouldApplyRandomMask() throws InterruptedException {
        RAM ram = new RAM(program(
                0xC000, // V0 = random & 0
                0xA300, // I = 0x300
                0xF055  // Store V0 in memory starting at I
        ));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        runCycles(cpu, 3);

        assertEquals(0x00, ram.readByte((short) 0x300));
    }

    @Test
    void shouldUseFullUnsignedByteRangeForRandomOpcode() throws InterruptedException {
        try (MockedConstruction<Random> randomConstruction = Mockito.mockConstruction(Random.class, (mock, context) -> {
            when(mock.nextInt(256)).thenReturn(255);
        })) {
            RAM ram = new RAM(program(
                    0xC0FF, // V0 = random & 0xFF
                    0xA300, // I = 0x300
                    0xF055  // Store V0 in memory starting at I
            ));
            CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

            runCycles(cpu, 3);

            assertEquals(255, Byte.toUnsignedInt(ram.readByte((short) 0x300)));
            verify(randomConstruction.constructed().getFirst()).nextInt(256);
        }
    }

    @Test
    void shouldThrowExceptionWhenOpcodeIsInvalid() {
        RAM ram = new RAM(program(0xFFFF));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            cpu.cycle(CPU_CYCLE_DELTA_TIME);
        });

        assertEquals("Invalid opcode found: 0xFFFF", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenZeroTypeOpcodeIsInvalid() {
        RAM ram = new RAM(program(0x0001));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            cpu.cycle(CPU_CYCLE_DELTA_TIME);
        });

        assertEquals("Invalid opcode found: 0x0001", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEightTypeOpcodeIsInvalid() {
        RAM ram = new RAM(program(0x8018));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            cpu.cycle(CPU_CYCLE_DELTA_TIME);
        });

        assertEquals("Invalid opcode found: 0x8018", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenKeyOpcodeIsInvalid() {
        RAM ram = new RAM(program(
                0x6002, // V0 = key 2
                0xE000  // Invalid key opcode
        ));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            runCycles(cpu, 2);
        });

        assertEquals("Invalid opcode found: 0xE000", exception.getMessage());
    }

    @Test
    void shouldCallSubroutineAndReturn() throws InterruptedException {
        RAM ram = new RAM(program(
                0x2208, // Call subroutine at 0x208
                0xA300, // I = 0x300
                0xF055, // Store V0 in memory starting at I
                0x0000, // Unused padding
                0x6007, // V0 = 7
                0x00EE  // Return from subroutine
        ));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        runCycles(cpu, 5);

        assertEquals(0x07, ram.readByte((short) 0x300));
    }

    @Test
    void shouldDrawSpriteAtRegisterCoordinates() throws InterruptedException {
        RAM ram = new RAM(program(
                0x6000, // V0 = 0
                0x6100, // V1 = 0
                0xA300, // I = 0x300
                0xD011  // Draw 1-byte sprite at V0,V1
        ));
        ram.writeByte((short) 0x300, (byte) 0x80);
        Graphics graphics = new Graphics();
        CPU cpu = cpu(ram, graphics, new Input(), mock(Sound.class));

        runCycles(cpu, 4);

        assertTrue(graphics.getPixel(0, 0));
    }

    @Test
    void shouldSetCollisionFlagWhenDrawingFlipsPixelOff() throws InterruptedException {
        RAM ram = new RAM(program(
                0x6000, // V0 = 0
                0x6100, // V1 = 0
                0xA300, // I = 0x300
                0xD011, // Draw 1-byte sprite at V0,V1
                0xD011, // Draw it again, flipping the pixel off
                0xA320, // I = 0x320
                0xFF55  // Store V0..VF in memory starting at I
        ));
        ram.writeByte((short) 0x300, (byte) 0x80);
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        runCycles(cpu, 7);

        assertEquals(0x01, ram.readByte((short) 0x32F));
    }

    @Test
    void shouldSkipInstructionWhenKeyIsPressed() throws InterruptedException {
        RAM ram = new RAM(program(
                0x6002, // V0 = key 2
                0xE09E, // Skip next instruction if key in V0 is pressed
                0x6000, // V0 = 0
                0xA300, // I = 0x300
                0xF055  // Store V0 in memory starting at I
        ));
        Input input = new Input();
        input.press(0x2);
        CPU cpu = cpu(ram, new Graphics(), input, mock(Sound.class));

        runCycles(cpu, 4);

        assertEquals(0x02, ram.readByte((short) 0x300));
    }

    @Test
    void shouldSkipInstructionWhenKeyIsNotPressed() throws InterruptedException {
        RAM ram = new RAM(program(
                0x6002, // V0 = key 2
                0xE0A1, // Skip next instruction if key in V0 is not pressed
                0x6000, // V0 = 0
                0xA300, // I = 0x300
                0xF055  // Store V0 in memory starting at I
        ));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        runCycles(cpu, 4);

        assertEquals(0x02, ram.readByte((short) 0x300));
    }

    @Test
    void shouldExecuteArithmeticAndBitwiseRegisterOperations() throws InterruptedException {
        assertRegisterOperationResult(0x0, 0x01, 0x07, 0x07, 0x00); // assign
        assertRegisterOperationResult(0x1, 0x0A, 0x05, 0x0F, 0x00); // or
        assertRegisterOperationResult(0x2, 0x0A, 0x06, 0x02, 0x00); // and
        assertRegisterOperationResult(0x3, 0x0A, 0x06, 0x0C, 0x00); // xor
        assertRegisterOperationResult(0x4, 0xFE, 0x02, 0x00, 0x01); // add with carry
        assertRegisterOperationResult(0x5, 0x07, 0x02, 0x05, 0x01); // subtract
        assertRegisterOperationResult(0x6, 0x05, 0x00, 0x02, 0x01); // shift right
        assertRegisterOperationResult(0x7, 0x02, 0x07, 0x05, 0x01); // reverse subtract
        assertRegisterOperationResult(0xE, 0x40, 0x00, 0x80, 0x00); // shift left
    }

    @Test
    void shouldLoadRegistersFromMemory() throws InterruptedException {
        RAM ram = new RAM(program(
                0xA300, // I = 0x300
                0xF165, // Load V0..V1 from memory starting at I
                0xA320, // I = 0x320
                0xF155  // Store V0..V1 in memory starting at I
        ));
        ram.writeByte((short) 0x300, (byte) 0x0A);
        ram.writeByte((short) 0x301, (byte) 0x0B);
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        runCycles(cpu, 4);

        assertEquals(0x0A, ram.readByte((short) 0x320));
        assertEquals(0x0B, ram.readByte((short) 0x321));
    }

    @Test
    void shouldAddRegisterValueToIndexRegister() throws InterruptedException {
        RAM ram = new RAM(program(
                0x6004, // V0 = 4
                0xA300, // I = 0x300
                0xF01E, // I += V0
                0x610A, // V1 = 10
                0xF155  // Store V0..V1 in memory starting at I
        ));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        runCycles(cpu, 5);

        assertEquals(0x04, ram.readByte((short) 0x304));
        assertEquals(0x0A, ram.readByte((short) 0x305));
    }

    @Test
    void shouldPointIndexRegisterToFontLocation() throws InterruptedException {
        RAM ram = new RAM(program(
                0x6002, // V0 = 2
                0xF029, // I = font location for V0
                0xF065, // Load V0 from memory at I
                0xA300, // I = 0x300
                0xF055  // Store V0 in memory starting at I
        ));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        runCycles(cpu, 5);

        assertEquals(FontSet.FONT_SET_ARRAY[10], ram.readByte((short) 0x300));
    }

    @Test
    void shouldStoreBinaryCodedDecimalDigits() throws InterruptedException {
        RAM ram = new RAM(program(
                0x607B, // V0 = 123
                0xA300, // I = 0x300
                0xF033  // Store BCD of V0 at I..I+2
        ));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        runCycles(cpu, 3);

        assertEquals(1, ram.readByte((short) 0x300));
        assertEquals(2, ram.readByte((short) 0x301));
        assertEquals(3, ram.readByte((short) 0x302));
    }

    @Test
    void shouldReadDelayTimerIntoRegister() throws InterruptedException {
        RAM ram = new RAM(program(
                0x6002, // V0 = 2
                0xF015, // delayTimer = V0
                0xF107, // V1 = delayTimer
                0xA300, // I = 0x300
                0xF155  // Store V0..V1 in memory starting at I
        ));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        runCycles(cpu, 5);

        assertEquals(0x02, ram.readByte((short) 0x301));
    }

    @Test
    void shouldPlayAndStopSoundWhenSoundTimerExpires() throws InterruptedException {
        RAM ram = new RAM(program(
                0x6001, // V0 = 1
                0xF018  // soundTimer = V0
        ));
        Sound sound = mock(Sound.class);
        CPU cpu = cpu(ram, new Graphics(), new Input(), sound);

        cpu.cycle(CPU_CYCLE_DELTA_TIME);
        cpu.cycle(TIMER_UPDATE_DELTA_TIME);

        verify(sound).play(Sound.TONE_A3);
        verify(sound).stop();
    }

    @Test
    void shouldSleepWhenCycleRunsFasterThanOpcodeSlice() throws InterruptedException {
        RAM ram = new RAM(program(0x00E0));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        try (MockedStatic<Utils> utilsMockedStatic = Mockito.mockStatic(Utils.class)) {
            utilsMockedStatic.when(Utils::systemNanoTime)
                    .thenReturn(0L, 0L, 4_000_000L);

            cpu.cycle(1);

            utilsMockedStatic.verify(() -> Utils.threadSleep(3));
        }
    }

    private void assertRegisterOperationResult(
            int operation,
            int left,
            int right,
            int expectedResult,
            int expectedCarry
    ) throws InterruptedException {
        RAM ram = new RAM(program(
                0x6000 | left,
                0x6100 | right,
                0x8010 | operation,
                0xA300,
                0xFF55
        ));
        CPU cpu = cpu(ram, new Graphics(), new Input(), mock(Sound.class));

        runCycles(cpu, 5);

        assertEquals(expectedResult, Byte.toUnsignedInt(ram.readByte((short) 0x300)), "operation " + operation);
        assertEquals(expectedCarry, Byte.toUnsignedInt(ram.readByte((short) 0x30F)), "operation " + operation);
    }

    private CPU cpu(RAM ram, Graphics graphics, Input input, Sound sound) {
        return new CPU(ram, graphics, input, sound);
    }

    private void runCycles(CPU cpu, int cycles) throws InterruptedException {
        for (int i = 0; i < cycles; i++) {
            cpu.cycle(CPU_CYCLE_DELTA_TIME);
        }
    }

    private byte[] program(int... opcodes) {
        byte[] program = new byte[opcodes.length * 2];
        for (int i = 0; i < opcodes.length; i++) {
            program[i * 2] = (byte) ((opcodes[i] & 0xFF00) >> 8);
            program[(i * 2) + 1] = (byte) (opcodes[i] & 0x00FF);
        }
        return program;
    }
}
