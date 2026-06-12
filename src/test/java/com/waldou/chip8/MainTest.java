package com.waldou.chip8;

import com.waldou.chip8.chipset.CPU;
import com.waldou.chip8.chipset.Graphics;
import com.waldou.chip8.chipset.Input;
import com.waldou.chip8.chipset.RAM;
import com.waldou.chip8.chipset.Sound;
import com.waldou.chip8.ui.Controller;
import com.waldou.chip8.ui.Screen;
import com.waldou.chip8.ui.UIFrame;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainTest {
    @AfterEach
    void tearDown() throws Exception {
        Main.getStartedForTesting().set(false);
        setFrameStarted(false);
    }

    @Test
    void shouldSleepWhenNotStartedAndEmptyFilename() {
        try (MockedStatic<Utils> utilsMockedStatic = Mockito.mockStatic(Utils.class);
             MockedStatic<SwingUtilities> swingUtilitiesMockedStatic = Mockito.mockStatic(SwingUtilities.class)) {
            Main.getStartedForTesting().set(false);

            utilsMockedStatic.when(() -> Utils.isEmpty(null)).thenReturn(true);
            utilsMockedStatic.when(() -> Utils.threadSleep(1000)).thenThrow(new InterruptedException("Fake exception"));

            Exception exception = assertThrows(InterruptedException.class, () -> {
                Main.main(null);
            });

            String expectedMessage = "Fake exception";
            String actualMessage = exception.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Test
    void shouldSleepWhenStartedAndEmptyFilename() {
        try (MockedStatic<Utils> utilsMockedStatic = Mockito.mockStatic(Utils.class);
             MockedStatic<SwingUtilities> swingUtilitiesMockedStatic = Mockito.mockStatic(SwingUtilities.class)) {
            Main.getStartedForTesting().set(true);

            utilsMockedStatic.when(() -> Utils.isEmpty(null)).thenReturn(true);
            utilsMockedStatic.when(() -> Utils.threadSleep(1000)).thenThrow(new InterruptedException("Fake exception"));

            Exception exception = assertThrows(InterruptedException.class, () -> {
                Main.main(null);
            });

            String expectedMessage = "Fake exception";
            String actualMessage = exception.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Test
    void shouldResetStartedFlagWhenProgramFailsToLoad() {
        try (MockedStatic<Utils> utilsMockedStatic = Mockito.mockStatic(Utils.class);
             MockedStatic<SwingUtilities> swingUtilitiesMockedStatic = Mockito.mockStatic(SwingUtilities.class)) {
            Main.getStartedForTesting().set(false);

            utilsMockedStatic.when(() -> Utils.isEmpty(null)).thenReturn(false, true);
            utilsMockedStatic.when(() -> Utils.readFile(null)).thenThrow(new RuntimeException("Fake load failure"));
            utilsMockedStatic.when(() -> Utils.threadSleep(1000)).thenThrow(new InterruptedException("Fake exception"));

            assertThrows(InterruptedException.class, () -> {
                Main.main(null);
            });

            assertTrue(!Main.getStartedForTesting().get());
        }
    }

    @Test
    void shouldLoadProgramAndStartEmulation() {
        try (MockedStatic<Utils> utilsMockedStatic = Mockito.mockStatic(Utils.class);
             MockedStatic<SwingUtilities> swingUtilitiesMockedStatic = Mockito.mockStatic(SwingUtilities.class);
             MockedConstruction<RAM> ramConstruction = Mockito.mockConstruction(RAM.class);
             MockedConstruction<Sound> soundConstruction = Mockito.mockConstruction(Sound.class);
             MockedConstruction<CPU> cpuConstruction = Mockito.mockConstruction(CPU.class, (mock, context) -> {
                 doThrow(new InterruptedException("Stop emulation")).when(mock).cycle(anyLong());
             })) {
            Main.getStartedForTesting().set(false);
            setFrameStarted(true);

            utilsMockedStatic.when(() -> Utils.isEmpty(null)).thenReturn(false, true);
            utilsMockedStatic.when(() -> Utils.readFile(null)).thenReturn(new byte[]{0x00, (byte) 0xE0});
            utilsMockedStatic.when(Utils::systemNanoTime).thenReturn(100L, 150L, 150L);
            utilsMockedStatic.when(() -> Utils.threadSleep(1000)).thenThrow(new InterruptedException("Fake exception"));

            assertThrows(InterruptedException.class, () -> {
                Main.main(null);
            });

            assertEquals(1, ramConstruction.constructed().size());
            assertEquals(1, soundConstruction.constructed().size());
            assertEquals(1, cpuConstruction.constructed().size());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldSetupResetCommand() throws Exception {
        Main main = new Main();
        Graphics mockGraphics = mock(Graphics.class);
        Sound mockSound = mock(Sound.class);
        setCurrentSound(main, mockSound);
        Controller controller = setupController(main, new Input(), mockGraphics, mock(JFrame.class), mock(Screen.class));
        Main.getStartedForTesting().set(true);

        controller.keyReleased(keyEvent(KeyEvent.VK_BACK_SPACE));

        assertTrue(!Main.getStartedForTesting().get());
        verify(mockSound).stop();
        verify(mockGraphics).clearScreen();
    }

    @Test
    void shouldStopSoundWhenEscapeCommandClosesRom() throws Exception {
        Main main = new Main();
        Graphics mockGraphics = mock(Graphics.class);
        Sound mockSound = mock(Sound.class);
        Screen mockScreen = mock(Screen.class);
        JFrame mockFrame = mock(JFrame.class);
        setCurrentSound(main, mockSound);
        Controller controller = setupController(main, new Input(), mockGraphics, mockFrame, mockScreen);
        Main.getStartedForTesting().set(true);

        controller.keyReleased(keyEvent(KeyEvent.VK_ESCAPE));

        assertTrue(!Main.getStartedForTesting().get());
        verify(mockSound).stop();
        verify(mockGraphics).clearScreen();
        verify(mockFrame).validate();
        verify(mockScreen).repaint();
    }

    @Test
    void shouldSetupSwitchThemeCommand() throws Exception {
        Main main = new Main();
        Screen mockScreen = mock(Screen.class);
        Controller controller = setupController(main, new Input(), mock(Graphics.class), mock(JFrame.class), mockScreen);

        controller.keyReleased(keyEvent(KeyEvent.VK_RIGHT));

        verify(mockScreen).switchTheme();
    }

    @Test
    void shouldSetupUI() throws Exception {
        Main main = new Main();
        Screen mockScreen = mock(Screen.class);
        java.awt.Container mockContainer = mock(java.awt.Container.class);

        try (MockedStatic<SwingUtilities> swingUtilitiesMockedStatic = Mockito.mockStatic(SwingUtilities.class);
             MockedConstruction<UIFrame> uiFrameConstruction = Mockito.mockConstruction(UIFrame.class, (mock, context) -> {
                 when(mock.getContentPane()).thenReturn(mockContainer);
                 when(mockContainer.getComponents()).thenReturn(new java.awt.Component[]{mockScreen});
             })) {
            swingUtilitiesMockedStatic.when(() -> SwingUtilities.invokeLater(any(Runnable.class)))
                    .thenAnswer(invocation -> {
                        invocation.getArgument(0, Runnable.class).run();
                        return null;
                    });
            setFrameStarted(false);

            setupUI(main, new Graphics(), new Input());

            UIFrame frame = uiFrameConstruction.constructed().getFirst();
            verify(frame).addKeyListener(any(Controller.class));
            verify(frame).setVisible(true);
            assertTrue(getFrameStarted());
        }
    }

    @Test
    void shouldRunEmulationLoopWhileStartedAndFrameStarted() throws Exception {
        Main main = new Main();
        CPU mockCpu = mock(CPU.class);
        Main.getStartedForTesting().set(true);
        setFrameStarted(true);

        try (MockedStatic<Utils> utilsMockedStatic = Mockito.mockStatic(Utils.class)) {
            utilsMockedStatic.when(Utils::systemNanoTime).thenReturn(100L, 150L, 150L);
            doThrow(new InterruptedException("Stop loop")).when(mockCpu).cycle(50L);

            assertThrows(InterruptedException.class, () -> {
                startEmulation(main, mockCpu);
            });

            verify(mockCpu).cycle(50L);
        } finally {
            Main.getStartedForTesting().set(false);
            setFrameStarted(false);
        }
    }

    private Controller setupController(
            Main main,
            Input input,
            Graphics graphics,
            JFrame frame,
            Screen panel
    ) throws Exception {
        Method method = Main.class.getDeclaredMethod(
                "setupController",
                Input.class,
                Graphics.class,
                JFrame.class,
                Screen.class
        );
        method.setAccessible(true);
        return (Controller) method.invoke(main, input, graphics, frame, panel);
    }

    private void setupUI(Main main, Graphics graphics, Input input) throws Exception {
        Method method = Main.class.getDeclaredMethod("setupUI", Graphics.class, Input.class);
        method.setAccessible(true);
        method.invoke(main, graphics, input);
    }

    private void startEmulation(Main main, CPU cpu) throws Exception {
        Method method = Main.class.getDeclaredMethod("startEmulation", CPU.class);
        method.setAccessible(true);
        try {
            method.invoke(main, cpu);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    private void setFrameStarted(boolean value) throws Exception {
        Field field = Main.class.getDeclaredField("frameStarted");
        field.setAccessible(true);
        AtomicBoolean frameStarted = (AtomicBoolean) field.get(null);
        frameStarted.set(value);
    }

    private void setCurrentSound(Main main, Sound sound) throws Exception {
        Field field = Main.class.getDeclaredField("currentSound");
        field.setAccessible(true);
        field.set(main, sound);
    }

    private boolean getFrameStarted() throws Exception {
        Field field = Main.class.getDeclaredField("frameStarted");
        field.setAccessible(true);
        AtomicBoolean frameStarted = (AtomicBoolean) field.get(null);
        return frameStarted.get();
    }

    private KeyEvent keyEvent(int keyCode) {
        KeyEvent mockKeyEvent = mock(KeyEvent.class);
        when(mockKeyEvent.getKeyCode()).thenReturn(keyCode);
        return mockKeyEvent;
    }
}
