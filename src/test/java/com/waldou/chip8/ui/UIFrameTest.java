package com.waldou.chip8.ui;

import com.waldou.chip8.chipset.Graphics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UIFrameTest {
    private UIFrame uiFrame;

    @Mock
    private Graphics mockGraphics;

    @BeforeEach
    void setUp() {
        uiFrame = new UIFrame(mockGraphics);
    }

    @Test
    void shouldExitOnClose() {
        assertEquals(WindowConstants.EXIT_ON_CLOSE, uiFrame.getDefaultCloseOperation());
    }

    @Test
    void shouldCreateScreenComponent() {
        Component[] components = uiFrame.getContentPane().getComponents();

        assertEquals(1, components.length);
        assertTrue(components[0] instanceof Screen);
    }

    @Test
    void shouldNotBeResizable() {
        assertFalse(uiFrame.isResizable());
    }
}