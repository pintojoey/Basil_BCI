package cz.zcu.kiv.eeg.basil;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import cz.zcu.kiv.eeg.basil.data.listeners.EEGMessageListener;
import cz.zcu.kiv.eeg.basil.data.providers.bva.OffLineDataProvider;
import cz.zcu.kiv.eeg.basil.data.providers.bva.OnLineDataProvider;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGStopMessage;

/**
 * Created by Tomas Prokop on 18.07.2017.
 * Update to JUnit test by Lukas Vareka 5. 6. 2018
 * 
 * Test both on-line and off-line communication using the BrainVision format  
 */
public class BVAOfflineTest {
	protected boolean startMsg;
    protected boolean dataMsg;
    protected boolean stopMsg;
    protected final String IP_ADDRESS = "147.228.127.95"; 
    protected final String TEST_FILE = "src/test/resources/data/P300/LED_28_06_2012_104.vhdr"; 
    
    

	@Test
    public void testOffline() {
		startMsg = false;
		dataMsg  = false;
		stopMsg  = false;
        File f = new File(TEST_FILE);
        OffLineDataProvider odp = new OffLineDataProvider(f);
       
        odp.addEEGMessageListener(new EEGMessageListener() {
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
                stopMsg = true;
            }
        });
        odp.run();
        assertTrue(startMsg && dataMsg && stopMsg);
        
    }
}
