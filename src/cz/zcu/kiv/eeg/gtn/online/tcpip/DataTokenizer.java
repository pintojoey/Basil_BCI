package cz.zcu.kiv.eeg.gtn.online.tcpip;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import cz.zcu.kiv.eeg.gtn.online.tcpip.objects.RDA_Marker;
import cz.zcu.kiv.eeg.gtn.online.tcpip.objects.RDA_MessageData;
import cz.zcu.kiv.eeg.gtn.online.tcpip.objects.RDA_MessageHeader;
import cz.zcu.kiv.eeg.gtn.online.tcpip.objects.RDA_MessageStart;
import cz.zcu.kiv.eeg.gtn.online.tcpip.objects.RDA_MessageStop;

/**
 * N�zev �lohy: Jednoduch� BCI T��da: DataTokenizer
 *
 * @author Michal Pato�ka Prvn� verze vytvo�ena: 3.3.2010
 * @version 2.0
 *
 * Tato t��da formuje z toku bajt� z�skan� od klienta TCPIP datov� objekty. Cel�
 * proces detekce za��n� hled�n�m unik�tn� posloupnosti 12 bajt�, kter� ozna�uj�
 * hlavi�ku datov�ho objektu. Toto je naimplementov�no tak, �e v nekone�n�m
 * cyklu p�id�v�m posledn� a ub�r�m prvn� z pole 12 bajt� a hled�m shodu mezi
 * t�mto polem a polem ozna�ujic�m hlavi�ku. Je - li tato posloupnost nalezena,
 * znamen� to, �e p�i�el jeden z p�ti typ� datov�ch objekt�. Jak� typ p�i�el a
 * jak� je jeho d�lka zjist�m p�e�ten�m n�sleduj�c�ch 8 bajt�, kter� n�sleduj�
 * po hlavi�ce. Obecn� plat�, �e na za��tku ka�d�ho p�enosu p�ijde objekt typu
 * RDA_MessageStart, ve kter�m jsou deklarov�ny pou�it� parametry pro
 * n�sleduj�c� datov� p�enos. Pot� chod� zna�n� mno�stv� objekt� typu
 * RDA_MessageData, p�i�em� ka�d� z nich m��e obsahovat n�kolik objekt� typu
 * RDA_Marker (nej�ast�ji v�ak pouze jeden). Kdy� zjist�m typ objektu, jak�
 * p�ich�z�, nen� probl�m do n�j na��st data konverz� pole ur�it�ho mno�stv�
 * bajt�, do po�adovan�ho datov�ho typu. Pokud je objekt nezn�m�ho typu, tak ho
 * nezpracov�v�m (p�i testech chodily objekty typu nType = 10000 jako v�pl� mezi
 * jednotliv�mi objekty). V�echny objekty na��t�m do bufferu, kde jsou
 * p�ipraveny k vyzvednut� pomoc� metody retriveDataBlock().
 */
public class DataTokenizer extends Thread {

    /**
     * Po�et kan�l� EEG
     */
    private int noOfChannels;
    
    /**
     * Buffer jako vyrovn�vac� pam� pro do�asn� ulo�en� objekt� *
     */
    private final SynchronizedLinkedListObject buffer = new SynchronizedLinkedListObject();
    
    /**
     * Unik�tn� posloupnost 12 bajt�, kter� ozna�uje hlavi�ku datov�ho objektu.
     * *
     */
    private final byte[] UID = {-114, 69, 88, 67, -106, -55, -122, 76, -81, 74, -104, -69, -10, -55, 20, 80};
    
    /**
     * Reference na TCP/IP klienta, ze kter�ho z�sk�v�m bajty ke zpracov�n�. *
     */
    private final TCPIPClient client;

    private RDA_MessageStart start;

    private boolean isRunning;

    /**
     * Reference na logger ud�lost�. *
     */
    private static final Logger logger = Logger.getLogger(DataTokenizer.class);

    /**
     * Zji��uje jestli jsou dv� pole bajt� shodn�.
     *
     * @param one prvn� pole bajt�
     * @param two dru� pole bajt�
     * @return shoda/neshoda
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
     * Poli bajt� p�id� na konec nov� bajt a posune index cel�ho pole o 1, ��m�
     * vyma�e odkaz na prvn� bajt.
     *
     * @param field pole bajt�
     * @param ap p�id�van� bajt
     * @return posunut� pole s bajtem nav�c
     */
    private byte[] appendByte(byte[] field, byte ap) {
        for (int i = 0; i < (field.length - 1); i++) {
            field[i] = field[i + 1];
        }
        field[field.length - 1] = ap;
        return field;
    }

    /**
     * Tato metoda zapisuje objekty typu RDA_Marker a p�ed�v� na n� reference
     * p��slu�n�mu datov�mu objektu.
     *
     * @param markerCount po�et marker�, kter� se zpracov�vaj�.
     * @return pole marker�
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
             * Tuto funkci doposud servr nem� implementovanou.
             * Proto vrac� blbost. V n�vodu je �e se defaultn� jedn�
             * o v�echny kan�ly, proto hodnota -1.
             * long channel = arr2long(nChannel);*/
            long channel = -1;

            byte[] sTypeDesc = client.read((int) size - 16);
            String typeDesc = "";
            for (int j = 0; j < sTypeDesc.length; j++) {
                char znak = (char) sTypeDesc[j];
                typeDesc = typeDesc + znak;
            }
            nMarkers[i] = new RDA_Marker(size, position, points, channel, typeDesc);
        }
        return nMarkers;
    }

    /**
     * Konstruktor, kter�mu je p�ed�v�m odkaz na TCP/IP clienta. Je pou�it
     * defaultn� logger.
     *
     * @param client TCP/IP client pro z�sk�v�n� dat
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
     * Metoda pro spu�t�n� vl�kna DataTokenizeru. Jeliko� proces z�sk�v�n� dat a
     * jejich p�ev�d�n� na datov� objekty mus� b�t paraelizov�n, mus� b�t
     * pou�ito vl�knov�ho zpracov�n�.
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

                    //jm�na kan�l� mohou m�t prom�nnou d�lku, jsou odd�len� znakem \0
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

                    int ch = 0;
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
                        logger.debug("P��choz� marker: " + markerField[j].getsTypeDesc());
                    }

                } else {
                    //v�echny nezn�m� typy objekt� se ignoruj�
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
     * Tato metoda vrac� prvn� objekt na vrcholu bufferu, do kter�ho jsou
     * na��t�ny datov� bloky.
     *
     * @return datov� objekt
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

    /**
     * Tato metoda zji��uje, jetli je pr�zdn� buffer.
     *
     * @return zda - li je pr�zdn� buffer.
     */
    public boolean hasNext() {
        return buffer.isEmpty();
    }

}
