package cz.zcu.kiv.eeg.basil.workflow;

import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;

import java.util.List;

public class EEGDataPackageList {
    List<EEGDataPackage> eegDataPackage;

    public EEGDataPackageList(List<EEGDataPackage> eegDataPackage) {
        this.eegDataPackage = eegDataPackage;
    }

    public List<EEGDataPackage> getEegDataPackage() {
        return eegDataPackage;
    }

    public void setEegDataPackage(List<EEGDataPackage> eegDataPackage) {
        this.eegDataPackage = eegDataPackage;
    }
}
