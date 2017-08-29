package cz.zcu.kiv.eeg.gtn;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.*;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms.BaselineCorrection;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.Buffer;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.OffLineDataProvider;
import cz.zcu.kiv.eeg.gtn.data.processing.DefaultWorkflowController;
import cz.zcu.kiv.eeg.gtn.data.processing.IWorkflowController;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.IClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.MLPClassifier;

public class TestController {
	
	public static void main(String[] args) {
		// data provider
		File f = new File("data/numbers/17ZS/17ZS_14_4_2015_02.vhdr");
	    OffLineDataProvider provider = new OffLineDataProvider(f);
	    
	    // buffer
	    IBuffer buffer = new Buffer();
	    
	    // preprocessings
	    ISegmentation epochExtraction = new EpochExtraction(100, 1000);
	    List<IPreprocessing> preprocessing = new ArrayList<IPreprocessing>();
		List<IPreprocessing> prepreprocessing = new ArrayList<IPreprocessing>();
	    preprocessing.add(new BaselineCorrection(0, 100, provider.getSamplingRate()));
	    AbstractDataPreprocessor dataPreprocessor = new EpochDataPreprocessor(preprocessing, prepreprocessing, buffer, epochExtraction);
	    
	    // feature extraction
	    List<IFeatureExtraction> featureExtraction = new ArrayList<IFeatureExtraction>();
	    IClassifier classification       		   = new MLPClassifier();
	    
	    // controller
	    IWorkflowController workFlowController = new DefaultWorkflowController(provider, buffer, dataPreprocessor, featureExtraction, classification);

	    	Thread t = new Thread(provider);
	    	t.start();
	}
    
    
}
