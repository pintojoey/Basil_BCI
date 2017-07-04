package cz.zcu.kiv.eeg.gtn.Data;

import cz.zcu.kiv.eeg.gtn.DataProcessing.Preprocessing.IPreprocessing;

import java.util.ArrayList;

/**
 * Class represents EEG data. It remembers data processing steps performed on the data it caries.
 *
 * Created by Tomas Prokop on 04.07.2017.
 */
public class EEGDataPackage {

    private ArrayList<IPreprocessing> preprocessingSteps;

    public EEGDataPackage(){
        preprocessingSteps = new ArrayList<>();
    }

    /**
     * EEG data
     */
    private double [][] data;

    /**
     * Returns data
     * @return data
     */
    public double[][] getData() {
        return data;
    }

    /**
     * Set EEG data
     * @param data Data
     * @param step Last preprocessing step
     */
    public void setData(double[][] data, IPreprocessing step) {
        preprocessingSteps.add(step);
        this.data = data;
    }

    /**
     * Get all preprocessing steps performed on curent data
     * @return ordered preprocessing steps
     */
    public ArrayList<IPreprocessing> getPreprocessingSteps() {
        return preprocessingSteps;
    }
}
