package com.waldou.chip8;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class MainTest {
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
}
