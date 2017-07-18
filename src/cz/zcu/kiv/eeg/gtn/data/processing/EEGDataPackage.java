package cz.zcu.kiv.eeg.gtn.data.processing;

import java.util.ArrayList;

import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IPreprocessing;

/**
 * Class represents EEG data. It remembers data processing steps performed on the data it carries.
 *
 * Created by Tomas Prokop on 04.07.2017.
 */
public class EEGDataPackage {

    private ArrayList<IPreprocessing> preprocessingSteps;

    /**
     * EEG data
     */
    private double [][] data;


    public EEGDataPackage(){
        preprocessingSteps = new ArrayList<>();
    }

    public EEGDataPackage(double[][] data) {
        this.data = data;
        preprocessingSteps = new ArrayList<>();
    }

    /**
     * Returns data
     * @return data
     */
    public double[][] getData() {
        return data;
    }

    /**
     * Set EEG data
     * @param data data
     * @param step Last preprocessing step
     */
    public void setData(double[][] data, IPreprocessing step) {
        preprocessingSteps.add(step);
        this.data = data;
    }

    public void setData(double[][] data) {
        this.data = data;
    }

    /**
     * Get all preprocessing steps performed on current data
     * @return ordered preprocessing steps
     */
    public ArrayList<IPreprocessing> getPreprocessingSteps() {
        return preprocessingSteps;
    }
}
