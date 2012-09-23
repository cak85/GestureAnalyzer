package imuanalyzer.device;

/**
 * Class for managing correction of one gyro by his temperature based on given
 * linear relation
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class GyroTempCorrection {

	private double[] coefficient;
	private double[] constant;

	/**
	 * Constructor for providing coefficients and constants
	 * 
	 * @param coefficient
	 *            for every axis a coefficient
	 * @param constant
	 *            for every axis a constant
	 */
	public GyroTempCorrection(double[] coefficient, double[] constant) {
		this.coefficient = coefficient;
		this.constant = constant;
	}

	/**
	 * Default constructor will not do any correction
	 */
	public GyroTempCorrection() {
		this.coefficient = new double[] { 1, 1, 1 };
		this.constant = new double[] { 0, 0, 0 };
	}

	/**
	 * Do one correction step based on temperature and provided linear relation
	 * 
	 * @param data
	 */
	public void correctGyro(MARGRawData data) {

		double temp = data.temp;

		// double for debug remove with other coefficients the
		// convertion to celsius
		temp = 35 + ((temp) + 13200) / 280.0;

		data.gyroskope.x = data.gyroskope.x
				- (temp * coefficient[0] + constant[0]);
		data.gyroskope.y = data.gyroskope.y
				- (temp * coefficient[1] + constant[1]);
		data.gyroskope.z = data.gyroskope.z
				- (temp * coefficient[2] + constant[2]);
	}
}
