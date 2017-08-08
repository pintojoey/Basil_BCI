package cz.zcu.kiv.eeg.gtn.data.processing.classification.test;

import cz.zcu.kiv.eeg.gtn.data.processing.classification.IClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.MLPClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.WaveletTransformFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.MessageType;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.OffLineDataProvider;
import cz.zcu.kiv.eeg.gtn.gui.ProbabilityComparator;
import cz.zcu.kiv.eeg.gtn.utils.Const;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by stebjan on 20.1.2015.
 */
public class TestClassificationAccuracy implements Observer {

    private final String infoFileName = "info.txt";
    private Map<String, Integer> results;
    private Integer[] result;
    private String filename;
    private boolean end;
    private Map<String, Statistics> stats;
    private Double humanAccuracy = null;
    public int sizeofStimuls;
    
    private final static String TRAINING_FILE_NAME = "data/new_models/winnermlpdwt.classifier";
    private final static String[] DIRECTORIES = {"data/numbers"};

    public static void main(String[] args) throws InterruptedException, IOException {
        TestClassificationAccuracy testClassificationAccuracy = new TestClassificationAccuracy();
        //System.out.println();
        System.out.println("Human accuracy: Total percentage:  " + testClassificationAccuracy.computeHumanAccuracy() + "%");
        
    }

    public TestClassificationAccuracy() throws InterruptedException, IOException {
        //this(null);
    }

    public TestClassificationAccuracy(IClassifier classifier) throws InterruptedException, IOException, ExecutionException {
        stats = new HashMap<>();
        Statistics.setTotalPts(0);
        results = new HashMap<>();
        int counter = 0;

        File directory;
        File f;
        ExecutorService service = Executors.newFixedThreadPool(10);
        for (String dirName : DIRECTORIES) {
            directory = new File(dirName);
            if (directory.exists() && directory.isDirectory()) {
                Map<String, Integer> map = loadExpectedResults(infoFileName, dirName);
                Map<String, Integer> localResults = new HashMap<String, Integer>(map);
                results.putAll(map);
                //System.out.println("Result size  -- " + results.size());
                for (Entry<String, Integer> entry : localResults.entrySet()) {
                    f = new File(entry.getKey());
                    if (f.exists() && f.isFile()) {
                        counter++;
                        //System.out.println(counter + ".filename: " + filename + "--" + results.size());
                        end = false;
                        filename = entry.getKey();

                        if (classifier == null) {
                            //classifier = new KNNClassifier();
                            classifier = new MLPClassifier();
                            classifier.load(TRAINING_FILE_NAME);
                            IFeatureExtraction fe = new WaveletTransformFeatureExtraction();
                        }
                        

                        //OnlineDetection detection = new OnlineDetection(classifier, this,sizeofStimuls);
                        //OffLineDataProvider offLineData = new OffLineDataProvider(f, detection);
                        //submits task for execution. Calling get() method blocks thread until work is done.
                        //service.submit(offLineData).get();
                        
                     
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
        for (String dirName : DIRECTORIES) {
            int[] res = getHumanGuessPercentage(infoFileName, dirName);
          
            totalGood += res[1];
            fileCount += res[0];
        }
        return (double) totalGood / fileCount * 100;
        
    }

    private void printStats() {
        System.out.println("----------------------------------------------");
        System.out.println("Statistics: ");
        System.out.println();
        int okNumber = 0;
        for (Map.Entry<String, Statistics> entry : stats.entrySet()) {
            if (entry.getValue().getRank() == 1) {
                okNumber++;
            }
          
        }
        System.out.println("Total points: " + Statistics.getTotalPts() + " of " + Statistics.MAX_POINT * stats.size());
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

    private Integer[] initProbabilities(double[] probabilities) {
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
/*        if (message instanceof OnlineDetection) {
            double[] probabilities = ((OnlineDetection) message).getWeightedResults();

            result = initProbabilities(probabilities);

        }*/
        if (message instanceof EEGDataMessage) {
            EEGDataMessage msg = (EEGDataMessage) message;
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
    }
}
