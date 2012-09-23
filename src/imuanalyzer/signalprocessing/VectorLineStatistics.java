package imuanalyzer.signalprocessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.jme3.math.Vector3f;

/**
 * Generate all statistic data over VectorLines which are necessary for boxplots
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class VectorLineStatistics implements IBoxplotData {

	private static final Logger LOGGER = Logger
			.getLogger(VectorLineStatistics.class.getName());

	private float median = 0;
	private float max = 0;
	private float min = 0;
	private float upperQuantile = 0;
	private float lowerQuantile = 0;

	protected ArrayList<VectorLine> lines;

	protected ArrayList<IStatisticsValue> outlinersUpper = new ArrayList<IStatisticsValue>();

	protected ArrayList<IStatisticsValue> outlinersLower = new ArrayList<IStatisticsValue>();

	protected ArrayList<Float> specialPoints = new ArrayList<Float>();

	protected String description;

	protected VectorLine avgLine = new VectorLine();

	/**
	 * Constructor
	 * 
	 * @param description
	 *            Description of statistics dataset
	 * @param lines
	 *            calculate statistics over these lines
	 * @param specialPercentPoints
	 *            add some special highlighters
	 */
	public VectorLineStatistics(String description,
			ArrayList<VectorLine> lines, ArrayList<Float> specialPercentPoints) {
		this.description = description;
		this.lines = lines;
		for (VectorLine l : lines) {
			l.updateLength();
		}
		Collections.sort(lines);
		int linesSize = lines.size();
		// now we have a ordered collection of touch lines

		if (linesSize > 1) {

			avgLine = calculateAvgLine(lines);

			median = calcMedian(lines);
			if (lines.size() % 2 == 0) {
				lowerQuantile = calcMedian(lines.subList(0,
						(int) (lines.size() / 2f)));
				upperQuantile = calcMedian(lines.subList(
						(int) (lines.size() / 2f), lines.size()));
			} else {
				lowerQuantile = calcMedian(lines.subList(0,
						(int) (lines.size() / 2f) + 1));
				upperQuantile = calcMedian(lines.subList(
						(int) (lines.size() / 2f), lines.size()));
			}

			ArrayList<VectorLine> linesWithoutOutliers = eliminateOutliners(
					lines, lowerQuantile, upperQuantile);
			if (linesWithoutOutliers.size() > 0) {
				max = linesWithoutOutliers.get(linesWithoutOutliers.size() - 1)
						.getLength();
				min = linesWithoutOutliers.get(0).getLength();
			}

		} else if (linesSize == 1) {
			median = lines.get(0).getLength();
			max = median;
			min = median;
			upperQuantile = median;
			lowerQuantile = median;
		}

		for (Float p : specialPercentPoints) {
			float idx = (linesSize * p);
			int intIdx = (int) idx;
			LOGGER.debug("Percent: " + p + "  idx: " + idx);
			specialPoints.add(lines.get(intIdx).getLength());
		}
	}

	/**
	 * Remove outliners from calc set
	 * 
	 * @param calcSet
	 * @param lowerQuantile
	 * @param upperQuantile
	 * @return
	 */
	private ArrayList<VectorLine> eliminateOutliners(
			ArrayList<VectorLine> calcSet, float lowerQuantile,
			float upperQuantile) {

		double IQR = upperQuantile - lowerQuantile;

		double lowOutlinersBorder = lowerQuantile - 1.5 * IQR;

		double highOutlinersBorder = upperQuantile + 1.5 * IQR;

		LOGGER.debug("IQR: " + IQR + " Border low: " + lowOutlinersBorder
				+ " Border high: " + highOutlinersBorder);

		ArrayList<VectorLine> cleanedLines = new ArrayList<VectorLine>();

		for (VectorLine v : calcSet) {
			if (v.length < lowOutlinersBorder) {
				LOGGER.debug("Outliner lower detected: " + v.length);
				outlinersLower.add(v);
			} else if (v.length > highOutlinersBorder) {
				LOGGER.debug("Outliner upper detected: " + v.length);
				outlinersUpper.add(v);
			} else {
				cleanedLines.add(v);
			}
		}

		return cleanedLines;

	}

	/**
	 * Calculat median
	 * 
	 * @param calcSet
	 * @return
	 */
	private float calcMedian(List<VectorLine> calcSet) {
		int pos = calcSet.size() / 2;

		float median;
		// if even
		if (calcSet.size() % 2 == 0) {
			// calculating average
			float len1 = calcSet.get(pos - 1).getLength();
			float len2 = calcSet.get(pos).getLength();

			median = (len1 + len2) / 2;

		} else { // odd
			median = calcSet.get(pos).getLength();
		}
		return median;
	}

	/**
	 * Calculate average line over all lines
	 * 
	 * @param calcSet
	 * @return
	 */
	public static VectorLine calculateAvgLine(ArrayList<VectorLine> calcSet) {
		VectorLine avg = new VectorLine();
		ArrayList<Vector3f> avgPoints = avg.getLineBuffer();
		ArrayList<Integer> count = new ArrayList<Integer>();

		VectorLine lastOne = calcSet.get(calcSet.size() - 1);

		for (VectorLine current : calcSet) {
			ArrayList<Vector3f> currentPoints = current.getLineBuffer();
			for (int i = 0; i < currentPoints.size(); i++) {
				// sum up
				if (i < count.size()) {
					// increase count
					count.set(i, count.get(i) + 1);
					avgPoints
							.set(i, avgPoints.get(i).add(currentPoints.get(i)));
				} else {
					count.add(1);
					avgPoints.add(currentPoints.get(i).clone());
				}

				// if it is the last line lets divide
				if (current.equals(lastOne)) {
					avgPoints.set(i, avgPoints.get(i).divide(count.get(i)));
				}
				LOGGER.debug("AVG-Point x:" + avgPoints.get(i).x + " y:"
						+ avgPoints.get(i).y + " z:" + avgPoints.get(i).z);
			}
		}
		avg.updateLength();

		return avg;
	}

	/**
	 * Get calculated Median
	 */
	public float getMedian() {
		return median;
	}

	/**
	 * Get calculated Maximum
	 */
	public float getMax() {
		return max;
	}

	/**
	 * Get calculated Minimum
	 */
	public float getMin() {
		return min;
	}

	/**
	 * Get calculated upper quantile
	 */
	public float getUpperQuantile() {
		return upperQuantile;
	}

	/**
	 * Get calculated lower quantile
	 */
	public float getLowerQuantile() {
		return lowerQuantile;
	}

	/**
	 * Get maximum object
	 */
	public VectorLine getMaxObj() {
		return lines.get(lines.size() - 1);
	}

	/**
	 * Get minimum object
	 */
	public VectorLine getMinObj() {
		return lines.get(0);
	}

	/**
	 * Get average object
	 */
	public VectorLine getAvgObj() {
		return avgLine;
	}

	@Override
	public ArrayList<IStatisticsValue> getOutliersUpper() {
		return outlinersUpper;
	}

	@Override
	public ArrayList<IStatisticsValue> getOutliersLower() {
		return outlinersLower;
	}

	@Override
	public ArrayList<IStatisticsValue> getOutliers() {
		ArrayList<IStatisticsValue> outliers = new ArrayList<IStatisticsValue>();
		outliers.addAll(outlinersLower);
		outliers.addAll(outlinersUpper);
		return outliers;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public ArrayList<Float> getSpecialPoints() {
		return specialPoints;
	}

}
