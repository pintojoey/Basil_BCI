package cz.zcu.kiv.eeg.gtn.data.providers.online.bva.objects;

/**
 * RDA_MessageStart
 *
 * @author Michal Patocka First version created: 3.3.2010
 * @version 1.0
 *
 * This object is sent when the communication with the server starts. It contains
 * information about the number of channels, sampling frequency, a list of channels
 * with their names and voltages.
 */
public class RDA_MessageStart extends RDA_MessageHeader {

    /**
     * Number of EEG channels. *
     */
    private final long nChannels;
    /**
     * Sampling frequency. *
     */
    private final double dSamplingInterval;
    /**
     * Channel voltages. *
     */
    private final double[] dResolutions;
    /**
     * Channel names. *
     */
    private final String[] sChannelNames;

    public RDA_MessageStart(long nSize, long nType, long nChannels,
            double dSamplingInterval, double[] dResolutions, String[] sChannelNames) {
        super(nSize, nType);
        this.nChannels = nChannels;
        this.dSamplingInterval = dSamplingInterval;
        this.dResolutions = dResolutions;
        this.sChannelNames = sChannelNames;
    }

    @Override
    public String toString() {

        String navrat = "RDA_MessageStart (size = " + nSize + ") \n"
                + "Sampling interval: " + dSamplingInterval + " �S \n"
                + "Number of channels: " + nChannels + "\n";

        for (int i = 0; i < dResolutions.length; i++) {
            navrat = navrat + (i + 1) + ": " + sChannelNames[i] + ": " + dResolutions[i] + "\n";
        }

        return navrat;
    }

    public long getnChannels() {
        return nChannels;
    }

    public double getdSamplingInterval() {
        return dSamplingInterval;
    }

    public double[] getdResolutions() {
        return dResolutions;
    }

    public String[] getsChannelNames() {
        return sChannelNames;
    }
}
