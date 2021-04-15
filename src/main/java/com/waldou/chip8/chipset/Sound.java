package com.waldou.chip8.chipset;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.HashMap;
import java.util.Map;

public class Sound {
    public static final short TONE_A3 = 220;

    private static final int MAX_VOLUME = 30;
    private static final int SAMPLE_RATE = 44100;
    private static final int LENGTH = 4000;
    private static final double TO_HZ = 2 * Math.PI;

    private boolean enabled = true;
    private boolean playing = false;

    private final Map<Short, byte[]> SOUND_CACHE;
    private AudioFormat af;
    private SourceDataLine sdl;

    public Sound() {
        SOUND_CACHE = new HashMap<>();
        try {
            af = new AudioFormat((float) SAMPLE_RATE, 8, 1, true, false);
            sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af);
            initialize();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            enabled = false;
        }
    }

    private void initialize() {
        sdl.start();
        sdl.write(new byte[4000], 0, 4000);
        sdl.drain();
        sdl.stop();
        sdl.flush();
    }

    public void play(short tone) {
        if (enabled && !playing) {
            byte[] cached = SOUND_CACHE.computeIfAbsent(tone, this::generateSoundData);
            SoundThread st = new SoundThread(cached);
            st.start();
            playing = true;
        }
    }

    public void stop() {
        playing = false;
    }

    private byte[] generateSoundData(int tone) {
        byte[] data = new byte[LENGTH];
        for (int i = 0; i < data.length; i++) {
            double angle = i / ((float) SAMPLE_RATE / tone) * TO_HZ;
            data[i] = (byte) (Math.sin(angle) * MAX_VOLUME);
        }
        return data;
    }

    boolean isEnabled() {
        return enabled;
    }

    boolean isPlaying() {
        return playing;
    }

    private class SoundThread extends Thread {
        private byte[] data;

        public SoundThread(byte[] data) {
            this.data = data;
        }

        @Override
        public void run() {
            try {
                sdl.start();
                while (playing) {
                    sdl.write(data, 0, data.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sdl.stop();
                sdl.flush();
                data = null;
            }
        }
    }
}
