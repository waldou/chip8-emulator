package com.waldou.chip8.chipset;

public class Graphics {
    private static final int SCREEN_WIDTH = 64;
    private static final int SCREEN_HEIGHT = 32;

    private boolean[][] screen;

    public Graphics() {
        screen = new boolean[SCREEN_WIDTH][SCREEN_HEIGHT];
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

    private boolean isBitSet(byte bytes, int mask) {
        return (bytes & (1 << mask)) != 0;
    }
}
