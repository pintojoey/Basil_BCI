package cz.zcu.kiv.eeg.gtn.data.processing.math;

/**
 *
 * @author Lukas Vareka
 *
 */
public interface IFilter {
    double getOutputSample(double inputSample);
}
