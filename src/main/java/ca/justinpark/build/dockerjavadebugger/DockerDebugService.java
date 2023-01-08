package ca.justinpark.build.dockerjavadebugger;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.components.StoragePathMacros.WORKSPACE_FILE;

@State(name = "DockerDebug", storages = @Storage(value = WORKSPACE_FILE, roamingType = RoamingType.DISABLED))
public class DockerDebugService implements PersistentStateComponent<DockerDebugState> {
    private DockerDebugState state = new DockerDebugState();

    public static DockerDebugService getInstance(Project project) {
        return project.getService(DockerDebugService.class);
    }

    @NotNull
    public DockerDebugState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull DockerDebugState state) {
        this.state = state;
    }
}
