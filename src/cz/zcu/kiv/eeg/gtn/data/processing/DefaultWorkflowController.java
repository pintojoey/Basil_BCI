package cz.zcu.kiv.eeg.gtn.data.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.classification.IClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.AbstractDataPreprocessor;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IDataPreprocessor;
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
		IBuffer buffer = getBuffer();
		if (buffer.isFull() || buffer.size() >= getBufferMinSize()){

        }
	}

	@Override
	public void start(EEGStartMessage start) {

	}

	@Override
	public void stop(EEGStopMessage stop) {

	}

	@Override
	public void storeData(EEGDataMessage data) {
		getBuffer().add(data.getData(), Arrays.asList(data.getMarkers()));
	}

}
