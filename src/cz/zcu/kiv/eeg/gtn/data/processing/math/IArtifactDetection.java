package cz.zcu.kiv.eeg.gtn.algorithm.math;

import cz.zcu.kiv.eeg.gtn.online.app.EpochMessenger;

/**
 * Interface which makes sure every artifact detector
 * implements detection method.
 * 
 * @author Jan Vampol
 * @version 1.00
 */
public interface IArtifactDetection {
	EpochMessenger detectArtifact(EpochMessenger epoch);
}
