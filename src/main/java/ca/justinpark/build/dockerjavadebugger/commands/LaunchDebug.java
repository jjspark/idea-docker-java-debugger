package ca.justinpark.build.dockerjavadebugger.commands;

import ca.justinpark.build.dockerjavadebugger.OperationFailedException;
import com.google.common.base.Strings;
import com.intellij.execution.Executor;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class LaunchDebug extends LaunchConfigurationCommand {
    public static LaunchDebug create(@NotNull Project project, @NotNull String launchConfigurationName) {
        return new LaunchDebug(project, launchConfigurationName);
    }

    private LaunchDebug(@NotNull Project project, @NotNull String launchConfigurationName) {
        super(project, launchConfigurationName);
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
        Executor executor = DefaultDebugExecutor.getDebugExecutorInstance();
        ProgramRunnerUtil.executeConfiguration(launchConfig.get(), executor);
    }
}
