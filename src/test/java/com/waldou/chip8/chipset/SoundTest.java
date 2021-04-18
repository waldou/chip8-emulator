package com.waldou.chip8.chipset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class SoundTest {
    private Sound sound;

    @BeforeEach
    void setUp() {
        sound = new Sound();
    }

    @Test
    void shouldSetEnabledOnInitialization() {
        assertTrue(sound.isEnabled());
    }

    @Test
    void shouldNotBePlayingOnInitialization() {
        assertFalse(sound.isPlaying());
    }

    @Test
    void shouldSetPlayingStateCorrectly() {
        sound.play(Sound.TONE_A3);

        await().until(() -> sound.isPlaying());

        assertTrue(sound.isPlaying());

        sound.stop();
        assertFalse(sound.isPlaying());
    }
}