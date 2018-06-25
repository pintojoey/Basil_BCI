package cz.zcu.kiv.eeg.basil.data.processing;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import cz.zcu.kiv.eeg.basil.data.listeners.EEGDataProcessingListener;
import cz.zcu.kiv.eeg.basil.data.processing.preprocessing.AbstractDataPreprocessor;
import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.basil.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.basil.data.providers.AbstractDataProvider;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGStopMessage;
import cz.zcu.kiv.eeg.basil.gui.ShowChart;


/**
 * 
 * This workflow controller aims at non-machine learning workflows.
 * The results of processing is depicted in a form of plot that is saved to the
 * file system.
 * 
 * 
 * 
 * @author Lukas Vareka
 *
 */
public class VisualizationWorkflowController extends AbstractWorkflowController {
	private final int MIN_MARKERS = 5;
	private boolean finished = false;
	private File pngFile; /* output PNG file that will contain the EEG chart output */
	private List<EEGDataPackage>dataPackages;
	
	/**
	 * 
	 * @param dataProvider
	 * @param buffer
	 * @param preprocessor
	 * @param pngFile output image
	 */
	public VisualizationWorkflowController(AbstractDataProvider dataProvider, IBuffer buffer, AbstractDataPreprocessor preprocessor, File pngFile) {
		super(dataProvider, buffer, preprocessor, null, null);
		this.pngFile = pngFile;
	}

	@Override
	public void processData() {
		if (finished || buffer.isFull() || buffer.getMarkersSize() > MIN_MARKERS) {
			dataPackages = preprocessor.preprocessData();

            if (dataPackages == null || dataPackages.size() == 0) return;
            
            for (EEGDataProcessingListener ls : listeners) {
                ls.dataPreprocessed(dataPackages);
            }
            if(pngFile!=null){
				ShowChart showChart = new ShowChart("EEG signal visualization");
				for (EEGDataPackage dataPackage : dataPackages) {
					showChart.update(dataPackage.getData(), dataPackage.getChannelNames());
				}
				try {
					showChart.saveToPng(pngFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void start(EEGStartMessage start) {
		finished = false;
	}

	@Override
	public void stop(EEGStopMessage stop) {
		finished = true;
	    processData();
        buffer.clear();
	}

	@Override
	public void storeData(EEGDataMessage data) {
		buffer.add(data.getData(), Arrays.asList(data.getMarkers()));
	}

	public List<EEGDataPackage> getDataPackages() {
		return dataPackages;
	}

	public void setDataPackages(List<EEGDataPackage> dataPackages) {
		this.dataPackages = dataPackages;
	}
}
