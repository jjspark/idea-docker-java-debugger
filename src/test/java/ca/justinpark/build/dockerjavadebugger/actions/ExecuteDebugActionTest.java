package ca.justinpark.build.dockerjavadebugger.actions;

import ca.justinpark.build.dockerjavadebugger.DockerDebugService;
import ca.justinpark.build.dockerjavadebugger.DockerDebugState;
import ca.justinpark.build.dockerjavadebugger.OperationFailedException;
import ca.justinpark.build.dockerjavadebugger.commands.LaunchDebug;
import ca.justinpark.build.dockerjavadebugger.commands.SaveRemoteJvmPort;
import ca.justinpark.build.dockerjavadebugger.providers.ContainerInfoProvider;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ExecuteDebugActionTest {

    ContainerInfoProvider containerInfoProviderMock = Mockito.mock(ContainerInfoProvider.class);

    @Test
    void actionPerformed() throws OperationFailedException {
        try (MockedStatic<DockerDebugService> dockerDebugService = mockStatic(DockerDebugService.class);
             MockedStatic<SaveRemoteJvmPort> saveRemoteJvmPort = mockStatic(SaveRemoteJvmPort.class);
             MockedStatic<LaunchDebug> launchDebug = mockStatic(LaunchDebug.class)) {
            ExecuteDebugAction action = new ExecuteDebugActionMock();
            AnActionEvent event = Mockito.mock(AnActionEvent.class);
            Project project = Mockito.mock(Project.class);
            DockerDebugState state = new DockerDebugState() {{
                container = "primitive_slinger";
                remoteJvmDebug = "debug";
                internalPort = 5005;
            }};
            DockerDebugService dockerDebugServiceInstance = Mockito.mock(DockerDebugService.class);
            SaveRemoteJvmPort saveRemoteJvmPortInstance = Mockito.mock(SaveRemoteJvmPort.class);
            LaunchDebug launchDebugInstance = Mockito.mock(LaunchDebug.class);

            when(event.getProject()).thenReturn(project);
            dockerDebugService.when(() -> DockerDebugService.getInstance(any()))
                    .thenReturn(dockerDebugServiceInstance);
            saveRemoteJvmPort.when(() -> SaveRemoteJvmPort.create(project, state.remoteJvmDebug, 49201))
                    .thenReturn(saveRemoteJvmPortInstance);
            launchDebug.when(() -> LaunchDebug.create(project, state.remoteJvmDebug))
                    .thenReturn(launchDebugInstance);

            when(dockerDebugServiceInstance.getState()).thenReturn(state);
            when(containerInfoProviderMock.fetchExternalPort(any(), any())).thenReturn(49201);

            action.actionPerformed(event);

            verify(launchDebugInstance, times(1)).run();
            verify(saveRemoteJvmPortInstance, times(1)).run();
        }

    }

    private class ExecuteDebugActionMock extends ExecuteDebugAction {

        public ExecuteDebugActionMock() {
            super();
            this.containerInfoProvider = containerInfoProviderMock;
        }
    }
}
