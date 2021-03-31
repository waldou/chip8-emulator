package com.waldou.chip8.chipset;

import com.waldou.chip8.OpcodeConstants;

import java.util.Random;

public class CPU {
    private static final long ONE_SECOND_IN_NANOS = 1_000_000_000;
    private static final long OPCODES_PER_SECOND = 60;
    private static final long OPCODES_SLICE = ONE_SECOND_IN_NANOS / OPCODES_PER_SECOND;
    private static final int GENERAL_PURPOSE_REGISTERS = 16;
    private static final int CALL_STACK_SIZE = 16;
    private static final char UNSIGNED_BYTE_MAX_VALUE = (char) 0xFF;

    private byte[] V;
    private short I;
    private byte delayTimer;
    private byte soundTimer;
    private short programCounter;

    private short callStack[];
    private short stackPointer = 0;

    private final Graphics graphics;
    private final Input input;
    private final RAM ram;
    private final Random rng;

    public CPU(Graphics graphics, Input input, RAM ram) {
        this.graphics = graphics;
        this.input = input;
        this.ram = ram;
        rng = new Random();

        V = new byte[GENERAL_PURPOSE_REGISTERS];
        I = 0x0000;
        delayTimer = 0x00;
        soundTimer = 0x00;
        callStack = new short[CALL_STACK_SIZE];
        programCounter = ram.getStartMemoryAddress();
    }

    /**
     * Throttle CPU speed. This will sleep the thread for the approximate time it should
     * so it can roughly emulate the CPU speed.
     */
    private void handleClockSpeed(long deltaTime) throws InterruptedException {
        if (OPCODES_SLICE > deltaTime) {
            long diffTime = OPCODES_SLICE - deltaTime;
            long diffTimeInMillis = (OPCODES_SLICE - deltaTime) / 1_000_000;
            long targetTime = System.nanoTime() + diffTime;
            while (System.nanoTime() < targetTime) {
                //
                Thread.sleep(diffTimeInMillis);
                diffTimeInMillis = 0;
            }
        }
    }

    /**
     * Executes full CPU cycle.
     *
     * @param deltaTime
     */
    public void cycle(long deltaTime) throws InterruptedException {
        handleClockSpeed(deltaTime);

        short opcode = ram.readOpcode(programCounter);
        programCounter += 2;
        execute(opcode);

        updateTimers();
    }

