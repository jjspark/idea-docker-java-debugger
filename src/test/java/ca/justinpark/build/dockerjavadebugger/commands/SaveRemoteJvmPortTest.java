package ca.justinpark.build.dockerjavadebugger.commands;

import ca.justinpark.build.dockerjavadebugger.OperationFailedException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

public class SaveRemoteJvmPortTest {
    private Project project;

    @BeforeEach
    void init() {
        this.project = Mockito.mock(Project.class);
    }

    @Test
    void run() throws OperationFailedException {
        SaveRemoteJvmPort saveRemoteJvmPort = Mockito.spy(SaveRemoteJvmPort.create(project, "debug", 49201));
        RunnerAndConfigurationSettings launchConfig = Mockito.mock(RunnerAndConfigurationSettings.class);
        MockRunConfiguration runConfiguration = Mockito.spy(new MockRunConfiguration());
        doReturn(Optional.of(launchConfig)).when(saveRemoteJvmPort).getLaunchConfig();
        doReturn(runConfiguration).when(launchConfig).getConfiguration();

        saveRemoteJvmPort.run();

        assertEquals("49201", runConfiguration.getPort());
    }

    @Test
    void emptyLaunchConfigurationName() {
        Exception exception = assertThrows(OperationFailedException.class, () -> {
            SaveRemoteJvmPort saveRemoteJvmPort = Mockito.spy(SaveRemoteJvmPort.create(project, "", 49201));
            saveRemoteJvmPort.run();
        });
        String expectedMessage = "No launch configuration provided";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void nullLaunchConfigurationName()  {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            SaveRemoteJvmPort saveRemoteJvmPort = Mockito.spy(SaveRemoteJvmPort.create(project, null, 49201));
            saveRemoteJvmPort.run();
        });
        String expectedMessage = "must not be null";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noRunnerAndConfigurationSettings() {
        Exception exception = assertThrows(OperationFailedException.class, () -> {
            SaveRemoteJvmPort saveRemoteJvmPort = Mockito.spy(SaveRemoteJvmPort.create(project, "debug", 49201));
            doReturn(Optional.empty()).when(saveRemoteJvmPort).getLaunchConfig();

            saveRemoteJvmPort.run();
        });
        String expectedMessage = "Could not find launch configuration named debug";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noRunConfigurationHasNoPortField() {
        Exception exception = assertThrows(OperationFailedException.class, () -> {
            SaveRemoteJvmPort saveRemoteJvmPort = Mockito.spy(SaveRemoteJvmPort.create(project, "debug", 49201));
            RunnerAndConfigurationSettings launchConfig = Mockito.mock(RunnerAndConfigurationSettings.class);
            RunConfiguration runConfiguration = Mockito.mock(RunConfiguration.class);
            doReturn(Optional.of(launchConfig)).when(saveRemoteJvmPort).getLaunchConfig();
            doReturn(runConfiguration).when(launchConfig).getConfiguration();

            saveRemoteJvmPort.run();
        });

        String expectedMessage = "Failed to save port to launch configuration to workspace.xml file";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    public static class MockRunConfiguration implements RunConfiguration {
        public String PORT;

        public String getPort() {
            return PORT;
        }

        @Override
        public @Nullable ConfigurationFactory getFactory() {
            return null;
        }

        @Override
        public void setName(String name) {

        }

        @Override
        public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
            return null;
        }

        @Override
        public Project getProject() {
            return null;
        }

        @Override
        public RunConfiguration clone() {
            return null;
        }

        @Override
        public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
            return null;
        }

        @Override
        public @NotNull String getName() {
            return "myrun";
        }

        @Override
        public @Nullable Icon getIcon() {
            return null;
        }
    }
}
