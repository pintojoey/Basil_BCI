package cz.zcu.kiv.eeg.gtn.online.tcpip.objects;

/**
 * RDA_Marker
 *
 * @author Michal Patocka 
 * 		First version created: 3.3.2010
 *      Refactoring and translation: 04. 07. 2017
 * 
 * @version 1.0
 *
 * This object represents incoming markers. It contains information about their size,
 * relative offset (from 0 to size of the block) and number of occupied data blocks (by default 1).
 * It also contains channel information. Since the server does not support sending selected channels
 * only, this value is by default set to -1 which means for all electrodes. The most important information
 * is about the code of the incoming marker that is separated by zero characters (/0).
 */

public class RDA_Marker {

    /**
     * Size of the block in bytes. *
     */
    private final long nSize;
    /**
     * Relative offset in data block. *
     */
    private final long nPosition;
    /**
     * Number of occupied blocks (by default 1). *
     */
    private final long nPoints;
    /**
     * Number of channels (by default -1 = all channels). *
     */
    private final long nChannel;
    /**
     * Name of the incoming marker
     */
    private final String sTypeDesc;

    public RDA_Marker(long nSize, long nPosition, long nPoints, long nChannel,
            String sTypeDesc) {
        super();
        this.nSize = nSize;
        this.nPosition = nPosition;
        this.nPoints = nPoints;
        this.nChannel = nChannel;
        this.sTypeDesc = sTypeDesc;
    }

    @Override
    public String toString() {
        return "RDA_Marker (size = " + nSize + ")\n"
                + "Channel= " + nChannel + "\n"
                + "Points= " + nPoints + "\n"
                + "Position= " + nPosition + "\n"
                + "TypeDesc=" + sTypeDesc + "\n";
    }

    public long getnSize() {
        return nSize;
    }

    public long getnPosition() {
        return nPosition;
    }

    public long getnPoints() {
        return nPoints;
    }

    public long getnChannel() {
        return nChannel;
    }

    public String getsTypeDesc() {
        return sTypeDesc;
    }

}
