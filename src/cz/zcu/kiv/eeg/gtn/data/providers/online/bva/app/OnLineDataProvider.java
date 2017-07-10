package cz.zcu.kiv.eeg.gtn.data.providers.online.bva.app;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import cz.zcu.kiv.eeg.gtn.data.processing.math.IArtifactDetection;
import cz.zcu.kiv.eeg.gtn.data.providers.online.bva.DataTokenizer;
import cz.zcu.kiv.eeg.gtn.data.providers.online.bva.TCPIPClient;
import cz.zcu.kiv.eeg.gtn.data.providers.online.bva.objects.RDA_Marker;
import cz.zcu.kiv.eeg.gtn.data.providers.online.bva.objects.RDA_MessageData;
import cz.zcu.kiv.eeg.gtn.data.providers.online.bva.objects.RDA_MessageStart;
import cz.zcu.kiv.eeg.gtn.data.providers.online.bva.objects.RDA_MessageStop;
import cz.zcu.kiv.eeg.gtn.online.gui.MainFrame;
import cz.zcu.kiv.eeg.gtn.utils.Const;

public class OnLineDataProvider extends Observable implements IDataProvider, Runnable { 

    private Buffer buffer; // for storing RDA_MessageData
    private final Logger logger = Logger.getLogger(OnLineDataProvider.class);
    private final String ipAddress;
    private final int port;
    private final TCPIPClient client;
    private final DataTokenizer dtk;
    private final Observer obs;
    private boolean isRunning;
    
    private IArtifactDetection artifactDetector;

    
    
    public OnLineDataProvider(String ip_adr, int port, Observer obs) throws Exception {
        super();
        this.ipAddress = ip_adr;
        this.port = port;
        this.obs = obs;
        client = new TCPIPClient(this.ipAddress, this.port);
        client.start();
        dtk = new DataTokenizer(client);
        dtk.start();
        isRunning = true;
    }

    @Override
    public void run() {
        addObserver(obs);
        
        this.buffer = new Buffer(Const.BUFFER_SIZE, Const.PREESTIMULUS_VALUES, Const.POSTSTIMULUS_VALUES);
        boolean stopped = false;
        int stimulusCounter = 0;
        while (isRunning && stimulusCounter < Const.NUMBER_OF_STIMULUS + 1) {
            Object o = dtk.retrieveDataBlock();
            if (o instanceof RDA_Marker) {
                /* retrieve the number of preceding marker */
                int currentNumber = ((Integer.parseInt(((RDA_Marker) o).getsTypeDesc().substring(11, 13).trim())) - 1);
                logger.debug("" + currentNumber);
                stimulusCounter++;
            } else if (o instanceof RDA_MessageData) {
                buffer.write((RDA_MessageData) o);
            } else if (o instanceof RDA_MessageStart) {
                RDA_MessageStart msg = (RDA_MessageStart) o;
                String[] chNames = msg.getsChannelNames();
                buffer.setNumChannels((int) msg.getnChannels());
                for (int i = 0; i < chNames.length; i++) {
                    if (chNames[i].equalsIgnoreCase("cz")) {
                        buffer.setIndexCz(i);
                    } else if (chNames[i].equalsIgnoreCase("pz")) {
                        buffer.setIndexPz(i);
                    } else if (chNames[i].equalsIgnoreCase("fz")) {
                        buffer.setIndexFz(i);
                    }
                }
            } else if (o instanceof RDA_MessageStop) {
                client.requestStop();
                dtk.requestStop();
                isRunning = false;
                stopped = true;
            }
            if (buffer.isFull() || (stimulusCounter > Const.NUMBER_OF_STIMULUS)) {
                for (EpochDataCarrier data = buffer.get(); data != null; data = buffer.get()) {
                    EpochMessenger em = new EpochMessenger();
                    em.setStimulusIndex(data.getStimulusType());
                    em.setFZ(data.getFzValues(), 0);
                    em.setCZ(data.getCzValues(), 0);
                    em.setPZ(data.getPzValues(), 0);
                    
                    artifactDetector = MainFrame.artifactDetection;
                    if(artifactDetector != null)
                    	em = artifactDetector.detectArtifact(em);

                    this.setChanged();
                    this.notifyObservers(em);
                    System.out.println(em);
                }
                buffer.clear();
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