    private void execute(short opcode) {
        System.out.println("Executing opcode: " + String.format("0x%04X", opcode));
        short type = getOpcodeType(opcode);
        switch (type) {
            case OpcodeConstants.TYPE_ZERO: {
                if (OpcodeConstants.OPCODE_CLEAR_SCREEN == opcode) {
                    graphics.clearScreen();
                } else if (OpcodeConstants.OPCODE_RETURN == opcode) {
                    programCounter = callStack[stackPointer--];
                }
                break;
            }
            case OpcodeConstants.TYPE_ONE:
                programCounter = (short) (opcode & OpcodeConstants.OPERANDS_MASK);
                break;
            case OpcodeConstants.TYPE_TWO: {
                callStack[stackPointer++] = programCounter;
                programCounter = (short) (opcode & OpcodeConstants.OPERANDS_MASK);
                break;
            }
            case OpcodeConstants.TYPE_THREE: {
                short vId = getVIdX(opcode);
                byte value = (byte) (opcode & OpcodeConstants.LAST_TWO_OPERANDS_MASK);
                if (V[vId] == value) {
                    programCounter += 0x0002;
                }
                break;
            }
            case OpcodeConstants.TYPE_FOUR: {
                short vId = getVIdX(opcode);
                byte value = (byte) (opcode & OpcodeConstants.LAST_TWO_OPERANDS_MASK);
                if (V[vId] != value) {
                    programCounter += 0x0002;
                }
                break;
            }
            case OpcodeConstants.TYPE_FIVE: {
                short vIdX = getVIdX(opcode);
                short vIdY = getVIdY(opcode);
                if (V[vIdX] == V[vIdY]) {
                    programCounter += 0x0002;
                }
                break;
            }
            case OpcodeConstants.TYPE_SIX: {
                short vId = getVIdX(opcode);
                byte operands = (byte) (opcode & OpcodeConstants.LAST_TWO_OPERANDS_MASK);
                V[vId] = operands;
                break;
            }
            case OpcodeConstants.TYPE_SEVEN: {
                short vId = getVIdX(opcode);
                byte operands = (byte) (opcode & OpcodeConstants.LAST_TWO_OPERANDS_MASK);
                short temp = (short) (V[vId] & 0xff);
                temp += operands;
                V[vId] = (byte) (temp & OpcodeConstants.LAST_TWO_OPERANDS_MASK);
                break;
            }
            case OpcodeConstants.TYPE_EIGHT: {
                executeOperationsTypeEight(opcode);
                break;
            }
            case OpcodeConstants.TYPE_NINE: {
                short vIdX = getVIdX(opcode);
                short vIdY = getVIdY(opcode);
                programCounter += (short) ((V[vIdX] != V[vIdY]) ? 0x0002 : 0);
                break;
            }
            case OpcodeConstants.TYPE_A: {
                I = (short) (opcode & OpcodeConstants.OPERANDS_MASK);
                break;
            }
            case OpcodeConstants.TYPE_B: {
                short operand = (short) (opcode & OpcodeConstants.OPERANDS_MASK);
                programCounter = (short) (V[0] + operand);
                break;
            }
            case OpcodeConstants.TYPE_C: {
                short vId = getVIdX(opcode);
                byte random = (byte) rng.nextInt(UNSIGNED_BYTE_MAX_VALUE);
                byte operands = (byte) (opcode & OpcodeConstants.LAST_TWO_OPERANDS_MASK);

                V[vId] = (byte) (random & operands);
                break;
            }
            case OpcodeConstants.TYPE_D: {
                // FIXME make prettier, give responsibility to graphics controller
                short vIdX = getVIdX(opcode);
                short vIdY = getVIdY(opcode);
                short x = V[vIdX];
                short y = V[vIdY];
                short height = (short) (opcode & OpcodeConstants.THIRD_OPERAND_MASK);
                byte bytes;
                int readBytes = 0;

                V[0xF] = 0;
                while (readBytes < height) {
                    bytes = ram.readByte((short) (I + readBytes));
                    for (int i = 0; i < 8; i++) {
                        int finalX = (x + i) % graphics.getScreenWidth();
                        int finalY = (y + readBytes) % graphics.getScreenHeight();

                        boolean prevPixel = graphics.getScreen()[finalX][finalY];
                        boolean newPixel = prevPixel ^ isBitSet(bytes, 7 - i);

                        graphics.getScreen()[finalX][finalY] = newPixel;

                        if (prevPixel && !newPixel) {
                            V[0xF] = 1;
                        }
                    }
                    readBytes++;
                }
                break;
            }
            case OpcodeConstants.TYPE_E:
                // TODO handle key presses
                break;
            case OpcodeConstants.TYPE_F:
                executeOperationsTypeF(opcode);
                break;
            default:
                throw new IllegalStateException("Invalid opcode found: " + opcode);
        }
    }

    private boolean isBitSet(byte bytes, int mask) {
        return (bytes & (1 << mask)) != 0;
    }

