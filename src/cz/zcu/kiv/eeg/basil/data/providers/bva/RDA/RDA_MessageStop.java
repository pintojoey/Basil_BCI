package cz.zcu.kiv.eeg.basil.data.providers.bva.RDA;

/**
 * RDA_MessageStop
 *
 * @author Michal Patocka First version created: 3.3.2010
 * @version 1.0
 *
 * This empty object is sent when communication with the server stops.
 * @author Michal Pato�ka.
 */
public class RDA_MessageStop extends RDA_MessageHeader {

    public RDA_MessageStop(long nSize, long nType) {
        super(nSize, nType);
    }

    @Override
    public String toString() {
        return "RDA_MessageStop []";
    }

}
