# Java Debugging in Container
This Java project demonstrates java container debugging using the docker-java-debugger plugin.

*Note* Docker and docker-compose must be installed prior to running this demo project

## How to run the demo project
1. Run `./gradlew buildImage` to build an image called _java-demo:latest_
2. In IntelliJ IDEA, create a new Run Configuration of type _Remote JVM Debug_
3. Put a breakpoint in the Main method of this project
4. Run `docker-compose -f docker/docker-compose.yml -p demo up` to launch the container
5. Click on _Docker Debug Config_ button on the toolbar. This should bring up a dialog box where you can set the _Remote JVM Debug_ configuration that you created earlier, and the name of currently running container, which should be _demo-javaservice-1_
6. Click on _Debug_ button to start debugging. This should bring you to the breakpoint that you created earlier.
