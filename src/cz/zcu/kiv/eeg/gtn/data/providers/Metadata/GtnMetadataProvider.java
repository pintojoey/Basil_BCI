package cz.zcu.kiv.eeg.gtn.data.providers.Metadata;

import cz.zcu.kiv.eeg.gtn.data.providers.IMetadataProvider;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.gtn.utils.FileUtils;
import cz.zcu.kiv.signal.ChannelInfo;
import cz.zcu.kiv.signal.DataTransformer;
import cz.zcu.kiv.signal.EEGDataTransformer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Tomas Prokop on 30.01.2018.
 */
public class GtnMetadataProvider implements IMetadataProvider {

    private String fileName;
    private final String infoFileName;
    private Map<String, Integer> expectedResults = null;

    public GtnMetadataProvider(String infoFileName) {
        this.infoFileName = infoFileName;
    }

    @Override
    public EEGStartMessage loadMetadata(int msgId) {
        try {
            if(expectedResults == null){
                expectedResults = FileUtils.loadExpectedResults(infoFileName);
            }

            return createStartMessage(new EEGDataTransformer(), msgId);
        } catch (IOException e) {
            //TODO log error
        }
        return  null;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private EEGStartMessage createStartMessage(DataTransformer dt, int msgId) throws IOException {
        List<ChannelInfo> channels = dt.getChannelInfo(fileName);
        String[] chNames = new String[channels.size()];
        double[] resolutions = new double[chNames.length];
        int i = 0;
        for (ChannelInfo channel : channels) {
            chNames[i] = channel.getName();
            resolutions[i] = channel.getResolution();
            i++;
        }

        int sampling;
        String val = getProperty("samplinginterval", dt);
        sampling = Integer.parseInt(val);
        val = getProperty("DataFile", dt);

        EEGStartMessage msg = new EEGStartMessage(msgId, chNames, resolutions, sampling);

        String eegFile = fileName.replaceAll(".vhdr", ".eeg");
        if(!eegFile.contains(val)) {
            eegFile = val;
        }

        msg.setDataFileName(eegFile);

        if(expectedResults.containsKey(eegFile)){
            msg.setTargetMarker("S  " + expectedResults.get(eegFile));
        }

        return msg;
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
}
