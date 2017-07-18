package cz.zcu.kiv.eeg.gtn;

import cz.zcu.kiv.eeg.gtn.data.providers.bva.OffLineDataProvider;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.OnLineDataProvider;
import cz.zcu.kiv.eeg.gtn.data.providers.lsl.LSLEEGDataMessageProvider;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Tomas Prokop on 18.07.2017.
 */
public class TestBVA  implements Observer {

    public static void main(String[] args) {
        testOffline();
        //testOnline();
    }

    private static void testOnline(){
        TestBVA td = new TestBVA();
        OnLineDataProvider odp = null;
        try {
            odp = new OnLineDataProvider("147.228.127.95", 51244, td);
        } catch (Exception e) {
            e.printStackTrace();
        }
        odp.run();
    }

    private static void testOffline(){
        TestBVA td = new TestBVA();
        File f = new File("data/numbers/17ZS/17ZS_14_4_2015_02.vhdr");
        OffLineDataProvider odp = new OffLineDataProvider(f, td);
        odp.run();
    }

    @Override
    public void update(Observable o, Object arg) {
        System.out.println(arg.toString());
    }
}
