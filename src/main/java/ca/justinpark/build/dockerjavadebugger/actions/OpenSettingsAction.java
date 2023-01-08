package ca.justinpark.build.dockerjavadebugger.actions;

import ca.justinpark.build.dockerjavadebugger.DockerDebugService;
import ca.justinpark.build.dockerjavadebugger.DockerDebugState;
import ca.justinpark.build.dockerjavadebugger.OperationFailedException;
import ca.justinpark.build.dockerjavadebugger.commands.LaunchDebug;
import ca.justinpark.build.dockerjavadebugger.commands.SaveRemoteJvmPort;
import ca.justinpark.build.dockerjavadebugger.providers.ContainerInfoProvider;
import ca.justinpark.build.dockerjavadebugger.settings.DockerDebugStateDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OpenSettingsAction extends AnAction {
    private final Logger logger = Logger.getLogger(OpenSettingsAction.class.getName());
    private final ContainerInfoProvider containerInfoProvider = new ContainerInfoProvider();

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        if (event.getProject() == null) {
            return;
        }
        DockerDebugState state = DockerDebugService.getInstance(event.getProject()).getState();
        DockerDebugStateDialog dialog = new DockerDebugStateDialog(state, event.getProject());
        if (!dialog.showAndGet()) {
            // dialog cancelled
            return;
        }

        // user pressed OK
        dialog.getAppSettings().getRemoteJvmDebugName().ifPresent(value -> state.remoteJvmDebug = value);
        dialog.getAppSettings().getContainerName().ifPresent(value -> state.container = value);
        dialog.getAppSettings().getInternalPort().ifPresent(value -> state.internalPort = value);
        logger.config(String.format("Remote JVM Debug = %s", state.remoteJvmDebug));
        logger.config(String.format("Container name = %s", state.container));
        logger.config(String.format("Internal port = %d", state.internalPort));

        if (dialog.getDialogCloseActionType() == DockerDebugStateDialog.DialogCloseActionType.SAVE) {
            // save and close
            return;
        }

        // start debugging process
        Integer externalPort;
        try {
            externalPort = this.containerInfoProvider.fetchExternalPort(state.container, state.internalPort);
            SaveRemoteJvmPort.create(event.getProject(), state.remoteJvmDebug, externalPort).run();
            LaunchDebug.create(event.getProject(), state.remoteJvmDebug).run();
        } catch (OperationFailedException | RuntimeException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            JOptionPane.showMessageDialog(dialog.getOwner(),
                    e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
