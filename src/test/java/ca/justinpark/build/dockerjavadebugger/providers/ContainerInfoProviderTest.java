package ca.justinpark.build.dockerjavadebugger.providers;

import ca.justinpark.build.dockerjavadebugger.OperationFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.model.Container;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class ContainerInfoProviderTest {

    private ContainerInfoProvider provider;
    private DockerClient dockerClient;
    private ListContainersCmd listContainersCmd;

    @BeforeEach
    void init() {
        this.provider = Mockito.spy(new ContainerInfoProvider());
        this.dockerClient = Mockito.spy(provider.getDockerClient());
        when(provider.getDockerClient()).thenReturn(dockerClient);
        this.listContainersCmd = Mockito.spy(dockerClient.listContainersCmd());
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
    }

    @Test
    void fetchContainerNames() throws JsonProcessingException {
        Container container = new ObjectMapper().readValue("{ \"Names\" : [ \"/primitive_slinger\" ] }",
                Container.class);
        when(listContainersCmd.exec()).thenReturn(List.of(container));

        assertTrue(provider.fetchContainerNames().contains("primitive_slinger"));
    }

    @Test
    void fetchContainerNamesThrowsRuntimeException() throws JsonProcessingException {
        when(listContainersCmd.exec()).thenThrow(new RuntimeException());

        assertEquals(0, provider.fetchContainerNames().size());
    }

    @Test
    void fetchExternalPort() throws JsonProcessingException, OperationFailedException {
        Container container = new ObjectMapper().readValue(
                "{ \"Names\" : [ \"/primitive_slinger\" ], " +
                        "\"Ports\":[{ \"PrivatePort\": \"5005\", \"PublicPort\": \"49201\"}] }",
                Container.class);
        when(listContainersCmd.exec()).thenReturn(List.of(container));

        assertEquals(49201, provider.fetchExternalPort("primitive_slinger", 5005));
    }

    @Test
    void fetchExternalPortNoMatchingName() throws JsonProcessingException {
        Container container = new ObjectMapper().readValue(
                "{ \"Names\" : [ \"/primitive_slinger\" ], " +
                        "\"Ports\":[{ \"PrivatePort\": \"5005\", \"PublicPort\": \"49201\"}] }",
                Container.class);
        when(listContainersCmd.exec()).thenReturn(List.of(container));
        Exception exception = assertThrows(OperationFailedException.class, () -> {
            provider.fetchExternalPort("assertive_king", 5005);
        });
        assertTrue(exception.getMessage().contains(String.format("Could find container named %s", "assertive_king")));
    }

    @Test
    void fetchExternalPortNoMatchingInternalPort() throws JsonProcessingException {
        Container container = new ObjectMapper().readValue(
                "{ \"Names\" : [ \"/primitive_slinger\" ], " +
                        "\"Ports\":[{ \"PrivatePort\": \"5001\", \"PublicPort\": \"49201\"}] }",
                Container.class);
        when(listContainersCmd.exec()).thenReturn(List.of(container));
        Exception exception = assertThrows(OperationFailedException.class, () -> {
            provider.fetchExternalPort("primitive_slinger", 5005);
        });
        assertTrue(exception.getMessage().contains(
                String.format("Port %d is not exposed on container %s", 5005, "primitive_slinger")));
    }
}
