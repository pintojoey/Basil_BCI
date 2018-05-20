package cz.zcu.kiv.eeg.basil.data.evaluation;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import cz.zcu.kiv.eeg.basil.data.processing.AbstractWorkflowController;
import cz.zcu.kiv.eeg.basil.data.processing.IWorkflowController;
import cz.zcu.kiv.eeg.basil.data.processing.TestingWorkflowController;
import cz.zcu.kiv.eeg.basil.data.processing.TrainWorkflowController;
import cz.zcu.kiv.eeg.basil.data.processing.classification.*;
import cz.zcu.kiv.eeg.basil.data.processing.featureExtraction.RawDataFeatureExtraction;
import cz.zcu.kiv.eeg.basil.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.basil.data.processing.featureExtraction.WaveletTransformFeatureExtraction;
import cz.zcu.kiv.eeg.basil.data.processing.preprocessing.*;
import cz.zcu.kiv.eeg.basil.data.processing.preprocessing.algorithms.BaselineCorrection;
import cz.zcu.kiv.eeg.basil.data.processing.preprocessing.algorithms.ChannelSelection;
import cz.zcu.kiv.eeg.basil.data.processing.preprocessing.algorithms.IntervalSelection;
import cz.zcu.kiv.eeg.basil.data.processing.structures.Buffer;
import cz.zcu.kiv.eeg.basil.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.basil.data.providers.metadata.GtnMetadataProvider;
import cz.zcu.kiv.eeg.basil.data.providers.bva.OffLineDataProvider;
import cz.zcu.kiv.eeg.basil.utils.FileUtils;

/**
 * Guess the number specific evaluation class.
 * Iterates over off-line available datasets, compares
 * expected results with classification outputs and
 * provides global classification metrics. 
 * 
 * @author lvareka
 *
 */
public class GTNOfflineEvaluation {
	private final static String TESTING_FILE = "info.txt";
	private final static String TRAINING_FILE = "infoTrain.txt";
	private List<String> directories;
	private Double humanAccuracy = null;
	private List<Boolean> correctClassifications;
	private double percentage;

	/**
	 * Run evaluation
	 * @param args args
	 */
	public static void main(String[] args) {
		long startTime = System.nanoTime();
		IFeatureExtraction fe  = new WaveletTransformFeatureExtraction();

		// Workflow
		ISegmentation epochExtraction = new EpochExtraction(100, 1000);
		List<IPreprocessing> preprocessing = new ArrayList<>();
		List<IPreprocessing> presegmentation = new ArrayList<>();
		preprocessing.add(new BaselineCorrection(0, 100));
		preprocessing.add(new IntervalSelection(175, 512));
		presegmentation.add(new ChannelSelection(new String[] {"Fz", "Cz", "Pz"}));

		RawDataFeatureExtraction efe = new RawDataFeatureExtraction();
		ArrayList<IFeatureExtraction> feLst = new ArrayList<>();
		feLst.add(efe);
		//feLst.add(fe);

		long estimatedTime = 0;
		double max = Double.MIN_VALUE, min = Double.MAX_VALUE, avg = 0;
		int iters = 1;
		for (int i = 0; i < iters; i++) {
			IClassifier classifier = train(epochExtraction, preprocessing, presegmentation, feLst);

			GTNOfflineEvaluation gtnOfflineEvaluation;
			List<String> directories = new ArrayList<>(Arrays.asList("data/numbers/Horazdovice",
            "data/numbers/Blatnice","data/numbers/Strasice","data/numbers/Masarykovo", "data/numbers/Stankov",
            "data/numbers/17ZS", "data/numbers/DolniBela", "data/numbers/KVary", "data/numbers/SPSD", "data/numbers/Strasice2",
            "data/numbers/Tachov", "data/numbers/Tachov2", "data/numbers/ZSBolevecka"));

			AbstractDataPreprocessor dataPreprocessor = new EpochDataPreprocessor(preprocessing, presegmentation, null,  epochExtraction);

			try {
                gtnOfflineEvaluation = new GTNOfflineEvaluation(feLst, classifier, dataPreprocessor, directories);
                System.out.println("Human accuracy: Total percentage:  " + gtnOfflineEvaluation.computeHumanAccuracy() + "%");
                double perc = gtnOfflineEvaluation.percentage;
                if(perc < min)
                	min = perc;
                if(perc > max)
                	max = perc;

                avg += perc;
            } catch (Exception e) {
                e.printStackTrace();
            }

			estimatedTime = System.nanoTime() - startTime;
		}

		System.out.println("Min => " + min);
		System.out.println("Max => " + max);
		System.out.println("Avg => " + avg / iters);

		System.out.println(estimatedTime /1000000000.0 + " sec");
	}

