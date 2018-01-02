package cz.zcu.kiv.eeg.gtn.data.evaluation;

import cz.zcu.kiv.eeg.gtn.data.processing.AbstractWorkflowController;
import cz.zcu.kiv.eeg.gtn.data.processing.TestingWorkflowController;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.IClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.SDADeepLearning4jClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.WaveletTransformFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.*;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms.BandpassFilter;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms.BaselineCorrection;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms.ChannelSelectionPointers;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.Buffer;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.OffLineDataProvider;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

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
	private final String INFO_FILE = "info.txt";
	private List<String> directories;
	private Map<String, Integer> expectedResults;
	private Double humanAccuracy = null;
	public int sizeofStimuls;
	private List<Boolean> correctClassifications;
	

	/**
	 * Run evaluation
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(String[] args) throws InterruptedException, IOException   {
		IFeatureExtraction fe  = new WaveletTransformFeatureExtraction();
		IClassifier classifier = new SDADeepLearning4jClassifier(fe.getFeatureDimension());
		classifier.load("data/classifiers/save201711071425.txt");
		
	    GTNOfflineEvaluation gtnOfflineEvaluation;
	    List<String> directories = new ArrayList<String>(Arrays.asList("data/numbers/Horazdovice", 
        "data/numbers/Blatnice","data/numbers/Strasice","data/numbers/Masarykovo", "data/numbers/Stankov", 
        "data/numbers/17ZS", "data/numbers/DolniBela", "data/numbers/KVary", "data/numbers/SPSD", "data/numbers/Strasice2",
        "data/numbers/Tachov", "data/numbers/Tachov2", "data/numbers/ZSBolevecka"));
	    
	    // Workflow
	    ISegmentation epochExtraction = new EpochExtraction(100, 1000);
	    List<IPreprocessing> preprocessing = new ArrayList<IPreprocessing>();
		List<IPreprocessing> prepreprocessing = new ArrayList<IPreprocessing>();
	    preprocessing.add(new BaselineCorrection(0, 100));
	    prepreprocessing.add(new BandpassFilter(0.1, 8));
	    prepreprocessing.add(new ChannelSelectionPointers(Arrays.asList(0, 1, 2)));
	    AbstractDataPreprocessor dataPreprocessor = new EpochDataPreprocessor(preprocessing, prepreprocessing, null,  epochExtraction);
	   
	    
		try {
			gtnOfflineEvaluation = new GTNOfflineEvaluation(fe, classifier, dataPreprocessor, directories);
			System.out.println("Human accuracy: Total percentage:  " + gtnOfflineEvaluation.computeHumanAccuracy() + "%");
		} catch (Exception e) {
			e.printStackTrace();
		}
	   
	}
	
	
	
	public GTNOfflineEvaluation(IFeatureExtraction fe, IClassifier classifier, AbstractDataPreprocessor dataPreprocessor, List<String> directories) throws InterruptedException, IOException, ExecutionException {
		if (classifier == null)
			throw new IllegalArgumentException("Classifier used for evaluation must not be null!");
	    this.directories = directories;
		
	    expectedResults = new HashMap<>();
	    correctClassifications = new ArrayList<Boolean>();
	    
	    IBuffer buffer = new Buffer();
	    GTNDetection gtnNumberDetection = new GTNDetection(); // TODO: fix
	    AbstractWorkflowController workFlowController = new TestingWorkflowController(null, buffer, dataPreprocessor, Arrays.asList(fe), classifier);
	    workFlowController.addListener(gtnNumberDetection);
	    File directory;
	    File f;
	   
	    // iterate over classifiable files
	    for (String dirName : directories) {
	        directory = new File(dirName);
	        if (directory.exists() && directory.isDirectory()) {
	            Map<String, Integer> map = loadExpectedResults(INFO_FILE, dirName);
	            Map<String, Integer> localResults = new HashMap<String, Integer>(map);
	            expectedResults.putAll(map);
	 
	            for (Entry<String, Integer> entry : localResults.entrySet()) {
	                f = new File(entry.getKey());
	                if (f.exists() && f.isFile()) {
	                    OffLineDataProvider offLineData = new OffLineDataProvider(f);    
	                    offLineData.addListener(gtnNumberDetection);
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
	
	 
	
	public double computeHumanAccuracy() throws IOException {
	    int totalGood = 0;
	    int fileCount = 0;
	    for (String dirName : this.directories) {
	        int[] res = getHumanGuessPercentage(INFO_FILE, dirName);
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
	    double percent = ((double) okNumber / correctClassifications.size()) * 100;
	    System.out.println("Accuracy: " + percent + " %");
	    try {
	    	if (humanAccuracy == null) {
	    		this.humanAccuracy = this.computeHumanAccuracy();
	       	}
	    		
	    	System.out.println("Human accuracy: " + this.humanAccuracy + " %");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	private Map<String, Integer> loadExpectedResults(String filename, String dir) throws IOException {
	    Map<String, Integer> res = new HashMap<>();
	    File file = new File(dir + File.separator + filename);
	    FileInputStream fis = new FileInputStream(file);
	
	    //Construct BufferedReader from InputStreamReader
	    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	
	    String line;
	    int num;
	    while ((line = br.readLine()) != null) {
	        String[] parts = line.split(" ");
	        if (parts.length > 1) {
	            try {
	                num = Integer.parseInt(parts[1]);
	                res.put(dir + File.separator + parts[0], num);
	            } catch (NumberFormatException ex) {
	                //NaN
	            }
	        }
	    }
	
	    br.close();
	    return res;
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
