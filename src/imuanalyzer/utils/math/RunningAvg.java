package imuanalyzer.utils.math;

import java.util.ArrayDeque;

/**
 * Class for calculating a running average over doubles 
 * @author Christopher-Eyk Hrabia
 *
 */
public class RunningAvg {

	int sizeLimit;

	ArrayDeque<Double> buffer;

	double sum = 0;

	/**
	 * Constructor 
	 * @param size Define size of average buffer
	 */
	public RunningAvg(int size) {
		sizeLimit = size - 1;
		buffer = new ArrayDeque<Double>();
	}

	/**
	 * Add new value for average calculation
	 * @param value
	 */
	public void add(double value) {
		if (Double.isNaN(value)) {
			return;
		}

		if (buffer.size() > sizeLimit) {
			Double last = buffer.pollLast();
			if (last != null) {
				sum -= last;
			}
		}
		sum += value;
		buffer.addFirst(value);
	}

	/**
	 * Get current average
	 * @return
	 */
	public double getAvg() {
		return sum / buffer.size();
	}

	/**
	 * get number of items in average buffer
	 * @return
	 */
	public int getCount() {
		return buffer.size();
	}

	/**
	 * Clear average buffer
	 */
	public void clear() {
		buffer.clear();
		sum = 0;
	}

}
