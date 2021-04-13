package com.waldou.chip8.chipset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InputTest {
    private Input input;

    @BeforeEach
    void setUp() {
        input = new Input();
    }

    @Test
    void shouldPressKey() {
        assertFalse(input.isKeyPressed(0x0));

        input.press(0x0);

        assertTrue(input.isKeyPressed(0x0));
    }

    @Test
    void shouldReleaseKey() {
        assertFalse(input.isKeyPressed(0x0));

        input.press(0x0);
        assertTrue(input.isKeyPressed(0x0));

        input.release(0x0);
        assertFalse(input.isKeyPressed(0x0));
    }

    @Test
    void shouldWaitForKeyPress() {
        byte expected = 2;
        byte[] key = new byte[1];

        Timer timer = new Timer("KeyPress");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                input.press(expected);
            }
        }, 5000);

        await().atMost(10, TimeUnit.SECONDS).until(waitForKey(input, key));

        assertEquals(expected, key[0]);
    }

    private Callable<Boolean> waitForKey(Input input, byte[] key) {
        return () -> {
            try {
                key[0] = input.waitForKey();
            } catch (Exception e) {
                return false;
            }
            return true;
        };
    }
}