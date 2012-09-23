package imuanalyzer.ui.swing.charts;

import imuanalyzer.signalprocessing.IBoxplotData;
import imuanalyzer.signalprocessing.IStatisticsValue;
import imuanalyzer.ui.swing.help.HelpManager;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

/**
 * Classical 2d box and whisker diagramm in a own frame
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public class Boxplot2d extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7933849892180600991L;

	private static final Logger LOGGER = Logger.getLogger(Boxplot2d.class
			.getName());

	ArrayList<IBoxplotData> statistics;

	class OutlierComparator implements Comparator<IStatisticsValue> {

		@Override
		public int compare(IStatisticsValue o1, IStatisticsValue o2) {
			float prio1 = o1.getStatisticsNumberRepresentation();
			float prio2 = o2.getStatisticsNumberRepresentation();

			if (prio1 > prio2) {
				return 1;
			} else if (prio1 < prio2) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	// TODO check outlier painting and scale calculation for positive and
	// negative outliers - should be ok now

	public Boxplot2d(String title, ArrayList<IBoxplotData> statistics) {
		super(title);
		
		HelpManager.getInstance().enableHelpKey(this, "diagramms");

		ImageIcon icon = new ImageIcon(getClass()
				.getResource("/Icons/hand.png"));
		this.setIconImage(icon.getImage());

		final BoxAndWhiskerCategoryDataset dataset = getData(statistics);

		final CategoryAxis xAxis = new CategoryAxis("Joint");
		final NumberAxis yAxis = new NumberAxis("Length");
		yAxis.setAutoRangeIncludesZero(false);
		final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRendererWithOutliers(
				statistics);
		renderer.setFillBox(false);
		renderer.setMeanVisible(false);
		renderer.setSeriesVisibleInLegend(0, false);
		renderer.setSeriesToolTipGenerator(0,
				new BoxAndWhiskerToolTipGenerator());

		final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis,
				renderer);

		final JFreeChart chart = new JFreeChart(null, new Font("SansSerif",
				Font.BOLD, 14), plot, true);
		final ChartPanel chartPanel = new ChartPanel(chart);
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

			Float minOutliner = null;
			Float maxOutliner = null;
			ArrayList<IStatisticsValue> outliners = d.getOutliers();
			if (outliners.size() > 0) {
				// get outlier extrema
				java.util.Collections.sort(outliners, new OutlierComparator());
				minOutliner = outliners.get(0)
						.getStatisticsNumberRepresentation();

				maxOutliner = outliners.get(outliners.size() - 1)
						.getStatisticsNumberRepresentation();
			} else {
				outlinerNumbers = null;
			}

			for (IStatisticsValue v : outliners) {
				float out = v.getStatisticsNumberRepresentation();
				if (out > maxOutliner && maxOutliner > d.getMedian()) {
					maxOutliner = out;
				}
				if (out < minOutliner && maxOutliner < d.getMedian()) {
					minOutliner = out;
				}
				outlinerNumbers.add(out);
			}
			BoxAndWhiskerItem item = new BoxAndWhiskerItem(null,
					(Float) d.getMedian(), (Float) d.getLowerQuantile(),
					(Float) d.getUpperQuantile(), (Float) d.getMin(),
					(Float) d.getMax(), (Float) minOutliner,
					(Float) maxOutliner, outlinerNumbers);
			dataset.add(item, "One", d.getDescription());
		}
		return dataset;
	}

}
