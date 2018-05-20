package cz.zcu.kiv.eeg.basil;


import cz.zcu.kiv.eeg.basil.data.listeners.EEGMessageListener;
import cz.zcu.kiv.eeg.basil.data.providers.lsl.LSLEEGDataMessageProvider;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGStopMessage;

public class TestLSL {

	public static void main(String[] args) {
		TestLSL td = new TestLSL();
		LSLEEGDataMessageProvider dataProvider = new LSLEEGDataMessageProvider();
		dataProvider.addEEGMessageListener(new EEGMessageListener() {
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