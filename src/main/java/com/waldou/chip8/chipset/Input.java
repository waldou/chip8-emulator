package com.waldou.chip8.chipset;

public class Input {
    private static final int KEYS = 16;
    private char[] keys;

    public Input() {
        keys = new char[KEYS];
    }
}
