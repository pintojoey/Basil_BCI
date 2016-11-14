package icp.application.classification;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by lukasvareka on 8. 11. 2016.
 */
public class BLDAClassifierMatlab implements  IERPClassifier {
    private IFeatureExtraction fe;
    private static MatlabProxy proxy = null;

    public BLDAClassifierMatlab() {
        MatlabProxyFactory factory = new MatlabProxyFactory();
        try {
            if (proxy == null) {
                proxy = factory.getProxy();

                // add paths
                proxy.eval("addpath(genpath('D:\\DG\\BCI\\matlab_experiments\\src'))");
                proxy.eval("addpath(genpath('D:\\DG\\BCI\\matlab_experiments\\lib'))");
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
                proxy.eval("model = trainblda(out_features', out_targets')");
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
                 proxy.eval("resultsblda = classify(model, test_features');");
             }
            return ((double[])proxy.getVariable("resultsblda"))[0];

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
