package cz.zcu.kiv.eeg.gtn.online.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;


/**
 * Created by Jamape on 16.03.2017.
 */
public class ScriptDialog extends JDialog {
    JFrame scriptFrame;
    int id = 1;
    ArrayList<TextField>NamesTF = new ArrayList<>();
    ArrayList<TextField>Files1TF = new ArrayList<>();
    ArrayList<TextField>Files2TF = new ArrayList<>();
    ArrayList<JButton> ChooseFile1BT = new ArrayList<>();
    ArrayList<JButton> ChooseFile2BT = new ArrayList<>();
    ArrayList<JButton> RemoveBT = new ArrayList<>();
    JPanel scriptPanel;
    JPanel boxPanel;


    public ScriptDialog(JFrame frame) {
        super(frame);
        this.scriptFrame = frame;
        this.setModal(true);
        this.setTitle("Script menu");
        this.getContentPane().add(createScriptPanel());
        //this.setResizable(false);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private JPanel createScriptPanel(){

        scriptPanel = new JPanel();
        scriptPanel.setLayout(new BorderLayout());
        boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel,BoxLayout.PAGE_AXIS));
        boxPanel.add(scriptMain());
        scriptPanel.add(boxPanel,BorderLayout.CENTER);
        scriptPanel.add(scriptOption(),BorderLayout.PAGE_END);
        return scriptPanel;
    }


    private JPanel scriptMain(){
        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints g = new GridBagConstraints();
        JPanel itemJP = new JPanel(gb);

        String p = setID();

        JLabel idLabel = new JLabel(p);

        TextField nameTF = new TextField(10);

        final TextField file1TF = new TextField(15);
        final TextField file2TF = new TextField(15);

        JButton chooseFile2BT = new JButton("Choose a file");
        JButton chooseFile1BT = new JButton("Choose a file");

        JButton removeBT = new JButton("Remove");


        ChooseFile1BT.add(chooseFile1BT);
        ChooseFile2BT.add(chooseFile2BT);

        Files1TF.add(file1TF);
        Files2TF.add(file2TF);

        NamesTF.add(nameTF);

        RemoveBT.add(removeBT);


        chooseFile1BT.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser fc = new JFileChooser();
                int f = fc.showOpenDialog(ScriptDialog.this);
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
                int f = fc.showOpenDialog(ScriptDialog.this);
                if (f == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    file2TF.setText(file.getAbsolutePath());
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
        g.gridx = 2;
        itemJP.add(file1TF,g);
        g.gridx = 3;
        itemJP.add(chooseFile1BT,g);
        g.gridy = 5;
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

        return itemJP;
    }

    private String setID(){
        String idText = Integer.toString(id);
        id++;
        return idText;
    }

    private JPanel scriptOption(){
        JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel,BoxLayout.LINE_AXIS));

        JButton addBT = new JButton("+");
        JButton applyBT = new JButton("Apply");
        JButton cancelBT = new JButton("Cancel");

        addBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boxPanel.add(scriptMain());
                //scriptFrame.setSize(scriptFrame.getWidth(),scriptFrame.getHeight()+100);
            }
        });

        optionPanel.add(Box.createRigidArea(new Dimension(10,0)));
        optionPanel.add(addBT);

        optionPanel.add(Box.createRigidArea(new Dimension(200,0)));
        optionPanel.add(applyBT);
        optionPanel.add(cancelBT);

        return optionPanel;
    }
}
