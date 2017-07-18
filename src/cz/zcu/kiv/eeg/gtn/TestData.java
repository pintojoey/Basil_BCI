package cz.zcu.kiv.eeg.gtn;

import java.util.Observable;
import java.util.Observer;


import cz.zcu.kiv.eeg.gtn.data.providers.lsl.LSLEEGDataMessageProvider;

public class TestData  implements Observer {

	public static void main(String[] args) {
		TestData td = new TestData();
		LSLEEGDataMessageProvider dataProvider = new LSLEEGDataMessageProvider(td);
		dataProvider.run();

	}

	@Override
	public void update(Observable o, Object arg) {
		System.out.println(arg.toString());
		
	}

}
