package cz.zcu.kiv.eeg.gtn;

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
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms.BandpassFilter;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms.BaselineCorrection;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms.ChannelSelection;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.Buffer;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.OffLineDataProvider;
import cz.zcu.kiv.eeg.gtn.utils.FileUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Tomas Prokop on 16.01.2018.
 */
public class RunTrainAndTest {
    public static void main(String[] args) {
        OffLineDataProvider provider = null;
        try {
            provider = new OffLineDataProvider(FileUtils.loadExpectedResults("data/numbers", "infoTrain.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // buffer
        IBuffer buffer = new Buffer();

        // preprocessings
        ISegmentation epochExtraction = new EpochExtraction(100, 1000);
        List<IPreprocessing> preprocessing = new ArrayList<>();
        List<IPreprocessing> prepreprocessing = new ArrayList<>();
        preprocessing.add(new BaselineCorrection(0, 100));
        prepreprocessing.add(new BandpassFilter(0.1, 30));
        prepreprocessing.add(new ChannelSelection(new String[] {"Fz", "Cz", "Pz"}));

        AbstractDataPreprocessor dataPreprocessor = new EpochDataPreprocessor(preprocessing, prepreprocessing, null, epochExtraction);

        // feature extraction
        List<IFeatureExtraction> featureExtraction = new ArrayList<>();
        IFeatureExtraction fe = new WaveletTransformFeatureExtraction();
        featureExtraction.add(fe);

        // classification
        IClassifier classification = new SDADeepLearning4jClassifier();
        ITrainCondition trainCondition = new ErpTrainCondition();

        // controller
        IWorkflowController workFlowController = new TrainWorkflowController(provider, buffer, dataPreprocessor, featureExtraction, classification, trainCondition);

        // run data provider thread
        Thread t = new Thread(provider);
        t.setName("DataProviderThread");
        t.start();

        String saveFileName;

        try {
            t.join();
            System.out.println("Saving the classifier");
            saveFileName = "data/classifiers/save" + new SimpleDateFormat("yyyyMMddHHmm'.zip'").format(new Date());
            classification.save(saveFileName);
            System.out.println("Remaining buffer size: "       + buffer.size());
            System.out.println("Remaining number of markers: " + buffer.getMarkersSize());

        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }

        //testing
        buffer.clear();
        try {
            provider = new OffLineDataProvider(FileUtils.loadExpectedResults("data/numbers", "info.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        workFlowController = new TestingWorkflowController(provider, buffer, dataPreprocessor, featureExtraction, classification);

        t = new Thread(provider);
        t.setName("DataProviderThread");
        t.start();

        try {
            t.join();
            System.out.println("Remaining buffer size: "       + buffer.size());
            System.out.println("Remaining number of markers: " + buffer.getMarkersSize());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
