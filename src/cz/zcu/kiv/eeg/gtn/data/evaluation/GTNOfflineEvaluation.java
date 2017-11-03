package cz.zcu.kiv.eeg.gtn.data.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cz.zcu.kiv.eeg.gtn.data.processing.classification.IClassifier;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.OffLineDataProvider;

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
	private final String infoFileName = "info.txt";
	private List<String> directories;
	private Map<String, Integer> results;
	private Map<String, GTNStatistics> stats;
	private Double humanAccuracy = null;
	public int sizeofStimuls;

	/**
	 * Run evaluation
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
		IClassifier classifier = null;
	    GTNOfflineEvaluation gtnOfflineEvaluation;
		try {
			gtnOfflineEvaluation = new GTNOfflineEvaluation(classifier);
			System.out.println("Human accuracy: Total percentage:  " + gtnOfflineEvaluation.computeHumanAccuracy() + "%");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
	}
	
	
	
	public GTNOfflineEvaluation(IClassifier classifier) throws InterruptedException, IOException, ExecutionException {
		if (classifier == null)
			throw new IllegalArgumentException("Classifier used for evaluation must not be null!");
	    
		stats = new HashMap<>();
	    GTNStatistics.setTotalPts(0);
	    results = new HashMap<>();
	
	    File directory;
	    File f;
	    ExecutorService service = Executors.newFixedThreadPool(10);
	    for (String dirName : directories) {
	        directory = new File(dirName);
	        if (directory.exists() && directory.isDirectory()) {
	            Map<String, Integer> map = loadExpectedResults(infoFileName, dirName);
	            Map<String, Integer> localResults = new HashMap<String, Integer>(map);
	            results.putAll(map);
	 
	            for (Entry<String, Integer> entry : localResults.entrySet()) {
	                f = new File(entry.getKey());
	                if (f.exists() && f.isFile()) {
	                    GTNDetection detection = new GTNDetection(); // TODO: fix
	                    OffLineDataProvider offLineData = new OffLineDataProvider(f);
	                    service.submit(offLineData).get(); 
	                    
	                }
	            }
	        }
	    }
	
	    service.shutdown();
	    // now wait for the jobs to finish
	    service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	
	    printStats();
	}
	
	 
	
	public double computeHumanAccuracy() throws IOException {
	    int totalGood = 0;
	    int fileCount = 0;
	    for (String dirName : this.directories) {
	        int[] res = getHumanGuessPercentage(infoFileName, dirName);
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
	    for (Map.Entry<String, GTNStatistics> entry : stats.entrySet()) {
	        if (entry.getValue().getRank() == 1) {
	            okNumber++;
	        }
	    }
	    System.out.println("Total points: " + GTNStatistics.getTotalPts() + " of " + GTNStatistics.MAX_POINT * stats.size());
	    System.out.println("Perfect guess: " + okNumber);
	    double percent = ((double) okNumber / stats.size()) * 100;
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
	
	/*private Integer[] initProbabilities(double[] probabilities) {
	    Integer[] ranks = new Integer[probabilities.length];
	    for (int i = 0; i < ranks.length; ++i) {
	        ranks[i] = i;
	    }
	    Comparator<Integer> gc = new ProbabilityComparator(probabilities);
	    Arrays.sort(ranks, gc);
	    return ranks;
	
	}
	
	private int getExpectedResult(String filename) {
	    return results.get(filename);
	}
	
	@Override
	public void update(Observable o, Object message) {
	    if (message instanceof OnlineDetection) {
	        double[] probabilities = ((OnlineDetection) message).getWeightedResults();
	
	        result = initProbabilities(probabilities);
	
	    }
	    if (message instanceof ObserverMessage) {
	        ObserverMessage msg = (ObserverMessage) message;
	        if (msg.getMsgType() == MessageType.END) {
	            //   System.out.println(filename);
	            int winner = (result[0] + 1);
	            Statistics st = new Statistics();
	            st.setExpectedResult(getExpectedResult(filename));
	            st.setThoughtResult(winner);
	
	            for (int i = 0; i < result.length; i++) {
	                if ((result[i] + 1) == getExpectedResult(filename)) {
	                    st.setRank(i + 1);
	                    break;
	                }
	            }
	            stats.put(filename, st);
	            end = true;
	
	        }
	    }
	}
	
	public Map<String, Statistics> getStats() {
	    return stats;
	}
	
	public void setSizeofStimuls(int sizeofStimuls) {
	    this.sizeofStimuls = sizeofStimuls;
	}*/

}
