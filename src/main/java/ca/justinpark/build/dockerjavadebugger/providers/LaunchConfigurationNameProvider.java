package ca.justinpark.build.dockerjavadebugger.providers;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LaunchConfigurationNameProvider {
    private final static String JVM_REMOTE_NAME_XPATH = "/project/component[@name='RunManager']/configuration[@type='Remote']/@name";

    private final Logger logger = Logger.getLogger(LaunchConfigurationNameProvider.class.getName());
    private final Project project;

    public LaunchConfigurationNameProvider(Project project) {
        this.project = project;
    }

    public @NotNull List<String> fetchLaunchConfigurationNames() {
        List<String> result = new ArrayList<>();
        if (project.getWorkspaceFile() == null) {
            return result;
        }
        try {
            InputStream workspaceFileInputStream = project.getWorkspaceFile().getInputStream();

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(workspaceFileInputStream);
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xPath.compile(JVM_REMOTE_NAME_XPATH).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                result.add(nodeList.item(i).getNodeValue());
            }
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            logger.log(Level.WARNING, "Could not get the name of JVM Remote launch configuration", e);
        }
        return result;
    }
}
