package imuanalyzer.utils.math;

import imuanalyzer.filter.Quaternion;

/**
 * 
 * Simple quaternion low pass filter
 *
 */
public class LowPass {

	Quaternion store = null;

	float smoothing;

	public LowPass(float smoothing) {
		this.smoothing = smoothing;
	}

	public Quaternion filter(Quaternion input) {
		if (store != null) {
			store = store.plus(input.minus(store).times(smoothing));
		} else {
			store = input;
		}

		return new Quaternion(store);

	}
}
