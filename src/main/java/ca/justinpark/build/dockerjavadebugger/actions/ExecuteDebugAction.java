package ca.justinpark.build.dockerjavadebugger.actions;

import ca.justinpark.build.dockerjavadebugger.DockerDebugService;
import ca.justinpark.build.dockerjavadebugger.DockerDebugState;
import ca.justinpark.build.dockerjavadebugger.OperationFailedException;
import ca.justinpark.build.dockerjavadebugger.commands.LaunchDebugWhenContainerStarts;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecuteDebugAction extends AnAction {
    private final Logger logger = Logger.getLogger(OpenSettingsAction.class.getName());

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        if (event.getProject() == null) {
            return;
        }
        DockerDebugState state = DockerDebugService.getInstance(event.getProject()).getState();
        try {
            LaunchDebugWhenContainerStarts.create(event.getProject(), state).run();
        } catch (OperationFailedException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            com.intellij.openapi.ui.Messages.showErrorDialog(e.getMessage(), "Configuration error");
//            JOptionPane.showMessageDialog(com.intellij.openapi.wm.WindowManager.getInstance().suggestParentWindow(event.getProject()),
//                    e.getMessage(), "Message", JOptionPane.PLAIN_MESSAGE);
//            JOptionPane.showInternalMessageDialog(null,
//                    e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
        }
    }
}
