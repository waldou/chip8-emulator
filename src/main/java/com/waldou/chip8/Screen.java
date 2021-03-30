package com.waldou.chip8;

import javax.swing.*;
import java.awt.*;

public class Screen extends JPanel {
    private static final int PIXEL_SIZE = 10;
    private Dimension PANEL_SIZE;

    com.waldou.chip8.chipset.Graphics graphics;

    public Screen(com.waldou.chip8.chipset.Graphics graphics) {
        this.graphics = graphics;
        PANEL_SIZE = new Dimension(
                graphics.getScreenWidth() * PIXEL_SIZE,
                graphics.getScreenHeight() * PIXEL_SIZE
        );
    }

    @Override
    public Dimension getPreferredSize() {
        return PANEL_SIZE;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, (int) PANEL_SIZE.getWidth(), (int) PANEL_SIZE.getHeight());

        for (int x = 0; x < graphics.getScreenWidth(); x++) {
            for (int y = 0; y < graphics.getScreenHeight(); y++) {
                boolean pixel = graphics.getScreen()[x][y];
                if (pixel) {
                    drawPixel(g, x, y, Color.RED);
                }
            }
        }
    }

    private void drawPixel(Graphics g, int x, int y, Color color) {
        g.setColor(color);
        g.fillRect(x * PIXEL_SIZE, y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
    }
}
