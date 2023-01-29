package ca.justinpark.build.dockerjavadebugger.settings;

import ca.justinpark.build.dockerjavadebugger.DockerDebugState;
import ca.justinpark.build.dockerjavadebugger.IntegerFilter;
import ca.justinpark.build.dockerjavadebugger.providers.ContainerInfoProvider;
import ca.justinpark.build.dockerjavadebugger.providers.LaunchConfigurationNameProvider;
import com.intellij.openapi.ui.ComboBox;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.Optional;

public class DockerDebugStateComponent {
    private final JPanel mainPanel = new JPanel(new MigLayout());
    private final JComboBox<String> cbRemoteJvmDebugName = new ComboBox<>();
    private final JComboBox<String> cbContainer = new ComboBox<>();
    private final JTextField txInternalPort;

    public DockerDebugStateComponent(ContainerInfoProvider containerProvider,
                                     LaunchConfigurationNameProvider launchConfigurationNamesProvider) {
        txInternalPort = createInternalPortTextField();

        mainPanel.add(new JLabel("Remote JVM Debug"), "align label");
        mainPanel.add(cbRemoteJvmDebugName, "width 100:500:800, pushx, growx, wrap");
        mainPanel.add(new JLabel("Internal Port (eg. 5005)"), "align label");
        mainPanel.add(txInternalPort, "width 60:120:, wrap");
        mainPanel.add(new JLabel("Docker Container"), "align label");
        mainPanel.add(cbContainer, "width 100:500:800, pushx, growx, wrap");

        launchConfigurationNamesProvider.fetchLaunchConfigurationNames().forEach(cbRemoteJvmDebugName::addItem);
        if (cbRemoteJvmDebugName.getItemCount() == 0) {
            cbRemoteJvmDebugName.setToolTipText("You must create new Remote JVM Debug configuration before you can debug a container");
        } else {
            cbRemoteJvmDebugName.setToolTipText("Only Remote JVM Debug configurations are listed here");
        }

        containerProvider.fetchContainerNames().forEach(cbContainer::addItem);
        if (cbContainer.getItemCount() == 0) {
            cbContainer.setToolTipText("No containers are running or unable to connect to local docker service at tcp://localhost:2375(Windows) or unix:///var/run/docker.sock");
        } else {
            cbContainer.setToolTipText("Select the container to debug");
        }
    }

    private JTextField createInternalPortTextField() {
        JTextField textField = new JTextField();
        textField.setText("5005");

        int defaultHeight = (int) textField.getSize().getHeight();
        textField.setPreferredSize(new Dimension(100, defaultHeight));

        PlainDocument doc = (PlainDocument) textField.getDocument();
        doc.setDocumentFilter(new IntegerFilter());
        return textField;
    }

    public void populateFromState(DockerDebugState state) {
        if (state == null) {
            return;
        }
        cbRemoteJvmDebugName.setSelectedItem(state.remoteJvmDebug);
        cbContainer.setSelectedItem(state.container);
        if (cbContainer.getSelectedItem() == null) {
            cbContainer.addItem(state.container);
        }
        txInternalPort.setText(String.valueOf(state.internalPort));
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    @NotNull
    public Optional<String> getRemoteJvmDebugName() {
        if (cbRemoteJvmDebugName.getSelectedItem() != null) {
            return Optional.of(cbRemoteJvmDebugName.getSelectedItem().toString());
        }
        return Optional.empty();
    }

    @NotNull
    public Optional<String> getContainerName() {
        if (cbContainer.getSelectedItem() == null) {
            return Optional.empty();
        }
        return Optional.of(cbContainer.getSelectedItem().toString());
    }

    public Optional<Integer> getInternalPort() {
        try {
            return Optional.of(Integer.parseInt(txInternalPort.getText()));
        } catch (NumberFormatException e) {
            return Optional.of(5005);
        }
    }
}
