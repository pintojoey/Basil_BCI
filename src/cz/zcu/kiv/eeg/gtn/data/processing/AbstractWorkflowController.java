package cz.zcu.kiv.eeg.gtn.data.processing;

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

    protected final AbstractDataProvider dataProvider;

    protected final IBuffer buffer;

    protected final AbstractDataPreprocessor preprocessor;

    protected final List<IFeatureExtraction> featureExtractions;

    protected final IClassifier classifier;

    protected int bufferMinSize;

    protected EEGStartMessage metadata;

    public AbstractWorkflowController(AbstractDataProvider dataProvider, IBuffer buffer,
                                      AbstractDataPreprocessor preprocessor, List<IFeatureExtraction> featureExtractions, IClassifier classifier) {
        if(dataProvider == null || buffer == null || classifier == null)
            throw new IllegalArgumentException("One or more arguments are null.");

        this.preprocessor = preprocessor;
        this.dataProvider = dataProvider;
        this.buffer = buffer;
        this.featureExtractions = featureExtractions;
        this.classifier = classifier;

        dataProvider.addListener(dataListener);
        dataProvider.run();
    }

    public EEGStartMessage getMetadata() {
        return metadata;
    }

    public void setMetadata(EEGStartMessage metadata) {
        this.metadata = metadata;
    }

    public int getBufferMinSize() {
        return bufferMinSize;
    }

    public void setBufferMinSize(int bufferMinSize) {
        this.bufferMinSize = bufferMinSize;
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

    /**
     * Receives and process the data from providers
     * 
     */
    private EEGMessageListener dataListener = new EEGMessageListener() {
        @Override
        public void startMessageSent(EEGStartMessage msg) {
            metadata = msg;
            preprocessor.setMetadata(msg);
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
}
