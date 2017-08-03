package cz.zcu.kiv.eeg.gtn.data.processing.classification.test;

import java.io.IOException;
import java.util.*;

import cz.zcu.kiv.eeg.gtn.data.processing.classification.*;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.WaveletTransformFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.math.ButterWorthFilter;
import cz.zcu.kiv.eeg.gtn.data.processing.math.IFilter;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.MessageType;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.OffLineDataProvider;
import cz.zcu.kiv.eeg.gtn.utils.Const;



/**
 * 
 * Trains the classifier using the collected off-line (stored) data
 * 
 * @author lvareka
 *
 */
public class TrainUsingOfflineProvider implements Observer {

    private final List<double[][]> epochs;
    private final List<Double> targets;
    private int numberOfTargets;
    private int numberOfNonTargets;
    private int iters;
    private int middleNeurons;
    private static IFeatureExtraction fe;
    private static IClassifier classifier;
    private static String file;
    private IFilter filter;
    private int receivedEpochsCounter = 0;

    
    public TrainUsingOfflineProvider(IFeatureExtraction fe,
            IClassifier classifier, String file, IFilter filter)  {
        TrainUsingOfflineProvider.fe = fe;
        TrainUsingOfflineProvider.classifier = classifier;
        TrainUsingOfflineProvider.file = file;

        this.filter = filter;
        this.epochs = new ArrayList<double[][]>();
        this.targets = new ArrayList<Double>();
        this.numberOfTargets = 0;
        this.numberOfNonTargets = 0;
        this.iters = 2000;
        this.middleNeurons = 8;

        OffLineDataProvider offLineData;
        try {
			offLineData =  new OffLineDataProvider(Const.INFO_DIR);
			Thread t = new Thread(offLineData);
	        t.start();
	        try {
	            t.join();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    }

    public TrainUsingOfflineProvider(int iters, int middleNeurons) throws IOException {
        this.epochs = new ArrayList<double[][]>();
        this.targets = new ArrayList<Double>();
        this.numberOfTargets = 0;
        this.numberOfNonTargets = 0;
        this.iters = iters;
        this.middleNeurons = middleNeurons;
        this.classifier = null;

        // used to open the thread for collecting single epochs from continuous EEG
        OffLineDataProvider offLineData = new OffLineDataProvider(Const.INFO_DIR);
        Thread t = new Thread(offLineData);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        if (classifier == null) {
            TrainUsingOfflineProvider train = new TrainUsingOfflineProvider(
                    2000, 8);
        } else {
            TrainUsingOfflineProvider train = new TrainUsingOfflineProvider(fe,
                    classifier, file, new ButterWorthFilter());
            System.out.print("train regular");
        }
    }

    /**
     * Receives the EEG epoch from data collection observable class. Also 
     * can receive a message declaring the end of the epoch list and this will be
     * interpreted as a signal to start training.
     * 
     */
    @Override
    public void update(Observable sender, Object message) {
        if (message instanceof EEGDataMessage) {
            EEGDataMessage msg = (EEGDataMessage) message;
            if (msg.getMsgType() == MessageType.END) {
               this.train();
            }
        }
/*        if (message instanceof EpochMessenger) {
            double[][] epoch = ((EpochMessenger) message).getEpoch();
            receivedEpochsCounter++;
           
            // 1 = target, 3 = non-target
            if (((EpochMessenger) message).isTarget() && numberOfTargets <= numberOfNonTargets) {
                epochs.add(epoch);

                targets.add(1.0);
                numberOfTargets++;
            } else if (!((EpochMessenger) message).isTarget() && numberOfTargets >= numberOfNonTargets) {
                epochs.add(epoch);
                targets.add(0.0);
                numberOfNonTargets++;
            }
        }*/
    }

    private void train() {
        // create classifiers

        if (classifier == null) {
            setDefaultClassifier();
        }

        double[][] tAvg = new double[epochs.get(0).length][epochs.get(0)[0].length];
        double[][] nAvg = new double[epochs.get(0).length][epochs.get(0)[0].length];
        for (int k = 0; k < tAvg.length; k++) {
            Arrays.fill(tAvg[k], 0);
            Arrays.fill(nAvg[k], 0);
        }
        int cnt = 0;
        int tCnt = 0;
        int nCnt = 0;
        double t;
        double val = 0;
        for (double[][] epoch : epochs) {
            t = targets.get(cnt);
            if (t == 1) {
                tCnt++;
            } else {
                nCnt++;
            }
            for (int i = 0; i < epoch.length; i++) {
                for (int j = 0; j < epoch[i].length; j++) {
                    val = epoch[i][j];
                    if (filter != null) {
                        val = filter.getOutputSample(val);
                    }

                    if (t == 1) {

                        tAvg[i][j] += val;
                    } else {

                        nAvg[i][j] += val;
                    }
                }
            }

            cnt++;
        }

        for (int i = 0; i < tAvg.length; i++) {
            for (int j = 0; j < tAvg[i].length; j++) {
                tAvg[i][j] = tAvg[i][j] / tCnt;
                nAvg[i][j] = nAvg[i][j] / nCnt;
            }
        }

        // training
        //fe = new FilterAndSubsamplingFeatureExtraction();
        classifier.train(this.epochs, this.targets, this.iters, fe);

        if (file == null || file.equals("")) {
            classifier.save(Const.TRAINING_FILE_NAME);
        } else {
            classifier.save(file);
        }

        System.out.println("Training finished.");
        
    }

    /**
     *
     * If no classifier is set, create a default classifier with empirically set
     * parameters
     *
     */
    private void setDefaultClassifier() {
        /*Random r = new Random(System.nanoTime());
        fe = new WaveletTransformFeatureExtraction(14, 512, 20, 8);*/
        fe = new WaveletTransformFeatureExtraction();
    	//fe = new FilterAndSubsamplingFeatureExtraction();
    	int numberOfInputNeurons = fe.getFeatureDimension();
        int middleNeurons = this.middleNeurons;
        int outputNeurons = 1;
        ArrayList<Integer> nnStructure = new ArrayList<Integer>();
        nnStructure.add(numberOfInputNeurons);
        nnStructure.add(middleNeurons);
        nnStructure.add(outputNeurons);

        //TODO clasifikatory
        //classifier = new MLPClassifier(nnStructure);
        //classifier = new SDADeepLearning4j();
        classifier = new SDADeepLearning4jEarlyStopClassifier();
        //classifier = new MLPDeepLearning4jEarlyStop();
        //classifier = new MLPDeepLearning4j();
        //classifier = new DBNDeepLearning4j();
        //classifier = new SVMClassifier();
        classifier.setFeatureExtraction(fe);
    }

    public IClassifier getClassifier() {
        return classifier;
    }

    /**
     * Writes epochs into comma delimited csv file Epochs.csv.
     *
     * @param epochs list of epochs
     * @throws java.lang.Exception
     *
     *
     */
    public static void writeCSV(List<double[][]> epochs) throws Exception {

        //create a File class object and give the file the name employees.csv
        java.io.File file = new java.io.File("Epochs.csv");

        //Create a Printwriter text output stream and link it to the CSV File
        java.io.PrintWriter outfile = new java.io.PrintWriter(file);

        //Iterate the elements actually being used
        for (double[][] epoch : epochs) {
            for (int i = 0; i < epoch[2].length; i++) {
                outfile.write(epoch[2][i] + ",");
            }
            outfile.write("\n");
        }

        outfile.close();
    }

}