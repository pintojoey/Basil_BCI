package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;

import java.util.List;

/**
 * Created by Tomas Prokop on 04.07.2017.
 */
public abstract class AbstractDataPreprocessor implements IDataPreprocessor {

    protected final List<IPreprocessing> preprocessing;

    protected final IBuffer buffer;

    protected final ISegmentation segmentation;

    protected EEGStartMessage metadata;

    public AbstractDataPreprocessor(List<IPreprocessing> preprocessing, IBuffer buffer, ISegmentation segmentation) {
    	this.preprocessing = preprocessing;
		this.buffer = buffer;
		this.segmentation = segmentation;
	}

	public EEGStartMessage getMetadata() {
		return metadata;
	}

	@Override
	public void setMetadata(EEGStartMessage metadata) {
    	segmentation.setSampling((int)metadata.getSampling());
		this.metadata = metadata;
	}

	public List<IPreprocessing> getPreprocessing() {
		return preprocessing;
	}

	public IBuffer getBuffer() {
		return buffer;
	}
}
