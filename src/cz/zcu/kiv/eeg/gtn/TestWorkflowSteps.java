package cz.zcu.kiv.eeg.gtn;

import cz.zcu.kiv.eeg.gtn.data.processing.IWorkflowController;
import cz.zcu.kiv.eeg.gtn.data.processing.TestingWorkflowController;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.IClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.MLPClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.WaveletTransformFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.*;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms.BandpassFilter;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms.BaselineCorrection;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms.ChannelSelection;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms.IntervalSelection;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.Buffer;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.OffLineDataProvider;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGMarker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Tomas Prokop on 22.01.2018.
 */
public class TestWorkflowSteps {
    public static void main(String[] args) {
        File f = new File("data/numbers/17ZS/17ZS_14_4_2015_02.vhdr");
        OffLineDataProvider provider = new OffLineDataProvider(f);

        // buffer
        IBuffer buffer = new Buffer();

        // preprocessings
        ISegmentation epochExtraction = new EpochExtraction(100, 1000);
        List<IPreprocessing> preprocessing = new ArrayList<IPreprocessing>();
        List<IPreprocessing> presegmentation = new ArrayList<IPreprocessing>();
        preprocessing.add(new BaselineCorrection(0, 100));
        preprocessing.add(new IntervalSelection(274, 512));
        presegmentation.add(new ChannelSelection(new String[]{"Fz","Cz", "Pz"} ));
        //presegmentation.add(new BandpassFilter(0.1, 8));
        //Averaging averaging = new Averaging(Arrays.asList(new EEGMarker("S  2", -1)));
        AbstractDataPreprocessor dataPreprocessor = new EpochDataPreprocessor(preprocessing, presegmentation, null, epochExtraction);

        // feature extraction
        List<IFeatureExtraction> featureExtraction = new ArrayList<IFeatureExtraction>();
        featureExtraction.add(new WaveletTransformFeatureExtraction());
        IClassifier classification       		   = new MLPClassifier();

        // controller
        IWorkflowController workFlowController = new TestingWorkflowController(provider, buffer, dataPreprocessor, featureExtraction, classification);

        // run data provider thread
        Thread t = new Thread(provider);
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
