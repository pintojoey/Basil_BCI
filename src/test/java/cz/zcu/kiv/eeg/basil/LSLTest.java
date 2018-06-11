package cz.zcu.kiv.eeg.basil;


import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cz.zcu.kiv.eeg.basil.data.listeners.EEGMessageListener;
import cz.zcu.kiv.eeg.basil.data.providers.lsl.LSLEEGDataMessageProvider;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGStopMessage;

/**
 * 
 * Test communication using Lab Streaming Layer 
 * communication protocol.
 * 
 * @author Lukas Vareka
 *
 */
public class LSLTest {
	protected boolean startMsg;
    protected boolean dataMsg;
   
   /**
    * Will fail if there is no EEG and marker stream available 
    */
    @Test
	public void testLSL() {
		startMsg = false;
		dataMsg  = false;
		LSLEEGDataMessageProvider dataProvider = new LSLEEGDataMessageProvider();
		dataProvider.addEEGMessageListener(new EEGMessageListener() {
			@Override
			public void startMessageSent(EEGStartMessage msg) {
				startMsg = true;
			}

			@Override
			public void dataMessageSent(EEGDataMessage msg) {
				dataMsg = true;
			}

			@Override
			public void stopMessageSent(EEGStopMessage msg) {
				System.out.println(msg.toString());
			}
		});
		dataProvider.run();
		
		assertTrue(startMsg && dataMsg);
	}
}
