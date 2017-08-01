package cz.zcu.kiv.eeg.gtn;

import cz.zcu.kiv.eeg.gtn.data.listeners.EEGMessageListener;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.OffLineDataProvider;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.OnLineDataProvider;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStopMessage;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Tomas Prokop on 18.07.2017.
 */
public class TestBVA {

    public static void main(String[] args) {
        testOffline();
        //testOnline();
    }

    private static void testOnline(){
        TestBVA td = new TestBVA();
        OnLineDataProvider odp = null;
        try {
            odp = new OnLineDataProvider("147.228.127.95", 51244);
            odp.addListener(new EEGMessageListener() {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        odp.run();
    }

    private static void testOffline(){
        TestBVA td = new TestBVA();
        File f = new File("data/numbers/17ZS/17ZS_14_4_2015_02.vhdr");
        OffLineDataProvider odp = new OffLineDataProvider(f);
        odp.run();
    }
}
