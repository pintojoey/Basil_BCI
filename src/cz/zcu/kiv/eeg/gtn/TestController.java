package cz.zcu.kiv.eeg.gtn;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.*;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms.BandpassFilter;
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
		File f = new File("data/P300/LED_28_06_2012_104.vhdr");
	    OffLineDataProvider provider = new OffLineDataProvider(f);
	   
	    
	    // buffer
	    IBuffer buffer = new Buffer();
	    
	    // preprocessings
	    ISegmentation epochExtraction = new EpochExtraction(100, 1000);
	    List<IPreprocessing> preprocessing = new ArrayList<IPreprocessing>();
		List<IPreprocessing> prepreprocessing = new ArrayList<IPreprocessing>();
	    preprocessing.add(new BaselineCorrection(0, 100, 1000));
	    prepreprocessing.add(new BandpassFilter(0.1, 8, 1000));
	    AbstractDataPreprocessor dataPreprocessor = new EpochDataPreprocessor(preprocessing, prepreprocessing, buffer, epochExtraction);
	    
	    // feature extraction
	    List<IFeatureExtraction> featureExtraction = new ArrayList<IFeatureExtraction>();
	    IClassifier classification       		   = new MLPClassifier();
	    
	    // controller
	    IWorkflowController workFlowController = new DefaultWorkflowController(provider, buffer, dataPreprocessor, featureExtraction, classification);
	   
	    // run data provider thread
	    Thread t = new Thread(provider);
	    t.start();
	   
	   
	   
	}
    
    
}
