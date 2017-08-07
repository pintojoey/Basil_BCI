package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.IBuffer;

import java.util.List;

/**
 * Created by Tomas Prokop on 04.07.2017.
 */
public abstract class AbstractDataPreprocessor implements IDataPreprocessor {

    protected final List<IPreprocessing> preprocessing;

    protected final IBuffer buffer;

    public AbstractDataPreprocessor(List<IPreprocessing> preprocessing, IBuffer buffer) {
    	this.preprocessing = preprocessing;
		this.buffer = buffer;
	}

	public List<IPreprocessing> getPreprocessing() {
		return preprocessing;
	}

	public IBuffer getBuffer() {
		return buffer;
	}
}
