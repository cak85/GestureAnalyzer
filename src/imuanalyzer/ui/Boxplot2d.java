package imuanalyzer.ui;

import imuanalyzer.signalprocessing.IBoxplotData;
import imuanalyzer.signalprocessing.VectorLine;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import com.jme3.math.ColorRGBA;

public class Boxplot2d extends JFrame {

	private static final Logger LOGGER = Logger.getLogger(Boxplot2d.class
			.getName());

	ArrayList<IBoxplotData> statistics;

	public Boxplot2d(String title, ArrayList<IBoxplotData> statistics) {
		super(title);

		final BoxAndWhiskerCategoryDataset dataset = getData(statistics);

		final CategoryAxis xAxis = new CategoryAxis("Joint");
		final NumberAxis yAxis = new NumberAxis("Length");
		yAxis.setAutoRangeIncludesZero(false);
		final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
		renderer.setFillBox(false);
		renderer.setMeanVisible(false);
		// renderer.setSeriesVisibleInLegend(false);
		renderer.setSeriesVisibleInLegend(0, false);
		// renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
		renderer.setSeriesToolTipGenerator(0,
				new BoxAndWhiskerToolTipGenerator());
		
		final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis,
				renderer);

		final JFreeChart chart = new JFreeChart(null, new Font("SansSerif",
				Font.BOLD, 14), plot, true);
		final ChartPanel chartPanel = new ChartPanel(chart);
		// chartPanel.setPreferredSize(new java.awt.Dimension(450, 270));
		setContentPane(chartPanel);

		int size = statistics.size();
		if (size == 0) {
			size = 1;
		}
		this.setSize(250 * size, 320);
		this.setVisible(true);

	}

	private BoxAndWhiskerCategoryDataset getData(
			ArrayList<IBoxplotData> statistics) {
		final DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

		for (IBoxplotData d : statistics) {
			ArrayList<Float> outlinerNumbers = new ArrayList<Float>();

			float minOutliner = 0;
			float maxOutliner = 0;
			ArrayList<Object> outliners = d.getOutliners();
			if (outliners.size() > 0) {
				minOutliner = ((VectorLine) outliners.get(0)).getLength();
				maxOutliner = ((VectorLine) outliners.get(0)).getLength();
			}

			for (Object v : outliners) {
				float out = ((VectorLine) v).getLength();
				if (out > maxOutliner && maxOutliner > d.getMedian()) {
					maxOutliner = out;
				}
				if (out < minOutliner && maxOutliner < d.getMedian()) {
					minOutliner = out;
				}
				outlinerNumbers.add(out);
			}
			BoxAndWhiskerItem item = new BoxAndWhiskerItem(0, d.getMedian(),
					d.getLowerQuantile(), d.getUpperQuantile(), d.getMin(),
					d.getMax(), minOutliner, maxOutliner, outlinerNumbers);
			dataset.add(item, "One", d.getDescription());
		}
		return dataset;
	}

}
