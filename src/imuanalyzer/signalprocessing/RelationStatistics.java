package imuanalyzer.signalprocessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Generate all statistic data over floating point collection which are
 * necessary for boxplots
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class RelationStatistics implements IBoxplotData {

	private static final Logger LOGGER = Logger
			.getLogger(RelationStatistics.class.getName());

	private float median = 0;
	private float max = 0;
	private float min = 0;
	private float upperQuantile = 0;
	private float lowerQuantile = 0;

	protected ArrayList<Float> elements;

	protected ArrayList<IStatisticsValue> outliners = new ArrayList<IStatisticsValue>();

	protected ArrayList<Float> specialPoints = new ArrayList<Float>();

	protected String description;

	protected Float avg = new Float(0);

	public RelationStatistics(String description, ArrayList<Float> elements,
			ArrayList<Float> specialPercentPoints) {
		this.description = description;
		this.elements = elements;

		Collections.sort(elements);
		int linesSize = elements.size();
		// now we have a ordered collection of touch lines

		avg = calculateAvg(elements);

		if (linesSize > 1) {

			median = calcMedian(elements);
			if (elements.size() % 2 == 0) {
				lowerQuantile = calcMedian(elements.subList(0,
						(int) (elements.size() / 2f)));
				upperQuantile = calcMedian(elements.subList(
						(int) (elements.size() / 2f), elements.size()));
			} else {
				lowerQuantile = calcMedian(elements.subList(0,
						(int) (elements.size() / 2f) + 1));
				upperQuantile = calcMedian(elements.subList(
						(int) (elements.size() / 2f), elements.size()));
			}

			ArrayList<Float> withoutOutliers = eliminateOutliners(elements,
					lowerQuantile, upperQuantile);
			if (withoutOutliers.size() > 0) {
				max = withoutOutliers.get(withoutOutliers.size() - 1);
				min = withoutOutliers.get(0);
			}

		} else if (linesSize == 1) {
			median = elements.get(0);
			max = median;
			min = median;
			upperQuantile = median;
			lowerQuantile = median;
		}

		for (Float p : specialPercentPoints) {
			float idx = (linesSize * p);
			int intIdx = (int) idx;
			LOGGER.debug("Percent: " + p + "  idx: " + idx);
			specialPoints.add(elements.get(intIdx));
		}
	}

	private ArrayList<Float> eliminateOutliners(ArrayList<Float> calcSet,
			float lowerQuantile, float upperQuantile) {

		double IQR = upperQuantile - lowerQuantile;

		double lowOutlinersBorder = lowerQuantile - 1.5 * IQR;

		double highOutlinersBorder = upperQuantile + 1.5 * IQR;

		LOGGER.debug("IQR: " + IQR + " Border low: " + lowOutlinersBorder
				+ " Border high: " + highOutlinersBorder);

		ArrayList<Float> cleanedLines = new ArrayList<Float>();

		for (Float v : calcSet) {
			if (v > lowOutlinersBorder && v < highOutlinersBorder) {
				cleanedLines.add(v);
			} else {
				outliners.add(new FloatStatWrapper(v));
				LOGGER.debug("Outliner detected: " + v);
			}
		}

		return cleanedLines;

	}

	private float calculateAvg(List<Float> calcSet) {
		float sum = 0;

		for (Float v : calcSet) {
			sum += v;
		}
		if (calcSet.size() > 0) {
			return sum / calcSet.size();
		} else {
			return 0;
		}
	}

	private float calcMedian(List<Float> calcSet) {
		int pos = calcSet.size() / 2;

		float median;
		// if even
		if (calcSet.size() % 2 == 0) {
			// calculating average
			float len1 = calcSet.get(pos - 1);
			float len2 = calcSet.get(pos);

			median = (len1 + len2) / 2;

		} else { // odd
			median = calcSet.get(pos);
		}
		return median;
	}

	public float getMedian() {
		return median;
	}

	public float getMax() {
		return max;
	}

	public float getMin() {
		return min;
	}

	public float getUpperQuantile() {
		return upperQuantile;
	}

	public float getLowerQuantile() {
		return lowerQuantile;
	}

	public IStatisticsValue getMaxObj() {
		if (elements.size() > 0) {
			return new FloatStatWrapper(elements.get(elements.size() - 1));
		} else {
			return new FloatStatWrapper();
		}
	}

	public IStatisticsValue getMinObj() {
		if (elements.size() > 0) {
			return new FloatStatWrapper(elements.get(0));
		} else {
			return new FloatStatWrapper();
		}
	}

	public IStatisticsValue getAvgObj() {
		return new FloatStatWrapper(avg);
	}

	@Override
	public ArrayList<IStatisticsValue> getOutliners() {
		return outliners;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public ArrayList<Float> getSpecialPoints() {
		return specialPoints;
	}

	/**
	 * Wrapping class for IStatisticsValue for float
	 * 
	 * @author "Christopher-Eyk Hrabia"
	 * 
	 */
	private class FloatStatWrapper implements IStatisticsValue {

		float f;

		public FloatStatWrapper() {
			this(0);
		}

		public FloatStatWrapper(float f) {
			this.f = f;
		}

		@Override
		public float getStatisticsNumberRepresentation() {
			return f;
		}

	}

}
