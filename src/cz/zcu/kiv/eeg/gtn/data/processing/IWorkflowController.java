package cz.zcu.kiv.eeg.gtn.data.processing;

import cz.zcu.kiv.eeg.gtn.data.processing.Structures.IBuffer;

/**
 * Created by Tomas Prokop on 04.07.2017.
 */
public interface IWorkflowController {
    IBuffer getBuffer();
    void storeData();


}
