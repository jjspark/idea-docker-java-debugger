package ca.justinpark.build.dockerjavadebugger.providers;

import ca.justinpark.build.dockerjavadebugger.OperationFailedException;
import ca.justinpark.build.dockerjavadebugger.actions.OpenSettingsAction;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerPort;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import org.apache.commons.lang3.SystemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ContainerInfoProvider {
    private final Logger logger = Logger.getLogger(OpenSettingsAction.class.getName());

    DockerClient getDockerClient() {
        String localDockerHost = SystemUtils.IS_OS_WINDOWS ? "tcp://localhost:2375" : "unix:///var/run/docker.sock";
        return DockerClientBuilder.getInstance(
                        DefaultDockerClientConfig.createDefaultConfigBuilder()
                                .withDockerHost(localDockerHost).build())
                .build();
    }

    public List<String> fetchContainerNames() {
        List<String> result = new ArrayList<>();
        try {
            DockerClient dockerClient = getDockerClient();
            List<Container> containers = dockerClient.listContainersCmd().exec();

            for (Container container : containers) {
                String name = container.getNames()[0];
                if (name.startsWith("/")) {
                    name = name.substring(1);
                }
                result.add(name);
            }
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Failed to list of active containers", e);
        }
        return result;
    }

    private boolean doesContainerNameMatch(Container container, String key) {
        String itemContainerName = container.getNames()[0];
        if (itemContainerName.startsWith("/")) {
            itemContainerName = container.getNames()[0].substring(1);
        }
        return (itemContainerName.equals(key));
    }

    public Integer fetchExternalPort(String containerName, Integer internalPort) throws OperationFailedException {
        DockerClient dockerClient = getDockerClient();
        List<Container> containers;
        try {
            containers = dockerClient.listContainersCmd().exec().stream()
                    .filter(item -> doesContainerNameMatch(item, containerName)).collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new OperationFailedException("Could connect to local docker service", e);
        }
        if (containers.isEmpty()) {
            throw new OperationFailedException(String.format("Could find container named %s", containerName));
        }
        Optional<Integer> externalPort = Optional.empty();
        for (ContainerPort item : containers.get(0).getPorts()) {
            if (item.getPrivatePort() != null && item.getPrivatePort().equals(internalPort)) {
                Integer publicPort = item.getPublicPort();
                if (publicPort != null) {
                    externalPort = Optional.of(publicPort);
                }
                break;
            }
        }
        if (externalPort.isEmpty()) {
            throw new OperationFailedException(
                    String.format("Port %d is not exposed on container %s", internalPort, containerName));
        }
        return externalPort.get();
    }
}
