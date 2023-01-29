# Idea Docker Java Debugger Plugin

IntelliJ IDEA plugin that automates port configuration of Remote JVM Debug launch configuration so that you can debug a docker container without having to know the randomly what public port has been randomly selected by docker.

## Usage
1. Launch a docker container with java debugger enabled.
    * This can be done with an environment variable on the container. For example, `JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005`
2. Put a breakpoint on your java project
3. Create a new _Remote JVM Debug_ run configuration
4. Click on _Docker Debug Config_ button the toolbar. On the popup dialog box, configure the following: 
   * Name of the _Remote JVM Debug_ configuration that you created earlier.
   * Internal port of the debugger, usually 5005.
   * Name of the currently running container
5. Click on _Debug_ button the dialog box to start debugging, or click on _Save_ button to debug later with _Docker Debug_ button on the toolbar.


Please refer to the demo project in the demo subdirectory for a Java project that's ready for debugging.

## Configuration
* Remote JVM Debug
  * The debug configuration that will be launched when docker debugger starts.
* Internal Port
  * Port number within the container that the Java process will listen for debugger.
* Docker Container
  * Docker container to attach a debugger to.
  * (waitForContainerSeconds)
    * Number of seconds to wait if container isn't running or the port isn't ready.
    * This configuration is not visible on UI. It can be modified by adding an _\<option\>_ element to workspace.xml
      under _\<component name="DockerDebug"\>_
    * For example, the snippet below would set maximum wait time to 10 seconds.
    ```xml
    <component name="DockerDebug">
        <option name="waitForContainerSeconds" value="10" />
    </component>
    ```