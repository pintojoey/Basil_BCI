package cz.zcu.kiv.eeg.gtn.algorithm.math;

/**
 *
 * @author Lukas Vareka
 *
 */
public interface IFilter {
    public double getOutputSample(double inputSample);
}
