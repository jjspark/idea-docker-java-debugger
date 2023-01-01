package ca.justinpark.build.dockerjavadebugger.commands;

import ca.justinpark.build.dockerjavadebugger.OperationFailedException;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.openapi.project.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;

public class LaunchDebugTest {
    private Project project;

    @BeforeEach
    void init() {
        this.project = Mockito.mock(Project.class);

    }

    @Test
    void run() throws OperationFailedException {
        LaunchDebug launchDebug = Mockito.spy(LaunchDebug.create(project, "debug"));
        RunnerAndConfigurationSettings launchConfig = Mockito.mock(RunnerAndConfigurationSettings.class);
        doReturn(Optional.of(launchConfig)).when(launchDebug).getLaunchConfig();
        DefaultDebugExecutor executor = new DefaultDebugExecutor();

        try (MockedStatic<DefaultDebugExecutor> mockedDefaultExecutor = mockStatic(DefaultDebugExecutor.class);
             MockedStatic<ProgramRunnerUtil> programRunnerUtil = mockStatic(ProgramRunnerUtil.class)) {
            mockedDefaultExecutor.when(DefaultDebugExecutor::getDebugExecutorInstance).thenReturn(executor);
            programRunnerUtil.when(() -> ProgramRunnerUtil.executeConfiguration(launchConfig, executor))
                    .thenAnswer((Answer<Void>) invocation -> null);

            launchDebug.run();

            mockedDefaultExecutor.verify(DefaultDebugExecutor::getDebugExecutorInstance);
            programRunnerUtil.verify(() -> ProgramRunnerUtil.executeConfiguration(launchConfig, executor));
        }
    }

    @Test
    void emptyLaunchConfigurationName() throws OperationFailedException {
        Exception exception = assertThrows(OperationFailedException.class, () -> {
            LaunchDebug launchDebug = Mockito.spy(LaunchDebug.create(project, ""));
            launchDebug.run();
        });
        String expectedMessage = "No launch configuration provided";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void nullLaunchConfigurationName() throws OperationFailedException {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            LaunchDebug launchDebug = Mockito.spy(LaunchDebug.create(project, null));
            launchDebug.run();
        });
        String expectedMessage = "must not be null";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noRunnerAndConfigurationSettings() throws OperationFailedException {
        Exception exception = assertThrows(OperationFailedException.class, () -> {
            LaunchDebug launchDebug = Mockito.spy(LaunchDebug.create(project, "debug"));
            doReturn(Optional.empty()).when(launchDebug).getLaunchConfig();

            launchDebug.run();
        });
        String expectedMessage = "Could not find launch configuration named debug";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
