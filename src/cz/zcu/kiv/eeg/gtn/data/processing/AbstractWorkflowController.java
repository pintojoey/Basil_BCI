package cz.zcu.kiv.eeg.gtn.data.processing;

import cz.zcu.kiv.eeg.gtn.data.listeners.DataProviderListener;
import cz.zcu.kiv.eeg.gtn.data.listeners.EEGDataProcessingListener;
import cz.zcu.kiv.eeg.gtn.data.listeners.EEGMessageListener;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.IClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.AbstractDataPreprocessor;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IDataPreprocessor;
import cz.zcu.kiv.eeg.gtn.data.providers.AbstractDataProvider;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStopMessage;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * Abstract workflow controller powers the whole application. I receives data from a data
 * provider, keeps the buffer that is filled from the data provider and collected by the preprocessor.
 * Consequently, it runs feature extraction and classification on EEGDataPackages.
 * 
 * 
 * @author lvareka
 *
 */
public abstract class AbstractWorkflowController implements IWorkflowController {
    protected List<EEGDataProcessingListener> listeners = new ArrayList<>();
    protected AbstractDataProvider dataProvider;
    protected final IBuffer buffer;
    protected final AbstractDataPreprocessor preprocessor;
    protected final List<IFeatureExtraction> featureExtractions;
    protected final IClassifier classifier;

    public AbstractWorkflowController(AbstractDataProvider dataProvider, IBuffer buffer,
                                      AbstractDataPreprocessor preprocessor, List<IFeatureExtraction> featureExtractions, IClassifier classifier) {
        if (buffer == null || classifier == null)
            throw new IllegalArgumentException("One or more arguments are null.");

        this.preprocessor = preprocessor;
        this.dataProvider = dataProvider;
        this.buffer = buffer;
        this.featureExtractions = featureExtractions;
        this.classifier = classifier;

        if (preprocessor != null)
        	preprocessor.setBuffer(buffer);
        
        if (dataProvider != null) {
            dataProvider.addEEGMessageListener(messageListener);
            dataProvider.addDataProviderListener(dataProviderListener);
        }
    }

    public AbstractDataProvider getDataProvider() {
        return dataProvider;
    }

    public IBuffer getBuffer() {
        return buffer;
    }

    public IDataPreprocessor getPreprocessor() {
        return preprocessor;
    }

    public List<IFeatureExtraction> getFeatureExtractions() {
        return featureExtractions;
    }

    public IClassifier getClassifier() {
        return classifier;
    }

    public void addListener(EEGDataProcessingListener listener){
        listeners.add(listener);
    }

    public void removeDataListener(){
        dataProvider.removeEEGMessageListener(messageListener);
    }

    /**
     * Receives and process the data from providers
     * 
     */
    private EEGMessageListener messageListener = new EEGMessageListener() {
        @Override
        public void startMessageSent(EEGStartMessage msg) {
            buffer.initialize(msg);
            start(msg);
        }

        @Override
        public void dataMessageSent(EEGDataMessage msg) {
            storeData(msg);
            processData();
        }

        @Override
        public void stopMessageSent(EEGStopMessage msg) {
            stop(msg);
        }
    };
    
    public void setDataProvider(AbstractDataProvider provider) {
    	this.dataProvider = provider;
    	if (dataProvider != null)
        	dataProvider.addEEGMessageListener(messageListener);
    }

    private DataProviderListener dataProviderListener = new DataProviderListener() {
        @Override
        public void dataReadStart() {
            onDataReadStarted();
        }

        @Override
        public void dataReadEnd() {
            onDataReadEnd();
        }

        @Override
        public void dataReadError(Exception ex) {
            onDataReadError(ex);
        }
    };

    protected void onDataReadStarted(){

    }

    protected void onDataReadError(Exception ex){

    }

    protected void onDataReadEnd(){

    }
}
