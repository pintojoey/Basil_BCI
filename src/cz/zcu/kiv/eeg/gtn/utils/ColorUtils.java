package cz.zcu.kiv.eeg.gtn.utils;

import java.awt.Color;

/**
 * Color-coding values for visualization purposes. 
 *
 * @author Prokop
 */
public class ColorUtils {

	/**
	 * 
	 * Create a color out of the value inserted 
	 * 
	 * @param min minimum value
	 * @param max maximum value
	 * @param value current value
	 * @return generated color 
	 */
    public static Color convertToRgb(double min, double max, double value) {
        double ratio = 2 * (value - min) / (max - min);
        int b = (int) (Math.max(0, 255 * (1 - ratio)));
        int r = (int) (Math.max(0, 255 * (ratio - 1)));
        int g = 255 - b - r;

        return new Color(r, g, b);
    }

    public static Color convertToRgb(double value) {
        return convertToRgb(0, 1, value);
    }
}
