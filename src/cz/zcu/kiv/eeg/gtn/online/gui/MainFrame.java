package cz.zcu.kiv.eeg.gtn.online.gui;

import cz.zcu.kiv.eeg.gtn.Const;
import cz.zcu.kiv.eeg.gtn.algorithm.math.IArtifactDetection;
import cz.zcu.kiv.eeg.gtn.algorithm.math.IFilter;
import cz.zcu.kiv.eeg.gtn.application.classification.*;
import cz.zcu.kiv.eeg.gtn.application.classification.test.TestClassificationAccuracy;
import cz.zcu.kiv.eeg.gtn.application.classification.test.TrainUsingOfflineProvider;
import cz.zcu.kiv.eeg.gtn.application.featureextraction.*;
import cz.zcu.kiv.eeg.gtn.online.app.IDataProvider;
import cz.zcu.kiv.eeg.gtn.online.app.OffLineDataProvider;
import cz.zcu.kiv.eeg.gtn.online.app.OnLineDataProvider;
import cz.zcu.kiv.eeg.gtn.online.app.OnlineDetection;
import cz.zcu.kiv.eeg.gtn.utils.ColorUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements Observer {

    private AbstractTableModel data;

    private ArrayList<Stimul> stimuls = new ArrayList<>();

    private ScenarioDialog scenarioDialog;

    private JTextPane winnerJTA;

    private JPanel winnerJP;

    private JFileChooser chooser;

    private Observer detection;

    private IDataProvider dp;

    public File eegFile;

    private Thread dataProvider;

    private final Logger log;

    private IFeatureExtraction fe;

    private IERPClassifier classifier;

    private final ShowChart epochCharts;

    private boolean trained = false;

    private JPanel statusBar;

    private JLabel feStatus;

    private JLabel classifierStatus;

    private JLabel artDetectionStatus;

    private JLabel filterStatus;

    public static IArtifactDetection artifactDetection = null;

    public static IFilter dataFilter = null;

    private String configurationFile;

    private final SetupDialogContent content = new SetupDialogContent();
    private boolean existScenario = false;

    public MainFrame() {
        super(Const.APP_NAME);

        BasicConfigurator.configure();
        log = Logger.getLogger(MainFrame.class);
        epochCharts = new ShowChart(this);

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.setLayout(new BorderLayout());
        getContentPane().add(createContentJP(), BorderLayout.CENTER);

        this.setJMenuBar(createMenu());

        this.setVisible(true);
        this.pack();

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height
                / 2 - this.getSize().height / 2);
        this.setSize(Const.MAIN_WINDOW_WIDTH, Const.MAIN_WINDOW_HEIGHT);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        loadConfiguration();

        getContentPane().add(createStatusBar(), BorderLayout.SOUTH);
        revalidate();
    }

    /**
     * Reads configuratin file of feature extraction and classifier and creates
     * them with parameters specified in the file
     *
     * @param f - configuratin file
     */
    private void readConfiguration(File f) {
        try {
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String row = br.readLine();
            if (row.equals("FilterAndSubsamplingFeatureExtraction")) {
                fe = new FilterAndSubsamplingFeatureExtraction();
                ((FilterAndSubsamplingFeatureExtraction) fe)
                        .setEpochSize(Integer.parseInt(br.readLine()));
                ((FilterAndSubsamplingFeatureExtraction) fe)
                        .setSubsampling(Integer.parseInt(br.readLine()));
                ((FilterAndSubsamplingFeatureExtraction) fe)
                        .setSkipSamples(Integer.parseInt(br.readLine()));
            } else if (row
                    .equalsIgnoreCase("WaveletTransformFeatureExtraction")) {
                fe = new WaveletTransformFeatureExtraction();
                ((WaveletTransformFeatureExtraction) fe).setEpochSize(Integer
                        .parseInt(br.readLine()));
                ((WaveletTransformFeatureExtraction) fe).setSkipSamples(Integer
                        .parseInt(br.readLine()));
                ((WaveletTransformFeatureExtraction) fe).setWaveletName(Integer
                        .parseInt(br.readLine()));
                ((WaveletTransformFeatureExtraction) fe).setFeatureSize(Integer
                        .parseInt(br.readLine()));
            } else if (row.equals("MatchingPursuitFeatureExtraction")) {
                fe = new MatchingPursuitFeatureExtraction();
                ((MatchingPursuitFeatureExtraction) fe).setEpochSize(Integer
                        .parseInt(br.readLine()));
                ((MatchingPursuitFeatureExtraction) fe).setDownSmplFactor(Integer
                        .parseInt(br.readLine()));
                ((MatchingPursuitFeatureExtraction) fe).setSkipSamples(Integer
                        .parseInt(br.readLine()));
            } else if (row.equals("HHTFeatureExtraction")) {
                fe = new HHTFeatureExtraction();
                ((HHTFeatureExtraction) fe).setEpochSize(Integer
                        .parseInt(br.readLine()));
                ((HHTFeatureExtraction) fe).setDownSmplFactor(Integer
                        .parseInt(br.readLine()));
                ((HHTFeatureExtraction) fe).setSkipSamples(Integer
                        .parseInt(br.readLine()));
                ((HHTFeatureExtraction) fe).setSampleWindowSize(Integer
                        .parseInt(br.readLine()));
                ((HHTFeatureExtraction) fe).setSampleWindowShift(Integer
                        .parseInt(br.readLine()));
                ((HHTFeatureExtraction) fe).setAmplitudeThreshold(Double
                        .parseDouble(br.readLine()));
                ((HHTFeatureExtraction) fe).setMinFreq(Double.parseDouble(br
                        .readLine()));
                ((HHTFeatureExtraction) fe).setMaxFreq(Double.parseDouble(br
                        .readLine()));
                ((HHTFeatureExtraction) fe).setTypeOfFeatures(Integer
                        .parseInt(br.readLine()));
            }
            row = br.readLine();
            if (row.equals("MLPClassifier")) {
                ArrayList<Integer> nnStructure = new ArrayList<Integer>();
                nnStructure.add(Integer.parseInt(br.readLine()));
                nnStructure.add(Integer.parseInt(br.readLine()));
                nnStructure.add(Integer.parseInt(br.readLine()));
                classifier = new MLPClassifier(nnStructure);
            } else if (row.equals("KNNClassifier")) {
                classifier = new KNNClassifier(Integer.parseInt(br.readLine()));
            } else if (row.equals("LinearDiscriminantAnalysisClassifier")) {
                classifier = new LinearDiscriminantAnalysisClassifier();
            } else if (row.equals("SVMClassifier")) {
                try {
                    classifier = new SVMClassifier(Double.parseDouble(br
                            .readLine()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (row.equals("CorrelationClassifier")) {
                classifier = new CorrelationClassifier();
            } else if (row.equals("DBNClassifier")) {
                try {
                    classifier = new DBNClassifier(Integer.parseInt(br
                            .readLine()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (row.equals("SDAClassifierEarlyStop")) {
                try {
                    classifier = new SDAClassifierEarlyStop(Integer.parseInt(br
                            .readLine()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens FileChooser and allow user to specify configuration file to be
     * opened
     */
    private void loadConfiguration() {
        JFileChooser open = new JFileChooser();
        open.setDialogTitle("Select file with saved configuration");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "TXT files .txt", "TXT", "txt");
        open.addChoosableFileFilter(filter);
        open.setFileFilter(filter);
        open.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int openResult = open.showOpenDialog(this);
        if (openResult == JFileChooser.APPROVE_OPTION) {
            configurationFile = open.getSelectedFile().getPath();
        } else {
            configurationFile = "";
        }

        File config = new File(configurationFile);
        if (config.exists()) {
            readConfiguration(config);
            String path = config.getAbsolutePath();
            path = path.substring(0, path.lastIndexOf('.')) + ".classifier";
            classifier.setFeatureExtraction(fe);
            classifier.load(path);
            //loadClassifier();
            setTrained(true);
        } else {
            classifier = new MLPClassifier();
            fe = new FilterAndSubsamplingFeatureExtraction();
            classifier.setFeatureExtraction(fe);
            setTrained(false);
        }
    }

    private void loadClassifier() {
        JFileChooser open = new JFileChooser();
        open.setDialogTitle("Select file with trained classifier");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "CLASSIFIER files .classifier", "CLASSIFIER", "classifier");
        open.addChoosableFileFilter(filter);
        open.setFileFilter(filter);
        open.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int openResult = open.showOpenDialog(this);
        String file = "";
        if (openResult == JFileChooser.APPROVE_OPTION) {
            file = open.getSelectedFile().getPath();
            classifier.load(file);
        }
    }

    /**
     * Creates status bar with selected feature extraction and classsifier
     *
     * @return panel with status bar
     */
    private JPanel createStatusBar() {
        if (statusBar != null) {
            getContentPane().remove(statusBar);
        }
        statusBar = new JPanel();
        statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.LINE_AXIS));
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());

        feStatus = new JLabel("Feature Extraction: "
                + this.fe.getClass().getSimpleName() + " ");
        statusBar.add(feStatus);

        statusBar.add(Box.createHorizontalStrut(5));
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setMaximumSize(new Dimension(1, Integer.MAX_VALUE));
        statusBar.add(separator);
        statusBar.add(Box.createHorizontalStrut(5));

        classifierStatus = new JLabel("Classifier: "
                + this.classifier.getClass().getSimpleName() + " ");
        statusBar.add(classifierStatus);

        if (artifactDetection != null) {
            separator = new JSeparator(SwingConstants.VERTICAL);
            separator.setMaximumSize(new Dimension(1, Integer.MAX_VALUE));
            statusBar.add(separator);
            statusBar.add(Box.createHorizontalStrut(5));

            artDetectionStatus = new JLabel("Artifact Detection: "
                    + this.artifactDetection.getClass().getSimpleName() + " ");
            statusBar.add(artDetectionStatus);
        }

        if (dataFilter != null) {
            separator = new JSeparator(SwingConstants.VERTICAL);
            separator.setMaximumSize(new Dimension(1, Integer.MAX_VALUE));
            statusBar.add(separator);
            statusBar.add(Box.createHorizontalStrut(5));

            filterStatus = new JLabel("Filter: "
                    + this.dataFilter.getClass().getSimpleName() + " ");
            statusBar.add(filterStatus);
        }

        return statusBar;
    }

    private JMenuBar createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem offlineMenuItem = new JMenuItem();
        offlineMenuItem.setAction((new LoadOfflineData()));
        JMenuItem onlineMenuItem = new JMenuItem();
        onlineMenuItem.setAction(new LoadOnlineData());
        JMenuItem loadConfigAndClassifierItem = new JMenuItem(
                "Load configuration and classifier files");
        JMenuItem chartMenuItem = new JMenuItem();
        chartMenuItem.setAction(this.epochCharts);
        JMenuItem testAllMenuItem = new JMenuItem();
        testAllMenuItem.setAction(new RunTestAll());
        JMenuItem endMenuItem = new JMenuItem("Close");
        endMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                ActionEvent.CTRL_MASK));
        endMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        loadConfigAndClassifierItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                loadConfiguration();
                File conf = new File(configurationFile);
                if (conf.exists()) {
                    readConfiguration(conf);
                    loadClassifier();
                    classifier.setFeatureExtraction(fe);
                    setTrained(true);

                    setFeStatus("Feature Extraction: "
                            + fe.getClass().getSimpleName());
                    setClassifierStatus("Classifier: "
                            + classifier.getClass().getSimpleName());
                }
            }
        });

        final MainFrame mf = this;
        JMenu settingsMenu = new JMenu("Settings");
        JMenuItem featureMenuItem = new JMenuItem(
                "Feature Extraction and Classifier");
        featureMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                ActionEvent.CTRL_MASK));
        featureMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ChangeFeatureExtractionFrame f = new ChangeFeatureExtractionFrame(
                        mf);
                f.setVisible(true);
            }
        });

        JMenuItem trainMenuItem = new JMenuItem("Train");
        trainMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
                ActionEvent.CTRL_MASK));
        trainMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO training
                JFileChooser save = new JFileChooser();
                save.setDialogTitle("Save file with trained classifier");
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "CLASSIFIER files .classifier", "CLASSIFIER",
                        "classifier");
                save.addChoosableFileFilter(filter);
                save.setFileFilter(filter);
                save.setCurrentDirectory(new File(System
                        .getProperty("user.dir")));
                int saveResult = save.showSaveDialog(mf);
                String file = "";
                if (saveResult == JFileChooser.APPROVE_OPTION) {
                    file = save.getSelectedFile().getPath();
                    file += ".classifier";

                    new TrainUsingOfflineProvider(fe, classifier, file, dataFilter);

                    setTrained(true);
                }
            }
        });

        JMenu artifactDetectionAndFiltering = new JMenu(
                "Filtering and Artifact detection");

        JMenuItem amplitude = new JMenuItem(
                "Amplitude and Gradient artifact detection");
        amplitude.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                ActionEvent.CTRL_MASK));
        amplitude.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new AmplDetDialog(mf);
                mf.getContentPane().add(createStatusBar(), BorderLayout.SOUTH);
                revalidate();
                //f.setVisible(true);
            }
        });

        JMenuItem LHpassFilter = new JMenuItem(
                "Low/High pass filter");
        LHpassFilter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
                ActionEvent.CTRL_MASK));
        LHpassFilter.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new LHPassFilterDialog(mf);
                mf.getContentPane().add(createStatusBar(), BorderLayout.SOUTH);
                revalidate();
                //f.setVisible(true);
            }
        });

        JMenuItem firFilter = new JMenuItem(
                "FIR filter");
        firFilter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
                ActionEvent.CTRL_MASK));
        firFilter.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new FirFilterDialog(mf);
                mf.getContentPane().add(createStatusBar(), BorderLayout.SOUTH);
                revalidate();
                //f.setVisible(true);
            }
        });

        JMenuItem buttFilter = new JMenuItem(
                "ButterWorth filter");
        buttFilter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
                ActionEvent.CTRL_MASK));
        buttFilter.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new ButterWorthFilterDialog(mf);
                mf.getContentPane().add(createStatusBar(), BorderLayout.SOUTH);
                revalidate();
                //f.setVisible(true);
            }
        });

        JMenuItem corrArtifact = new JMenuItem(
                "Correlation artifact detection");
        corrArtifact.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                ActionEvent.CTRL_MASK));
        corrArtifact.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new CorrDetDialog(mf);
                mf.getContentPane().add(createStatusBar(), BorderLayout.SOUTH);
                revalidate();
                //f.setVisible(true);
            }
        });

        JMenuItem addScenario = new JMenuItem("New Scenario");
        addScenario.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                ActionEvent.CTRL_MASK));
        addScenario.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ScenarioDialog sd = new ScenarioDialog(mf);
                sd.createDialog(mf);
                revalidate();

            }
        });

        menuBar.add(fileMenu);
        fileMenu.add(onlineMenuItem);
        fileMenu.add(offlineMenuItem);
        fileMenu.add(testAllMenuItem);
        fileMenu.add(loadConfigAndClassifierItem);
        fileMenu.add(chartMenuItem);
        fileMenu.add(addScenario);
        fileMenu.addSeparator();
        fileMenu.add(endMenuItem);

        artifactDetectionAndFiltering.add(buttFilter);
        artifactDetectionAndFiltering.add(firFilter);
        artifactDetectionAndFiltering.add(LHpassFilter);
        artifactDetectionAndFiltering.add(corrArtifact);
        artifactDetectionAndFiltering.add(amplitude);
        settingsMenu.add(artifactDetectionAndFiltering);

        menuBar.add(settingsMenu);
        settingsMenu.add(featureMenuItem);
        settingsMenu.add(trainMenuItem);

        return menuBar;
    }

    public JPanel createContentJP() {
        GridLayout mainLayout = new GridLayout(0, 2);
        JPanel contentJP = new JPanel(mainLayout);
        int count = stimuls.size();

        if (stimuls.size() == 0) count = 9;

        //System.out.println(count + "createContentJp count");
        contentJP.add(createStimuliJT(count));
      //  contentJP.add(createWinnerJTA());
        contentJP.add(createWinnerJP());
        return contentJP;
    }

    private JTextPane createWinnerJTA() {
        winnerJTA = new JTextPane();
        Font font = new Font(Const.RESULT_FONT_NAME, Const.RESULT_FONT_STYLE,
                Const.RESULT_FONT_SIZE);
        winnerJTA.setFont(font);
        winnerJTA.setBackground(Color.BLACK);
        winnerJTA.setForeground(Color.WHITE);
        StyledDocument doc = winnerJTA.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        winnerJTA.setText(Const.UNKNOWN_RESULT);
        return winnerJTA;
    }

    private JPanel createWinnerJP(){
        GridLayout gridLayout = new GridLayout(2,0);
        winnerJP = new JPanel(gridLayout);
        Font font = new Font(Const.RESULT_FONT_NAME, Const.RESULT_FONT_STYLE,
                Const.RESULT_FONT_SIZE);
        winnerJP.setFont(font);
        winnerJP.setBackground(Color.WHITE);
        //winnerJP.setForeground(Color.WHITE);
        return winnerJP;
    }
    public JScrollPane createStimuliJT(int id) {
        data = new StimuliTableModel(id);
        JTable stimuliJT = new JTable(data) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                //  Color row based on a cell value
                c.setBackground(getBackground());
                int modelRow = convertRowIndexToModel(row);
                Double weight = (Double) getModel().getValueAt(modelRow, 2);
                if (weight == null || weight == 0) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(ColorUtils.convertToRgb(weight));
                }

                return c;
            }
        };

        JScrollPane jsp = new JScrollPane(stimuliJT);
        stimuliJT.setFillsViewportHeight(true);
        return jsp;
    }

    private void initProbabilities(double[] probabilities, int[] counts) {
        winnerJP.removeAll();
        JLabel label;
        JLabel label2;
        Integer[] ranks = new Integer[probabilities.length];
        for (int i = 0; i < ranks.length; ++i) {
            ranks[i] = i;
        }
        Comparator<Integer> gc = new ProbabilityComparator(probabilities);
        Arrays.sort(ranks, gc);

        if (stimuls.get(ranks[0]).isImgFile1 == true) {
            ImageIcon icon = new ImageIcon(stimuls.get(ranks[0]).image1);
            Image image = icon.getImage(); // transform it
            Image newimg = image.getScaledInstance(200, 200,  Image.SCALE_SMOOTH);
            icon = new ImageIcon(newimg);
            label = new JLabel(icon);
            winnerJP.add(label,BorderLayout.CENTER);
        }
        else{
            TextField textField = new TextField(stimuls.get(ranks[0]).getName());
            winnerJP.add(textField,BorderLayout.CENTER);
        }
        if (stimuls.get(ranks[0]).isImgFile2 == true) {
            ImageIcon icon2 = new ImageIcon(stimuls.get(ranks[0]).image1);
            Image image = icon2.getImage(); // transform it
            Image newimg = image.getScaledInstance(200, 200,  Image.SCALE_SMOOTH);
            icon2 = new ImageIcon(newimg);
            label2 = new JLabel(icon2);
            winnerJP.add(label2,BorderLayout.CENTER);
        }
        else{
            TextField textField = new TextField(stimuls.get(ranks[0]).getName());
            winnerJP.add(textField,BorderLayout.CENTER);
        }

        for (int i = 0; i < probabilities.length; i++) {
            data.setValueAt(probabilities[ranks[i]], i, 2);
            data.setValueAt(stimuls.get(ranks[i]).name,i,1);
            data.setValueAt(ranks[i] + 1, i, 0);
            data.setValueAt(counts[ranks[i]], i, 3);
        }
        this.validate();
        this.repaint();
    }

    @Override
    public void update(Observable sender, Object message)
            throws IllegalArgumentException {
        if (message instanceof OnlineDetection) {
            double[] probabilities = ((OnlineDetection) message)
                    .getWeightedResults();
            int[] counts = ((OnlineDetection) message).getClassificationCounters();
            initProbabilities(probabilities, counts);

            this.epochCharts.update(((OnlineDetection) message).getPzAvg());
        }
        /*
         * else { log.error(MainFrame.class.toString() +
         * ": Expencted online detection, but received something else."); throw
         * new IllegalArgumentException(
         * "Expencted online detection, but received something else."); }
         */

    }

    private void initGui() {
        double[] zeros = new double[data.getRowCount()];
        int[] intZeros = new int[data.getRowCount()];
        Arrays.fill(zeros, 0);
        Arrays.fill(intZeros, 0);
        initProbabilities(zeros, intZeros);
       // winnerJTA.setText(Const.UNKNOWN_RESULT);
    }

    private void stopRunningThread() {
        try {
            if (dataProvider != null) {
                if (dp != null) {
                    dp.stop();
                } else {
                    dataProvider.interrupt();
                }
            }
        } catch (Exception ex) {

        }
    }

    /**
     * Getter for classifier
     *
     * @return classifier
     */
    public IERPClassifier getClassifier() {
        return classifier;
    }

    /**
     * Setter for classifier
     *
     * @param classifier - classifier to be set
     */
    public void setClassifier(IERPClassifier classifier) {
        this.classifier = classifier;
    }

    /**
     * Getter for feature extraction
     *
     * @return feature extraction
     */
    public IFeatureExtraction getFe() {
        return fe;
    }

    /**
     * Setter for feature extraction
     *
     * @param fe - feature extraction to be set
     */
    public void setFe(IFeatureExtraction fe) {
        this.fe = fe;
    }

    /**
     * Checks if the classifier is trained
     *
     * @return <code>true</code> if classifier is trained;<code>false</code> if
     * it is not trained
     */
    public boolean isTrained() {
        return trained;
    }

    /**
     * Setter for trained parameter
     *
     * @param trained - <code>true</code> if classifier is trained;
     * <code>false</code> if it is not trained
     */
    public void setTrained(boolean trained) {
        this.trained = trained;
    }

    /**
     * Setter for feature extraction status in the status bar
     *
     * @param feStatus - feature extraction status, e.g. "FeatureExtraction" +
     * fe.getClass().getSimpleName()
     */
    public void setFeStatus(String feStatus) {
        this.feStatus.setText(feStatus);
    }

    /**
     * Setter for classifier status in the status bar
     *
     * @param classifierStatus - classifier status, e.g. "Classifier" +
     * classifier.getClass().getSimpleName()
     */
    public void setClassifierStatus(String classifierStatus) {
        this.classifierStatus.setText(classifierStatus);
    }

    /**
     * Opens dialog that points out that classifier is not trained and prompts
     * the user to train it
     */
    private void trainingDialog() {
        if (isTrained() == false) {
            int dialogResult = JOptionPane.showConfirmDialog(null,
                    "You have to train the classifier in order to use it",
                    "Classifier is not trained", JOptionPane.OK_CANCEL_OPTION);
            if (dialogResult == JOptionPane.OK_OPTION) {
                JFileChooser save = new JFileChooser();
                save.setDialogTitle("Save file with trained classifier");
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "CLASSIFIER files .classifier", "CLASSIFIER",
                        "classifier");
                save.addChoosableFileFilter(filter);
                save.setFileFilter(filter);
                save.setCurrentDirectory(new File(System
                        .getProperty("user.dir")));
                int saveResult = save.showSaveDialog(this);
                String file = "";
                if (saveResult == JFileChooser.APPROVE_OPTION) {
                    file = save.getSelectedFile().getPath();
                    file += ".classifier";
                    new TrainUsingOfflineProvider(fe, classifier, file, dataFilter);

                    setTrained(true);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "You have to train the classifier first", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public class LoadOfflineData extends AbstractAction {

        MainFrame mainFrame;

        @Override
        public void actionPerformed(ActionEvent actionevent) {
            if (!isExistScenario()) {
                scenarioDialog();

            }else if (!isTrained()){
                trainingDialog();
            }else {
                initGui();
                chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "EEG files .eeg", "EEG", "eeg");
                chooser.addChoosableFileFilter(filter);
                chooser.setFileFilter(filter);
                chooser.setCurrentDirectory(new File(System
                        .getProperty("user.dir")));
                int i = chooser.showDialog(mainFrame, "Open");
                if (i == 0) {
                    eegFile = chooser.getSelectedFile();
                    detection = new OnlineDetection(classifier, mainFrame);
                    stopRunningThread();

                    try {
                        dp = new OffLineDataProvider(eegFile, detection);
                        dataProvider = new Thread((OffLineDataProvider) dp);
                        dataProvider.start();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(mainFrame,
                                ex.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

        public LoadOfflineData() {
            super();
            mainFrame = MainFrame.this;
            putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_O,
                    ActionEvent.CTRL_MASK));
            putValue("Name", "Offline data");
        }
    }

    public class LoadOnlineData extends AbstractAction {

        MainFrame mainFrame;

        @Override
        public void actionPerformed(ActionEvent actionevent) {
            if (!isExistScenario()) {
                scenarioDialog();

            }else if (!isTrained()){
                trainingDialog();
            }
            else
                {
                initGui();

                //SetupDialogContent content = new SetupDialogContent();
                int result;
                boolean isOk = false;
                String recorderIPAddress = null;
                int port = -1;
                while (!isOk) {
                    result = JOptionPane.showConfirmDialog(null, content,
                            "Guess the Number: Setup",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE);
                    if (result == JOptionPane.CANCEL_OPTION) {
                        return;
                    }

                    recorderIPAddress = content.getIP();
                    port = content.getPort();
                    if (port != -1 && recorderIPAddress != null) {
                        isOk = true;
                    } else {
                        if (port == -1) {
                            JOptionPane.showMessageDialog(null,
                                    "Invalid port number!", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                        if (recorderIPAddress == null) {
                            JOptionPane.showMessageDialog(null,
                                    "Invalid IP address!", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }

                if (isOk) {
                    stopRunningThread();

                    detection = new OnlineDetection(classifier, mainFrame);
                    try {
                        dp = new OnLineDataProvider(recorderIPAddress, port,
                                detection);
                        dataProvider = new Thread((OnLineDataProvider) dp);
                        dataProvider.start();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(mainFrame,
                                ex.getMessage(), "Connection Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

        public LoadOnlineData() {
            super();
            mainFrame = MainFrame.this;
            putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_P,
                    ActionEvent.CTRL_MASK));
            putValue("Name", "Online data");
        }
    }

    public class RunTestAll extends AbstractAction {

        MainFrame mainFrame;

        public RunTestAll() {
            super();
            mainFrame = MainFrame.this;
            putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_A,
                    ActionEvent.CTRL_MASK));
            putValue("Name", "Test all datasets");
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            if (!isExistScenario()) {
                scenarioDialog();

            }else if (!isTrained()){
                trainingDialog();
            } else {
                try {
                    TestClassificationAccuracy testClassificationAccuracy = new TestClassificationAccuracy(mainFrame.classifier);
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }
    public void setStimuls(ArrayList<Stimul> stimuls) {
        this.stimuls = stimuls;
        for (int i = 0; i < stimuls.size(); i++) {
            stimuls.get(i).loadImages();
        }
        this.setLayout(new BorderLayout());

        getContentPane().removeAll();
        getContentPane().add(createContentJP(), BorderLayout.CENTER);
        getNames(stimuls);
    }

    public void getNames(ArrayList<Stimul>stimuls){
        for (int i = 0; i < stimuls.size(); i++) {
            data.setValueAt(stimuls.get(i).getName(),i,1);

        }
    }

    public boolean isExistScenario() {
        return existScenario;
    }

    public void setExistScenario(boolean existScenario) {
        this.existScenario = existScenario;
    }

    private void scenarioDialog() {
        if (isExistScenario() == false) {
            int dialogResult = JOptionPane.showConfirmDialog(null,
                    "You have to create stimuls scenario",
                    "Scenario is not created", JOptionPane.OK_CANCEL_OPTION);
            if (dialogResult == JOptionPane.OK_OPTION) {
                ScenarioDialog sd = new ScenarioDialog(this);
                sd.createDialog(this);
                revalidate();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "You have to create stimuls scenario first", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
    }
}
