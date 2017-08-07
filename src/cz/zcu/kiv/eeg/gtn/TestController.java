package cz.zcu.kiv.eeg.gtn;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.AbstractDataPreprocessor;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.ERPDataPreprocessor;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.EpochExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IDataPreprocessor;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IEpochExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IPreprocessing;
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
	    IEpochExtraction epochExtraction = new EpochExtraction(0.1, 1);
	    List<IPreprocessing> preprocessing = new ArrayList<IPreprocessing>();
	    preprocessing.add(new BaselineCorrection(0, 0.1, provider.getSamplingRate()));
	    AbstractDataPreprocessor dataPreprocessor = new ERPDataPreprocessor(epochExtraction, preprocessing);
	    
	    // feature extraction
	    List<IFeatureExtraction> featureExtraction = null;
	    IClassifier classification       = null;
	    
	    // controller
	    IWorkflowController workFlowController = new DefaultWorkflowController(provider, buffer, dataPreprocessor, featureExtraction, classification);
	    
	    
	}
    
    
}
