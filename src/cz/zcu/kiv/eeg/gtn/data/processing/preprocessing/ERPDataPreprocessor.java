package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import java.util.List;

public class ERPDataPreprocessor extends AbstractDataPreprocessor {

	public ERPDataPreprocessor(IEpochExtraction epochExtraction, List<IPreprocessing> preprocessing) {
		super(epochExtraction, preprocessing);
	}

}
