package ca.justinpark.build.dockerjavadebugger.actions;

import ca.justinpark.build.dockerjavadebugger.DockerDebugService;
import ca.justinpark.build.dockerjavadebugger.DockerDebugState;
import ca.justinpark.build.dockerjavadebugger.OperationFailedException;
import ca.justinpark.build.dockerjavadebugger.commands.LaunchDebugWhenContainerStarts;
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
        try (MockedStatic<DockerDebugService> dockerDebugServiceStatic = mockStatic(DockerDebugService.class);
             MockedStatic<LaunchDebugWhenContainerStarts> launchDebugStatic = mockStatic(LaunchDebugWhenContainerStarts.class);
        ) {
            ExecuteDebugAction action = new ExecuteDebugActionMock();
            AnActionEvent event = Mockito.mock(AnActionEvent.class);
            Project project = Mockito.mock(Project.class);
            DockerDebugState state = new DockerDebugState() {{
                container = "primitive_slinger";
                remoteJvmDebug = "debug";
                internalPort = 5005;
            }};
            DockerDebugService dockerDebugServiceInstance = Mockito.mock(DockerDebugService.class);
            LaunchDebugWhenContainerStarts launchDebugWhenContainerStarts = Mockito.mock(LaunchDebugWhenContainerStarts.class);
            doAnswer(I -> null).when(launchDebugWhenContainerStarts).run();

            when(event.getProject()).thenReturn(project);
            dockerDebugServiceStatic.when(() -> DockerDebugService.getInstance(any()))
                    .thenReturn(dockerDebugServiceInstance);
            launchDebugStatic.when(() -> LaunchDebugWhenContainerStarts.create(project, state))
                    .thenReturn(launchDebugWhenContainerStarts);
            when(dockerDebugServiceInstance.getState()).thenReturn(state);
            when(containerInfoProviderMock.fetchExternalPort(any(), any())).thenReturn(49201);

            action.actionPerformed(event);

            verify(launchDebugWhenContainerStarts, times(1)).run();
        }

    }

    private class ExecuteDebugActionMock extends ExecuteDebugAction {

        public ExecuteDebugActionMock() {
            super();
            this.containerInfoProvider = containerInfoProviderMock;
        }
    }
}
