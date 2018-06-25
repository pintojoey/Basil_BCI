package cz.zcu.kiv.eeg.basil.workflow;

import cz.zcu.kiv.WorkflowDesigner.Annotations.*;
import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;
import java.util.List;
import static cz.zcu.kiv.WorkflowDesigner.Type.STRING_ARRAY;

@BlockType(type="ChannelSelection",family = "Preprocessing")
public class ChannelSelectionBlock {

    @BlockProperty(name="channels",type = STRING_ARRAY)
    private List<String> selectedChannels;

    @BlockInput(name = "EEGData", type = "DataProvider")
    private List<EEGDataPackage> eegDataPackagesInput;

    @BlockOutput(name = "EEGData", type = "DataProvider")
    private List<EEGDataPackage> eegDataPackagesOutput;

    @BlockExecute
    private void process(){

    }

}
