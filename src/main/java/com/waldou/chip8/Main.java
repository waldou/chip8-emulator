package com.waldou.chip8;

import com.waldou.chip8.chipset.Graphics;
import com.waldou.chip8.chipset.*;
import com.waldou.chip8.ui.Controller;
import com.waldou.chip8.ui.Screen;
import com.waldou.chip8.ui.UIFrame;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    public static final boolean DEBUG = false;
    private static final String CHIP8_FILE_TYPE = "ch8";
    private static final String CHIP8_FILE_TYPE_DESCRIPTION = "Chip8 ROM";
    private static final AtomicBoolean started = new AtomicBoolean(false);
    private static final AtomicBoolean frameStarted = new AtomicBoolean(false);

    private String filename = null;
    private File currentDirectory = null;

    public static void main(String[] args) throws InterruptedException {
        new Main().initialize();
    }

    private void initialize() throws InterruptedException {
        Graphics graphics = new Graphics();
        Input input = new Input();
        setupUI(graphics, input);

        while (true) {
            // Awaiting for user to select a ROM
            while (!started.get() && Utils.isEmpty(filename)) {
                Utils.threadSleep(1000);
            }

            // Starting system
            started.set(true);
            try {
                byte[] program = Utils.readFile(filename);
                RAM ram = new RAM(program);
                Sound sound = new Sound();
                CPU cpu = new CPU(ram, graphics, input, sound);
                startEmulation(cpu);
            } catch (Exception e) {
                started.set(false);
            }
        }
    }

    /**
     * Setups UI related components and UI related system commands.
     *
     * @param graphics
     * @param input
     */
    private void setupUI(Graphics graphics, Input input) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new UIFrame(graphics);
            ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource(("chip8-emu-icon-high-res.png")));
            frame.setIconImage(icon.getImage());

            Screen panel = (Screen) frame.getContentPane().getComponents()[0];
            Controller controller = setupController(input, graphics, frame, panel);
            frame.addKeyListener(controller);

            setupLoadRomButton(panel);

            frame.setVisible(true);
            frameStarted.set(true);
        });
    }

    /**
     * Adds load rom button to UI.
     *
     * @param panel
     */
    private void setupLoadRomButton(JPanel panel) {
        Button showFileDialogButton = new Button("Open File");

        showFileDialogButton.addActionListener(e -> {
            if (currentDirectory == null) {
                this.currentDirectory = Paths.get(".").toFile();
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(currentDirectory);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.addChoosableFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory())
                        return true;
                    return (f.getName().toLowerCase().endsWith(CHIP8_FILE_TYPE));
                }

                @Override
                public String getDescription() {
                    return CHIP8_FILE_TYPE_DESCRIPTION;
                }
            });
            int returnVal = chooser.showOpenDialog(panel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                filename = chooser.getSelectedFile().getAbsolutePath();
                currentDirectory = chooser.getSelectedFile().getParentFile();
                panel.remove(showFileDialogButton);
            } else {
                filename = null;
            }
        });

        panel.add(showFileDialogButton);
    }

    /**
     * Setups the user controller.
     *
     * @param input
     * @param graphics
     * @param frame
     * @param panel
     * @return
     */
    private Controller setupController(Input input, Graphics graphics, JFrame frame, Screen panel) {
        Controller controller = new Controller(input);
        controller.setupResetCommand(() -> {
            if (started.get()) {
                started.set(false);
                graphics.clearScreen();
            }
        });
        controller.setupEscapeCommand(() -> {
            if (started.get()) {
                started.set(false);
                graphics.clearScreen();
                filename = null;
                setupLoadRomButton(panel);
                frame.validate();
                panel.repaint();
            }
        });

        controller.setupSwitchThemeCommand(panel::switchTheme);
        return controller;
    }

    /**
     * Runs the ROM emulation loop.
     *
     * @param cpu
     * @throws InterruptedException
     */
    private void startEmulation(CPU cpu) throws InterruptedException {
        long startTime = Utils.systemNanoTime();
        long deltaTime;
        while (started.get() && frameStarted.get()) {
            deltaTime = Utils.systemNanoTime() - startTime;
            startTime = Utils.systemNanoTime();
            cpu.cycle(deltaTime);
        }
    }

    static AtomicBoolean getStartedForTesting() {
        return started;
    }
}
