package cz.zcu.kiv.eeg.basil;

import static org.junit.Assert.assertTrue;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import cz.zcu.kiv.eeg.basil.data.processing.VisualizationWorkflowController;
import cz.zcu.kiv.eeg.basil.data.processing.preprocessing.*;
import cz.zcu.kiv.eeg.basil.data.processing.preprocessing.algorithms.BaselineCorrection;
import cz.zcu.kiv.eeg.basil.data.processing.preprocessing.algorithms.ChannelSelection;
import cz.zcu.kiv.eeg.basil.data.processing.structures.Buffer;
import cz.zcu.kiv.eeg.basil.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.basil.data.providers.bva.OffLineDataProvider;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGMarker;


/**
 * This class can be used to test data providers,
 * processing and averaging. It is expected
 * that the P300 component is prominent in the
 * resulting output average epoch.
 * 
 * 
 * @author Lukas Vareka
 *
 */
public class AveragingWorkflowTest {
	
	@Test
	public void testAveragingVisualization() {
		// data IO
		File inputDataFile = new File("src/test/resources/data/P300/LED_28_06_2012_104.vhdr");
		File outputChartImage = new File("src/test/resources/data/P300/chart.png");
		
		// first, clean the previous output file if any
		if (outputChartImage.exists()) {
			outputChartImage.delete();
		}
		
	    OffLineDataProvider provider = new OffLineDataProvider(inputDataFile);

	    // buffer
	    IBuffer buffer = new Buffer();
	    
	    // lists of preprocessing methods
	    List<IPreprocessing> preprocessing = new ArrayList<IPreprocessing>();
		List<IPreprocessing> prepreprocessing = new ArrayList<IPreprocessing>();
		
		// methods used in this testing workflow
		// first, only Fz, Cz, and Pz channels are selected out of all channels
	    prepreprocessing.add(new ChannelSelection(new String[]{"Fz", "Cz", "Pz"}));
	    // epochs are extracted from the continuous EEG signal
	    ISegmentation epochExtraction = new EpochExtraction(100, 1000);
	    // baseline is corrected in the 100 ms pre-stimulus intervals
	    preprocessing.	 add(new BaselineCorrection(0, 100));
	    // averaging is only done for the epochs associated with the "S  2" marker
		Averaging averaging = new Averaging(Arrays.asList(new EEGMarker("S  2", -1)));
	    AbstractDataPreprocessor dataPreprocessor = new EpochDataPreprocessor(preprocessing, prepreprocessing, averaging, epochExtraction);
	   	    	    
	    // controller
	    new VisualizationWorkflowController(provider, buffer, dataPreprocessor, outputChartImage);
	    
	     // run data provider thread
	    Thread t = new Thread(provider);
	    t.setName("DataProviderThread");
	    t.start();
	   
	    try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    assertTrue(outputChartImage.exists());
	    
	}
    
    
}
