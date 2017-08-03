package cz.zcu.kiv.eeg.gtn.data.processing;

import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.classification.IClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IDataPreprocessor;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.gtn.data.providers.AbstractDataProvider;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStopMessage;

public class DefaultWorkflowController extends AbstractWorkflowController {

	public DefaultWorkflowController(AbstractDataProvider dataProvider, IBuffer buffer, IDataPreprocessor preprocessor,
			List<IFeatureExtraction> featureExtractions, IClassifier classifier) {
		super(dataProvider, buffer, preprocessor, featureExtractions, classifier);
	}

	@Override
	public void processData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start(EEGStartMessage start) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop(EEGStopMessage stop) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void storeData(EEGDataMessage data) {
		// TODO Auto-generated method stub
		
	}

}
