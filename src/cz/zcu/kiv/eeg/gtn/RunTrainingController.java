package cz.zcu.kiv.eeg.gtn;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.TestingWorkflowController;
import cz.zcu.kiv.eeg.gtn.data.processing.IWorkflowController;
import cz.zcu.kiv.eeg.gtn.data.processing.TrainWorkflowController;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.ErpTrainCondition;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.IClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.ITrainCondition;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.KNNClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.MLPClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.SDADeepLearning4jClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.WaveletTransformFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.AbstractDataPreprocessor;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.Averaging;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.EpochDataPreprocessor;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.EpochExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IPreprocessing;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.ISegmentation;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms.BandpassFilter;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms.BaselineCorrection;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.Buffer;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.OffLineDataProvider;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGMarker;


/**
 * 
 * This main class is used purely for testing the functionality of a workflow
 * that will be used for classifier training.
 * 
 * @author lvareka
 *
 */
public class RunTrainingController {

	public static void main(String[] args) {
		// data provider
		File f = new File("data/P300/LED_28_06_2012_104.vhdr");
		OffLineDataProvider provider = new OffLineDataProvider(f);
			   
			    
		// buffer
		IBuffer buffer = new Buffer();
			    
		// preprocessings
		int samplingFq = 1000; // TODO: get correctly from the data provider
		ISegmentation epochExtraction = new EpochExtraction(100, 1000);
		List<IPreprocessing> preprocessing = new ArrayList<IPreprocessing>();
		List<IPreprocessing> prepreprocessing = new ArrayList<IPreprocessing>();
		preprocessing.add(new BaselineCorrection(0, 100));
		prepreprocessing.add(new BandpassFilter(0.1, 8));
		
		AbstractDataPreprocessor dataPreprocessor = new EpochDataPreprocessor(preprocessing, prepreprocessing, null, buffer, epochExtraction);
			    
		// feature extraction
		List<IFeatureExtraction> featureExtraction = new ArrayList<IFeatureExtraction>();
		IFeatureExtraction fe = new WaveletTransformFeatureExtraction();
		featureExtraction.add(fe);
		
		// classification
		IClassifier classification       		   = new SDADeepLearning4jClassifier(fe.getFeatureDimension());
			    
		ITrainCondition trainCondition = new ErpTrainCondition();
		
		// controller
		IWorkflowController workFlowController = new TrainWorkflowController(provider, buffer, dataPreprocessor, featureExtraction, classification, trainCondition);
			   
		// run data provider thread
		Thread t = new Thread(provider);
		t.setName("DataProviderThread");
		t.start();
	}

}
