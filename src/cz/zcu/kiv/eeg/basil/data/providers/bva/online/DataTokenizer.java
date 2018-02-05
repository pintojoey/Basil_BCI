package cz.zcu.kiv.eeg.basil.data.providers.bva.online;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import cz.zcu.kiv.eeg.basil.data.providers.bva.RDA.RDA_Marker;
import cz.zcu.kiv.eeg.basil.data.providers.bva.RDA.RDA_MessageData;
import cz.zcu.kiv.eeg.basil.data.providers.bva.RDA.RDA_MessageHeader;
import cz.zcu.kiv.eeg.basil.data.providers.bva.RDA.RDA_MessageStart;
import cz.zcu.kiv.eeg.basil.data.providers.bva.RDA.RDA_MessageStop;

/**
 * 
 *
 * @author Michal Patocka First version created: 3.3.2010
 * @version 2.0
 *
 * This class converts byte stream into data RDA. The whole process of
 * starts with searching for a unique sequence of 12 bytes denoting the header.
 * Therefore, in an infinite cycle, we remove first 12 bytes and try to find
 * the match between this array and an array announcing the header. 
 * Once it is found, it means one of the data RDA. First,
 * RDA_MessageStart is expected with parameters for subsequent data transfer.
 * Then many RDA RDA_MessageData that can contain RDA_Marker (mostly just one).
 * If the data are of an unknown type, they will not be processed.
 * All RDA are sent into a buffer from which they can
 * be taken using the retriveDataBlock() method.
 */
public class DataTokenizer extends Thread {

    /**
     * Number of EEG channels
     */
    private int noOfChannels;
    
    /**
     * Buffer used to cache incoming RDA
     */
    private final SynchronizedLinkedListObject buffer = new SynchronizedLinkedListObject();
    
    /**
     * Unique sequence denoting the beginning of the data object.
     * *
     */
    private final byte[] UID = {-114, 69, 88, 67, -106, -55, -122, 76, -81, 74, -104, -69, -10, -55, 20, 80};
    
    /**
     * Reference to the TCP/IP client from which the incoming data are processed
     */
    private final TCPIPClient client;

    private RDA_MessageStart start;

    private boolean isRunning;

    private static final Logger logger = Logger.getLogger(DataTokenizer.class);

