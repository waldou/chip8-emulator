package com.waldou.chip8;

import com.waldou.chip8.chipset.CPU;
import com.waldou.chip8.chipset.Graphics;
import com.waldou.chip8.chipset.Input;
import com.waldou.chip8.chipset.RAM;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        new Main().start(args[0]);
    }

    private void start(String filename) throws IOException, InterruptedException {
        byte[] program = readFile(filename);

        RAM ram = new RAM(program);
        Graphics graphics = new Graphics();
        Input input = new Input();
        CPU cpu = new CPU(graphics, input, ram);

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new UIFrame(graphics);
            frame.setVisible(true);
        });

        long startTime = System.nanoTime();
        long deltaTime;
        while (true) {
            deltaTime = System.nanoTime() - startTime;
            startTime = System.nanoTime();
            cpu.cycle(deltaTime);
        }
    }

    private byte[] readFile(String filename) throws IOException {
        File file = new File(filename);
        return Files.readAllBytes(file.toPath());
    }
}
