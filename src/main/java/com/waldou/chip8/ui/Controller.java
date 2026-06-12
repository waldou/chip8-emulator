package com.waldou.chip8.ui;

import com.waldou.chip8.chipset.Input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;

public class Controller implements KeyListener {
    private static final ControllerCommand DEFAULT_COMMAND_IMPL = () -> {
    };

    private final Map<Integer, Integer> keyIndexMapping;
    private final Input input;

    ControllerCommand escapeCommand = DEFAULT_COMMAND_IMPL;
    ControllerCommand resetCommand = DEFAULT_COMMAND_IMPL;
    ControllerCommand switchThemeCommand = DEFAULT_COMMAND_IMPL;

    public Controller(Input input) {
        this.input = input;
        keyIndexMapping = keyKeyIndexMapping();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // unused
    }

    @Override
    public void keyPressed(KeyEvent e) {
        Integer keyIndex = keyIndexMapping.get(e.getKeyCode());
        if (keyIndex != null) {
            input.press(keyIndex);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Integer keyIndex = keyIndexMapping.get(e.getKeyCode());
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
        return Map.ofEntries(
                Map.entry(49, 0x1),
                Map.entry(50, 0x2),
                Map.entry(51, 0x3),
                Map.entry(52, 0xC),
                Map.entry(81, 0x4),
                Map.entry(87, 0x5),
                Map.entry(69, 0x6),
                Map.entry(82, 0xD),
                Map.entry(65, 0x7),
                Map.entry(83, 0x8),
                Map.entry(68, 0x9),
                Map.entry(70, 0xE),
                Map.entry(90, 0xA),
                Map.entry(88, 0x0),
                Map.entry(67, 0xB),
                Map.entry(86, 0xF)
        );
    }

    public interface ControllerCommand {
        void execute();
    }
}
