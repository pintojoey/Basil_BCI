package cz.zcu.kiv.eeg.gtn.data.providers.bva.app;

import java.util.Arrays;
import java.util.LinkedList;

import cz.zcu.kiv.eeg.gtn.data.processing.math.Baseline;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.RDA.RDA_Marker;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.RDA.RDA_MessageData;
import cz.zcu.kiv.eeg.gtn.utils.Const;

/**
 *
 * Data storage for RDA MessageData.
 * Subsequently, results are processed and can be returned as a float array.
 */
public class Buffer {

    /**
     * Fz electrode index
     */
    private int indexFz;
    /**
     * Pz electrode index
     */
    private int indexPz;
    /**
     * Cz electrode index
     */
    private int indexCz;

    private int numChannels;

    private float[] dataFZ;
    private float[] dataCZ;
    private float[] dataPZ;

    private int endIndex;
    private int size;
    private final int preMarker;
    private final int postMarker;
    private LinkedList<Integer> indexes;
    private final LinkedList<Integer> stimuli;

    /**
     * 
     * @param size - initial array size
     * @param preMarker - number of fields in the array that can be popped before the marker
     * @param postMarker -number of fields in the array that can be popped after the marker
     */
    public Buffer(int size, int preMarker, int postMarker) {
        this.size = size;

        this.dataFZ = new float[size];
        this.dataCZ = new float[size];
        this.dataPZ = new float[size];

        for (int i = 0; i < this.size; i++) {
            this.dataFZ[i] = Float.MAX_VALUE;
            this.dataCZ[i] = Float.MAX_VALUE;
            this.dataPZ[i] = Float.MAX_VALUE;
        }
        this.endIndex = preMarker;
        this.preMarker = preMarker;
        this.postMarker = postMarker;
        this.indexes = new LinkedList<Integer>();
        this.stimuli = new LinkedList<Integer>();
    }

    public int getIndexCz() {
        return indexCz;
    }

    public int getIndexFz() {
        return indexFz;
    }

    public int getIndexPz() {
        return indexPz;
    }

    public int getNumChannels() {
        return numChannels;
    }

    public void setIndexCz(int indexCz) {
        this.indexCz = indexCz;
    }

    public void setIndexFz(int indexFz) {
        this.indexFz = indexFz;
    }

    public void setIndexPz(int indexPz) {
        this.indexPz = indexPz;
    }

    public void setNumChannels(int numChannels) {
        this.numChannels = numChannels;
    }

    private float[] getFz(float[] array) {
        float[] vals = new float[Const.ELECTROD_VALS];
        for (int i = 0; i < Const.ELECTROD_VALS; i++) {
            vals[i] = array[indexFz + numChannels * i];
        }
        return vals;
    }

    private float[] getPz(float[] array) {
        float[] vals = new float[Const.ELECTROD_VALS];
        for (int i = 0; i < Const.ELECTROD_VALS; i++) {
            vals[i] = array[indexPz + numChannels * i];
        }
        return vals;
    }

    private float[] getCz(float[] array) {
        float[] vals = new float[Const.ELECTROD_VALS];
        for (int i = 0; i < Const.ELECTROD_VALS; i++) {
            vals[i] = array[indexCz + numChannels * i];
        }
        return vals;
    }

    /**
     * Write into the buffer and add a marker into a queue. If the
     * buffer is full, its size will be doubled. In best case,
     * it will be used so that its enlargement is not necessary.
     *
     * @param data - object containing an array to write into the buffer
     */
    public void write(RDA_MessageData data) {
        float[] fz = getFz(data.getfData());
        float[] cz = getCz(data.getfData());
        float[] pz = getPz(data.getfData());

        /* if too little of space remains in the buffer */
        if (this.size - this.endIndex <= fz.length) {
            this.dataFZ = resize(this.dataFZ); // increase the array size
            this.dataCZ = resize(this.dataCZ);
            this.dataPZ = resize(this.dataPZ);
        }
        /* write values into the buffer */
        System.arraycopy(fz, 0, this.dataFZ, endIndex, fz.length);
        System.arraycopy(cz, 0, this.dataCZ, endIndex, cz.length);
        System.arraycopy(pz, 0, this.dataPZ, endIndex, pz.length);

        RDA_Marker[] markers = data.getMarkers();
        if (markers != null) {
            for (RDA_Marker marker : markers) {
                /* index denotes position of the current marker;
                 it is a current position in an array
                 + relative position of marker in the data object */
                int index = this.endIndex + (int) marker.getnPosition();
                this.indexes.addLast(index);
                /* stimulus index is inserted into the queue */
                this.stimuli.addLast(Integer.parseInt(marker.getsTypeDesc().substring(11, 13).trim()) - 1);
            }
        }

        this.endIndex += fz.length;
    }

