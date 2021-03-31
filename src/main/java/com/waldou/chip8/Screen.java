package com.waldou.chip8;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class Screen extends JPanel {
    private static final long REFRESH_RATE = 1000 / 60;
    private static final int PIXEL_SIZE = 10;
    private Dimension PANEL_SIZE;

    com.waldou.chip8.chipset.Graphics graphics;
    java.util.Timer timer;

    public Screen(com.waldou.chip8.chipset.Graphics graphics) {
        this.graphics = graphics;
        PANEL_SIZE = new Dimension(
                graphics.getScreenWidth() * PIXEL_SIZE,
                graphics.getScreenHeight() * PIXEL_SIZE
        );

        timer = new Timer("Timer");
        timer.schedule(new RepaintTask(), 0, REFRESH_RATE);
    }

    @Override
    public Dimension getPreferredSize() {
        return PANEL_SIZE;
    }

    @Override
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

    private class RepaintTask extends TimerTask {
        @Override
        public void run() {
            repaint();
        }
    }
}
