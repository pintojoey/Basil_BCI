package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import cz.zcu.kiv.eeg.gtn.data.processing.Structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStopMessage;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Tomas Prokop on 04.07.2017.
 */
public abstract class AbstractDataPreprocessor extends Observable implements Observer {

    protected  boolean running = false;

    protected final IEpochExtraction epochExtraction;

    protected final List<IPreprocessing> preprocessing;

    public AbstractDataPreprocessor(IEpochExtraction epochExtraction, List<IPreprocessing> preprocessing) {
    	this.epochExtraction = epochExtraction;
    	this.preprocessing = preprocessing;
    }

    public abstract void storeData(EEGDataMessage data);

    public abstract EEGDataPackage retrieveData(int prestimulus, int size);

    public abstract void start(EEGStartMessage start);

    public abstract void stop(EEGStopMessage stop);

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof EEGMessage) {
            if (arg instanceof EEGDataMessage) {
                storeData((EEGDataMessage) arg);
                //processData(); //TODO možná to dát do vlákna? + nutno implementovat buffer
            } else if (arg instanceof EEGStartMessage) {
                start((EEGStartMessage) arg);
                running = true;
            } else if (arg instanceof EEGStopMessage) {
                stop((EEGStopMessage) arg);
            }
        }
    }

    protected void processAllData(){
        EEGDataPackage data = null;
                //= retrieveData();
        if(data == null) return;

        List<EEGDataPackage> epochs = null;

        if (this.epochExtraction != null) {
        	epochs = this.epochExtraction.extractEpochs(data);
        	for (EEGDataPackage epoch: epochs) {
        		processData(epoch);
        	}
        } else {
        	processData(data);
        }

    }

	protected void processData(EEGDataPackage data) {
		for (IPreprocessing prep : preprocessing){
			data = prep.preprocess(data);
	    }

	    this.setChanged();
	    this.notifyObservers(data);
	}
}
