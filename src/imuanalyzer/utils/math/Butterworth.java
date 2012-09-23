package imuanalyzer.utils.math;

/**
 * 2 pole Butterworth filter based on
 * http://baumdevblog.blogspot.de/2010/11/butterworth
 * -lowpass-filter-coefficients.html
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public class Butterworth {

	double ax[] = new double[3];
	double by[] = new double[3];

	double xv[] = new double[3];
	double yv[] = new double[3];

	public Butterworth(double frequency, double cutoff) {
		init();
		getLPCoefficientsButterworth2Pole(frequency, cutoff, ax, by);
	}

	public Butterworth() {
		init();
	}

	private void init() {
		for (int i = 0; i < ax.length; i++) {
			ax[i] = 0;
			by[i] = 0;
			xv[i] = 0;
			yv[i] = 0;
		}
	}

	private void getLPCoefficientsButterworth2Pole(double samplerate,
			double cutoff, double[] ax, double[] by) {
		double sqrt2 = 1.4142135623730950488;

		// Find cutoff frequency in [0..PI]
		double QcRaw = (2 * Math.PI * cutoff) / samplerate;
		double QcWarp = Math.tan(QcRaw); // Warp cutoff frequency

		double gain = 1 / (1 + sqrt2 / QcWarp + 2 / (QcWarp * QcWarp));
		by[2] = (1 - sqrt2 / QcWarp + 2 / (QcWarp * QcWarp)) * gain;
		by[1] = (2 - 2 * 2 / (QcWarp * QcWarp)) * gain;
		by[0] = 1;
		ax[0] = 1 * gain;
		ax[1] = 2 * gain;
		ax[2] = 1 * gain;
	}

	/**
	 * Filter one sample
	 * 
	 * @param sample
	 * @return
	 */
	public double filter(double sample) {
		xv[2] = xv[1];
		xv[1] = xv[0];
		xv[0] = sample;
		yv[2] = yv[1];
		yv[1] = yv[0];

		yv[0] = (ax[0] * xv[0] + ax[1] * xv[1] + ax[2] * xv[2] - by[1] * yv[0] - by[2]
				* yv[1]);

		return yv[0];
	}

	/**
	 * Filter one sample with specific new cutoff and frequency
	 * 
	 * @param sample
	 * @param frequency
	 * @param cutoff
	 * @return
	 */
	public double filter(double sample, double frequency, double cutoff) {

		getLPCoefficientsButterworth2Pole(frequency, cutoff, ax, by);

		xv[2] = xv[1];
		xv[1] = xv[0];
		xv[0] = sample;
		yv[2] = yv[1];
		yv[1] = yv[0];

		yv[0] = (ax[0] * xv[0] + ax[1] * xv[1] + ax[2] * xv[2] - by[1] * yv[0] - by[2]
				* yv[1]);

		return yv[0];
	}

}
