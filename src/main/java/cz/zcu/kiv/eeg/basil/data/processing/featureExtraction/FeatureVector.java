package cz.zcu.kiv.eeg.basil.data.processing.featureExtraction;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Arrays;

/**
 * Represents a feature vector as
 * an input for classification
 * <p>
 * Created by Tomas Prokop on 07.08.2017.
 */
public class FeatureVector {
    private INDArray featureVector;
    private double expectedOutput;

    public FeatureVector() {
    }

    public FeatureVector(INDArray featureVector) {
        this.featureVector = featureVector;
    }

    public FeatureVector(INDArray featureVector, double expectedOutput) {
        this.featureVector = featureVector;
        this.expectedOutput = expectedOutput;
    }

    public FeatureVector(double[][] featureVector) {
        this.featureVector = Nd4j.create(featureVector);
    }

    public FeatureVector(double[][] featureVector, double expectedOutput) {
        this.featureVector = Nd4j.create(featureVector);
        this.expectedOutput = expectedOutput;
    }

    public FeatureVector(double[] featureVector) {
        this.featureVector = Nd4j.create(featureVector);
    }

    public FeatureVector(double[] featureVector, double expectedOutput) {
        this.featureVector = Nd4j.create(featureVector);
        this.expectedOutput = expectedOutput;
    }

    public FeatureVector(double[] featureVector, int[] shape) {
        this.featureVector = Nd4j.create(featureVector, shape);
    }

    public FeatureVector(double[] featureVector, int[] shape, double expectedOutput) {
        this.featureVector = Nd4j.create(featureVector, shape);
        this.expectedOutput = expectedOutput;
    }

    /**
     * Join two feature vectors
     *
     * @param features feature vector
     */
    public void addFeatures(double[][] features) {
        if (featureVector == null)
            featureVector = Nd4j.create(features);
        else {
            int[] shape = featureVector.shape();
            if (features.length != shape[0])
                throw new IllegalArgumentException("Dimension of given features does not match dimension of current feature vector");

            double[][] a1 = featureVector.toDoubleMatrix();

            double[][] copy = new double[a1.length][a1[0].length + features[0].length];
            for (int i = 0; i < copy.length; i++) {
                System.arraycopy(a1[i], 0, copy, 0, a1[i].length);
                System.arraycopy(features[i], 0, copy, a1[i].length, features[i].length);
            }

            featureVector = Nd4j.create(copy);
        }
    }

    /**
     * Join two feature vectors
     *
     * @param features feature vector
     */
    public void addFeatures(double[] features) {
        if (featureVector == null) {
            featureVector = Nd4j.create(features);
        } else {
            if (features.length != 1)
                throw new IllegalArgumentException("Dimension of given features does not match dimension of current feature vector");

            featureVector = Nd4j.toFlattened(featureVector, Nd4j.create(features));
        }
    }

    /**
     * Join two feature vectors
     *
     * @param features feature vector
     */
    public void addFeatures(FeatureVector features) {
        if (featureVector == null) {
            featureVector = features.getFeatures();
        } else {
            addFeatures(features.getFeatureMatrix());
        }
    }

    /**
     * Get feature vector transformed into matrix.
     * For 1d will return [1][featureVector.length]
     * 3 and more dimensions will be "flattened" into matrix. First dimension is preserved.
     * It means that for 3D it will return [featureVector.length][featureVector[1].length * featureVector[2].length]
     *
     * @return Feature vector as matrix
     */
    public double[][] getFeatureMatrix() {
        if(featureVector == null) return null;

        if(featureVector.rank() == 2){
            return featureVector.toDoubleMatrix();
        }

        int[] shape = featureVector.shape();
        if(featureVector.rank() == 1) {//1D array
            return featureVector.reshape(1, shape[0]).toDoubleMatrix();
        } else { //3d, ...
            int len = 1;
            for (int i = 0; i < shape.length - 1; i++)
                len *= shape[i];

            return featureVector.dup().reshape(shape[0], len).toDoubleMatrix();
        }
    }

    /**
     * Get feature vector transformed into array.
     * Feature vector with dimension 2 and more will be flattened
     *
     * @return features as array
     */
    public double[] getFeatureArray() {

        if(featureVector == null) return null;

        if(featureVector.rank() == 1) {//1D array
            return featureVector.toDoubleVector();
        } else { //matrix, 3d, ...
            int[] shape = featureVector.shape();
            int len = 1;
            for (int i = 0; i < shape.length; i++)
                len *= shape[i];

            return featureVector.dup().reshape(len).toDoubleVector();
        }
    }

    /**
     * Get feature vector
     * @return feature vector
     */
    public INDArray getFeatures(){
        return featureVector;
    }

    /**
     * Reshapes current feature vector and returns it.
     * Note that returns copy of feature vector if current shape is not equal to original one.
     * @param shape new shape
     * @return reshaped feature vector
     */
    public INDArray getShapedFeatureVector(int[] shape){
        int[] orig = featureVector.shape();
        if(Arrays.equals(shape, orig))
            return featureVector;

        return featureVector.dup().reshape(shape);
    }

    /**
     * Get the number of features.
     * E.g. for feature matrix 2x3 will return 6.
     * @return Feature vector size
     */
    public int size() {
        return featureVector != null ? featureVector.length() : 0;
    }

    /**
     * Get the feature vector dimension:
     *  1 - array
     *  2 - matrix
     *  3 - ...
     * @return Feature vector dimension
     */
    public int dimension(){
        return featureVector != null ? featureVector.rank() : 0;
    }

    /**
     * Get the feature vector shape.
     * It means data length for each dimension.
     * E.g. 2x3 matrix will return {2, 3}
     * @return Feature vector shape
     */
    public int[] shape(){
        return featureVector != null ?  featureVector.shape() : null;
    }

    /**
     * Get the expected output for this feature vector (classification class)
     * @return expected classification class
     */
    public double getExpectedOutput() {
        return expectedOutput;
    }

    /**
     * Set expected classification class for this feature vector
     * @param expectedOutput classification class
     */
    public void setExpectedOutput(double expectedOutput) {
        this.expectedOutput = expectedOutput;
    }
}
