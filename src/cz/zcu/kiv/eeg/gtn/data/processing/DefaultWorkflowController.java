package cz.zcu.kiv.eeg.gtn.data.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.classification.IClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.AbstractDataPreprocessor;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IDataPreprocessor;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.gtn.data.providers.AbstractDataProvider;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGMarker;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStopMessage;

public class DefaultWorkflowController extends AbstractWorkflowController {

	public DefaultWorkflowController(AbstractDataProvider dataProvider, IBuffer buffer, AbstractDataPreprocessor preprocessor,
			List<IFeatureExtraction> featureExtractions, IClassifier classifier) {
		super(dataProvider, buffer, preprocessor, featureExtractions, classifier);
	}

	@Override
	public void processData() {
		System.out.println("Buffer size: " + buffer.size());
		if (buffer.isFull() || buffer.size() >= getBufferMinSize()){
            List<EEGDataPackage> packs =  preprocessor.preprocessData();
            if (packs == null || packs.size() == 0) return;
            

            for (EEGDataPackage pack : packs) {

            }
        }
        	
        
	}

	// TODO
	@Override
	public void start(EEGStartMessage start) {
		System.out.println("Starting: " + start);

	}

	// TODO
	@Override
	public void stop(EEGStopMessage stop) {
		System.out.println("Stopping: " + stop);

	}

	@Override
	public void storeData(EEGDataMessage data) {
		//System.out.println("Adding: " + data);
		buffer.add(data.getData(), Arrays.asList(data.getMarkers()));
	}

}
