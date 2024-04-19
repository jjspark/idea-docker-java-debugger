package ca.justinpark.build.dockerjavadebugger.commands;

import ca.justinpark.build.dockerjavadebugger.DockerDebugState;
import ca.justinpark.build.dockerjavadebugger.OperationFailedException;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.impl.CoreProgressManager;
import com.intellij.openapi.progress.util.ProgressWindow;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LaunchDebugWhenContainerStartsTest {
    private Project project;

    @BeforeEach
    void init() {
        this.project = Mockito.mock(Project.class);

    }

    @Test
    void run() throws OperationFailedException {
        DockerDebugState state = new DockerDebugState();
        state.remoteJvmDebug = "debug";
        state.internalPort = 5005;
        state.container = "reflective_pigeon";
        LaunchDebugWhenContainerStarts launchDebug = Mockito.spy(LaunchDebugWhenContainerStarts.create(project, state));
        RunnerAndConfigurationSettings launchConfig = Mockito.mock(RunnerAndConfigurationSettings.class);
        doReturn(Optional.of(launchConfig)).when(launchDebug).getLaunchConfig();
        DefaultDebugExecutor executor = new DefaultDebugExecutor();
        Application mockApplication = Mockito.mock(Application.class);
        MessageBus mockMessageBus = Mockito.mock(MessageBus.class);
        doReturn(mockMessageBus).when(mockApplication).getMessageBus();
        ProgressWindow.Listener mockListener = Mockito.mock(ProgressWindow.Listener.class);
        doReturn(mockListener).when(mockMessageBus).syncPublisher(any());
        ProgressManager mockProgressManager = Mockito.mock(ProgressManager.class);

        try (MockedStatic<DefaultDebugExecutor> mockedDefaultExecutor = mockStatic(DefaultDebugExecutor.class);
             MockedStatic<ProgramRunnerUtil> programRunnerUtil = mockStatic(ProgramRunnerUtil.class);
             MockedStatic<ModalityState> modalityState = mockStatic(ModalityState.class);
             MockedStatic<ApplicationManager> applicationManager = mockStatic(ApplicationManager.class);
             MockedStatic<CoreProgressManager> coreProgressManager = mockStatic(CoreProgressManager.class);
             MockedStatic<ProgressManager> progressManager = mockStatic(ProgressManager.class)) {
            mockedDefaultExecutor.when(DefaultDebugExecutor::getDebugExecutorInstance).thenReturn(executor);
            programRunnerUtil.when(() -> ProgramRunnerUtil.executeConfiguration(launchConfig, executor))
                    .thenAnswer((Answer<Void>) invocation -> null);
            modalityState.when(ModalityState::defaultModalityState).thenReturn(Mockito.mock(ModalityState.class));
            applicationManager.when(ApplicationManager::getApplication).thenReturn(mockApplication);
            coreProgressManager.when(CoreProgressManager::getInstance).thenReturn(mockProgressManager);
            progressManager.when(ProgressManager::getInstance).thenReturn(mockProgressManager);

            launchDebug.run();

            verify(mockProgressManager).runProcessWithProgressAsynchronously(any(), any());
        }
    }

    @Test
    void emptyLaunchConfigurationName() {
        DockerDebugState state = new DockerDebugState();
        state.remoteJvmDebug = "";
        Exception exception = assertThrows(OperationFailedException.class, () -> {
            LaunchDebugWhenContainerStarts launchDebug = Mockito.spy(LaunchDebugWhenContainerStarts.create(project, state));
            launchDebug.run();
        });
        String expectedMessage = "No launch configuration provided";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void nullLaunchConfigurationName() {
        DockerDebugState state = new DockerDebugState();
        Exception exception = assertThrows(OperationFailedException.class, () -> {
            LaunchDebugWhenContainerStarts launchDebug = Mockito.spy(LaunchDebugWhenContainerStarts.create(project, state));
            launchDebug.run();
        });
        String expectedMessage = "No launch configuration provided";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noRunnerAndConfigurationSettings() {
        DockerDebugState state = new DockerDebugState();
        state.remoteJvmDebug = "debug";
        Exception exception = assertThrows(OperationFailedException.class, () -> {
            LaunchDebugWhenContainerStarts launchDebug = Mockito.spy(LaunchDebugWhenContainerStarts.create(project, state));
            doReturn(Optional.empty()).when(launchDebug).getLaunchConfig();

            launchDebug.run();
        });
        String expectedMessage = "No container name provided";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void taskExpires() {
        DockerDebugState state = new DockerDebugState() {{
            container = "primitive_slinger";
            remoteJvmDebug = "debug";
            internalPort = 5005;
            waitForContainerSeconds = 1;
        }};
        ProgressIndicator mockProgressIndicator = Mockito.mock(ProgressIndicator.class);
        RunnerAndConfigurationSettings launchConfig = Mockito.mock(RunnerAndConfigurationSettings.class);
        LaunchDebugWhenContainerStarts.WaitForContainerAndLaunchDebugBackgroundTask task =
                new LaunchDebugWhenContainerStarts.WaitForContainerAndLaunchDebugBackgroundTask(project, state, launchConfig);
        NotificationGroupManager mockedNotificationGroupManager = Mockito.mock(NotificationGroupManager.class);
        NotificationGroup mockedNotificationGroup = Mockito.mock(NotificationGroup.class);
        Notification mockedNotification = Mockito.mock(Notification.class);
        doReturn(mockedNotificationGroup).when(mockedNotificationGroupManager).getNotificationGroup("Docker Java Debugger");
        doReturn(mockedNotification).when(mockedNotificationGroup).createNotification("Container not found", NotificationType.INFORMATION);

        try (MockedStatic<NotificationGroupManager> notificationGroupManager = mockStatic(NotificationGroupManager.class)) {
            notificationGroupManager.when(NotificationGroupManager::getInstance).thenReturn(mockedNotificationGroupManager);

            task.run(mockProgressIndicator);

            verify(mockedNotification).notify(project);
        }

    }
}
