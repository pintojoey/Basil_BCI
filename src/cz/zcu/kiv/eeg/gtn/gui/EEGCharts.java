package cz.zcu.kiv.eeg.gtn.gui;

import java.awt.Color;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class EEGCharts extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6331107099427920835L;
	private XYDataset dataset;
    private JFreeChart chart;
    private ChartPanel chartPanel;

    public EEGCharts(final String title) {
        super(title);
        dataset = null;
        chart = createChart(dataset, title);
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
    }

    /**
     * Creates a chart.
     *
     * @param dataset the data for the chart.
     *
     * @return a chart.
     */
    private JFreeChart createChart(XYDataset dataset, String chartTitle) {

        // create the chart...
        final JFreeChart chartComponent = ChartFactory.createXYLineChart(
                chartTitle, // chart title
                "Sample [ms]", // x axis label
                "Value  [microV]", // y axis label
                dataset, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chartComponent.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot = chartComponent.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        plot.setRenderer(renderer);

        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // OPTIONAL CUSTOMISATION COMPLETED.

        return chartComponent;
    }

    public void update(double[][] signal, String[] labels) {
    	boolean labelsAvailable = !(labels == null || labels.length != signal.length); 
    	
        final XYSeriesCollection dataset = new XYSeriesCollection();
        
                
        for (int i = 0; i < signal.length; i++) {
        	final XYSeries series;
        	if (labelsAvailable)
        		series = new XYSeries(labels[i]);
        	else
        		series = new XYSeries("" + (i + 1));
        	
        	for (int j = 0; j < signal[i].length; j++) {
                series.add(j, signal[i][j]);
            }
            dataset.addSeries(series);
        }
        chart = createChart(dataset, this.getTitle());
        chartPanel.setChart(chart);
        chartPanel.repaint();

        chart.fireChartChanged();
    }
}
