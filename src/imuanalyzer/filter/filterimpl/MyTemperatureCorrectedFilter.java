package imuanalyzer.filter.filterimpl;

import imuanalyzer.filter.Filter;
import imuanalyzer.utils.math.Butterworth;
import imuanalyzer.utils.math.Quaternion;

/**
 * This Filter corrects gyroskope only by temperature data
 * 
 * This filter assumes that the gyro data is already corrected by linear
 * temperature dependency
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public class MyTemperatureCorrectedFilter extends Filter {

	Quaternion qFilt = new Quaternion();

	public MyTemperatureCorrectedFilter() {
		initInteral();
	}

	// ====================================================================================================
	// Function
	// ====================================================================================================
	// TODO check and improve lowpass
	Butterworth butterX = new Butterworth(40000, 0.4);
	Butterworth butterY = new Butterworth(40000, 0.4);
	Butterworth butterZ = new Butterworth(40000, 0.4);

	@Override
	public Quaternion filterStep(double gx, double gy, double gz, double ax,
			double ay, double az, double mx, double my, double mz, double temp) {

		// gx = butterX.filter(gx);
		// gy = butterX.filter(gy);
		// gz = butterX.filter(gz);

		// get Quaternion from angeluar acceleration and sampleperiod
		Quaternion change = new Quaternion(0, gx, gy, gz);

		Quaternion newqFilt = qFilt.plus(
				qFilt.quaternionProduct(change).times(0.5 * samplePeriod))
				.normalized();

		qFilt = updateAndAdjust(newqFilt);

		return qFilt;
	}

	@Override
	public Quaternion getFilteredQuaternions() {
		return qFilt;
	}

	@Override
	public void initInteral() {
		// quaternion elements representing the estimated orientation
		qFilt = initialOrientation;
	}

}
