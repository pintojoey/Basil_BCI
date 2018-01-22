package cz.zcu.kiv.eeg.gtn.data.evaluation;

import cz.zcu.kiv.eeg.gtn.data.processing.AbstractWorkflowController;
import cz.zcu.kiv.eeg.gtn.data.processing.IWorkflowController;
import cz.zcu.kiv.eeg.gtn.data.processing.TestingWorkflowController;
import cz.zcu.kiv.eeg.gtn.data.processing.TrainWorkflowController;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.ErpTrainCondition;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.IClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.ITrainCondition;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.SDADeepLearning4jClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.WaveletTransformFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.*;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms.*;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.Buffer;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.OffLineDataProvider;
import cz.zcu.kiv.eeg.gtn.utils.FileUtils;

import java.io.*;
import java.text.SimpleDateFormat;
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
	public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {

		IFeatureExtraction fe  = new WaveletTransformFeatureExtraction();

		// Workflow
		ISegmentation epochExtraction = new EpochExtraction(100, 750);
		List<IPreprocessing> preprocessing = new ArrayList<IPreprocessing>();
		List<IPreprocessing> presegmentation = new ArrayList<IPreprocessing>();
		preprocessing.add(new BaselineCorrection(0, 100));
		preprocessing.add(new IntervalSelection(274, 512));
		presegmentation.add(new ChannelSelection(new String[] {"Fz", "Cz", "Pz"}));
		//presegmentation.add(new BandpassFilter(0.1, 20));

		String savedModel = train(epochExtraction, preprocessing, presegmentation, Arrays.asList(fe));

		IClassifier classifier = new SDADeepLearning4jClassifier(fe.getFeatureDimension());
		classifier.load(savedModel);
		
	    GTNOfflineEvaluation gtnOfflineEvaluation;
	    List<String> directories = new ArrayList<String>(Arrays.asList("data/numbers/Horazdovice", 
        "data/numbers/Blatnice","data/numbers/Strasice","data/numbers/Masarykovo", "data/numbers/Stankov", 
        "data/numbers/17ZS", "data/numbers/DolniBela", "data/numbers/KVary", "data/numbers/SPSD", "data/numbers/Strasice2",
        "data/numbers/Tachov", "data/numbers/Tachov2", "data/numbers/ZSBolevecka"));

	    AbstractDataPreprocessor dataPreprocessor = new EpochDataPreprocessor(preprocessing, presegmentation, null,  epochExtraction);

		try {
			gtnOfflineEvaluation = new GTNOfflineEvaluation(fe, classifier, dataPreprocessor, directories);
			System.out.println("Human accuracy: Total percentage:  " + gtnOfflineEvaluation.computeHumanAccuracy() + "%");
		} catch (Exception e) {
			e.printStackTrace();
		}
	   
	}

	private static String train(ISegmentation epochExtraction, List<IPreprocessing> preprocessing, List<IPreprocessing> presegmentation,
								List<IFeatureExtraction> featureExtraction){
		System.out.println("Training started");
		OffLineDataProvider provider = null;
		try {
			provider = new OffLineDataProvider(FileUtils.loadExpectedResults("data/numbers", "infoTrain.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// buffer
		IBuffer buffer = new Buffer();
		AbstractDataPreprocessor dataPreprocessor = new EpochDataPreprocessor(preprocessing, presegmentation, null, epochExtraction);

		// classification
		IClassifier classification = new SDADeepLearning4jClassifier();
		ITrainCondition trainCondition = new ErpTrainCondition();

		// controller
		IWorkflowController workFlowController = new TrainWorkflowController(provider, buffer, dataPreprocessor, featureExtraction, classification, trainCondition);

		// run data provider thread
		Thread t = new Thread(provider);
		t.setName("DataProviderThread");
		t.start();

		String saveFileName = null;

		try {
			t.join();
			System.out.println("Saving the classifier");
			saveFileName = "data/classifiers/save" + new SimpleDateFormat("yyyyMMddHHmm'.zip'").format(new Date());
			classification.save(saveFileName);
			System.out.println("Remaining buffer size: "       + buffer.size());
			System.out.println("Remaining number of markers: " + buffer.getMarkersSize());

		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Training finished");
		return saveFileName;
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
