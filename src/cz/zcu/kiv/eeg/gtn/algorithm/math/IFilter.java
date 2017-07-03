package cz.zcu.kiv.eeg.gtn.algorithm.math;

/**
 *
 * @author Lukas Vareka
 *
 */
public interface IFilter {
    double getOutputSample(double inputSample);
}
