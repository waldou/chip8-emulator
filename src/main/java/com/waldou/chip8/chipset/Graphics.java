package com.waldou.chip8.chipset;

import java.util.List;

public class Graphics {
    private static final Character[] FONT_SET_ARRAY = {
            0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
            0x20, 0x60, 0x20, 0x20, 0x70, // 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
            0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    };
    private static final int SCREEN_WIDTH = 64;
    private static final int SCREEN_HEIGHT = 32;

    private final List<Character> FONT_SET;
    private boolean[][] screen;

    public Graphics() {
        screen = new boolean[SCREEN_WIDTH][SCREEN_HEIGHT];
        FONT_SET = List.of(FONT_SET_ARRAY);
    }

    boolean drawLine(int x, int y, int currentRow, byte bytes) {
        boolean flippedPixel = false;
        for (int i = 0; i < 8; i++) {
            int finalX = (x + i) % getScreenWidth();
            int finalY = (y + currentRow) % getScreenHeight();

            boolean prevPixel = getPixel(finalX, finalY);
            boolean newPixel = prevPixel ^ isBitSet(bytes, 7 - i);

            setPixel(finalX, finalY, newPixel);

            if (prevPixel && !newPixel) {
                flippedPixel = true;
            }
        }
        return flippedPixel;
    }

    void clearScreen() {
        for (int i = 0; i < SCREEN_WIDTH; i++) {
            for (int j = 0; j < SCREEN_HEIGHT; j++) {
                screen[i][j] = false;
            }
        }
    }

    public int getScreenWidth() {
        return SCREEN_WIDTH;
    }

    public int getScreenHeight() {
        return SCREEN_HEIGHT;
    }

    public boolean getPixel(int x, int y) {
        return screen[x][y];
    }

    private void setPixel(int x, int y, boolean value) {
        screen[x][y] = value;
    }

    List<Character> getFontSet() {
        return FONT_SET;
    }

    private boolean isBitSet(byte bytes, int mask) {
        return (bytes & (1 << mask)) != 0;
    }
}
