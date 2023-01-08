package ca.justinpark.build.dockerjavadebugger.commands;

import ca.justinpark.build.dockerjavadebugger.OperationFailedException;
import com.google.common.base.Strings;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
//import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Optional;

public class SaveRemoteJvmPort extends LaunchConfigurationCommand {
    private final int externalPort;

    public static SaveRemoteJvmPort create(@NotNull Project project, @NotNull String launchConfigurationName, int externalPort) {
        return new SaveRemoteJvmPort(project, launchConfigurationName, externalPort);
    }

    private SaveRemoteJvmPort(@NotNull Project project, @NotNull String launchConfigurationName, int externalPort) {
        super(project, launchConfigurationName);
        this.externalPort = externalPort;
    }

    public void run() throws OperationFailedException {
        if (Strings.isNullOrEmpty(this.launchConfigurationName)) {
            throw new OperationFailedException("No launch configuration provided");
        }
        Optional<RunnerAndConfigurationSettings> launchConfig = getLaunchConfig();
        if (launchConfig.isEmpty()) {
            throw new OperationFailedException(
                    String.format("Could not find launch configuration named %s", this.launchConfigurationName));
        }
        RunConfiguration config = launchConfig.get().getConfiguration();
        try {
            Field portField = config.getClass().getDeclaredField("PORT");
            portField.setAccessible(true);
            portField.set(config, String.valueOf(externalPort));
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            throw new OperationFailedException("Failed to save port to launch configuration to workspace.xml file", e);
        }
    }
}
