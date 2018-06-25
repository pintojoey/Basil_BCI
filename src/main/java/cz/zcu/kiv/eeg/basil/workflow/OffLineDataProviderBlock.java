package cz.zcu.kiv.eeg.basil.workflow;

import cz.zcu.kiv.WorkflowDesigner.Annotations.*;
import cz.zcu.kiv.WorkflowDesigner.Type;
import cz.zcu.kiv.eeg.basil.data.processing.VisualizationWorkflowController;
import cz.zcu.kiv.eeg.basil.data.processing.preprocessing.*;
import cz.zcu.kiv.eeg.basil.data.processing.structures.Buffer;
import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.basil.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.basil.data.providers.bva.OffLineDataProvider;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGMarker;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@BlockType(type="OffLineDataProvider", family = "DataProvider")
public class OffLineDataProviderBlock implements Serializable {

    @BlockProperty(name = "EEG File", type = Type.FILE)
    private File eegFileInput;

    @BlockOutput(name = "EEGData", type = "DataProvider")
    private List<EEGDataPackage> eegDataPackagesOutput;

    @BlockInput(name="ISegmentation", type="ISegmentation")
    private ISegmentation iSegmentation;


    @BlockExecute
    public void process(){
        ArrayList files = new ArrayList<>(1);
        files.add(eegFileInput.getAbsolutePath());
        OffLineDataProvider offLineDataProvider = new OffLineDataProvider(files);

        Buffer buffer = new Buffer();
        EpochExtraction epochExtraction = (EpochExtraction) iSegmentation;
        EEGDataPackage dataPackage = retrieve(epochExtraction,buffer);

        List<EEGDataPackage> epochs = epochExtraction.split(dataPackage);
    }

    private EEGDataPackage retrieve(EpochExtraction epochExtraction, Buffer buffer) {
        if (buffer == null)
            return null;
        List<EEGMarker> markers = buffer.getMarkers();
        if (markers.size() == 0) {
            System.err.println("No markers");
            return null;
        }

        int bufferSize = buffer.size();
        EEGMarker m;
        int postStimulus = epochExtraction.getPostStimulus();
        int samples = 0;
        for(int i = markers.size() - 1; i >= 0; i--){
            m = markers.get(i);
            if (m.getOffset() + postStimulus < buffer.size()) {
                samples = m.getOffset() + postStimulus;
            }
            if(samples <= bufferSize){
                break;
            }
        }

        if(samples == 0) return null;

        return buffer.getAndRemove(samples);
    }


}
