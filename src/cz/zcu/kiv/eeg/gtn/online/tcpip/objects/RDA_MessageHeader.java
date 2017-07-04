package cz.zcu.kiv.eeg.gtn.online.tcpip.objects;

/**
 * RDA_MessageHeader
 *
 * @author Michal Patocka: first version 3.3.2010
 * @version 1.0
 *
 * This class represents an incoming data object from the server. It is always
 * denoted using unique byte sequence. It carries information about type and
 * size of the next data block. Tuto hlavi�ku obsahuj� v�echny ostatn� datov�
 * objekty (s v�jimkou objektu typu RDA_Marker). D�ky t�to t��d� v�m, jak� data
 * m�m zpracov�vat.
 */
public class RDA_MessageHeader {

    /**
     * Size of the whole data block. *
     */
    protected long nSize;
    /**
     * Data block type. *
     */
    protected long nType;

    public RDA_MessageHeader(long nSize, long nType) {
        this.nSize = nSize;
        this.nType = nType;

    }

    public long getnSize() {
        return nSize;
    }

    public long getnType() {
        return nType;
    }

    @Override
    public String toString() {
        return "RDA_MessageHeader [nSize=" + nSize + ", nType=" + nType + "]";
    }
}
