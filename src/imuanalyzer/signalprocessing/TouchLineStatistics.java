package imuanalyzer.signalprocessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TouchLineStatistics {

	private float median = 0;
	private float max = 0;
	private float min = 0;
	private float upperQuantile = 0;
	private float lowerQuantile = 0;

	ArrayList<TouchLine> lines;

	public TouchLineStatistics(ArrayList<TouchLine> lines) {
		this.lines = lines;
		for(TouchLine l:lines){
			l.updateLength();
		}
		Collections.sort(lines);
		int linesSize = lines.size();
		// now we have a ordered collection of touch lines

		if (linesSize > 1) {
			max = lines.get(lines.size() - 1).getLength();
			min = lines.get(0).getLength();
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
		} else if (linesSize == 1) {
			median = lines.get(0).getLength();
			max = median;
			min = median;
			upperQuantile = median;
			lowerQuantile = median;
		}
	}

	private float calcMedian(List<TouchLine> calcSet) {
		int pos = calcSet.size() / 2;

		float median;
		// if gerade
		if (calcSet.size() % 2 == 0) {
			// calculating average
			float len1 = calcSet.get(pos-1).getLength();
			float len2 = calcSet.get(pos).getLength();

			median = (len1 + len2) / 2;

		} else { // ungerade
			median = calcSet.get(pos).getLength();
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

	public TouchLine getMaxObj() {
		return lines.get(lines.size() - 1);
	}

	public TouchLine getMinObj() {
		return lines.get(0);
	}

}
