package cz.zcu.kiv.eeg.gtn.online.gui;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Marek on 4. 4. 2017.
 */
public class Xml {
    private static String konfigName;

    private ArrayList<TextField> names;

    public static void save(ArrayList<TextField> names, ArrayList<TextField>files1, ArrayList<TextField>files2)
    {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document xmlDoc = docBuilder.newDocument();

            Element rootElement = xmlDoc.createElement("class");
            // Prochází po párech klíče a ArrayListu dané kategorie
            for (int i = 0; i < names.size(); i++) {
                String name =names.get(i).getText();
                String file1 =files1.get(i).getText();
                String file2 =files2.get(i).getText();

                Element mainElement = xmlDoc.createElement("STIMUL");
                mainElement.setAttribute("id", String.valueOf(i));

                org.w3c.dom.Text elementName =  xmlDoc.createTextNode(name);
                Element itemName = xmlDoc.createElement("name");
                itemName.appendChild(elementName);
                mainElement.appendChild(itemName);

                org.w3c.dom.Text elementFile1 =  xmlDoc.createTextNode(file1);
                Element itemFile1 = xmlDoc.createElement("file1");
                itemFile1.appendChild(elementFile1);
                mainElement.appendChild(itemFile1);

                org.w3c.dom.Text elementFile2 =  xmlDoc.createTextNode(file2);
                Element itemFile2 = xmlDoc.createElement("file2");
                itemFile2.appendChild(elementFile2);
                mainElement.appendChild(itemFile2);

                rootElement.appendChild(mainElement);

            }
            xmlDoc.appendChild(rootElement);


            OutputFormat outFormat = new OutputFormat(xmlDoc);
            outFormat.setIndenting(true);
            konfigName="vystup";

            File file = new File("configs/"+konfigName);
            if (!file.exists()) {
                if (file.mkdir()) {
                    System.out.println("Directory is created!");

                    File xmlFile = new File("configs/"+konfigName+"/"+konfigName+".xml");
                    try {
                        FileOutputStream outStream = new FileOutputStream(xmlFile);
                        XMLSerializer serializer = new XMLSerializer(outStream,outFormat);
                        try {
                            serializer.serialize(xmlDoc);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                } else {
                    System.out.println("Failed to create directory!");
                }
            }else {
                System.out.println("The name of directory is existing!");
            }


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
}
