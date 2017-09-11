package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGMarker;
import cz.zcu.kiv.eeg.gtn.gui.ShowChart;

import java.util.ArrayList;
import java.util.Arrays;
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
	private Averaging averaging;

    public EpochDataPreprocessor(List<IPreprocessing> preprocessings, List<IPreprocessing> preSegmentationPreprocessings, Averaging averaging, IBuffer buffer, ISegmentation segmentation) {
        super(preprocessings, preSegmentationPreprocessings, buffer, segmentation);
        this.averaging = averaging;
    }

    @Override
    public List<EEGDataPackage> preprocessData() {
        EpochExtraction epochExtraction = (EpochExtraction) segmentation;
        EEGDataPackage dataPackage = retrieve(epochExtraction);

        if (dataPackage == null) return null;

        for (IPreprocessing p : preSegmentationPreprocessings) {
            dataPackage = p.preprocess(dataPackage);
        }

        List<EEGDataPackage> epochs = epochExtraction.split(dataPackage);

        System.out.println("Created " + epochs.size() + " epochs");

        ArrayList<EEGDataPackage> preprocessed = new ArrayList<>(epochs.size());
        for (EEGDataPackage epoch : epochs) {
            for (IPreprocessing p : preprocessings) {
                epoch = p.preprocess(epoch);
            }

            preprocessed.add(epoch);
        }
        
        EEGDataPackage average = averaging.average(preprocessed);
       
        // just for testing purposes
        System.out.println(average);
        ShowChart showCharts = new ShowChart("Target epoch averages, all channels");
        showCharts.update(average.getData(), null); // TODO: provide channel names

        return Arrays.asList(average);
    }

    private EEGDataPackage retrieve(EpochExtraction epochExtraction) {
        List<EEGMarker> markers = buffer.getMarkers();
        if (markers.size() == 0) {
            System.out.println("No markers");
            return null;
        }

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
