package com.waldou.chip8.chipset;

public class Input {
    private static final int KEYS = 16;
    private boolean[] keys;

    public Input() {
        keys = new boolean[KEYS];
    }

    public void press(int index) {
        keys[index] = true;
    }

    public void release(int index) {
        keys[index] = false;
    }

    boolean isKeyPressed(int index) {
        return keys[index];
    }
}
