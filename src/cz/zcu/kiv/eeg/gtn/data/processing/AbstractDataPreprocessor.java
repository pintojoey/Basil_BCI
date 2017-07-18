package cz.zcu.kiv.eeg.gtn.data.processing;

import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IPreprocessing;
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

    protected  final int prestimulus;

    protected final int packageSize;

    protected final List<IPreprocessing> preprocessing;

    public AbstractDataPreprocessor(int prestimulus, int packageSize, List<IPreprocessing> preprocessing) {
        this.prestimulus = prestimulus;
        this.packageSize = packageSize;
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
                processData(); //TODO možná to dát do vlákna? + nutno implementovat buffer
            } else if (arg instanceof EEGStartMessage) {
                start((EEGStartMessage) arg);
                running = true;
            } else if (arg instanceof EEGStopMessage) {
                stop((EEGStopMessage) arg);
            }
        }
    }

    protected void processData(){
        EEGDataPackage data = retrieveData(prestimulus, packageSize);
        if(data == null) return;

        for (IPreprocessing prep : preprocessing){
            data = prep.preprocess(data);
        }

        this.setChanged();
        this.notifyObservers(data);
    }
}