	private static IClassifier train(ISegmentation epochExtraction, List<IPreprocessing> preprocessing, List<IPreprocessing> presegmentation,
								List<IFeatureExtraction> featureExtraction){
		System.out.println("Training started");
		OffLineDataProvider provider = null;
		try {
			String filePath = "data/numbers" + File.separator + TRAINING_FILE;
			provider = new OffLineDataProvider(new ArrayList<>(FileUtils.loadExpectedResults(filePath).keySet()));
			provider.setMetadataProvider(new GtnMetadataProvider(filePath));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// buffer
		IBuffer buffer = new Buffer();
		AbstractDataPreprocessor dataPreprocessor = new EpochDataPreprocessor(preprocessing, presegmentation, null, epochExtraction);

		// classification
		//IClassifier classification = new BLDAMatlabClassifier();
		//IClassifier classification = new SDADeepLearning4jClassifier();
        IClassifier classification = new CNNDeepLearning4jClassifier();
		ITrainCondition trainCondition = new ErpTrainCondition();

		// controller
		IWorkflowController workFlowController = new TrainWorkflowController(provider, buffer, dataPreprocessor, featureExtraction, classification, trainCondition);

		// run data provider thread
		Thread t = new Thread(provider);
		t.setName("DataProviderThread");
		t.start();

		try {
			t.join();
			
			System.out.println("Remaining buffer size: "       + buffer.size());
			System.out.println("Remaining number of markers: " + buffer.getMarkersSize());

		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		} 
		System.out.println("Training finished");
		return classification;
	}
	
	
	private GTNOfflineEvaluation(List<IFeatureExtraction> fe, IClassifier classifier, AbstractDataPreprocessor dataPreprocessor, List<String> directories) throws InterruptedException, IOException {
		if (classifier == null)
			throw new IllegalArgumentException("Classifier used for evaluation must not be null!");
	    this.directories = directories;

		Map<String, Integer> expectedResults = new HashMap<>();
	    correctClassifications = new ArrayList<>();
	    
	    IBuffer buffer = new Buffer();
	    GTNDetection gtnNumberDetection = new GTNDetection(); // TODO: fix
	    AbstractWorkflowController workFlowController = new TestingWorkflowController(null, buffer, dataPreprocessor, fe, classifier);
	    workFlowController.addListener(gtnNumberDetection);
	    File directory;
	    File f;
	   
	    // iterate over classifiable files
	    for (String dirName : directories) {
	        directory = new File(dirName);
	        if (directory.exists() && directory.isDirectory()) {
	            Map<String, Integer> map = FileUtils.loadExpectedResults(dirName, TESTING_FILE);
	            Map<String, Integer> localResults = new HashMap<>(map);
	            expectedResults.putAll(map);
	 
	            for (Entry<String, Integer> entry : localResults.entrySet()) {
	                f = new File(entry.getKey());
	                if (f.exists() && f.isFile()) {
	                    OffLineDataProvider offLineData = new OffLineDataProvider(f);    
	                    offLineData.addEEGMessageListener(gtnNumberDetection);
	                    workFlowController.setDataProvider(offLineData);
	                    
	            		// run data provider thread
	                    Thread t = new Thread(offLineData);
	            		t.setName("DataProviderThread");
	            		t.start();
	            		System.out.println("Buffer size before: " + buffer.size());
	            		t.join();
	            		
	            		correctClassifications.add(gtnNumberDetection.getWinner() == entry.getValue());
	            		
	            		System.out.println("GTN winner: " + gtnNumberDetection.getWinner() + ", real number: " + entry.getValue());
	            		
	                }
	            }
	        }
	    }
	
	    printStats();
	}
	
	private double computeHumanAccuracy() throws IOException {
	    int totalGood = 0;
	    int fileCount = 0;
	    for (String dirName : this.directories) {
	        int[] res = getHumanGuessPercentage(TESTING_FILE, dirName);
	        totalGood += res[1];
	        fileCount += res[0];
	    }
	    return (double) totalGood / fileCount * 100;
	}
	
	private void printStats() {
	    System.out.println("----------------------------------------------");
	    System.out.println("GTNStatistics: ");
	    System.out.println();
	    int okNumber = 0;
	    for (Boolean okResult: correctClassifications) {
	        if (okResult)
	        	okNumber++;
	    }
	    
	    System.out.println("Perfect guess: " + okNumber);
	    percentage = ((double) okNumber / correctClassifications.size()) * 100;
	    System.out.println("Accuracy: " + percentage + " %");
	    try {
	    	if (humanAccuracy == null) {
	    		this.humanAccuracy = this.computeHumanAccuracy();
	       	}
	    		
	    	System.out.println("Human accuracy: " + this.humanAccuracy + " %");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	private int[] getHumanGuessPercentage(String filename, String dir) throws IOException {
	    File file = new File(dir + File.separator + filename);
	
	    FileInputStream fis = new FileInputStream(file);
	    int fileCount = 0;
	    int goodGuess = 0;
	    int[] countAllAndGood = new int[2];
	
	    //Construct BufferedReader from InputStreamReader
	    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	
	    String line;
	    while ((line = br.readLine()) != null) {
	        if (line.trim().length() == 0 || line.charAt(0) == '#' ) {
	            continue;
	        }
	        fileCount++;
	        String[] parts = line.split(" ");
	        if (parts.length > 2) {
	            try {
	                if (Integer.parseInt(parts[1]) == Integer.parseInt(parts[2])) {
	                    goodGuess++;
	                }
	            } catch (NumberFormatException ex) {
	                //NaN
	            }
	        }
	    }
	
	    br.close();
	    countAllAndGood[0] = fileCount;
	    //System.out.println(fileCount);
	    countAllAndGood[1] = goodGuess;
	    return countAllAndGood;
	}


}
