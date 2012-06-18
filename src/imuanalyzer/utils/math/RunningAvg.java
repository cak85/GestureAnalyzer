package imuanalyzer.utils.math;

import java.util.ArrayDeque;

public class RunningAvg {

	int size;

	ArrayDeque<Double> buffer;

	double sum = 0;

	public RunningAvg(int size) {
		this.size = size;
		buffer = new ArrayDeque<Double>();
	}

	public void add(double value) {
		if(Double.isNaN(value)){
			return;
		}

		if (buffer.size() > size) {
			Double last = buffer.pollLast();
			if (last != null) {
				sum -= last;
			}
		}
		sum += value;
		buffer.addFirst(value);
	}

	public double getAvg() {
		return sum / buffer.size();
	}

	public int getCount() {
		return buffer.size();
	}

	public void clear() {
		buffer.clear();
		sum = 0;
	}

}
