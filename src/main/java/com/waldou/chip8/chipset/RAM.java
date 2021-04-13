package com.waldou.chip8.chipset;

import static com.waldou.chip8.chipset.FontSet.FONT_SET_ARRAY;

public class RAM {
    private static final short START_MEMORY_ADDRESS = 0x200;
    private static final int MEMORY_SIZE_IN_BYTES = 4096;
    private final byte[] memory;

    public RAM(byte[] program) {
        if (program == null) {
            throw new IllegalArgumentException("Program to load cannot be null.");
        }

        try {
            memory = new byte[MEMORY_SIZE_IN_BYTES];
            System.arraycopy(program, 0, memory, START_MEMORY_ADDRESS, program.length);
            System.arraycopy(FONT_SET_ARRAY, 0, memory, 0, FONT_SET_ARRAY.length);
        } catch (Exception e) {
            throw new IllegalArgumentException("Program failed to load.", e);
        }
    }

    short readOpcode(short position) {
        short first = (short) (memory[position] << 8);
        short second = memory[position + 1];
        return (short) (first | (second & 0x00FF));
    }

    byte readByte(short position) {
        return memory[position];
    }

    void writeByte(short position, byte value) {
        memory[position] = value;
    }

    short getFontLocation(byte bytes) {
        return (short) (bytes * 5);
    }

    short getStartMemoryAddress() {
        return START_MEMORY_ADDRESS;
    }
}
