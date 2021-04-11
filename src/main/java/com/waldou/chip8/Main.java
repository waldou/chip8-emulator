package com.waldou.chip8;

import com.waldou.chip8.chipset.CPU;
import com.waldou.chip8.chipset.Graphics;
import com.waldou.chip8.chipset.Input;
import com.waldou.chip8.chipset.RAM;
import com.waldou.chip8.ui.Controller;
import com.waldou.chip8.ui.UIFrame;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    public static final boolean DEBUG = false;

    public static void main(String[] args) throws IOException, InterruptedException {
        new Main().start(args[0]);
    }

    private void start(String filename) throws IOException, InterruptedException {
        byte[] program = readFile(filename);

        RAM ram = new RAM(program);
        Graphics graphics = new Graphics();
        Input input = new Input();
        CPU cpu = new CPU(graphics, input, ram);

        Controller controller = new Controller(input);

        AtomicBoolean frameStarted = new AtomicBoolean(false);
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new UIFrame(graphics, controller);
            frame.setVisible(true);
            frameStarted.set(true);
        });

        long startTime = System.nanoTime();
        long deltaTime;
        while (true) {
            if (frameStarted.get()) {
                deltaTime = System.nanoTime() - startTime;
                startTime = System.nanoTime();
                cpu.cycle(deltaTime);
            }
        }
    }

    private byte[] readFile(String filename) throws IOException {
        File file = new File(filename);
        return Files.readAllBytes(file.toPath());
    }
}