    /**
     * Double the size of an array and update all parameters as needed
     *
     * 
     */
    private float[] resize(float[] array) {
        System.out.println("*** The metod called prolongs the buffer size ***");
        int newSize = 2 * this.size;
        float[] newData = new float[newSize];
        System.arraycopy(array, 0, newData, 0, this.endIndex);
        for (int i = this.endIndex; i < newSize; i++) {
            newData[i] = Float.MAX_VALUE;
        }
        this.size = newSize;
        return newData;
    }

    /**
     * Collects data from the buffer. No remaining marker = no remaining valuable data when doing ERPs.
     * If so, null is returned.
     * If not enough values after the last marker, null is returned, too. FIFO ordering
     * for marker removal is used.
     *
     * @return - float array of sizes (this.preMarker + this.postMarker)
     */
    public EpochDataCarrier get() {
        if (this.indexes.isEmpty()) {
            return null;
        }
        /* in case not enough values found around the marker  */
        if (this.indexes.peek() + this.postMarker > this.endIndex) {
            return null;
        }

        float[] fz = new float[this.preMarker + this.postMarker];
        float[] cz = new float[fz.length];
        float[] pz = new float[fz.length];

     
        int waveType = this.stimuli.removeFirst();
        int index = this.indexes.removeFirst() - this.preMarker + 1;
        for (int i = 0; i < (this.preMarker + this.postMarker); i++) {
            fz[i] = this.dataFZ[index + i];
            cz[i] = this.dataCZ[index + i];
            pz[i] = this.dataPZ[index + i];
        }

        Baseline.correct(fz, this.preMarker);
        Baseline.correct(cz, this.preMarker);
        Baseline.correct(pz, this.preMarker);

        float[] baselineFZ = Arrays.copyOfRange(fz, this.preMarker, fz.length);
        float[] baselineCZ = Arrays.copyOfRange(cz, this.preMarker, cz.length);
        float[] baselinePZ = Arrays.copyOfRange(pz, this.preMarker, pz.length);

        return new EpochDataCarrier(baselineFZ, baselineCZ, baselinePZ, waveType);
    }

    /**
     * Check if the buffer is full, i.e. less free space than RESERVE is found
     *
     */
    public boolean isFull() {
        return (this.size - this.endIndex <= Const.RESERVE);
    }

    /**
     * Free the buffer. Recommended after all values are obtained using the get() method.
     * Float data are set to Float.MAX_VALUE. Not all values are removed, some
     * values near the end are retained, so case of early arrival of a new marker,
     * those old values could be referenced.
     * Marker denotes the position before which to return the data using the  get() method. For worst case.
     * at least this.preMarker old values are retained.
     */
    public void clear() {
        /* indexPredMarkerem denotes an array position, where a marker was found that is due in a queue,
         minus number of values before the marker to retained;
        i.e. the index of the first unreturned data block */
        int preMarkerIndex;
        if (this.indexes.peekFirst() == null) {
            preMarkerIndex = this.endIndex - this.preMarker;
        } else {
            preMarkerIndex = this.indexes.peek() - this.preMarker;
        }

        for (int i = 0; i < this.endIndex - preMarkerIndex; i++) {
            this.dataFZ[i] = this.dataFZ[preMarkerIndex + i];
            this.dataCZ[i] = this.dataCZ[preMarkerIndex + i];
            this.dataPZ[i] = this.dataPZ[preMarkerIndex + i];
        }
        for (int i = this.endIndex - preMarkerIndex; i < this.size; i++) {
            this.dataFZ[i] = Float.MAX_VALUE;
            this.dataCZ[i] = Float.MAX_VALUE;
            this.dataPZ[i] = Float.MAX_VALUE;
        }


        LinkedList<Integer> newIndexes = new LinkedList<Integer>();
        while (!this.indexes.isEmpty()) {
            int indexMarkeru = this.indexes.removeFirst() - preMarkerIndex;
            newIndexes.add(indexMarkeru);
        }
        this.indexes = newIndexes;
        this.endIndex = this.endIndex - preMarkerIndex;
    }

    public int getIndexesCount() {
        return this.indexes.size();
    }

    public int getStimulusCount() {
        return this.stimuli.size();
    }
}
