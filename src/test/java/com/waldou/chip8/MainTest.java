package com.waldou.chip8;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class MainTest {
    private final MockedStatic<Utils> utilsMockedStatic = Mockito.mockStatic(Utils.class);
    private final MockedStatic<SwingUtilities> swingUtilitiesMockedStatic = Mockito.mockStatic(SwingUtilities.class);

    @AfterAll
    public void close() {
        utilsMockedStatic.close();
    }

    @Test
    void shouldSleepWhenNotStartedAndEmptyFilename() {
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

    @Test
    void shouldSleepWhenStartedAndEmptyFilename() {
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