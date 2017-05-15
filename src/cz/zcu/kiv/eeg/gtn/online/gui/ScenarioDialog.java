package cz.zcu.kiv.eeg.gtn.online.gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import static javax.swing.JOptionPane.showMessageDialog;


/**
 * Created by Jamape on 16.03.2017.
 */
public class ScenarioDialog extends JDialog {
    MainFrame mf;
    StimuliTableModel stm;
    JFrame scriptFrame;
    ImportExportScenarios importExportScenarios = new ImportExportScenarios();
    private int id = 1;
    private int countID = 0;
    ArrayList<Stimul>stimuls = new ArrayList<>();
    ArrayList<TextField>NamesTF = new ArrayList<>();
    ArrayList<TextField>Files1TF = new ArrayList<>();
    ArrayList<TextField>Files2TF = new ArrayList<>();
    ArrayList<TextField>DescTF = new ArrayList<>();
    ArrayList<JButton> ChooseFile1BT = new ArrayList<>();
    ArrayList<JButton> ChooseFile2BT = new ArrayList<>();
    ArrayList<JButton> RemoveBT = new ArrayList<>();
    ArrayList<JPanel> ItemJP = new ArrayList<>();
    private JScrollPane sP;
    private JPanel scriptPanel;
    private JPanel boxPanel;
    String scenarioName;
    TextField fileTF;


    public ScenarioDialog(MainFrame frame) {
        super(frame);
        this.mf=frame;
    }
    public void createDialog(JFrame frame){
        this.scriptFrame = frame;
        this.setModal(true);
        this.setTitle("Script menu");
        this.getContentPane().add(createScriptPanel());
        this.setMinimumSize(new Dimension(420,280));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private JPanel createScriptPanel(){

        scriptPanel = new JPanel();
        scriptPanel.setLayout(new BorderLayout());

        boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel,BoxLayout.PAGE_AXIS));
        boxPanel.add(scriptMain(),BorderLayout.CENTER);
        boxPanel.add(scriptMain(),BorderLayout.CENTER);
        sP = new JScrollPane(boxPanel);
        scriptPanel.add(sP,BorderLayout.CENTER);
        scriptPanel.add(scriptOption(),BorderLayout.PAGE_END);

