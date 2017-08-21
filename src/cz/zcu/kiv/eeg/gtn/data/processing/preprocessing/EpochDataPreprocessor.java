package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGMarker;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Pre-processing for ERP data: splits EEG into ERP trials
 * using stimuli markers
 * 
 * Created by Tomas Prokop on 07.08.2017.
 * 
 */
public class EpochDataPreprocessor extends AbstractDataPreprocessor {

    public EpochDataPreprocessor(List<IPreprocessing> preprocessings, List<IPreprocessing> preSegmentationPreprocessings, IBuffer buffer, ISegmentation segmentation) {
        super(preprocessings, preSegmentationPreprocessings, buffer, segmentation);
    }

    @Override
    public List<EEGDataPackage> preprocessData() {
        EpochExtraction epochExtraction = (EpochExtraction) segmentation;
        EEGDataPackage pack = retrieve(epochExtraction);

        if (pack == null) return null;

        for(IPreprocessing p : preSegmentationPreprocessings){
            pack = p.preprocess(pack);
        }

        List<EEGDataPackage> epochs = epochExtraction.split(pack);
        ArrayList<EEGDataPackage> preprocessed = new ArrayList<>(epochs.size());
        for(EEGDataPackage epoch : epochs) {
            for(IPreprocessing p : preprocessings){
                epoch = p.preprocess(epoch);
            }

            preprocessed.add(epoch);
        }

        return preprocessed;
    }

    private EEGDataPackage retrieve(EpochExtraction epochExtraction) {
        List<EEGMarker> markers = buffer.getMarkers();
        if (markers.size() == 0) return null;

        int bufferSize = buffer.size();
        EEGMarker m;
        int postStimulus = epochExtraction.getPostStimulus();
        int samples = 0;
        for(int i = markers.size() - 1; i >= 0; i--){
            m = markers.get(i);
            samples = m.getOffset() + postStimulus;
            if(samples <= bufferSize){
                break;
            }
        }

        if(samples == 0) return null;

        return buffer.getAndRemove(samples);
    }
}
