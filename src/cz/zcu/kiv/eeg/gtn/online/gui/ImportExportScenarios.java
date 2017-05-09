package cz.zcu.kiv.eeg.gtn.online.gui;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
public class ImportExportScenarios {
    private static String configName;
    private static ArrayList<Stimul> stimuls;



    //Osetrit, kdy to vrati stimuls a kdy to vrati null
    public static ArrayList<Stimul> save(ArrayList<TextField> names, ArrayList<TextField>files1, ArrayList<TextField>files2)
    {
        //configName = "new";
        stimuls = new ArrayList<>();

        File file = new File("configs/"+ configName);
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

                Stimul stimul = new Stimul(i+1,name,url1,url2);
                stimuls.add(i,stimul);

                File file1= new File(url1);
                File file2= new File(url2);
                try {
                    copyFile(file1,new File("configs/"+ configName +"/"+file1.getName()));
                    copyFile(file2,new File("configs/"+ configName +"/"+file2.getName()));
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
            File xmlFile = new File("configs/"+ configName +"/"+ configName +".xml");
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
        return stimuls;
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

    public ArrayList<Stimul> load(File file)
    {    ArrayList<Stimul>stimuls = new ArrayList<>();

        String name = null;
        String file1 = null;
        String file2 = null;
        try {
            File inputFile = new File(String.valueOf(file));
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("STIMUL");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                org.w3c.dom.Node nNode = nList.item(temp);

                if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String id = eElement.getAttribute("id");

                    name = eElement.getElementsByTagName("name").item(0).getTextContent();
                    file1 = eElement.getElementsByTagName("file1").item(0).getTextContent();
                    file2 = eElement.getElementsByTagName("file2").item(0).getTextContent();

                    Stimul stimul = new Stimul(temp+1,name,file1,file2);
                    stimuls.add(temp,stimul);
                }
            }
        } catch (Exception e) {
            System.out.println("Chyba při načítání souboru!");
            e.printStackTrace();
        }

        return stimuls;
    }


    public static void setConfigName(String configName) {
        ImportExportScenarios.configName = configName;
    }
}
