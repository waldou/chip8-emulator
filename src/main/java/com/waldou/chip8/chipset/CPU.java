package com.waldou.chip8.chipset;

import com.waldou.chip8.Main;

import java.util.Random;

import static com.waldou.chip8.chipset.OpcodeConstants.*;

public class CPU {
    private static final long ONE_SECOND_IN_NANOS = 1_000_000_000;
    private static final long OPCODES_PER_SECOND = 250;
    private static final long OPCODES_SLICE = ONE_SECOND_IN_NANOS / OPCODES_PER_SECOND;
    private static final int GENERAL_PURPOSE_REGISTERS = 16;
    private static final int CALL_STACK_SIZE = 16;
    private static final short UNSIGNED_BYTE_MAX_VALUE = 255;

    private static final long TIMER_UPDATES_PER_SECOND = 60;
    private static final long TIME_TO_UPDATE_TIMERS_IN_NANOS = ONE_SECOND_IN_NANOS / TIMER_UPDATES_PER_SECOND;
    private long timeSinceTimerUpdate = 0;

    private byte[] V;
    private short I;
    private byte delayTimer;
    private byte soundTimer;
    private short programCounter;

    private short callStack[];
    private short stackPointer = 0;

    private final RAM ram;
    private final Graphics graphics;
    private final Input input;
    private final Sound sound;

    private final Random rng;

    public CPU(RAM ram, Graphics graphics, Input input, Sound sound) {
        this.ram = ram;
        this.graphics = graphics;
        this.input = input;
        this.sound = sound;
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

        updateTimers(deltaTime);
    }

    private void execute(short opcode) throws InterruptedException {
        if (Main.DEBUG) {
            System.out.println("Executing opcode: " + String.format("0x%04X", opcode));
        }
        short type = getOpcodeType(opcode);
        switch (type) {
            case TYPE_ZERO: {
                if (OPCODE_CLEAR_SCREEN == opcode) {
                    graphics.clearScreen();
                } else if (OPCODE_RETURN == opcode) {
                    programCounter = callStack[--stackPointer];
                }
                break;
            }
            case TYPE_ONE:
                programCounter = (short) (opcode & ALL_OPERANDS_MASK);
                break;
            case TYPE_TWO: {
                callStack[stackPointer++] = programCounter;
                programCounter = (short) (opcode & ALL_OPERANDS_MASK);
                break;
            }
            case TYPE_THREE: {
                short vId = getVIdX(opcode);
                byte value = (byte) (opcode & LAST_TWO_OPERANDS_MASK);
                if (V[vId] == value) {
                    programCounter += 0x0002;
                }
                break;
            }
            case TYPE_FOUR: {
                short vId = getVIdX(opcode);
                byte value = (byte) (opcode & LAST_TWO_OPERANDS_MASK);
                if (V[vId] != value) {
                    programCounter += 0x0002;
                }
                break;
            }
            case TYPE_FIVE: {
                short vIdX = getVIdX(opcode);
                short vIdY = getVIdY(opcode);
                if (V[vIdX] == V[vIdY]) {
                    programCounter += 0x0002;
                }
                break;
            }
            case TYPE_SIX: {
                short vId = getVIdX(opcode);
                byte operands = (byte) (opcode & LAST_TWO_OPERANDS_MASK);
                V[vId] = operands;
                break;
            }
            case TYPE_SEVEN: {
                short vId = getVIdX(opcode);
                byte operands = (byte) (opcode & LAST_TWO_OPERANDS_MASK);
                short temp = V[vId];
                temp += operands;
                V[vId] = (byte) (temp & LAST_TWO_OPERANDS_MASK);
                break;
            }
            case TYPE_EIGHT: {
                executeOperationsTypeEight(opcode);
                break;
            }
            case TYPE_NINE: {
                short vIdX = getVIdX(opcode);
                short vIdY = getVIdY(opcode);
                programCounter += (short) ((V[vIdX] != V[vIdY]) ? 0x0002 : 0);
                break;
            }
            case TYPE_A: {
                I = (short) (opcode & ALL_OPERANDS_MASK);
                break;
            }
            case TYPE_B: {
                short operand = (short) (opcode & ALL_OPERANDS_MASK);
                programCounter = (short) (V[0] + operand);
                break;
            }
            case TYPE_C: {
                short vId = getVIdX(opcode);
                byte random = (byte) rng.nextInt(UNSIGNED_BYTE_MAX_VALUE);
                byte operands = (byte) (opcode & LAST_TWO_OPERANDS_MASK);

                V[vId] = (byte) (random & operands);
                break;
            }
            case TYPE_D: {
                short x = V[getVIdX(opcode)];
                short y = V[getVIdY(opcode)];
                short height = (short) (opcode & THIRD_OPERAND_MASK);
                V[0xF] = 0;
                for (int row = 0; row < height; row++) {
                    byte bytes = ram.readByte((short) (I + row));
                    boolean flippedPixel = graphics.drawLine(x, y, row, bytes);
                    if (flippedPixel) {
                        V[0xF] = 1;
                    }
                }
                break;
            }
            case TYPE_E:
                byte key = V[getVIdX(opcode)];
                short keyEvent = (short) (opcode & LAST_TWO_OPERANDS_MASK);
                if (KEY_EVENT_PRESSED == keyEvent) {
                    programCounter += (short) ((input.isKeyPressed(key)) ? 0x0002 : 0);
                } else if (KEY_EVENT_NOT_PRESSED == keyEvent) {
                    programCounter += (short) ((!input.isKeyPressed(key)) ? 0x0002 : 0);
                }
                break;
            case TYPE_F:
                executeOperationsTypeF(opcode);
                break;
            default:
                throw new IllegalStateException("Invalid opcode found: " + opcode);
        }
    }


