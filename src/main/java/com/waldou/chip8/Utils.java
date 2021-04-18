package com.waldou.chip8;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Utils {
    private Utils() {
    }

    public static byte[] readFile(String filename) throws IOException {
        File file = new File(filename);
        return Files.readAllBytes(file.toPath());
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static long systemNanoTime() {
        return System.nanoTime();
    }

    public static void threadSleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }
}
