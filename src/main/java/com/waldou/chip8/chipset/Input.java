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

    public byte waitForKey() throws InterruptedException {
        boolean[] copy = new boolean[KEYS];
        System.arraycopy(keys, 0, copy, 0, keys.length);
        byte key = monitorKeyChange(keys, copy);
        while (key == -1) {
            Thread.sleep(1);
            key = monitorKeyChange(keys, copy);
        }
        return key;
    }

    private byte monitorKeyChange(boolean[] a, boolean[] b) {
        for (byte i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return i;
            }
        }
        return -1;
    }
}
