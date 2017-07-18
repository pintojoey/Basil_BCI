package cz.zcu.kiv.eeg.gtn.data.providers.bva;

import java.util.Observer;

import cz.zcu.kiv.eeg.gtn.data.providers.AbstractDataProvider;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.RDA.RDA_Marker;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.*;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.online.DataTokenizer;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.online.TCPIPClient;
import org.apache.log4j.Logger;

import cz.zcu.kiv.eeg.gtn.data.providers.bva.RDA.RDA_MessageData;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.RDA.RDA_MessageStart;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.RDA.RDA_MessageStop;

public class OnLineDataProvider extends AbstractDataProvider {

    private final Logger logger = Logger.getLogger(OnLineDataProvider.class);
    private final TCPIPClient client;
    private final DataTokenizer dtk;
    private final Observer obs;

    private int channelCnt = 0;
    private boolean isRunning;

    public OnLineDataProvider(String ipAddress, int port, Observer obs) throws Exception {
        super();
        this.obs = obs;
        client = new TCPIPClient(ipAddress, port);
        client.start();
        dtk = new DataTokenizer(client);
        dtk.start();
        isRunning = true;
    }

    @Override
    public void run() {
        if (obs != null)
            addObserver(obs);

        boolean stopped = false;
        EEGMessage msg = null;
        int count = 0;

        while (isRunning) {
            Object o = dtk.retrieveDataBlock();
            if (o instanceof RDA_MessageData) {
                RDA_MessageData rda = (RDA_MessageData) o;
                float[][] data = new float[channelCnt][(int) rda.getnPoints()];
                EEGMarker[] markers = new EEGMarker[(int) rda.getnMarkers()];

                float[] rdaDta = rda.getfData();
                int pts = (int) rda.getnPoints();
                for (int i = 0; i < channelCnt; i++) {
                    System.arraycopy(rdaDta, i * pts, data[i], 0, pts);
                }

                int i = 0;
                if (rda.getMarkers() != null) {
                    for (RDA_Marker m : rda.getMarkers()) {
                        markers[i] = new EEGMarker(m.getsTypeDesc(), (int) m.getnPosition());
                        i++;
                    }
                }

                msg = new EEGDataMessage(MessageType.DATA, count, markers, data);
            } else if (o instanceof RDA_MessageStart) {
                RDA_MessageStart rda = (RDA_MessageStart) o;
                String[] chNames = rda.getsChannelNames();
                super.setAvailableChannels(chNames);
                channelCnt = chNames.length;
                msg = new EEGStartMessage(MessageType.START, count, chNames, rda.getdResolutions(), (int) rda.getnChannels(), rda.getdSamplingInterval());
            } else if (o instanceof RDA_MessageStop) {
                msg = new EEGStopMessage(MessageType.DATA, count);
                client.requestStop();
                dtk.requestStop();
                isRunning = false;
                stopped = true;
            }

            if (msg != null) {
                count++;
                this.setChanged();
                this.notifyObservers(msg);
            }
        }

        if (!stopped) {
            client.requestStop();
            dtk.requestStop();
        }

        logger.info("Experiment has ended.");
    }

    @Override
    public synchronized void stop() {
        isRunning = false;
    }

}
