package ca.justinpark.build.dockerjavadebugger.settings;

import ca.justinpark.build.dockerjavadebugger.DockerDebugState;
import ca.justinpark.build.dockerjavadebugger.providers.ContainerInfoProvider;
import ca.justinpark.build.dockerjavadebugger.providers.LaunchConfigurationNameProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DockerDebugStateDialog extends DialogWrapper {
    private final DockerDebugStateComponent mainComponent;
    private final DockerDebugState state;
    private DialogCloseActionType closeActionType = DialogCloseActionType.NONE;
    private final Action saveAction = new SaveAction();
    private final Action debugAction = new DebugAction();

    public DockerDebugStateDialog(DockerDebugState state, Project project) {
        super(true);
        mainComponent = new DockerDebugStateComponent(new ContainerInfoProvider(), new LaunchConfigurationNameProvider(project));
        this.state = state;
        setTitle("Docker Debug Settings");
        init();

    }

    @Override
    protected JComponent createCenterPanel() {
        mainComponent.populateFromState(state);
        return mainComponent.getPanel();
    }

    public DockerDebugStateComponent getAppSettings() {
        return mainComponent;
    }

    private Action getSaveAction() {
        return saveAction;
    }

    private Action getDebugAction() {
        return debugAction;
    }

    private void setDialogCloseActionType(DialogCloseActionType actionType) {
        this.closeActionType = actionType;
    }

    public DialogCloseActionType getDialogCloseActionType() {
        return this.closeActionType;
    }

    @Override
    protected Action @NotNull [] createActions() {
        super.createDefaultActions();
        return new Action[]{getDebugAction(), getSaveAction(), getCancelAction()};
    }

    boolean isOkEnabled() {
        // return true if dialog can be closed
        return true;
    }

    public enum DialogCloseActionType {
        NONE,
        DEBUG,
        SAVE
    }

    private class SaveAction extends DialogWrapperAction {

        private static final long serialVersionUID = -8933218291466893901L;

        protected SaveAction() {
            super("Save");
            putValue(Action.NAME, "Save");
            putValue(DEFAULT_ACTION, Boolean.TRUE);
        }

        @Override
        protected void doAction(ActionEvent e) {
            if (doValidate() == null) {
                getOKAction().setEnabled(isOkEnabled());
            }

            setDialogCloseActionType(DialogCloseActionType.SAVE);
            doOKAction();
        }
    }

    private class DebugAction extends DialogWrapperAction {
        private static final long serialVersionUID = -6091871625401912573L;

        protected DebugAction() {
            super("Debug");
            putValue(Action.NAME, "Debug");
            putValue(DEFAULT_ACTION, Boolean.TRUE);
        }

        @Override
        protected void doAction(ActionEvent e) {
            if (doValidate() == null) {
                getOKAction().setEnabled(isOkEnabled());
            }
            setDialogCloseActionType(DialogCloseActionType.DEBUG);
            doOKAction();
        }
    }
}
