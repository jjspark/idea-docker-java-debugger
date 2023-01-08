package ca.justinpark.build.dockerjavadebugger.commands;

import ca.justinpark.build.dockerjavadebugger.OperationFailedException;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class LaunchConfigurationCommand {
    protected final Project project;
    protected final String launchConfigurationName;

    public LaunchConfigurationCommand(@NotNull Project project, @NotNull String launchConfigurationName) {
        this.project = project;
        this.launchConfigurationName = launchConfigurationName;
    }

    public Optional<RunnerAndConfigurationSettings> getLaunchConfig() {
        RunnerAndConfigurationSettings result =
                RunManager.getInstance(this.project).findConfigurationByName(this.launchConfigurationName);
        return result != null ? Optional.of(result) : Optional.empty();
    }

    public abstract void run() throws OperationFailedException;

}
