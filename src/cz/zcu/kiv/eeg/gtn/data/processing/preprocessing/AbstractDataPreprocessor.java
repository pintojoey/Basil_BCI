package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import java.util.List;

/**
 * Created by Tomas Prokop on 04.07.2017.
 */
public abstract class AbstractDataPreprocessor {

    protected  boolean running = false;

    protected final IEpochExtraction epochExtraction;

    protected final List<IPreprocessing> preprocessing;

    public AbstractDataPreprocessor(IEpochExtraction epochExtraction, List<IPreprocessing> preprocessing) {
    	this.epochExtraction = epochExtraction;
    	this.preprocessing = preprocessing;
    }
       

    public void preprocessAllData(EEGDataPackage data){
         
        if (data == null)
        	return;

        List<EEGDataPackage> epochs = null;
        if (this.epochExtraction != null) {
        	epochs = this.epochExtraction.extractEpochs(data);
        	for (EEGDataPackage epoch: epochs) {
        		processData(epoch);
        	}
        } else {
        	processData(data);
        }

    }

	protected void processData(EEGDataPackage data) {
		for (IPreprocessing prep : preprocessing){
			data = prep.preprocess(data);
	    }

	    
	}
}
