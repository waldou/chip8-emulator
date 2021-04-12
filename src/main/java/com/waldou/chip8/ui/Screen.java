package com.waldou.chip8.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class Screen extends JPanel {
    public static final Color[] PIPBOY_THEME = {new Color(55, 59, 53), new Color(89, 255, 101)};

    private static final long REFRESH_RATE = 1000 / 60;
    private static final int PIXEL_SIZE = 10;

    private final Dimension PANEL_SIZE;
    private final Color[] THEME;
    private final com.waldou.chip8.chipset.Graphics graphics;
    private final java.util.Timer timer;

    public Screen(com.waldou.chip8.chipset.Graphics graphics, Color[] theme) {
        this.graphics = graphics;
        PANEL_SIZE = new Dimension(
                graphics.getScreenWidth() * PIXEL_SIZE,
                graphics.getScreenHeight() * PIXEL_SIZE
        );

        timer = new Timer("Timer");
        timer.schedule(new RepaintTask(), 0, REFRESH_RATE);
        THEME = theme;
    }

    @Override
    public Dimension getPreferredSize() {
        return PANEL_SIZE;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(THEME[0]);
        g.fillRect(0, 0, (int) PANEL_SIZE.getWidth(), (int) PANEL_SIZE.getHeight());

        for (int x = 0; x < graphics.getScreenWidth(); x++) {
            for (int y = 0; y < graphics.getScreenHeight(); y++) {
                boolean pixel = graphics.getPixel(x, y);
                if (pixel) {
                    drawPixel(g, x, y, THEME[1]);
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