       return scriptPanel;


    }


    private JPanel scriptMain(){
        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints g = new GridBagConstraints();
        final JPanel itemJP = new JPanel(gb);

        String p = setID();

        final JLabel idLabel = new JLabel(p);

        final TextField nameTF = new TextField(10);
        final TextField descTF = new TextField(10);
        final TextField file1TF = new TextField(15);
        final TextField file2TF = new TextField(15);

        JButton chooseFile2BT = new JButton("Choose a file");
        final JButton chooseFile1BT = new JButton("Choose a file");

        JButton removeBT = new JButton("Remove");


        ChooseFile1BT.add(chooseFile1BT);
        ChooseFile2BT.add(chooseFile2BT);

        Files1TF.add(file1TF);
        Files2TF.add(file2TF);

        NamesTF.add(nameTF);

        RemoveBT.add(removeBT);

        DescTF.add(descTF);

        chooseFile1BT.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser fc = new JFileChooser();
                int f = fc.showOpenDialog(ScenarioDialog.this);
                if (f == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    file1TF.setText(file.getAbsolutePath());
                }
            }
        });

        chooseFile2BT.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser fc = new JFileChooser();
                int f = fc.showOpenDialog(ScenarioDialog.this);
                if (f == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    file2TF.setText(file.getAbsolutePath());
                }
            }
        });

        removeBT.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                countID--;
                int dRes = JOptionPane.showConfirmDialog(null,
                        "Do you want delete this stimul? ", nameTF.getText(),
                        JOptionPane.OK_CANCEL_OPTION);
                if (JOptionPane.OK_OPTION == dRes) {
                    if (countID == 1) {
                        showMessageDialog(ScenarioDialog.this,
                                "You can't delete last two stimuls", "Error",
                                JOptionPane.ERROR_MESSAGE);
                        countID++;
                    } else {

                        itemJP.removeAll();
                        NamesTF.remove(countID-1);
                        Files1TF.remove(countID-1);
                        Files2TF.remove(countID-1);
                        ChooseFile1BT.remove(countID-1);
                        ChooseFile2BT.remove(countID-1);
                        RemoveBT.remove(countID-1);
                        DescTF.remove(countID-1);
                        ScenarioDialog.this.setSize(ScenarioDialog.this.getWidth(), ScenarioDialog.this.getHeight() - 100);
                    }
                }
            }
        });

        g.insets = new Insets(0,10,0,10);
        g.fill = GridBagConstraints.HORIZONTAL;
        itemJP.add(new JLabel("ID"),g);
        g.gridx = 1;
        itemJP.add(new JLabel("Name"),g);
        g.gridx = 2;
        itemJP.add(new JLabel("File 1"),g);
        g.gridx = 0;
        g.gridy = 1;
        itemJP.add(idLabel,g);
        g.gridx = 1;
        itemJP.add(nameTF,g);
        g.gridy = 2;
        itemJP.add(new JLabel("Description"),g);
        g.gridy = 3;
        itemJP.add(descTF,g);
        g.gridy = 1;
        g.gridx = 2;
        itemJP.add(file1TF,g);
        g.gridx = 3;
        itemJP.add(chooseFile1BT,g);
        g.gridy = 2;
        g.gridx = 6;
        itemJP.add(removeBT,g);
        g.gridy = 2;
        g.gridx = 2;
        itemJP.add(new JLabel("File 2"),g);
        g.gridy = 3;
        itemJP.add(file2TF,g);
        g.gridx = 3;
        itemJP.add(chooseFile2BT,g);
        g.gridwidth = 4;
        g.gridy = 6;
        g.gridx = 0;
        itemJP.add(new JSeparator(JSeparator.HORIZONTAL),g);

        scriptPanel.revalidate();
        ItemJP.add(itemJP);
        return itemJP;
    }

    private String setID(){
        String idText = Integer.toString(id);
        id++;
        countID++;
        return idText;
    }

    private JPanel scriptOption(){
        JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel,BoxLayout.LINE_AXIS));

        JButton addBT = new JButton("+");
        JButton importBT = new JButton("Import data");
        JLabel fileLB = new JLabel("Scenario name:");
        fileTF = new TextField(6);
        JButton applyBT = new JButton("Apply");
        JButton cancelBT = new JButton("Cancel");

        addBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boxPanel.add(scriptMain());
                if (ScenarioDialog.this.getHeight() <= 500)ScenarioDialog.this.setSize(ScenarioDialog.this.getWidth(),ScenarioDialog.this.getHeight()+100);
            }
        });
        cancelBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ScenarioDialog.this.dispose();
            }
        });

        applyBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scenarioName = fileTF.getText().toString();
                System.out.println(fileTF.getText());
                if(fileTF.getText()==""){
                    showMessageDialog(ScenarioDialog.this,
                            "Scenario does not have name!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                else{
                    importExportScenarios.setConfigName(scenarioName);
                    stimuls= importExportScenarios.save(NamesTF,Files1TF,Files2TF,DescTF);
                    mf.setStimuls(stimuls);
                    ScenarioDialog.this.dispose();
                }


            }
        });

        importBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileNameExtensionFilter filter = new FileNameExtensionFilter("XML Files","xml");
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(filter);
                fc.setCurrentDirectory(new File(System
                        .getProperty("user.dir")));
                int f = fc.showOpenDialog(ScenarioDialog.this);
                if (f == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    stimuls= importExportScenarios.load(file);
                    int value = countID - stimuls.size();
                    System.out.println(stimuls.size());
                    System.out.println(value);
                    if (value < 0){
                        countID = stimuls.size();
                        value = Math.abs(value);
                        for (int i = 0; i < value+1; i++) {
                            boxPanel.add(scriptMain());
                            System.out.println("JSEM TU");
                            if (ScenarioDialog.this.getHeight() <= 500)ScenarioDialog.this.setSize(ScenarioDialog.this.getWidth(),ScenarioDialog.this.getHeight()+100);
                            repaint();
                        }
                    }else if (value > 0){
                        countID = stimuls.size();
                        int v = value;
                        for (int i = 0; i < value+1; i++){
                            ItemJP.get(v).removeAll();
                            ItemJP.remove(v);
                            ScenarioDialog.this.setSize(ScenarioDialog.this.getWidth(), ScenarioDialog.this.getHeight() - 100);
                            v--;

                        }
                    for (int i = 0; i < NamesTF.size(); i++) {
                        NamesTF.get(i).setText(stimuls.get(i).name);
                        Files1TF.get(i).setText(stimuls.get(i).file1);
                        Files2TF.get(i).setText(stimuls.get(i).file2);
                    }
                }




                }

            }
        });

        optionPanel.add(Box.createRigidArea(new Dimension(10,0)));
        optionPanel.add(addBT);
        optionPanel.add(Box.createRigidArea(new Dimension(10,0)));
        optionPanel.add(importBT);
        optionPanel.add(Box.createRigidArea(new Dimension(10,0)));
        optionPanel.add(fileLB);
        optionPanel.add(fileTF);
        optionPanel.add(Box.createRigidArea(new Dimension(10,0)));
        optionPanel.add(applyBT);
        optionPanel.add(Box.createRigidArea(new Dimension(5,0)));
        optionPanel.add(cancelBT);

        return optionPanel;
    }


}
