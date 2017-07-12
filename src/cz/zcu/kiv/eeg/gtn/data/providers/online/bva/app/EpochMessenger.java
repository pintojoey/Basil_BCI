package cz.zcu.kiv.eeg.gtn.data.providers.online.bva.app;

import java.util.Arrays;

import cz.zcu.kiv.eeg.gtn.utils.Const;

/**
 * Serves as a data container transfered to observers
 *
 * @author Lukas Vareka
 *
 */
public class EpochMessenger {

    /**
     * Channels * time samples
     */
    private final double[][] epoch;
    private boolean isTarget;
    private int stimulusIndex;
    
    public static final int DEFAULT_USED_CHANNELS = 3;
    public static final int NUMBER_OF_STIMULI = 9;

    public EpochMessenger() {
        this.epoch = new double[DEFAULT_USED_CHANNELS][Const.POSTSTIMULUS_VALUES];
        this.stimulusIndex = -1;
    }

    public EpochMessenger(double[][] epoch, int stimulusIndex) {
        this.epoch = epoch;
        this.stimulusIndex = stimulusIndex;
    }
    
    

    public double[][] getEpoch() {
        return epoch;
    }

    public int getStimulusIndex() {
        return stimulusIndex;
    }

    public boolean isTarget() {
        return isTarget;
    }

    public void setTarget(boolean isTarget) {
        this.isTarget = isTarget;
    }

    public void setStimulusIndex(int stimulusIndex) {
        this.stimulusIndex = stimulusIndex;
    }

    public void setFZ(float[] fz, int offset) {
        for (int i = 0; i < Const.POSTSTIMULUS_VALUES; i++) {
            epoch[0][i] = (double) fz[i + offset];
        }
    }

    public void setCZ(float[] cz, int offset) {
        for (int i = 0; i < Const.POSTSTIMULUS_VALUES; i++) {
            epoch[1][i] = (double) cz[i + offset];
        }
    }

    public void setPZ(float[] pz, int offset) {
        for (int i = 0; i < Const.POSTSTIMULUS_VALUES; i++) {
            epoch[2][i] = (double) pz[i + offset];
        }
    }

    @Override
    public String toString() {
        return "FZ: " + Arrays.toString(epoch[0]) + "\n"
                + "CZ: " + Arrays.toString(epoch[1]) + "\n"
                + "PZ: " + Arrays.toString(epoch[2]) + "\n\n";
    }
}
