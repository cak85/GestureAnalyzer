package imuanalyzer.utils.math;

import imuanalyzer.filter.Quaternion;

import java.util.ArrayDeque;

public class RunningAvgQuaternion {

	int sizeLimit;

	ArrayDeque<Quaternion> buffer;

	Quaternion sum;

	public RunningAvgQuaternion(int size) {
		sum = new Quaternion();
		sum.clear();
		sizeLimit = size - 1;
		buffer = new ArrayDeque<Quaternion>();
	}

	public void add(Quaternion value) {

		if (buffer.size() > sizeLimit) {
			Quaternion last = buffer.pollLast();
			if (last != null) {
				sum = sum.minus(last);
			}
		}
		sum = sum.quaternionProduct(value);
		buffer.addFirst(value);
	}

	public Quaternion getAvg() {
		return sum.pow(1 / buffer.size());
	}

	public int getCount() {
		return buffer.size();
	}

	public void clear() {
		buffer.clear();
		sum.clear();
	}

}
