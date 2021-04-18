package com.waldou.chip8.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class Screen extends JPanel {
    public static final Color[] CLASSIC_THEME = {Color.BLACK, Color.WHITE};
    public static final Color[] PIPBOY_THEME = {new Color(55, 59, 53), new Color(89, 255, 101)};
    private static final java.util.List<Color[]> THEMES = Arrays.asList(PIPBOY_THEME, CLASSIC_THEME);

    private static final long REFRESH_RATE = 1000 / 60;
    private static final int PIXEL_SIZE = 10;

    private final Dimension panelSize;
    private final com.waldou.chip8.chipset.Graphics graphics;
    private final java.util.Timer timer;

    private int currentThemeIndex = 0;

    public Screen(com.waldou.chip8.chipset.Graphics graphics) {
        this.graphics = graphics;
        panelSize = new Dimension(
                graphics.getScreenWidth() * PIXEL_SIZE,
                graphics.getScreenHeight() * PIXEL_SIZE
        );

        timer = new Timer("Timer");
        timer.schedule(new RepaintTask(), 0, REFRESH_RATE);
    }

    @Override
    public Dimension getPreferredSize() {
        return panelSize;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Color[] theme = THEMES.get(currentThemeIndex);
        g.setColor(theme[0]);
        g.fillRect(0, 0, (int) panelSize.getWidth(), (int) panelSize.getHeight());
        for (int x = 0; x < graphics.getScreenWidth(); x++) {
            for (int y = 0; y < graphics.getScreenHeight(); y++) {
                boolean pixel = graphics.getPixel(x, y);
                if (pixel) {
                    drawPixel(g, x, y, theme[1], PIXEL_SIZE);
                }
            }
        }
    }

    private void drawPixel(Graphics g, int x, int y, Color color, int pixelSize) {
        g.setColor(color);
        g.fillRect((x * pixelSize) + 1, (y * pixelSize) + 1, pixelSize - 2, pixelSize - 2);
    }

    public void switchTheme() {
        currentThemeIndex = currentThemeIndex == THEMES.size() - 1 ? 0 : currentThemeIndex + 1;
    }

    Color[] getCurrentTheme() {
        return THEMES.get(currentThemeIndex);
    }

    int getCurrentThemeIndex() {
        return currentThemeIndex;
    }

    private class RepaintTask extends TimerTask {
        @Override
        public void run() {
            repaint();
        }
    }
}
