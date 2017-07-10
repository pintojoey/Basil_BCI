package cz.zcu.kiv.eeg.gtn.data.processing.classification;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.featureextraction.IFeatureExtraction;

/**
 * 
 * This class can only be used with installed MATLAB
 * containing BCILAB, EEGLAB and other libraries. 
 * 
 * Created by lukasvareka on 8. 11. 2016.
 */
public class LDAMatlabClassifier implements  IERPClassifier {
    private IFeatureExtraction fe;
    private static MatlabProxy proxy = null;
    private String MATLAB_PATH = "D:\\DG\\BCI\\matlab_experiments";

    public LDAMatlabClassifier() {
        MatlabProxyFactory factory = new MatlabProxyFactory();
        try {
            if (proxy == null) {
                proxy = factory.getProxy();

                // add paths
                proxy.eval("addpath(genpath('" + MATLAB_PATH + "\\src'))");
                proxy.eval("addpath(genpath('" + MATLAB_PATH + "\\lib'))");
            }



        } catch (MatlabConnectionException e) {
            e.printStackTrace();
        } catch (MatlabInvocationException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void setFeatureExtraction(IFeatureExtraction fe) {
        this.fe = fe;

    }

    @Override
    public void train(List<double[][]> epochs, List<Double> targets, int numberOfiter, IFeatureExtraction fe) {
        try {
            if (proxy != null) {
                proxy.eval("out_features = zeros(" + epochs.size() + ", " + fe.getFeatureDimension() + ");");
                proxy.eval("out_targets = zeros(" + epochs.size() + ", 4);");
                for (int i = 0; i < epochs.size(); i++) {
                    double[][] epoch = epochs.get(i);
                    double target    = targets.get(i);

                    double[] features = fe.extractFeatures(epoch);
                    proxy.setVariable("features", features);
                    proxy.eval("out_features(" + (i + 1) + ", :) = features;");

                    if (target == 0) {
                        proxy.eval("out_targets(" + (i + 1) + ", 4) = 1;");
                    } else if (target == 1) {
                        proxy.eval("out_targets(" + (i + 1) + ", 2) = 1;");
                    }
                }
                // Object[] model = proxy.returningFeval(epochs, targets);
                proxy.eval("model = trainlda(out_features', out_targets', 'shrinkage');");

            }
        } catch (MatlabInvocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ClassificationStatistics test(List<double[][]> epochs, List<Double> targets) {
        return null;
    }

    @Override
    public double classify(double[][] epoch) {
        try {
            double[] feature = fe.extractFeatures(epoch);
            proxy.setVariable("test_features", feature);
            if (proxy != null) {
                proxy.eval("classresults = ml_predictlda(test_features, model); classresults = classresults{1,2};resultlda=classresults(1, 2);");
            }
            return ((double[])proxy.getVariable("resultlda"))[0];

        } catch (MatlabInvocationException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void load(InputStream is) {

    }

    @Override
    public void save(OutputStream dest) {

    }

    @Override
    public void save(String file) {

    }

    @Override
    public void load(String file) {

    }

    @Override
    public IFeatureExtraction getFeatureExtraction() {
        return fe;
    }
}
