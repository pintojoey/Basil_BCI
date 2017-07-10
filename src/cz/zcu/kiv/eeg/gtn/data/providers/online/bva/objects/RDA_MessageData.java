package cz.zcu.kiv.eeg.gtn.data.providers.online.bva.objects;

/**
 * RDA_MessageData
 *
 * @author Michal Patocka: First version created: 3.3.2010
 * @version 1.0
 *
 * This class represents a data message sent by the server. It contains information about
 * the number of data blocks, number of contained markers a relative position of the block
 * since communication started.. Furthermore, the data itself are contained that are stored in an 
 * array (number of channels x number of data blocks).
 * Also contains the information about markers.
 */
public class RDA_MessageData extends RDA_MessageHeader {

    /**
     * Position of the data block since communication started. *
     */
    private final long nBlock;
    /**
     * Number of occupied data blocks. *
     */
    private final long nPoints;
    /**
     * Number of markers *
     */
    private final long nMarkers;
    /**
     * Array with the data itself. *
     */
    private final float[] fData;
    /**
     * Array referencing the markers. *
     */
    private final RDA_Marker[] markers;

    public RDA_MessageData(long nSize, long nType, long nBlock,
            long nPoints, long nMarkers, float[] fData, RDA_Marker[] markers) {
        super(nSize, nType);
        this.nBlock = nBlock;
        this.nPoints = nPoints;
        this.nMarkers = nMarkers;
        this.fData = fData;
        this.markers = markers;
    }

    @Override
    public String toString() {

        String stringValue = "RDA_MessageData (size = " + super.getnSize() + ") \n"
                + "block NO.: " + nBlock + " \n"
                + "points: " + nPoints + "\n"
                + "NO of markers: " + nMarkers + "\n";
        int nChannels = fData.length / (int) nPoints;

        for (int i = 0; i < nChannels; i++) {
            stringValue = stringValue + (i + 1) + ": ";
            for (int j = i; j < fData.length; j += nPoints) {
                stringValue = stringValue + fData[j] + ", ";
            }
            stringValue = stringValue + "\n";
        }
        stringValue = stringValue + "\n";

        for (int i = 0; i < nMarkers; i++) {
            stringValue = stringValue + markers[i].toString();
        }

        return stringValue;
    }

    public long getnBlock() {
        return nBlock;
    }

    public long getnPoints() {
        return nPoints;
    }

    public long getnMarkers() {
        return nMarkers;
    }

    public float[] getfData() {
        return fData;
    }

    public RDA_Marker[] getMarkers() {
        return markers;
    }

}
