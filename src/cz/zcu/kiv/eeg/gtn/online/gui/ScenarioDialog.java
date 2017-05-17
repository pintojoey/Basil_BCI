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
 * Trida pro vytvoreni dialogoveho okna,
 * ktere slouzi pro tvorbu scenaru
 */
public class ScenarioDialog extends JDialog {
    MainFrame mf;
    StimuliTableModel stm;
    JFrame scenarioFrame;
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
    private JPanel scenarioPanel;
    private JPanel boxPanel;
    String scenarioName;
    TextField fileTF;

    /**
     * Konstruktor
     * @param frame
     */

    public ScenarioDialog(MainFrame frame) {
        super(frame);
        this.mf=frame;
    }

    /**
     * Vytvoreni okna
     * @param frame JFrame
     */
    public void createDialog(JFrame frame){
        this.scenarioFrame = frame;
        this.setModal(true);
        this.setTitle("New scenario");
        this.getContentPane().add(createScenarioPanel());
        this.setMinimumSize(new Dimension(420,280));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /**
     * Vytvori panel s komponentami
     * @return panel s pozadovanym poctem stimul panelu a ovladacim panelem
     */
    private JPanel createScenarioPanel(){

        scenarioPanel = new JPanel();
        scenarioPanel.setLayout(new BorderLayout());

        boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel,BoxLayout.PAGE_AXIS));
        boxPanel.add(scenarioMain(),BorderLayout.CENTER);
        boxPanel.add(scenarioMain(),BorderLayout.CENTER);
        sP = new JScrollPane(boxPanel);
        scenarioPanel.add(sP,BorderLayout.CENTER);
        scenarioPanel.add(scenarioOption(),BorderLayout.PAGE_END);

       return scenarioPanel;


    }

    /**
     * JPanel, ktery slouzi jako jeden samotny stimul
     * @return panel s komponentami pro parametry stimulu a jeho manipulaci
     */
    private JPanel scenarioMain(){
        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints g = new GridBagConstraints();
        final JPanel itemJP = new JPanel(gb);

        String p = ""+ItemJP.size();

        final JLabel idLabel = new JLabel(p);

        final TextField nameTF = new TextField(10);
        final TextField descTF = new TextField(10);
        final TextField file1TF = new TextField(15);
        final TextField file2TF = new TextField(15);

        final JButton chooseFile2BT = new JButton("Choose a file");
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
                countID = ItemJP.size();
                countID--;
                System.out.println(countID);
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
                        ItemJP.remove(Integer.parseInt(idLabel.getText()));
                        NamesTF.remove(Integer.parseInt(idLabel.getText()));
                        Files1TF.remove(Integer.parseInt(idLabel.getText()));
                        Files2TF.remove(Integer.parseInt(idLabel.getText()));
                        ChooseFile1BT.remove(Integer.parseInt(idLabel.getText()));
                        ChooseFile2BT.remove(Integer.parseInt(idLabel.getText()));
                        RemoveBT.remove(Integer.parseInt(idLabel.getText()));
                        DescTF.remove(Integer.parseInt(idLabel.getText()));
                        ScenarioDialog.this.setSize(ScenarioDialog.this.getWidth(), ScenarioDialog.this.getHeight() - 100);
                    }
                }
            }
        });

        g.insets = new Insets(0,10,0,10);
        g.fill = GridBagConstraints.HORIZONTAL;
        itemJP.add(new JLabel("ID"),g);
        g.gridx = 1;
        itemJP.add(new JLabel("Name*"),g);
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

        scenarioPanel.revalidate();
        ItemJP.add(itemJP);
        return itemJP;
    }

    /**
     * Ovladaci panel scenare
     * @return JPanel s ovladanim
     */
    private JPanel scenarioOption(){
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
                boxPanel.add(scenarioMain());
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
                if (requiredField() == true) {
                scenarioName = fileTF.getText().toString();
                requiredField();
                importExportScenarios.setConfigName(scenarioName);
                stimuls= importExportScenarios.createDirectory(NamesTF,Files1TF,Files2TF,DescTF);
                    if (importExportScenarios.isCreated){
                        mf.setStimuls(stimuls);
                        boolean created = true;
                        mf.setExistScenario(created);
                        ScenarioDialog.this.dispose();
                    }
                }
                else {
                    showMessageDialog(ScenarioDialog.this,
                            "You have to name all the stimuli", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }


            }
        });

        importBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countID = ItemJP.size();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("XML Files","xml");
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(filter);
                fc.setCurrentDirectory(new File(System
                        .getProperty("user.dir")));
                int f = fc.showOpenDialog(ScenarioDialog.this);
                if (f == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    stimuls= importExportScenarios.load(file);
                    if (countID<(stimuls.size())) {
                        for (; countID < stimuls.size(); countID++) {
                            boxPanel.add(scenarioMain());
                            if (ScenarioDialog.this.getHeight() <= 500)
                                ScenarioDialog.this.setSize(ScenarioDialog.this.getWidth(), ScenarioDialog.this.getHeight() + 100);
                            repaint();
                        }
                        countID = stimuls.size();
                    }
                    else if (stimuls.size()< countID) {

                        for (; stimuls.size() < countID; countID--) {
                            ItemJP.get(countID-1).removeAll();
                            ItemJP.remove(countID-1);
                            NamesTF.remove(countID - 1);
                            Files1TF.remove(countID - 1);
                            Files2TF.remove(countID - 1);
                            ChooseFile1BT.remove(countID - 1);
                            ChooseFile2BT.remove(countID - 1);
                            RemoveBT.remove(countID - 1);
                            DescTF.remove(countID - 1);
                            ScenarioDialog.this.setSize(ScenarioDialog.this.getWidth(), ScenarioDialog.this.getHeight() - 100);
                            repaint();
                        }
                        countID = stimuls.size();

                    }

                   for (int i = 0; i < NamesTF.size(); i++) {
                        NamesTF.get(i).setText(stimuls.get(i).name);
                        Files1TF.get(i).setText(stimuls.get(i).url1);
                        Files2TF.get(i).setText(stimuls.get(i).url2);
                        DescTF.get(i).setText(stimuls.get(i).description);
                        fileTF.setText(file.getName().split("\\.xml")[0]);
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

    /**
     *
     * @return
     */
    boolean requiredField(){
        for (int i = 0; i < NamesTF.size(); i++) {
            if (NamesTF.get(i).getText().equals("")){
                    return false;
            }
        }
        return true;
    }


}
