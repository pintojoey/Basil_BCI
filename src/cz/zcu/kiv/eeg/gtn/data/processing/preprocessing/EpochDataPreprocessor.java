package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.IBuffer;

import java.util.List;

/**
 * Created by Tomas Prokop on 07.08.2017.
 */
public class EpochDataPreprocessor extends AbstractDataPreprocessor {

    private final IEpochExtraction epochExtraction;

    public EpochDataPreprocessor(List<IPreprocessing> preprocessing, IBuffer buffer, IEpochExtraction epochExtraction) {
        super(preprocessing, buffer);
        this.epochExtraction = epochExtraction;
    }

    @Override
    public List<EEGDataPackage> preprocessData() {
        return null;
    }
}