    private void executeOperationsTypeEight(short opcode) {
        short vIdX = getVIdX(opcode);
        short vIdY = getVIdY(opcode);
        short type = (short) (opcode & OpcodeConstants.THIRD_OPERAND_MASK);
        switch (type) {
            case 0x0: {
                V[vIdX] = V[vIdY];
                break;
            }
            case 0x1: {
                V[vIdX] = (byte) (V[vIdX] | V[vIdY]);
                break;
            }
            case 0x2: {
                V[vIdX] = (byte) (V[vIdX] & V[vIdY]);
                break;
            }
            case 0x3: {
                V[vIdX] = (byte) (V[vIdX] ^ V[vIdY]);
                break;
            }
            case 0x4: {
                short tempX = V[vIdX];
                short tempY = V[vIdY];
                tempX += tempY;
                V[0xF] = (byte) ((tempX > UNSIGNED_BYTE_MAX_VALUE) ? 1 : 0);
                V[vIdX] = (byte) (tempX & OpcodeConstants.LAST_TWO_OPERANDS_MASK);
                break;
            }
            case 0x5: {
                V[0xF] = (byte) ((V[vIdX] > V[vIdY]) ? 1 : 0);
                V[vIdX] = (byte) (V[vIdX] - V[vIdY]);
                break;
            }
            case 0x6: {
                byte lsb = (byte) (V[vIdX] & 0x0001);
                V[0xF] = lsb;
                V[vIdX] = (byte) (V[vIdX] >> 1);
                break;
            }
            case 0x7: {
                V[0xF] = (byte) ((V[vIdY] > V[vIdX]) ? 1 : 0);
                V[vIdX] = (byte) (V[vIdY] - V[vIdX]);
                break;
            }
            case 0xE: {
                byte msb = (byte) (V[vIdX] & 0x8000);
                V[0xF] = msb;
                V[vIdX] = (byte) (V[vIdX] << 1);
                break;
            }
        }
    }

    private void executeOperationsTypeF(short opcode) {
        short vIdX = getVIdX(opcode);
        short type = (short) (opcode & OpcodeConstants.LAST_TWO_OPERANDS_MASK);
        switch (type) {
            case 0x0007: {
                V[vIdX] = delayTimer;
                break;
            }
            case 0x000A: {
                // TODO wait for key press
                break;
            }
            case 0x0015: {
                delayTimer = V[vIdX];
                break;
            }
            case 0x0018: {
                soundTimer = V[vIdX];
                break;
            }
            case 0x001E: {
                I += V[vIdX];
                break;
            }
            case 0x0029: {
                // TODO double check
                Character character = graphics.getFontSet().get(V[vIdX]);
                I = (short) character.charValue();
                break;
            }
            case 0x0033: {
                // Based on http://multigesture.net/articles/how-to-write-an-emulator-chip-8-interpreter/
                byte first = (byte) (V[vIdX] / 100);
                byte second = (byte) ((V[vIdX] / 10) % 10);
                byte third = (byte) ((V[vIdX] % 100) % 10);
                ram.writeByte(I, first);
                ram.writeByte((short) (I + 1), second);
                ram.writeByte((short) (I + 2), third);
                break;
            }
            case 0x0055: {
                short position = I;
                for (int i = 0; i <= vIdX; i++) {
                    ram.writeByte(position, V[i]);
                }
                break;
            }
            case 0x0065: {
                short position = I;
                for (int i = 0; i <= vIdX; i++) {
                    V[i] = ram.readByte(position);
                }
                break;
            }
        }
    }

    private void updateTimers() {
        if (delayTimer > 0) {
            delayTimer--;
        }
        if (soundTimer > 0) {
            if (soundTimer == 1) {
                System.out.println("SOUND!");
            }
            soundTimer--;
        }
    }

    private short getOpcodeType(short opcode) {
        return (short) (opcode & OpcodeConstants.TYPE_MASK);
    }

    private short getVIdX(short opcode) {
        return (short) ((opcode & OpcodeConstants.FIRST_OPERAND_MASK) >> OpcodeConstants.FIRST_OPERAND_SHIFT);
    }

    private short getVIdY(short opcode) {
        return (short) ((opcode & OpcodeConstants.SECOND_OPERAND_MASK) >> OpcodeConstants.SECOND_OPERAND_SHIFT);
    }
}
