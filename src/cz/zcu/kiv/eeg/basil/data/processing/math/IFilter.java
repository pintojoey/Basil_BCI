package cz.zcu.kiv.eeg.basil.data.processing.math;

/**
 *
 * 
 *
 * @author Lukas Vareka
 *
 */
public interface IFilter {
    double getOutputSample(double inputSample);
    void reset();
}
