package com.waldou.chip8.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;

public class UIFrame extends JFrame {
    private static final String WINDOW_NAME = "Chip8 Emulator";

    public UIFrame(com.waldou.chip8.chipset.Graphics graphics, KeyListener input) {
        super(WINDOW_NAME);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().add(new Screen(graphics), BorderLayout.CENTER);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        addKeyListener(input);
    }
}
