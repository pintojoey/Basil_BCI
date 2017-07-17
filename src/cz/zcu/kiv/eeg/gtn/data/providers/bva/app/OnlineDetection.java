package cz.zcu.kiv.eeg.gtn.data.providers.bva.app;

import cz.zcu.kiv.eeg.gtn.data.processing.classification.IERPClassifier;
import cz.zcu.kiv.eeg.gtn.data.providers.EEGDataBlock;
import cz.zcu.kiv.eeg.gtn.utils.Const;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

public class OnlineDetection extends Observable implements Observer {

    private final IERPClassifier classifier;
    private final double[] classificationResults;
    private final int[] classificationCounters;
    private final double[][][] sumEpoch;
    private final double[][][] avgEpoch;
    public int sizeOfStimuls;

    private double[] weightedResults;

    public OnlineDetection(IERPClassifier classifier, Observer observer,int sizeOfStimuls) {
        super();
        this.sizeOfStimuls=sizeOfStimuls;
        this.addObserver(observer);
        this.classifier = classifier;
        this.classificationCounters = new int[sizeOfStimuls];
        this.classificationResults = new double[sizeOfStimuls];
        this.sumEpoch = new double[EpochMessenger.DEFAULT_USED_CHANNELS][sizeOfStimuls][Const.POSTSTIMULUS_VALUES];
        this.avgEpoch = new double[EpochMessenger.DEFAULT_USED_CHANNELS][sizeOfStimuls][Const.POSTSTIMULUS_VALUES];

        Arrays.fill(classificationCounters, 0);
        Arrays.fill(classificationResults, 0);
        for (int i = 0; i < sumEpoch.length; i++) {
            for (int j = 0; j < sumEpoch[i].length; j++) {
                Arrays.fill(sumEpoch[i][j], 0);
                Arrays.fill(avgEpoch[i][j], 0);
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) throws IllegalArgumentException {
        if (arg instanceof EpochMessenger) {
            EpochMessenger epochMsg = (EpochMessenger) arg;
            int stimulusID = epochMsg.getStimulusIndex();

            if (stimulusID < sizeOfStimuls) {
                classificationCounters[stimulusID]++;
                for (int i = 0; i < sumEpoch.length; i++) {
                    for (int j = 0; j < sumEpoch[i][stimulusID].length; j++) {
                        sumEpoch[i][stimulusID][j] += epochMsg.getEpoch()[i][j]; // Pz
                        avgEpoch[i][stimulusID][j] = sumEpoch[i][stimulusID][j] / classificationCounters[stimulusID];
                    }
                }
   
                
                double classificationResult = this.classifier.classify(epochMsg.getEpoch());

                
                classificationResults[stimulusID] += classificationResult;
                this.weightedResults = this.calcClassificationResults();
                setChanged();
                notifyObservers(this);
            }
        } else if (arg instanceof EEGDataBlock) {
            setChanged();
            notifyObservers(arg); 
        }
    }

    private double calcEnergy( double[][] epochStimulus, int start, int end) {
    	double energy = 0;
    	for (int i = 0; i < sumEpoch.length; i++) {
    		for (int j = start; j < end; j++) {
    			energy += Math.pow(epochStimulus[i][j], 2);
    		}
    	}
    	return Math.sqrt(energy);
    }
    
    private double[][] getAvgEpochWithStimulus(int stimulusIndex, double[][][] epoch) {
        double[][] epochStimulus = new double[sumEpoch.length][Const.POSTSTIMULUS_VALUES];
        for (int i = 0; i < sumEpoch.length; i++) {
            System.arraycopy(epoch[i][stimulusIndex], 0, epochStimulus[i], 0, Const.POSTSTIMULUS_VALUES);
        }
        return epochStimulus;

    }

    private double[] calcClassificationResults() {
      //  double[] wResults = new double[Const.GUESSED_NUMBERS];
        double[] wResults = new double[sizeOfStimuls];
        for (int i = 0; i < wResults.length; i++) {
            if (classificationCounters[i] == 0) {
                wResults[i] = 0;
            } else {
                wResults[i] = classificationResults[i] / classificationCounters[i];
            }
        }

        return wResults;
    }

 

    public double[] getWeightedResults() {
        return weightedResults;
    }

    public int[] getClassificationCounters() {
        return classificationCounters;
    }
    public void setSizeOfStimuls(int sizeOfStimuls) {
        this.sizeOfStimuls = sizeOfStimuls;
    }

    public int getSizeOfStimuls() {
        return sizeOfStimuls;
    }
}
