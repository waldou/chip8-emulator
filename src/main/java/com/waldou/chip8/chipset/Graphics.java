package com.waldou.chip8.chipset;

import java.util.Arrays;

public class Graphics {
    private static final int SCREEN_WIDTH = 64;
    private static final int SCREEN_HEIGHT = 32;

    private final boolean[] screen;

    public Graphics() {
        screen = new boolean[SCREEN_WIDTH * SCREEN_HEIGHT];
    }

    boolean drawLine(int x, int y, int currentRow, byte bytes) {
        boolean flippedPixel = false;
        for (int i = 0; i < 8; i++) {
            int finalX = (x + i) % SCREEN_WIDTH;
            int finalY = (y + currentRow) % SCREEN_HEIGHT;
            int index = pixelIndex(finalX, finalY);

            boolean prevPixel = screen[index];
            boolean newPixel = prevPixel ^ ((bytes & (1 << (7 - i))) != 0);

            screen[index] = newPixel;

            if (prevPixel && !newPixel) {
                flippedPixel = true;
            }
        }
        return flippedPixel;
    }

    public void clearScreen() {
        Arrays.fill(screen, false);
    }

    public int getScreenWidth() {
        return SCREEN_WIDTH;
    }

    public int getScreenHeight() {
        return SCREEN_HEIGHT;
    }

    public boolean getPixel(int x, int y) {
        return screen[pixelIndex(x, y)];
    }

    private int pixelIndex(int x, int y) {
        return (y * SCREEN_WIDTH) + x;
    }
}
