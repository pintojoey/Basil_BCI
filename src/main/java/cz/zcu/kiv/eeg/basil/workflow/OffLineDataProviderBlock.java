package cz.zcu.kiv.eeg.basil.workflow;

import cz.zcu.kiv.WorkflowDesigner.Annotations.*;
import cz.zcu.kiv.WorkflowDesigner.Type;
import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.basil.data.providers.metadata.GtnMetadataProvider;
import cz.zcu.kiv.signal.ChannelInfo;
import cz.zcu.kiv.signal.DataTransformer;
import cz.zcu.kiv.signal.EEGDataTransformer;
import cz.zcu.kiv.signal.EEGMarker;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteOrder;
import java.util.*;

@BlockType(type="OffLineDataProvider", family = "DataProvider")
public class OffLineDataProviderBlock implements Serializable {
    private static final String VHDR_EXTENSION = ".vhdr";
    private static final String VMRK_EXTENSION = ".vmrk";

    @BlockProperty(name = "EEG File", type = Type.FILE_ARRAY)
    private List<File> eegFileInputs;

    @BlockOutput(name = "EEGData", type = "EEGDataList")
    private EEGDataPackageList eegDataPackageList;

    @BlockExecute
    public void process() throws IOException {
        ArrayList<EEGDataPackage> eegDataList = new ArrayList<>();
        ByteOrder order = ByteOrder.LITTLE_ENDIAN;
        for(File eegFileInput:eegFileInputs){
            File vhdrFile = new File(eegFileInput.getParentFile().getAbsolutePath()+File.separator+eegFileInput.getName().split("\\.")[0]+VHDR_EXTENSION);
            File vmrkFile = new File(eegFileInput.getParentFile().getAbsolutePath()+File.separator+eegFileInput.getName().split("\\.")[0]+VMRK_EXTENSION);

            DataTransformer dt = new EEGDataTransformer();

            List<ChannelInfo> channels = dt.getChannelInfo(vhdrFile.getAbsolutePath());

            List<EEGMarker> markers = dt.readMarkerList(vmrkFile.getAbsolutePath());

            int len = channels.size();
            double[][] data = new double[len][];
            String channelNames[]=new String[len];
            for (int i = 0; i < channels.size(); i++) {
                data[i] = dt.readBinaryData(vhdrFile.getAbsolutePath(), eegFileInput.getAbsolutePath(), i + 1, order);
                channelNames[i]=channels.get(i).getName();
            }
            EEGDataPackage eegData=new EEGDataPackage();
            eegData.setData(data);
            eegData.setChannelNames(channelNames);
            Configuration configuration=new Configuration();
            configuration.setSamplingInterval(Double.parseDouble(getProperty("samplinginterval", dt)));
            eegData.setConfiguration(configuration);

            cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGMarker[] eegMarkers = new cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGMarker[markers.size()];
            int i = 0;
            for (EEGMarker m : markers) {
                eegMarkers[i] = new cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGMarker(m.getStimulus(), m.getPosition());
                i++;
            }
            eegData.setMarkers(Arrays.asList(eegMarkers));

            eegDataList.add(eegData);
        }
        eegDataPackageList=new EEGDataPackageList(eegDataList);
    }

    private String getProperty(String propName, DataTransformer dt) {
        HashMap<String, HashMap<String, String>> props = dt.getProperties();
        for (Map.Entry<String, HashMap<String, String>> entry : props.entrySet()) {
            for (Map.Entry<String, String> prop : entry.getValue().entrySet()) {
                if (prop.getKey().equalsIgnoreCase(propName)) {
                    return prop.getValue();
                }
            }
        }

        return null;
    }

    public List<File> getEegFileInputs() {
        return eegFileInputs;
    }

    public void setEegFileInputs(List<File> eegFileInputs) {
        this.eegFileInputs = eegFileInputs;
    }

    public EEGDataPackageList getEegDataPackageList() {
        return eegDataPackageList;
    }

    public void setEegDataPackageList(EEGDataPackageList eegDataPackageList) {
        this.eegDataPackageList = eegDataPackageList;
    }
}
