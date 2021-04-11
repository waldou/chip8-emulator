package com.waldou.chip8.chipset;

public class OpcodeConstants {
    public static final short TYPE_MASK = (short) 0xF000;

    public static final short TYPE_ZERO = 0x0000;
    public static final short TYPE_ONE = 0x1000;
    public static final short TYPE_TWO = 0x2000;
    public static final short TYPE_THREE = 0x3000;
    public static final short TYPE_FOUR = 0x4000;
    public static final short TYPE_FIVE = 0x5000;
    public static final short TYPE_SIX = 0x6000;
    public static final short TYPE_SEVEN = 0x7000;
    public static final short TYPE_EIGHT = (short) 0x8000;
    public static final short TYPE_NINE = (short) 0x9000;
    public static final short TYPE_A = (short) 0xA000;
    public static final short TYPE_B = (short) 0xB000;
    public static final short TYPE_C = (short) 0xC000;
    public static final short TYPE_D = (short) 0xD000;
    public static final short TYPE_E = (short) 0xE000;
    public static final short TYPE_F = (short) 0xF000;

    public static final short ALL_OPERANDS_MASK = (short) 0x0FFF;
    public static final short FIRST_OPERAND_MASK = (short) 0x0F00;
    public static final short FIRST_OPERAND_SHIFT = 8;
    public static final short SECOND_OPERAND_MASK = (short) 0x00F0;
    public static final short SECOND_OPERAND_SHIFT = 4;
    public static final short THIRD_OPERAND_MASK = (short) 0x000F;
    public static final short LAST_TWO_OPERANDS_MASK = (short) 0x00FF;

    public static final short OPCODE_CLEAR_SCREEN = (short) 0x00E0;
    public static final short OPCODE_RETURN = (short) 0x00EE;

    public static final short KEY_EVENT_PRESSED = (short) 0x009E;
    public static final short KEY_EVENT_NOT_PRESSED = (short) 0x00A1;

    private OpcodeConstants() {
    }
}
