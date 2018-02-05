package cz.zcu.kiv.eeg.basil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import cz.zcu.kiv.eeg.basil.data.processing.IWorkflowController;
import cz.zcu.kiv.eeg.basil.data.processing.TrainWorkflowController;
import cz.zcu.kiv.eeg.basil.data.processing.classification.ErpTrainCondition;
import cz.zcu.kiv.eeg.basil.data.processing.classification.IClassifier;
import cz.zcu.kiv.eeg.basil.data.processing.classification.ITrainCondition;
import cz.zcu.kiv.eeg.basil.data.processing.classification.SDADeepLearning4jClassifier;
import cz.zcu.kiv.eeg.basil.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.basil.data.processing.featureExtraction.WaveletTransformFeatureExtraction;
import cz.zcu.kiv.eeg.basil.data.processing.preprocessing.*;
import cz.zcu.kiv.eeg.basil.data.processing.preprocessing.algorithms.BandpassFilter;
import cz.zcu.kiv.eeg.basil.data.processing.preprocessing.algorithms.BaselineCorrection;
import cz.zcu.kiv.eeg.basil.data.processing.preprocessing.algorithms.ChannelSelection;
import cz.zcu.kiv.eeg.basil.data.processing.structures.Buffer;
import cz.zcu.kiv.eeg.basil.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.basil.data.providers.bva.OffLineDataProvider;
import cz.zcu.kiv.eeg.basil.utils.FileUtils;


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
		//File f = new File("data/P300/LED_28_06_2012_104.vhdr");
		//OffLineDataProvider provider = new OffLineDataProvider(f);
		OffLineDataProvider provider = null;
		try {
			List<String> lst = new ArrayList<>(FileUtils.loadExpectedResults("data/numbers", "infoTrain.txt").keySet());
			provider = new OffLineDataProvider(lst);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// buffer
		IBuffer buffer = new Buffer();
			    
		// preprocessings
		ISegmentation epochExtraction = new EpochExtraction(100, 1000);
		List<IPreprocessing> preprocessing = new ArrayList<>();
		List<IPreprocessing> prepreprocessing = new ArrayList<>();
		preprocessing.add(new BaselineCorrection(0, 100));
		prepreprocessing.add(new BandpassFilter(0.1, 8));
		prepreprocessing.add(new ChannelSelection(new String[] {"Fz", "Cz", "Pz"}));
		//prepreprocessing.add(new ChannelSelectionPointers(Arrays.asList(16, 17, 18)));
		
		AbstractDataPreprocessor dataPreprocessor = new EpochDataPreprocessor(preprocessing, prepreprocessing, null, epochExtraction);
			    
		// feature extraction
		List<IFeatureExtraction> featureExtraction = new ArrayList<>();
		IFeatureExtraction fe = new WaveletTransformFeatureExtraction();
		featureExtraction.add(fe);
		
		// classification
		IClassifier classification       		   = new SDADeepLearning4jClassifier();
			    
		ITrainCondition trainCondition = new ErpTrainCondition();
		
		// controller
		IWorkflowController workFlowController = new TrainWorkflowController(provider, buffer, dataPreprocessor, featureExtraction, classification, trainCondition);
		
		// run data provider thread
		Thread t = new Thread(provider);
		t.setName("DataProviderThread");
		t.start();
		
		try {
			t.join();
			System.out.println("Saving the classifier");
			String timeStamp = new SimpleDateFormat("yyyyMMddHHmm'.zip'").format(new Date());
			classification.save("data/classifiers/save" + timeStamp);
			System.out.println("Remaining buffer size: "       + buffer.size());
			System.out.println("Remaining number of markers: " + buffer.getMarkersSize());
		
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