    /**
     * For finding match between two byte arrays.
     *
     */
    private boolean comparator(byte[] one, byte[] two) {
        for (int i = 0; i < one.length; i++) {
            if (one[i] != two[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a new byte into a byte array 
     *
     * @param field byte array
     * @param ap new byte
     * @return resulting array
     */
    private byte[] appendByte(byte[] field, byte ap) {
        for (int i = 0; i < (field.length - 1); i++) {
            field[i] = field[i + 1];
        }
        field[field.length - 1] = ap;
        return field;
    }

    /**
     * This method writes RDA of RDA_Marker type and transfer
     * references to corresponding data RDA.
     *
     * @param markerCount number of markers to process
     * @return array of markers
     */
    private RDA_Marker[] writeMarkers(int markerCount) {
        RDA_Marker[] nMarkers = new RDA_Marker[markerCount];
        for (int i = 0; i < markerCount; i++) {
            byte[] nSize = client.read(4);
            long size = getInt(nSize);

            byte[] nPosition = client.read(4);
            long position = getInt(nPosition);

            byte[] nPoints = client.read(4);
            long points = getInt(nPoints);

            client.read(4);
            /*
             * This function has so far not been implemented by the server.
             * Therefore, its return values do not make any sense. Default - all channels.
             * -1 is used for it;*/
            long channel = -1;

            byte[] sTypeDesc = client.read((int) size - 16);
            String typeDesc = "";
            for (int j = 0; j < sTypeDesc.length; j++) {
                char currentChar = (char) sTypeDesc[j];
                typeDesc = typeDesc + currentChar;
            }
            nMarkers[i] = new RDA_Marker(size, position, points, channel, typeDesc);
        }
        return nMarkers;
    }

    /**
     *
     * @param client TCP/IP client to obtain the data
     */
    public DataTokenizer(TCPIPClient client) {
        this.client = client;
    }

    private int getInt(byte[] buff) {
        ByteBuffer bf = ByteBuffer.wrap(buff);
        bf.order(ByteOrder.LITTLE_ENDIAN);
        return bf.getInt();
    }

    private double getDouble(byte[] buff) {
        ByteBuffer bf = ByteBuffer.wrap(buff);
        bf.order(ByteOrder.LITTLE_ENDIAN);
        return bf.getDouble();
    }

    private float getFloat(byte[] buff) {
        ByteBuffer bf = ByteBuffer.wrap(buff);
        bf.order(ByteOrder.LITTLE_ENDIAN);
        return bf.getFloat();
    }

    /**
     * Since the process of data collection
     * and their translation into data RDA must be
     * parallelized, threads are used 
     */
    @Override
    public void run() {
        isRunning = true;
        byte[] value = client.read(16);
        while (isRunning) {

            if (comparator(value, UID)) {

                byte[] nSize = client.read(4);
                byte[] nType = client.read(4);
                int size = getInt(nSize);
                int type = getInt(nType);
                RDA_MessageHeader pHeader = new RDA_MessageHeader(size, type);

                //RDA_MessageStart
                if (pHeader.getnType() == 1) {

                    byte[] nChannels = client.read(4);
                    long channels = getInt(nChannels);
                    noOfChannels = (int) channels;

                    byte[] dSamplingInterval = client.read(8);
                    double samplingInterval = getDouble(dSamplingInterval);

                    String[] typeDesc = new String[(int) channels];
                    double[] resolutions = new double[(int) channels];

                    for (int j = 0; j < channels; j++) {
                        byte[] dResolutions = client.read(8);
                        resolutions[j] = getDouble(dResolutions);
                    }

                    // channel lengths may have different lengths; they are separated by \0
                    for (int j = 0; j < channels; j++) {
                        byte[] b = client.read(1);
                        char rd = (char) b[0];
                        String channelName = "";
                        while (rd != '\0') {
                            channelName = channelName + rd;
                            b = client.read(1);
                            rd = (char) b[0];
                        }
                        typeDesc[j] = channelName;
                    }

                    RDA_MessageStart pMsgStart = new RDA_MessageStart(pHeader.getnSize(), pHeader.getnType(),
                            channels, samplingInterval, resolutions, typeDesc);
                    start = pMsgStart;
                    buffer.addLast(pMsgStart);
                    logger.debug("Zah�jena komunikace se serverem.");

                    //RDA_MessageStop	
                } else if (pHeader.getnType() == 3) {
                    RDA_MessageStop pMsgStop = new RDA_MessageStop(pHeader.getnSize(), pHeader.getnType());
                    buffer.addLast(pMsgStop);
                    logger.debug("Ukon�ena komunikace se serverem.");

                    break;

                    //RDA_MessageData	
                } else if (pHeader.getnType() == 4) {

                    byte[] nBlock = client.read(4);
                    int block = getInt(nBlock);

                    byte[] nPoints = client.read(4);
                    int points = getInt(nPoints);

                    byte[] nMarkers = client.read(4);
                    int markers = getInt(nMarkers);

                    float[] data = new float[noOfChannels * points];

                    for (int j = 0; j < data.length; j++) {
                        byte[] fData = client.read(4);
                        data[j] = getFloat(fData) * (float) start.getdResolutions()[0];
                    }

                    //RDA_Marker
                    RDA_Marker[] markerField = null;
                    if (markers > 0) {
                        markerField = writeMarkers(markers);
                    }

                    RDA_MessageData pMsgData = new RDA_MessageData(pHeader.getnSize(), pHeader.getnType(),
                            block, points, markers, data, markerField);

                    buffer.addLast(pMsgData);

                    for (int j = 0; j < markers; j++) {
                        buffer.addLast(markerField[j]);
                        logger.debug("Incoming marker: " + markerField[j].getsTypeDesc());
                    }

                } else {
                    // Unknown RDA are ignored.
                }

            }
            byte[] ap = client.read(1);
            value = appendByte(value, ap[0]);
        }
    }

    public void requestStop() {
        isRunning = false;
    }

    /**
     * This method returns the first object on the top of the buffer, 
     * into which data blocks have been inserted.
     *
     * @return data RDA
     */
    public synchronized Object retrieveDataBlock() {

        Object o = null;
        while (true) {
            if (!buffer.isEmpty()) {
                try {
                    o = buffer.removeFirst();
                    break;
                } catch (NoSuchElementException e) {
                    //OVERFLOW
                    e.printStackTrace();
                }
            }
        }
        return o;
    }

    
    public boolean hasNext() {
        return buffer.isEmpty();
    }

}
