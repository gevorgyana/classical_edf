import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.w3c.dom.Node.ELEMENT_NODE;

/**
 * A simple parser of xml file used to
 * retrieve Task and todo TimedTask objects from files
 * */

public class Config2TaskConverter {

    private TaskIDManager taskIDManager;

    public Config2TaskConverter(TaskIDManager taskIDManager) {
        this.taskIDManager = taskIDManager;
    }

    public ArrayList<Task> parseTasks(String fileName) {

        ArrayList<Task> tasksNative = new ArrayList<>();

        try {

            File inputFile = new File(fileName);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputFile);
            document.getDocumentElement().normalize();
            NodeList tasksXml = document.getElementsByTagName("task");

            for (int i = 0; i < tasksXml.getLength(); ++i) {

                Node currentTask = tasksXml.item(i);

                if (currentTask.getNodeType() == ELEMENT_NODE) {
                    Element element = (Element) currentTask;

                    tasksNative.add(

                            new Task(
                                    Integer.parseInt(element.getElementsByTagName
                                            ("absoluteDeadline").item(0).getTextContent().trim()),
                                    Integer.parseInt(element.getElementsByTagName
                                            ("worstCaseRuntime").item(0).getTextContent().trim()),
                                    taskIDManager
                            )
                    );
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return tasksNative;
    }
}