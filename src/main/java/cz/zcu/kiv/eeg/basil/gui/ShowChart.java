package cz.zcu.kiv.eeg.basil.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import org.jfree.ui.RefineryUtilities;


/**
 * Displays 2D double data such as epoch averages,
 * can be associated with data labels (such as channel names)
 * 
 * @author lvareka
 *
 */
public class ShowChart extends AbstractAction {

    private static final long serialVersionUID = 1L;
    private final EEGCharts chart;

    @Override
    public void actionPerformed(ActionEvent actionevent) {
        this.chart.pack();
        RefineryUtilities.centerFrameOnScreen(this.chart);
        this.chart.setVisible(true);
    }

    /**
     * 
     * @param title title to be shown above the chart
     */
    public ShowChart(String title) {
        super();
        this.chart = new EEGCharts(title);
        putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        putValue("Name", "Show charts");
        
        this.chart.pack();
        RefineryUtilities.centerFrameOnScreen(this.chart);
        this.chart.setVisible(true);
        this.chart.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Displays signal coupled with String labels 
     * @param signal <number_of_channels x number_of_samples>
     * @param labels channel labels
     */
    public void update(double[][] signal, String[] labels) {
        this.chart.update(signal, labels);
    }
}
