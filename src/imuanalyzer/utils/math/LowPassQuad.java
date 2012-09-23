package imuanalyzer.utils.math;

/**
 * 
 * Simple quaternion low pass filter
 * 
 */
public class LowPassQuad {

	Quaternion store = null;

	float smoothing;

	public LowPassQuad(float smoothing) {
		this.smoothing = smoothing;
	}

	public Quaternion filter(Quaternion input) {
		if (store != null) {
			// this works better!!
			store = store.plus(input.minus(store).times(smoothing));
			//Quaternion rotDiff = input.quaternionProduct(store.getConjugate());
			//store = store.quaternionProduct(rotDiff).pow(smoothing);
		} else {
			store = input;
		}

		return new Quaternion(store).normalized();

	}
}
