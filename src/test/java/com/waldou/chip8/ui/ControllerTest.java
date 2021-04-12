package com.waldou.chip8.ui;

import com.waldou.chip8.chipset.Input;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControllerTest {
    private Controller controller;

    @Mock
    private Input mockInput;

    @BeforeEach
    void setUp() {
        controller = new Controller(mockInput);
    }

    @Test
    void shouldSendKeyPressToInput() {
        Map<Integer, Integer> mapping = keyKeyIndexMapping();
        for (Integer key : mapping.keySet()) {
            KeyEvent mockKeyEvent = mock(KeyEvent.class);
            when(mockKeyEvent.getKeyCode()).thenReturn(key);
            controller.keyPressed(mockKeyEvent);

            verify(mockInput).press(mapping.get(key));
        }
    }

    @Test
    void shouldSendKeyReleaseToInput() {
        Map<Integer, Integer> mapping = keyKeyIndexMapping();
        for (Integer key : mapping.keySet()) {
            KeyEvent mockKeyEvent = mock(KeyEvent.class);
            when(mockKeyEvent.getKeyCode()).thenReturn(key);
            controller.keyReleased(mockKeyEvent);

            verify(mockInput).release(mapping.get(key));
        }
    }

    @Test
    void shouldNotSendKeyPressToInputIfKeyIsNotMapped() {
        KeyEvent mockKeyEvent = mock(KeyEvent.class);
        when(mockKeyEvent.getKeyCode()).thenReturn(10);
        controller.keyPressed(mockKeyEvent);

        verify(mockInput, never()).press(any(Integer.class));
    }

    @Test
    void shouldNotSendKeyReleaseToInputIfKeyIsNotMapped() {
        KeyEvent mockKeyEvent = mock(KeyEvent.class);
        when(mockKeyEvent.getKeyCode()).thenReturn(10);
        controller.keyReleased(mockKeyEvent);

        verify(mockInput, never()).release(any(Integer.class));
    }

    private Map<Integer, Integer> keyKeyIndexMapping() {
        Map<Integer, Integer> mapping = new HashMap<>();
        mapping.put(49, 0x1);
        mapping.put(50, 0x2);
        mapping.put(51, 0x3);
        mapping.put(52, 0xC);
        mapping.put(81, 0x4);
        mapping.put(87, 0x5);
        mapping.put(69, 0x6);
        mapping.put(82, 0xD);
        mapping.put(65, 0x7);
        mapping.put(83, 0x8);
        mapping.put(68, 0x9);
        mapping.put(70, 0xE);
        mapping.put(90, 0xA);
        mapping.put(88, 0x0);
        mapping.put(67, 0xB);
        mapping.put(86, 0xF);
        return mapping;
    }
}