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

public class TestController {
	
	public static void main(String[] args) {
		// data provider
		File f = new File("data/numbers/17ZS/17ZS_14_4_2015_02.vhdr");
	    OffLineDataProvider provider = new OffLineDataProvider(f);
	    
	    // buffer
	    IBuffer buffer = new Buffer();
	    
	    // preprocessing
		EpochExtraction epochExtraction = new EpochExtraction(100, 1000);
	    List<IPreprocessing> preprocessing = new ArrayList<IPreprocessing>();
	    preprocessing.add(new BaselineCorrection(0, 0.1, provider.getSamplingRate()));
	    AbstractDataPreprocessor dataPreprocessor = new EpochDataPreprocessor(preprocessing, buffer, epochExtraction, 0);
	    
	    // feature extraction
	    List<IFeatureExtraction> featureExtraction = null;
	    IClassifier classification       = null;
	    
	    // controller
	    IWorkflowController workFlowController = new DefaultWorkflowController(provider, buffer, dataPreprocessor, featureExtraction, classification);
	    
	    
	}
    
    
}