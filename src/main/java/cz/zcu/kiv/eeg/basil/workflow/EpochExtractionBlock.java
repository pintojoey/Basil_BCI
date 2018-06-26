package cz.zcu.kiv.eeg.basil.workflow;

import cz.zcu.kiv.WorkflowDesigner.Annotations.*;
import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGMarker;
import cz.zcu.kiv.signal.ChannelInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cz.zcu.kiv.WorkflowDesigner.Type.NUMBER;
import static cz.zcu.kiv.WorkflowDesigner.Type.STRING_ARRAY;

@BlockType(type="EpochExtraction",family = "Preprocessing")
public class EpochExtractionBlock {

    @BlockProperty(name="PreStimulus onset",type=NUMBER, defaultValue = "0")
    private int preStimulus;  /* time before the stimulus onset in ms */

    @BlockProperty(name="PostStimulus onset",type=NUMBER, defaultValue = "0")
    private int postStimulus; /* time after the stimulus onset in ms */

    @BlockInput(name = "EEGData", type = "EEGData[]")
    private List<EEGDataPackage> eegDataList;

    @BlockOutput(name = "Epochs", type = "EEGData[]")
    private List<EEGDataPackage> epochs;


    @BlockExecute
    public void process(){

        epochs = new ArrayList<>();
        for(EEGDataPackage eegData:eegDataList){

            List<EEGMarker> markers = eegData.getMarkers();
            double[][]        data  = eegData.getData();
            double sampling = eegData.getConfiguration().getSamplingInterval();

            for (EEGMarker currentMarker: markers) {
                int startSample =  - (int)((0.001 * this.preStimulus) /* time in s */ * sampling);
                int endSample = (int) ((0.001 * this.postStimulus) /* time in s */ * sampling);
                int offset = currentMarker.getOffset();
                double[][] epochData = new double[data.length][endSample - startSample];

                if (offset + startSample < 0) {
                    System.err.println("Epoch outside of the expected range");
                    continue; /* epoch prestimulus offset outside of the range */
                }
                for (int i = 0; i < data.length; i++) {
                    System.arraycopy(data[i], offset + startSample , epochData[i], 0, endSample - startSample);
                }
                epochs.add(new EEGDataPackage(epochData, Arrays.asList(currentMarker), eegData.getChannelNames()));
            }
        }

    }

    public int getPreStimulus() {
        return preStimulus;
    }

    public void setPreStimulus(int preStimulus) {
        this.preStimulus = preStimulus;
    }

    public int getPostStimulus() {
        return postStimulus;
    }

    public void setPostStimulus(int postStimulus) {
        this.postStimulus = postStimulus;
    }

    public List<EEGDataPackage> getEegDataList() {
        return eegDataList;
    }

    public void setEegDataList(List<EEGDataPackage> eegDataList) {
        this.eegDataList = eegDataList;
    }

    public List<EEGDataPackage> getEpochs() {
        return epochs;
    }

    public void setEpochs(List<EEGDataPackage> epochs) {
        this.epochs = epochs;
    }
}
