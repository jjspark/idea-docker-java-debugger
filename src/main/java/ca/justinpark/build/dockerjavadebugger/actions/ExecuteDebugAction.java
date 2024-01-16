package ca.justinpark.build.dockerjavadebugger.actions;

import ca.justinpark.build.dockerjavadebugger.DockerDebugService;
import ca.justinpark.build.dockerjavadebugger.DockerDebugState;
import ca.justinpark.build.dockerjavadebugger.OperationFailedException;
import ca.justinpark.build.dockerjavadebugger.commands.LaunchDebugWhenContainerStarts;
import ca.justinpark.build.dockerjavadebugger.providers.ContainerInfoProvider;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecuteDebugAction extends AnAction {
    private final Logger logger = Logger.getLogger(OpenSettingsAction.class.getName());

    protected ContainerInfoProvider containerInfoProvider = new ContainerInfoProvider();

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        if (event.getProject() == null) {
            return;
        }
        DockerDebugState state = DockerDebugService.getInstance(event.getProject()).getState();
        try {
            LaunchDebugWhenContainerStarts.create(event.getProject(), state).run();
        } catch (OperationFailedException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            JOptionPane.showMessageDialog(event.getProject().getActualComponentManager().getService(Window.class),
                    e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
