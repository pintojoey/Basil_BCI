package cz.zcu.kiv.eeg.basil.data.processing;

import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockExecute;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockInput;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockOutput;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockType;
import cz.zcu.kiv.WorkflowDesigner.Type;
import cz.zcu.kiv.eeg.basil.data.listeners.EEGDataProcessingListener;
import cz.zcu.kiv.eeg.basil.data.processing.preprocessing.AbstractDataPreprocessor;
import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.basil.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.basil.data.providers.AbstractDataProvider;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGStopMessage;
import cz.zcu.kiv.eeg.basil.gui.ShowChart;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


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
@BlockType(type="WorkflowController",family = "Controller")
public class WorkflowDesignerController extends AbstractWorkflowController {
	private final int MIN_MARKERS = 5;
	private boolean finished = false;

	@BlockInput(name="DataProvider",type="DataProvider")
    private AbstractDataProvider abstractDataProvider;

	@BlockInput(name="IBuffer", type="IBuffer")
    private IBuffer buffer;

    @BlockInput(name="DataPreprocessor", type="DataPreprocessor")
    private AbstractDataPreprocessor dataPreprocessor;

	@BlockOutput(name="PNGFile",type=Type.FILE)
	private File pngFile; /* output PNG file that will contain the EEG chart output */

    public WorkflowDesignerController(){
        //Required Empty Default for Workflow Designer
    }

    @BlockExecute
    private File process(){
        super.buffer=this.buffer;
        super.preprocessor=this.dataPreprocessor;
        super.dataProvider=this.abstractDataProvider;

        if (buffer == null)
            throw new IllegalArgumentException("The buffer is null!");

        if (preprocessor != null)
            preprocessor.setBuffer(buffer);

        if (dataProvider != null) {
            dataProvider.addEEGMessageListener(messageListener);
            dataProvider.addDataProviderListener(dataProviderListener);
        }
        pngFile=new File("chart.png");
        Thread t = new Thread(dataProvider);
        t.setName("DataProviderThread");
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return pngFile;
    }
	/**
	 *
	 * @param dataProvider
	 * @param buffer
	 * @param preprocessor
	 * @param pngFile output image
	 */
	public WorkflowDesignerController(AbstractDataProvider dataProvider, IBuffer buffer, AbstractDataPreprocessor preprocessor, File pngFile) {
		super(dataProvider, buffer, preprocessor, null, null);
		this.pngFile = pngFile;
	}

	@Override
	public void processData() {
		if (finished || buffer.isFull() || buffer.getMarkersSize() > MIN_MARKERS) {
            final List<EEGDataPackage> dataPackages = preprocessor.preprocessData();

            if (dataPackages == null || dataPackages.size() == 0) return;
            
            for (EEGDataProcessingListener ls : listeners) {
                ls.dataPreprocessed(dataPackages);
            }
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

}
