package imuanalyzer.utils.math;


/**
 * 
 * Simple low pass filter for doubles
 * 
 */
public class LowPassDouble {

	double store = 0;

	float smoothing;

	boolean init = true;

	public LowPassDouble(float smoothing) {
		this.smoothing = smoothing;
	}

	public double filter(double input) {

		if (init) {
			store = input;
		} else {
			store = store + (input - store) * smoothing;
		}
		return store;
	}
}
