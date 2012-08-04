package imuanalyzer.filter.filterimpl;

import imuanalyzer.filter.Filter;
import imuanalyzer.filter.Quaternion;

/**
 * based on http://www.varesano.net/blog/fabio/my-first-6-dof-imu-sensors-fusion-implementation-adxl345-itg3200-arduino-and-processing
 * @author "Christopher-Eyk Hrabia"
 * but not working well, no z axis rotation...
 *
 */
public class VaranesoDOF extends Filter {

	double[] RwAcc = new double[3]; // projection of normalized gravitation
									// force vector on x/y/z axis, as measured
									// by accelerometer
	double[] Gyro = new double[3]; // Gyro readings
	double[] RwGyro = new double[3]; // Rw obtained from last estimated value
										// and gyro movement
	double[] Awz = new double[2]; // angles between projection of R on XZ/YZ
									// plane and Z axis (deg)
	double[] RwEst = new double[3];

	double wGyro = 10.0;

	boolean firstSample = true;

	Quaternion qFilt = new Quaternion();

	public VaranesoDOF() {
		initInteral();
	}

	@Override
	public Quaternion filterStep(double gx, double gy, double gz, double ax,
			double ay, double az, double mx, double my, double mz, float temp) {

		double[] RwAcc = new double[3]; // projection of normalized gravitation
										// force vector on x/y/z axis, as
										// measured by accelerometer
		double[] Gyro = new double[3]; // Gyro readings

		int w = 0;
		double tmpf = 0.0;
		int signRzGyro;

		double normAcc = Math.sqrt(ax * ax + ay * ay + az * az);
		RwAcc[0] = ax / normAcc;
		RwAcc[1] = ay / normAcc;
		RwAcc[2] = az / normAcc;

		Gyro[0] = gx;
		Gyro[1] = gy;
		Gyro[2] = gz;

		if (firstSample || Double.isNaN(RwEst[0])) { // the NaN check is used to
														// wait for good data
														// from the Arduino
			for (w = 0; w <= 2; w++) {
				RwEst[w] = RwAcc[w]; // initialize with accelerometer readings
			}
		} else {
			// evaluate RwGyro vector
			if (Math.abs(RwEst[2]) < 0.1) {
				// Rz is too small and because it is used as reference for
				// computing Axz, Ayz it's error fluctuations will amplify
				// leading to bad results
				// in this case skip the gyro data and just use previous
				// estimate
				for (w = 0; w <= 2; w++) {
					RwGyro[w] = RwEst[w];
				}
			} else {
				// get angles between projection of R on ZX/ZY plane and Z axis,
				// based on last RwEst
				for (w = 0; w <= 1; w++) {
					tmpf = Gyro[w]; // get current gyro rate in deg/s
					tmpf *= samplePeriod; // get angle change in deg
					// get angle and convert to degrees
					Awz[w] = Math.atan2(RwEst[w], RwEst[2]) * 180 / Math.PI; 
					Awz[w] += tmpf; // get updated angle according to gyro
									// movement
				}

				// estimate sign of RzGyro by looking in what qudrant the angle
				// Axz is,
				// RzGyro is pozitive if Axz in range -90 ..90 => cos(Awz) >= 0
				signRzGyro = (Math.cos(Awz[0] * Math.PI / 180) >= 0) ? 1 : -1;

				// reverse calculation of RwGyro from Awz angles, for formulas
				// deductions see http://starlino.com/imu_guide.html
				for (w = 0; w <= 1; w++) {
					RwGyro[0] = Math.sin(Awz[0] * Math.PI / 180);
					RwGyro[0] /= Math.sqrt(1
							+ squared(Math.cos(Awz[0] * Math.PI / 180))
							* squared(Math.tan(Awz[1] * Math.PI / 180)));
					RwGyro[1] = Math.sin(Awz[1] * Math.PI / 180);
					RwGyro[1] /= Math.sqrt(1
							+ squared(Math.cos(Awz[1] * Math.PI / 180))
							* squared(Math.tan(Awz[0] * Math.PI / 180)));
				}
				RwGyro[2] = signRzGyro
						* Math.sqrt(1 - squared(RwGyro[0]) - squared(RwGyro[1]));
			}

			// combine Accelerometer and gyro readings
			for (w = 0; w <= 2; w++)
				RwEst[w] = (RwAcc[w] + wGyro * RwGyro[w]) / (1 + wGyro);

			double normRwEst = Math.sqrt(RwEst[0] * RwEst[0] + RwEst[1]
					* RwEst[1] + RwEst[2] * RwEst[2]);

			qFilt = updateAndAdjust(new Quaternion((RwEst[0] / normRwEst),
					(RwEst[1] / normRwEst), (RwEst[2] / normRwEst)));

		}

		firstSample = false;

		return qFilt;
	}

	private double squared(double num) {
		return num * num;
	}

	@Override
	public Quaternion getFilteredQuaternions() {
		return qFilt;
	}

	@Override
	public void initInteral() {
		firstSample = true;
	}

	@Override
	public int getNumberOfParameters() {
		return 1;
	}

	@Override
	public double getParameter(int index) {

		switch (index) {
		case 0:
			return wGyro;
		default:
			return wGyro;
		}
	}

	@Override
	public void setParameter(int index, double value) {
		switch (index) {
		case 0:
			wGyro = value;
			break;
		default:
			wGyro = value;
		}
	}

	@Override
	public double getMaxValueFromParameter(int index) {
		switch (index) {
		case 0:
			return 100;
		default:
			return 100;
		}
	}

	@Override
	public double getMinValueFromParameter(int index) {
		return 0;
	}

	public String getParameterName(int index) {
		switch (index) {
		case 0:
			return "wGyro";
		default:
			return "wGyro";
		}
	}

}
