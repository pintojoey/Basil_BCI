package cz.zcu.kiv.eeg.gtn.online.gui;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Created by Marek on 4. 4. 2017.
 */
public class Xml {
    private static String konfigName;

    private ArrayList<TextField> names;

    public static void save(ArrayList<TextField> names, ArrayList<TextField>files1, ArrayList<TextField>files2)
    {
        konfigName="vystup";

        File file = new File("configs/"+konfigName);
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("Directory is created!");


            } else {
                System.out.println("Failed to create directory!");
            }
        }else {
            System.out.println("The name of directory is existing!");
        }

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document xmlDoc = docBuilder.newDocument();

            Element rootElement = xmlDoc.createElement("class");
            // Prochází po párech klíče a ArrayListu dané kategorie
            for (int i = 0; i < names.size(); i++) {
                String name =names.get(i).getText();
                String url1 =files1.get(i).getText();
                String url2 =files2.get(i).getText();

                File file1= new File(url1);
                File file2= new File(url2);
                try {
                    copyFile(file1,new File("configs/"+konfigName+"/"+file1.getName()));
                    copyFile(file2,new File("configs/"+konfigName+"/"+file2.getName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Element mainElement = xmlDoc.createElement("STIMUL");
                mainElement.setAttribute("id", String.valueOf(i));

                org.w3c.dom.Text elementName =  xmlDoc.createTextNode(name);
                Element itemName = xmlDoc.createElement("name");
                itemName.appendChild(elementName);
                mainElement.appendChild(itemName);

                org.w3c.dom.Text elementFile1 =  xmlDoc.createTextNode(url1);
                Element itemFile1 = xmlDoc.createElement("file1");
                itemFile1.appendChild(elementFile1);
                mainElement.appendChild(itemFile1);

                org.w3c.dom.Text elementFile2 =  xmlDoc.createTextNode(url2);
                Element itemFile2 = xmlDoc.createElement("file2");
                itemFile2.appendChild(elementFile2);
                mainElement.appendChild(itemFile2);

                rootElement.appendChild(mainElement);

            }
            xmlDoc.appendChild(rootElement);


            OutputFormat outFormat = new OutputFormat(xmlDoc);
            outFormat.setIndenting(true);
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

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
    private static void copyFile(File sourceFile, File destFile)throws IOException {
        if (!sourceFile.exists()) {
            return;
        }
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }

    }
}
