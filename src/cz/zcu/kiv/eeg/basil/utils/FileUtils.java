package cz.zcu.kiv.eeg.basil.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tomas Prokop on 16.01.2018.
 */
public class FileUtils {
    public static Map<String, Integer> loadExpectedResults(String dir, String infoFile) throws IOException {
        return  loadExpectedResults(dir + File.separator + infoFile);
    }

    public static Map<String, Integer> loadExpectedResults(String infoFile) throws IOException {
        return  loadExpectedResults(new File(infoFile));
    }

    public static Map<String, Integer> loadExpectedResults(File file) throws IOException {
        Map<String, Integer> res = new HashMap<>();

        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        String line;
        int num;
        while ((line = br.readLine()) != null) {
            if (line.charAt(0) == '#') { //comment in info txt
                continue;
            }
            String[] parts = line.split(" ");
            if (parts.length > 1) {
                try {
                    num = Integer.parseInt(parts[1]);
                    res.put(file.getParent() + File.separator + parts[0], num);
                } catch (NumberFormatException ex) {
                    //NaN
                }
            }
        }

        br.close();
        return res;
    }
}
