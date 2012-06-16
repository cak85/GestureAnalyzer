package imuanalyzer.ui;

import imuanalyzer.signalprocessing.IBoxplotData;
import imuanalyzer.signalprocessing.VectorLine;

import java.util.ArrayList;

import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;

/**
 * Overloaded BoxAndWhiskerRenderer which calculates the boxplot range including outliers
 * and some additional space at the bottom
 * @author "Christopher-Eyk Hrabia"
 *
 */
public class BoxAndWhiskerRendererWithOutliers extends BoxAndWhiskerRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6838700832888719175L;
	ArrayList<IBoxplotData> statistics;

	public BoxAndWhiskerRendererWithOutliers(ArrayList<IBoxplotData> statistics) {
		this.statistics = statistics;
	}

	@Override
	public Range findRangeBounds(CategoryDataset dataset) {
		float max = Float.NEGATIVE_INFINITY;
		float min = Float.POSITIVE_INFINITY;

		for (IBoxplotData d : statistics) {
			max = Math.max(max, d.getMax());
			min = Math.min(min, d.getMin());
			for (Object o : d.getOutliners()) {
				VectorLine v = (VectorLine) o;
				max = Math.max(max, v.getLength());
				min = Math.min(min, v.getLength());
			}
		}
		return new Range(min - 1, max);

	}
}
