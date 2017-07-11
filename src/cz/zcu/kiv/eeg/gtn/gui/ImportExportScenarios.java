package cz.zcu.kiv.eeg.gtn.gui;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Saving and loading settings files
 */
public class ImportExportScenarios {
    private static String configName;
    private static ArrayList<Stimul> stimuls;
    public static boolean isCreated = false;

    /**
     * Creating a new directory
     * @param names - name of stimulus
     * @param files1 - url to file 1
     * @param files2 - url to file 2
     * @param desc - description of stimulus
     * @return
     */
    public static ArrayList<Stimul> createDirectory(ArrayList<TextField> names, ArrayList<TextField>files1, ArrayList<TextField>files2, ArrayList<TextField>desc)
    {
        stimuls = new ArrayList<>();
        if (configName.equals("")){
            configName="Unnamed";
        }
        File root = new File("configs");
        if (!root.exists() && !root.isDirectory()) {
            root.mkdir();
        }
        File file = new File("configs/"+ configName);
        if (!file.exists()) {
            if (file.mkdir()) {
                isCreated = true;
                save(names,files1,files2,desc);
            } else {
                JOptionPane.showMessageDialog(null,
                        "Failed to create directory!", "Error",
                        JOptionPane.ERROR_MESSAGE);
                isCreated = false;
            }
        }else {
                int dRes = JOptionPane.showConfirmDialog(null,
                        "Do you want to replace it? ", "The file already exists",
                        JOptionPane.OK_CANCEL_OPTION);
                if (JOptionPane.OK_OPTION == dRes) {
                    isCreated = true;
                    save(names,files1,files2,desc);
                }else isCreated = false;

        }
        return stimuls;
    }

    /**
     * Saving to a XML file
     * @param names - name of stimulus
     * @param files1 - url to file 1
     * @param files2 - url to file 2
     * @param desc - description of stimulus
     * @return
     */
   public static void save(ArrayList<TextField> names, ArrayList<TextField> files1, ArrayList<TextField> files2, ArrayList<TextField> desc){
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
                String descrip = desc.get(i).getText();

                Stimul stimul = new Stimul(i+1,name,url1,url2,descrip);
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
                Element itemFile1 = xmlDoc.createElement("url1");
                itemFile1.appendChild(elementFile1);
                mainElement.appendChild(itemFile1);

                org.w3c.dom.Text elementFile2 =  xmlDoc.createTextNode(url2);
                Element itemFile2 = xmlDoc.createElement("url2");
                itemFile2.appendChild(elementFile2);
                mainElement.appendChild(itemFile2);

                org.w3c.dom.Text elementDescription =  xmlDoc.createTextNode(descrip);
                Element itemDescription = xmlDoc.createElement("description");
                itemDescription.appendChild(elementDescription);
                mainElement.appendChild(itemDescription);

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
    }
    /**
     * Copy the files to the directory
     * @param sourceFile - original file
     * @param destFile - copy file
     * @throws IOException
     */
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
            destFile.renameTo(sourceFile);
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }

    }

    /**
     * Loading settings from file
     * @param file - xml input file
     * @return - stimuli retrieved from the file
     */
    public ArrayList<Stimul> load(File file)
    {    ArrayList<Stimul>stimuls = new ArrayList<>();

        String name = null;
        String file1 = null;
        String file2 = null;
        String desc = null;
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

                    name = eElement.getElementsByTagName("name").item(0).getTextContent();
                    file1 = eElement.getElementsByTagName("url1").item(0).getTextContent();
                    file2 = eElement.getElementsByTagName("url2").item(0).getTextContent();
                    desc = eElement.getElementsByTagName("description").item(0).getTextContent();

                    Stimul stimul = new Stimul(temp+1,name,file1,file2,desc);
                    stimuls.add(temp,stimul);
                }
            }
        } catch (Exception e) {
            System.out.println("Chyba při načítání souboru!");
            e.printStackTrace();
        }

        return stimuls;
    }

    /**
     * Set the file name and directory
     * @param configName - user specified name
     */
    public static void setConfigName(String configName) {
        ImportExportScenarios.configName = configName;
    }
}
