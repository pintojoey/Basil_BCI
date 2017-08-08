package cz.zcu.kiv.eeg.gtn;

import java.util.Observable;
import java.util.Observer;


import cz.zcu.kiv.eeg.gtn.data.listeners.EEGMessageListener;
import cz.zcu.kiv.eeg.gtn.data.providers.lsl.LSLEEGDataMessageProvider;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStopMessage;

public class TestLSL {

	public static void main(String[] args) {
		TestLSL td = new TestLSL();
		LSLEEGDataMessageProvider dataProvider = new LSLEEGDataMessageProvider();
		dataProvider.addListener(new EEGMessageListener() {
			@Override
			public void startMessageSent(EEGStartMessage msg) {
				System.out.println(msg.toString());
			}

			@Override
			public void dataMessageSent(EEGDataMessage msg) {
				System.out.println(msg.toString());
			}

			@Override
			public void stopMessageSent(EEGStopMessage msg) {
				System.out.println(msg.toString());
			}
		});
		dataProvider.run();
	}
}
