package cz.zcu.kiv.eeg.gtn.data.processing.classification;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.FeatureVector;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;
import libsvm.LibSVM;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;

public class JavaMLClassifier implements IClassifier {
	private Classifier classifier;

	public JavaMLClassifier() {
		//this.classifier = new  SOM(10, 10, GridType.HEXAGONAL, 1000, 0.1, 3, LearningType.EXPONENTIAL, NeighbourhoodFunction.GAUSSIAN);
		//this.classifier = new  KNearestNeighbors(50);
		this.classifier = new  LibSVM();
		
	}

	@Override
	public void train(List<FeatureVector> featureVectors, List<Double> targets,
			int numberOfiter) {
		Dataset dataset = createDataset(featureVectors, targets);
		classifier.buildClassifier(dataset);
	}

	@Override
	public ClassificationStatistics test(List<FeatureVector> featureVectors, List<Double> targets) {
        ClassificationStatistics resultsStats = new ClassificationStatistics();
		Dataset dataset = createDataset(featureVectors, targets);
		for (Instance inst : dataset) {
			Object predictedClassValue = classifier.classify(inst);
			Object realClassValue = inst.classValue();
			resultsStats.add((Double)realClassValue, (Double)predictedClassValue);
		}
		return resultsStats;
	}

	@Override
	public double classify(FeatureVector fv) {
		double[] feature = fv.getFeatureVector();
		Instance instance = new DenseInstance(feature);
		Object predictedClassValue = classifier.classify(instance);

		return (Double)predictedClassValue;
	}

	@Override
	public void load(InputStream is) {
		throw new NotImplementedException();
		
	}

	@Override
	public void save(OutputStream dest) {
		throw new NotImplementedException();
		
	}

	@Override
	public void save(String file)  {
		
	    OutputStream fileF;
		try {
			fileF = new FileOutputStream(file);
			OutputStream buffer = new BufferedOutputStream(fileF);
		    ObjectOutput output = new ObjectOutputStream(buffer);
		    
		      output.writeObject(classifier);
		      output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
			     
		  
			    
	}

	@Override
	public void load(String file) {
		  try   {
			 InputStream fileF = new FileInputStream(file);
			 InputStream buffer = new BufferedInputStream(fileF);
			 ObjectInput input = new ObjectInputStream (buffer);
				 
			 // deserialize the List
			 this.classifier = (Classifier)input.readObject();
			 input.close();
		} catch(ClassNotFoundException ex){
			   ex.printStackTrace();
		} catch(IOException ex){
			  ex.printStackTrace();
		}
		
		
	}
	
	private Dataset createDataset(List<FeatureVector> featureVectors, List<Double> targets) {
		Dataset dataset= new DefaultDataset();
        for (int i = 0; i < featureVectors.size(); i++ ) {
			double[] features = featureVectors.get(i).getFeatureVector();
			Instance instance = new DenseInstance(features, targets.get(i));
			dataset.add(instance);
			
		}
		return dataset;
		
	}
}