    private void executeOperationsTypeEight(short opcode) {
        short vIdX = getVIdX(opcode);
        short vIdY = getVIdY(opcode);
        short type = (short) (opcode & THIRD_OPERAND_MASK);
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
                short tempX = (short) (V[vIdX] & 0xFF);
                short tempY = (short) (V[vIdY] & 0xFF);
                short result = (short) (tempX + tempY);
                V[0xF] = (byte) ((result > UNSIGNED_BYTE_MAX_VALUE) ? 1 : 0);
                V[vIdX] = (byte) (result & LAST_TWO_OPERANDS_MASK);
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

    private void executeOperationsTypeF(short opcode) throws InterruptedException {
        short vIdX = getVIdX(opcode);
        short type = (short) (opcode & LAST_TWO_OPERANDS_MASK);
        switch (type) {
            case 0x0007: {
                V[vIdX] = delayTimer;
                break;
            }
            case 0x000A: {
                V[vIdX] = input.waitForKey();
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
                I = ram.getFontLocation(V[vIdX]);
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
                for (int i = 0; i <= vIdX; i++) {
                    ram.writeByte((short) (I + i), V[i]);
                }
                break;
            }
            case 0x0065: {
                for (int i = 0; i <= vIdX; i++) {
                    V[i] = ram.readByte((short) (I + i));
                }
                break;
            }
        }
    }

    private void updateTimers(long deltaTime) {
        timeSinceTimerUpdate += deltaTime;
        if (timeSinceTimerUpdate >= TIME_TO_UPDATE_TIMERS_IN_NANOS) {
            if (delayTimer > 0) {
                delayTimer--;
            }
            if (soundTimer > 0) {
                sound.play(Sound.TONE_A3);
                soundTimer--;
                if (soundTimer == 0) {
                    sound.stop();
                }
            }
            timeSinceTimerUpdate = 0;
        }
    }

    private short getOpcodeType(short opcode) {
        return (short) (opcode & TYPE_MASK);
    }

    private short getVIdX(short opcode) {
        return (short) ((opcode & FIRST_OPERAND_MASK) >> FIRST_OPERAND_SHIFT);
    }

    private short getVIdY(short opcode) {
        return (short) ((opcode & SECOND_OPERAND_MASK) >> SECOND_OPERAND_SHIFT);
    }
}
