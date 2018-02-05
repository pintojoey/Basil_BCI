package cz.zcu.kiv.eeg.basil.data.providers.bva;

import cz.zcu.kiv.eeg.basil.data.listeners.DataProviderListener;
import cz.zcu.kiv.eeg.basil.data.listeners.EEGMessageListener;
import cz.zcu.kiv.eeg.basil.data.providers.AbstractDataProvider;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGStopMessage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.MessageType;
import cz.zcu.kiv.signal.ChannelInfo;
import cz.zcu.kiv.signal.DataTransformer;
import cz.zcu.kiv.signal.EEGDataTransformer;
import cz.zcu.kiv.signal.EEGMarker;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides off-line data recorded with the BrainVision
 * format
 * 
 * @author Tomas Prokop
 *
 */
public class OffLineDataProvider extends AbstractDataProvider {
    private static final String VHDR_EXTENSION = ".vhdr";
	private static final String VMRK_EXTENSION = ".vmrk";
	private static final String EEG_EXTENSION  = ".eeg";

	private String vhdrFile;
    private String vmrkFile;
    private String eegFile;
    private Map<String, Integer> files;
    private boolean running;

    public OffLineDataProvider(File eegFile) {
        files = new HashMap<>();
        files.put(eegFile.getAbsolutePath(), -1);
    }

    public OffLineDataProvider(Map<String, Integer> files) {
        this.files = files;
    }

    private void setFileName(String filename) {
        int index = filename.lastIndexOf(".");
        String baseName = filename.substring(0, index);
        this.vhdrFile = baseName + VHDR_EXTENSION;
        this.vmrkFile = baseName + VMRK_EXTENSION;
        this.eegFile = baseName +  EEG_EXTENSION;
    }

    @Override
    public void run() {
        this.running = true;
        try {
            synchronized (super.dataProviderListeners) {
                for (DataProviderListener ls : super.dataProviderListeners) {
                    ls.dataReadStart();
                }
            }

            int cnt = 0;
            for (Map.Entry<String, Integer> fileEntry : files.entrySet()) {
                if (!running)
                    break;

                DataTransformer dt = new EEGDataTransformer();
                setFileName(fileEntry.getKey());
                File file = new File(fileEntry.getKey());
                if (!file.exists()) {
                    System.out.println(file.getAbsolutePath() + " does not exist!");
                    continue;
                }

                //Create start msg
                EEGStartMessage start;
                if(metadataProvider != null){
                    metadataProvider.setFileName(vhdrFile);
                    start = getMetadataProvider().loadMetadata(getMessageId());
                    availableChannels = start.getAvailableChannels();
                    samplingRate = (int)start.getSampling();
                }else {
                   start = createStartMessage(dt, vhdrFile, cnt);
                }
                cnt++;
                synchronized (super.eegMessageListeners) {
                    for (EEGMessageListener ls : super.eegMessageListeners) {
                        ls.startMessageSent(start);
                    }
                }

                ByteOrder order = ByteOrder.LITTLE_ENDIAN;
                int len = start.getChannelCount();
                double[][] data = new double[len][];
                for (int i = 0; i < len; i++) {
                    data[i] = dt.readBinaryData(vhdrFile, eegFile, i + 1, order);;
                }

                List<EEGMarker> markers = dt.readMarkerList(vmrkFile);
                cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGMarker[] eegMarkers = new cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGMarker[markers.size()];
                int i = 0;
                int target = fileEntry.getValue();
                for (EEGMarker m : markers) {
                    eegMarkers[i] = new cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGMarker(m.getStimulus(), m.getPosition());
                    eegMarkers[i].setExpectedClass(target);
                    i++;
                }

                EEGDataMessage dataMsg = new EEGDataMessage(MessageType.DATA, 1, eegMarkers, data);
                synchronized (super.eegMessageListeners) {
                    for (EEGMessageListener ls : super.eegMessageListeners) {
                        ls.dataMessageSent(dataMsg);
                    }
                }

                EEGStopMessage stop = new EEGStopMessage(MessageType.END, cnt);
                synchronized (super.eegMessageListeners) {
                    for (EEGMessageListener ls : super.eegMessageListeners) {
                        ls.stopMessageSent(stop);
                    }
                }
                cnt++;
            }

        } catch (IOException e) {
            synchronized (super.dataProviderListeners) {
                for (DataProviderListener ls : super.dataProviderListeners) {
                    ls.dataReadError(e);
                }
            }
        } finally {
            running = false;

            synchronized (super.dataProviderListeners) {
                for (DataProviderListener ls : super.dataProviderListeners) {
                    ls.dataReadEnd();
                }
            }
        }
    }


    private EEGStartMessage createStartMessage(DataTransformer dt, String vhdrFile, int cnt) throws IOException {
        List<ChannelInfo> channels = dt.getChannelInfo(vhdrFile);
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
        this.samplingRate = sampling;

        super.availableChannels = chNames;
        EEGStartMessage msg = new EEGStartMessage(cnt, chNames, resolutions, sampling);
        msg.setDataFileName(val);

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

    @Override
    public void stop() {
        this.running = false;
    }
}
