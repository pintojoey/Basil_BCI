package cz.zcu.kiv.eeg.basil.data.processing.preprocessing;

import java.util.List;

import cz.zcu.kiv.eeg.basil.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGStartMessage;

/**
 * 
 * Introduces common code for EEG data pre-processing
 * (consisting of segmentation and subsequent pre-processing methods) 
 *
 * Created by Tomas Prokop on 04.07.2017.
 * 
 */
public abstract class AbstractDataPreprocessor implements IDataPreprocessor {

    protected final List<IPreprocessing> preprocessings;

    protected final List<IPreprocessing> preSegmentationPreprocessings;

    protected final ISegmentation segmentation;
    
    protected IBuffer buffer;

    /**
     * @param preprocessings methods for preprocessings (such as frequency filtering)
     * @param preSegmentationPreprocessings
	 * @param segmentation method for segmentation or epoch extraction
	 */
    public AbstractDataPreprocessor(List<IPreprocessing> preprocessings, List<IPreprocessing> preSegmentationPreprocessings, ISegmentation segmentation) {
    	this.preprocessings = preprocessings;
		this.preSegmentationPreprocessings = preSegmentationPreprocessings;
		this.segmentation = segmentation;
	}

	public List<IPreprocessing> getPreprocessings() {
		return preprocessings;
	}

	public ISegmentation getSegmentation() {
		return segmentation;
	}

	public List<IPreprocessing> getPreSegmentationPreprocessings() {

		return preSegmentationPreprocessings;
	}
	
	public void setBuffer(IBuffer buffer) {
		this.buffer = buffer;
	}

	
	
	
}
