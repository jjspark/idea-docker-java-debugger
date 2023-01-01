package ca.justinpark.build.dockerjavadebugger.providers;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class LaunchConfigurationNameProviderTest {
    private LaunchConfigurationNameProvider provider;
    private Project project;

    @BeforeEach
    void init() {
        this.project = Mockito.mock(Project.class);
        this.provider = Mockito.spy(new LaunchConfigurationNameProvider(project));
    }

    @Test
    void fetchLaunchConfigurationNames() throws IOException {
        VirtualFile vf = Mockito.mock(VirtualFile.class);
        when(vf.getInputStream()).thenReturn(new ByteArrayInputStream(
                ("<project version=\"4\"><component name=\"RunManager\">" +
                        "<configuration name=\"debug\" type=\"Remote\"></configuration></component></project>")
                        .getBytes()));
        when(this.project.getWorkspaceFile()).thenReturn(vf);
        assertEquals("debug", this.provider.fetchLaunchConfigurationNames().get(0));
    }

    @Test
    void fetchLaunchConfigurationNamesNoWorkspaceFile() throws IOException {
        when(this.project.getWorkspaceFile()).thenReturn(null);
        assertEquals(0, this.provider.fetchLaunchConfigurationNames().size());
    }

    @Test
    void fetchLaunchConfigurationNamesNoConfigurationsDefined() throws IOException {
        VirtualFile vf = Mockito.mock(VirtualFile.class);
        when(vf.getInputStream()).thenReturn(new ByteArrayInputStream(
                "<project version=\"4\"><component name=\"RunManager\"></component></project>"
                        .getBytes()));
        when(this.project.getWorkspaceFile()).thenReturn(vf);
        assertEquals(0, this.provider.fetchLaunchConfigurationNames().size());
    }

    @Test
    void fetchLaunchConfigurationNamesNoRemoteJvmConfigurationsDefined() throws IOException {
        VirtualFile vf = Mockito.mock(VirtualFile.class);
        when(vf.getInputStream()).thenReturn(new ByteArrayInputStream(
                ("<project version=\"4\"><component name=\"RunManager\">" +
                        "<configuration name=\"run\" type=\"GradleRunConfiguration\"></configuration></component></project>")
                        .getBytes()));
        when(this.project.getWorkspaceFile()).thenReturn(vf);
        assertEquals(0, this.provider.fetchLaunchConfigurationNames().size());
    }

    @Test
    void fetchLaunchConfigurationNamesMalformedWorkspaceXml() throws IOException {
        VirtualFile vf = Mockito.mock(VirtualFile.class);
        when(vf.getInputStream()).thenReturn(new ByteArrayInputStream(
                ("<project>").getBytes()));
        when(this.project.getWorkspaceFile()).thenReturn(vf);
        assertEquals(0, this.provider.fetchLaunchConfigurationNames().size());
    }
}
