package ca.justinpark.build.dockerjavadebugger.actions;

import ca.justinpark.build.dockerjavadebugger.DockerDebugService;
import ca.justinpark.build.dockerjavadebugger.DockerDebugState;
import ca.justinpark.build.dockerjavadebugger.OperationFailedException;
import ca.justinpark.build.dockerjavadebugger.commands.LaunchDebugWhenContainerStarts;
import ca.justinpark.build.dockerjavadebugger.settings.DockerDebugStateDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OpenSettingsAction extends AnAction {
    private final Logger logger = Logger.getLogger(OpenSettingsAction.class.getName());

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
        try {
            LaunchDebugWhenContainerStarts.create(event.getProject(), state).run();
        } catch (OperationFailedException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            JOptionPane.showMessageDialog(dialog.getOwner(),
                    e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
