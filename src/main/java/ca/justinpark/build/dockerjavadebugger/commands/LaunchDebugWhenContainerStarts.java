package ca.justinpark.build.dockerjavadebugger.commands;

import ca.justinpark.build.dockerjavadebugger.DockerDebugState;
import ca.justinpark.build.dockerjavadebugger.OperationFailedException;
import ca.justinpark.build.dockerjavadebugger.providers.ContainerInfoProvider;
import com.google.common.base.Strings;
import com.intellij.execution.Executor;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.progress.impl.CoreProgressManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Optional;

public class LaunchDebugWhenContainerStarts extends LaunchConfigurationCommand {
    protected final Project project;
    protected final DockerDebugState state;

    /**
     * This method can be stubbed for unit testing
     *
     * @param project IntelliJ project
     * @param state   State of the plugin
     * @return LaunchDebug
     */
    public static LaunchDebugWhenContainerStarts create(@NotNull Project project, @NotNull DockerDebugState state) throws OperationFailedException {
        if (Strings.isNullOrEmpty(state.remoteJvmDebug)) {
            throw new OperationFailedException("No launch configuration provided. Please open Docker Debug Settings dialog box and select Remote JVM Debug");
        }
        if (Strings.isNullOrEmpty(state.container)) {
            throw new OperationFailedException("No container name provided. Please open Docker Debug Settings dialog box and enter container name to attach the debugger to.");
        }
        return new LaunchDebugWhenContainerStarts(project, state);
    }

    /**
     * Use {@link #create create} static method for creating a new instance.
     *
     * @param project IntelliJ project
     * @param state   State of the plugin
     */
    private LaunchDebugWhenContainerStarts(@NotNull Project project, @NotNull DockerDebugState state) {
        super(project, state.remoteJvmDebug);
        this.project = project;
        this.state = state;
    }

    public void run() throws OperationFailedException {
        Optional<RunnerAndConfigurationSettings> launchConfig = getLaunchConfig();
        if (launchConfig.isEmpty()) {
            throw new OperationFailedException(
                    String.format("Could not find launch configuration named %s", this.launchConfigurationName));
        }

        Task.Backgroundable task = new WaitForContainerAndLaunchDebugBackgroundTask(project, state, launchConfig.get());
        BackgroundableProcessIndicator progressIndicator = new BackgroundableProcessIndicator(task);
        CoreProgressManager.getInstance().runProcessWithProgressAsynchronously(task, progressIndicator);
    }

    static class WaitForContainerAndLaunchDebugBackgroundTask extends com.intellij.openapi.progress.Task.Backgroundable {

        private static final int DEFAULT_WAIT_SECONDS = 120;
        private static final int MAX_WAIT_SECONDS = 600;
        private final DockerDebugState state;
        private final Project project;
        private final RunnerAndConfigurationSettings launchConfig;

        WaitForContainerAndLaunchDebugBackgroundTask(Project project, DockerDebugState state, RunnerAndConfigurationSettings launchConfig) {
            super(project, "Waiting for the container", true);
            this.launchConfig = launchConfig;
            this.state = state;
            this.project = project;
        }

        public int getWaitForContainerSeconds() {
            if (state.waitForContainerSeconds == null
                    || state.waitForContainerSeconds <= 0
                    || state.waitForContainerSeconds > MAX_WAIT_SECONDS) {
                return DEFAULT_WAIT_SECONDS;
            }
            return state.waitForContainerSeconds;
        }

        @Override
        public void run(@NotNull ProgressIndicator progressIndicator) {
            boolean debuggerLaunched = false;
            ContainerInfoProvider containerInfoProvider = new ContainerInfoProvider();
            long startTimestamp = System.currentTimeMillis();
            long expiryTimestamp = startTimestamp + getWaitForContainerSeconds() * Duration.ofSeconds(1).toMillis();
            long nextPollTimestamp = startTimestamp;
            while (System.currentTimeMillis() < expiryTimestamp) {
                try {
                    progressIndicator.setFraction((double) (System.currentTimeMillis() - startTimestamp) / Duration.ofSeconds(1).toMillis() / getWaitForContainerSeconds());
                    progressIndicator.checkCanceled();

                    Integer externalPort = containerInfoProvider.fetchExternalPort(state.container, state.internalPort);
                    SaveRemoteJvmPort.create(project, state.remoteJvmDebug, externalPort).run();
                    Executor executor = DefaultDebugExecutor.getDebugExecutorInstance();
                    ProgramRunnerUtil.executeConfiguration(launchConfig, executor);
                    debuggerLaunched = true;
                    break;
                } catch (OperationFailedException e) {
                    nextPollTimestamp = nextPollTimestamp + Duration.ofSeconds(1).toMillis();
                    try {
                        while (System.currentTimeMillis() < nextPollTimestamp) {
                            Thread.sleep(100);
                            progressIndicator.checkCanceled();
                        }
                    } catch (InterruptedException ex) {
                    }
                } catch (ProcessCanceledException e) {
                    break;
                }
            }
            if (!debuggerLaunched) {
                NotificationGroupManager.getInstance()
                        .getNotificationGroup("Docker Java Debugger")
                        .createNotification("Container not found", NotificationType.INFORMATION)
                        .notify(project);
            }
        }
    }
}
