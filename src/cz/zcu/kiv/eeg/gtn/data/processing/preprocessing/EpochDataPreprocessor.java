package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGMarker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tomas Prokop on 07.08.2017.
 */
public class EpochDataPreprocessor extends AbstractDataPreprocessor {

    private final int preepochPreprocessingCount;

    public EpochDataPreprocessor(List<IPreprocessing> preprocessing, IBuffer buffer, EpochExtraction epochExtraction, int preepochPreprocessingCount) {
        super(preprocessing, buffer, epochExtraction);
        this.preepochPreprocessingCount = preepochPreprocessingCount;
    }

    @Override
    public List<EEGDataPackage> preprocessData() {
        EpochExtraction epochExtraction = (EpochExtraction) segmentation;
        EEGDataPackage pack = retrieve(epochExtraction);

        if(pack == null) return null;

        for(int i = 0; i < preepochPreprocessingCount; i++){
            pack = preprocessing.get(i).preprocess(pack);
        }

        List<EEGDataPackage> epochs = epochExtraction.split(pack);
        ArrayList<EEGDataPackage> preprocessed = new ArrayList<>(epochs.size());
        for(EEGDataPackage epoch : epochs) {
            for (int i = preepochPreprocessingCount; i < preprocessing.size(); i++) {
                epoch = preprocessing.get(i).preprocess(epoch);
            }

            preprocessed.add(epoch);
        }

        return preprocessed;
    }

    private EEGDataPackage retrieve(EpochExtraction epochExtraction) {
        List<EEGMarker> markers = buffer.getMarkers();
        if(markers.size() == 0) return null;

        int bufferSize = buffer.size();
        EEGMarker m;
        int preStimulus = epochExtraction.getPreStimulus();
        int postStimulus = epochExtraction.getPostStimulus();
        int samples = 0;
        for(int i = markers.size() - 1; i >= 0; i--){
            m = markers.get(i);
            samples = m.getOffset() + postStimulus;
            if(samples >= bufferSize){
                break;
            }
        }

        if(samples == 0) return null;

        return buffer.getAndRemove(samples);
    }
}
