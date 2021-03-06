package com.waldou.chip8.ui;

import com.waldou.chip8.chipset.Input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Controller implements KeyListener {
    private static final ControllerCommand DEFAULT_COMMAND_IMPL = () -> {
    };

    private final Map<Integer, Integer> KEY_INDEX_MAPPING;
    private final Input input;

    ControllerCommand escapeCommand = DEFAULT_COMMAND_IMPL;
    ControllerCommand resetCommand = DEFAULT_COMMAND_IMPL;
    ControllerCommand switchThemeCommand = DEFAULT_COMMAND_IMPL;

    public Controller(Input input) {
        this.input = input;
        KEY_INDEX_MAPPING = Collections.unmodifiableMap(keyKeyIndexMapping());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // unused
    }

    @Override
    public void keyPressed(KeyEvent e) {
        Integer keyIndex = KEY_INDEX_MAPPING.get(e.getKeyCode());
        if (keyIndex != null) {
            input.press(keyIndex);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Integer keyIndex = KEY_INDEX_MAPPING.get(e.getKeyCode());
        if (keyIndex != null) {
            input.release(keyIndex);
        } else {
            checkSystemKeys(e);
        }
    }

    private void checkSystemKeys(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_BACK_SPACE: {
                resetCommand.execute();
                break;
            }
            case KeyEvent.VK_ESCAPE: {
                escapeCommand.execute();
                break;
            }
            case KeyEvent.VK_RIGHT: {
                switchThemeCommand.execute();
                break;
            }
        }
    }

    public void setupEscapeCommand(ControllerCommand command) {
        escapeCommand = command;
    }

    public void setupResetCommand(ControllerCommand command) {
        resetCommand = command;
    }

    public void setupSwitchThemeCommand(ControllerCommand command) {
        switchThemeCommand = command;
    }

    private Map<Integer, Integer> keyKeyIndexMapping() {
        Map<Integer, Integer> mapping = new HashMap<>();
        // 1234
        mapping.put(49, 0x1);
        mapping.put(50, 0x2);
        mapping.put(51, 0x3);
        mapping.put(52, 0xC);

        // qwer
        mapping.put(81, 0x4);
        mapping.put(87, 0x5);
        mapping.put(69, 0x6);
        mapping.put(82, 0xD);

        // asdf
        mapping.put(65, 0x7);
        mapping.put(83, 0x8);
        mapping.put(68, 0x9);
        mapping.put(70, 0xE);

        // zxcv
        mapping.put(90, 0xA);
        mapping.put(88, 0x0);
        mapping.put(67, 0xB);
        mapping.put(86, 0xF);

        return mapping;
    }

    public interface ControllerCommand {
        void execute();
    }
}
