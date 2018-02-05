package cz.zcu.kiv.eeg.basil.data.processing.classification;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import cz.zcu.kiv.eeg.basil.data.processing.featureExtraction.FeatureVector;

/**
 * 
 * This class can only be used with installed MATLAB
 * containing BCILAB, EEGLAB and other libraries. 
 * 
 * Created by lukasvareka on 8. 11. 2016.
 */
public class BLDAMatlabClassifier implements  IClassifier {
    
    private static MatlabProxy proxy = null;
    private String MATLAB_PATH = "E:\\eeg_data\\DizertaceSkripty";
    
    public BLDAMatlabClassifier() {
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
	public void train(List<FeatureVector> featureVectors, List<Double> targets, int numberOfiter) {
		 try {
            if (proxy != null) {
                proxy.eval("out_features = zeros(" + featureVectors.size() + ", " + featureVectors.get(0).size() + ");");
                proxy.eval("out_targets = zeros(" +featureVectors.size() + ", 1);");
                for (int i = 0; i <featureVectors.size(); i++) {
                    double target    =  featureVectors.get(i).getExpectedOutput();
                    double[] features = featureVectors.get(i).getFeatureVector();
                    proxy.setVariable("features", features);
                    proxy.eval("out_features(" + (i + 1) + ", :) = features;");

                    
                    proxy.eval("out_targets(" + (i + 1) + ", 1) = " + target + ";");
                
                }
                // Object[] model = proxy.returningFeval(epochs, targets);
                
                proxy.eval("model = bayeslda(1);");
                proxy.eval("model = train(model, out_features', out_targets');");
            }
	        } catch (MatlabInvocationException e) {
	            e.printStackTrace();
	        }
		
	}


	@Override
	public ClassificationStatistics test(List<FeatureVector> featureVectors, List<Double> targets) {
		ClassificationStatistics resultsStats = new ClassificationStatistics();

		for(int i = 0; i < featureVectors.size(); i++) {
			double result = this.classify(featureVectors.get(i));
			resultsStats.add(result, targets.get(i));
		}

		return resultsStats;
	}


	@Override
	public double classify(FeatureVector fv) {
		  try {
	           
	             proxy.setVariable("test_features", fv.getFeatureVector());
	             if (proxy != null) {
	                 proxy.eval("resultsblda = classify(model, test_features');");
	             }
	            return ((double[])proxy.getVariable("resultsblda"))[0];

	        } catch (MatlabInvocationException e) {
	            e.printStackTrace();
	            return -1;
	        }
	}
}
