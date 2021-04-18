package com.waldou.chip8.ui;

import com.waldou.chip8.chipset.Graphics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ScreenTest {
    private Screen screen;

    @Mock
    private Graphics mockGraphics;

    @BeforeEach
    void setUp() {
        when(mockGraphics.getScreenWidth()).thenReturn(2);
        when(mockGraphics.getScreenHeight()).thenReturn(1);
        screen = new Screen(mockGraphics);
    }

    @Test
    void shouldHaveCorrectPreferredSize() {
        Dimension preferredSize = screen.getPreferredSize();

        assertEquals(20, preferredSize.getWidth());
        assertEquals(10, preferredSize.getHeight());
    }

    @Test
    void shouldClearScreenWithThemeColor() {
        java.awt.Graphics mockSuperAwtGraphics = mock(java.awt.Graphics.class);
        java.awt.Graphics mockAwtGraphics = mock(java.awt.Graphics.class);
        when(mockAwtGraphics.create()).thenReturn(mockSuperAwtGraphics);

        when(mockGraphics.getPixel(0, 0)).thenReturn(false);
        when(mockGraphics.getPixel(1, 0)).thenReturn(false);

        screen.paintComponent(mockAwtGraphics);

        Color[] currentTheme = screen.getCurrentTheme();

        verify(mockAwtGraphics, atLeastOnce()).setColor(currentTheme[0]);
        verify(mockAwtGraphics, atLeastOnce()).fillRect(0, 0, 20, 10);
    }

    @Test
    void shouldPaintPixelWithThemeColor() {
        java.awt.Graphics mockSuperAwtGraphics = mock(java.awt.Graphics.class);
        java.awt.Graphics mockAwtGraphics = mock(java.awt.Graphics.class);
        when(mockAwtGraphics.create()).thenReturn(mockSuperAwtGraphics);

        when(mockGraphics.getPixel(0, 0)).thenReturn(false);
        when(mockGraphics.getPixel(1, 0)).thenReturn(true);

        screen.paintComponent(mockAwtGraphics);

        Color[] currentTheme = screen.getCurrentTheme();

        verify(mockAwtGraphics, atLeastOnce()).setColor(currentTheme[0]);
        verify(mockAwtGraphics, atLeastOnce()).fillRect(0, 0, 20, 10);

        verify(mockAwtGraphics, atLeastOnce()).setColor(currentTheme[1]);
        verify(mockAwtGraphics, atLeastOnce()).fillRect(11, 1, 8, 8);
    }

    @Test
    void shouldSwitchThemeIndex() {
        assertEquals(0, screen.getCurrentThemeIndex());
        screen.switchTheme();
        assertEquals(1, screen.getCurrentThemeIndex());
    }

    @Test
    void shouldGoBackToFirstThemeIndex() {
        assertEquals(0, screen.getCurrentThemeIndex());
        screen.switchTheme();
        assertEquals(1, screen.getCurrentThemeIndex());
        screen.switchTheme();
        assertEquals(0, screen.getCurrentThemeIndex());
    }
}