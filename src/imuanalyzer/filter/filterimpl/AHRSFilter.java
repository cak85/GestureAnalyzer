package imuanalyzer.filter.filterimpl;

import imuanalyzer.filter.Filter;
import imuanalyzer.filter.Quaternion;

/**
 * 
 * based on C-sources of S.O.H. Madgwick from 25th August 2010
 * ==================
 * ============================================================
 * ======================= Description:
 * 
 * Quaternion implementation of the 'DCM filter' [Mayhony et al]. Incorporates
 * the magnetic distortion compensation algorithms from my filter [Madgwick]
 * which eliminates the need for a reference direction of flux (bx bz) to be
 * predefined and limits the effect of magnetic distortions to yaw axis only.
 * 
 * User must define 'halfT' as the (sample period / 2), and the filter gains
 * 'Kp' and 'Ki'.
 * 
 * Global variables 'q0', 'q1', 'q2', 'q3' are the quaternion elements
 * representing the estimated orientation. See my report for an overview of the
 * use of quaternions in this application.
 * 
 * User must call 'filterStep()' every sample period and parse calibrated
 * gyroscope ('gx', 'gy', 'gz'), accelerometer ('ax', 'ay', 'ay') and
 * magnetometer ('mx', 'my', 'mz') data. Gyroscope units are radians/second,
 * accelerometer and magnetometer units are irrelevant as the vector is
 * normalised.
 * 
 */
public class AHRSFilter extends Filter {

	// ----------------------------------------------------------------------------------------------------
	// Definitions

	protected static final double Kp = 2.0; // proportional gain governs rate of
											// convergence to
											// accelerometer/magnetometer
	protected static final double Ki = 0.005; // integral gain governs rate of
												// convergence of gyroscope
												// biases

	// ---------------------------------------------------------------------------------------------------
	// Variable definitions

	// quaternion elements representing the estimated orientation
	double q0;
	double q1;
	double q2;
	double q3;
	double exInt, eyInt, ezInt; // scaled integral error

	Quaternion qFilt = new Quaternion();

	public AHRSFilter() {
		initInteral();
	}

	// ====================================================================================================
	// Function
	// ====================================================================================================

	@Override
	public Quaternion filterStep(double gx, double gy, double gz, double ax,
			double ay, double az, double mx, double my, double mz) {
		double norm;
		double hx, hy, hz, bx, bz;
		double vx, vy, vz, wx, wy, wz;
		double ex, ey, ez;

		q0 = qFilt.get(0);
		q1 = qFilt.get(1);
		q2 = qFilt.get(2);
		q3 = qFilt.get(3);

		// auxiliary variables to reduce number of repeated operations
		double q0q0 = q0 * q0;
		double q0q1 = q0 * q1;
		double q0q2 = q0 * q2;
		double q0q3 = q0 * q3;
		double q1q1 = q1 * q1;
		double q1q2 = q1 * q2;
		double q1q3 = q1 * q3;
		double q2q2 = q2 * q2;
		double q2q3 = q2 * q3;
		double q3q3 = q3 * q3;

		double halfT = samplePeriod / 2; // half the sample period (inverse of
											// sample rate) in s

		// normalise the measurements
		norm = Math.sqrt(ax * ax + ay * ay + az * az);
		ax = ax / norm;
		ay = ay / norm;
		az = az / norm;
		norm = Math.sqrt(mx * mx + my * my + mz * mz);
		mx = mx / norm;
		my = my / norm;
		mz = mz / norm;

		// compute reference direction of flux
		hx = 2 * mx * (0.5 - q2q2 - q3q3) + 2 * my * (q1q2 - q0q3) + 2 * mz
				* (q1q3 + q0q2);
		hy = 2 * mx * (q1q2 + q0q3) + 2 * my * (0.5 - q1q1 - q3q3) + 2 * mz
				* (q2q3 - q0q1);
		hz = 2 * mx * (q1q3 - q0q2) + 2 * my * (q2q3 + q0q1) + 2 * mz
				* (0.5 - q1q1 - q2q2);
		bx = Math.sqrt((hx * hx) + (hy * hy));
		bz = hz;

		// estimated direction of gravity and flux (v and w)
		vx = 2 * (q1q3 - q0q2);
		vy = 2 * (q0q1 + q2q3);
		vz = q0q0 - q1q1 - q2q2 + q3q3;

		wx = 2 * bx * (0.5 - q2q2 - q3q3) + 2 * bz * (q1q3 - q0q2);
		wy = 2 * bx * (q1q2 - q0q3) + 2 * bz * (q0q1 + q2q3);
		wz = 2 * bx * (q0q2 + q1q3) + 2 * bz * (0.5 - q1q1 - q2q2);

		// error is sum of cross product between reference direction of fields
		// and direction measured by sensors
		ex = (ay * vz - az * vy) + (my * wz - mz * wy);
		ey = (az * vx - ax * vz) + (mz * wx - mx * wz);
		ez = (ax * vy - ay * vx) + (mx * wy - my * wx);

		// TODO I got better results without that correction...??
		// integral error scaled integral gain
		// exInt = exInt + ex * Ki;
		// eyInt = eyInt + ey * Ki;
		// ezInt = ezInt + ez * Ki;
		//
		// // adjusted gyroscope measurements
		// gx = gx + Kp * ex + exInt;
		// gy = gy + Kp * ey + eyInt;
		// gz = gz + Kp * ez + ezInt;

		// integrate quaternion rate and normalise
		q0 = q0 + (-q1 * gx - q2 * gy - q3 * gz) * halfT;
		q1 = q1 + (q0 * gx + q2 * gz - q3 * gy) * halfT;
		q2 = q2 + (q0 * gy - q1 * gz + q3 * gx) * halfT;
		q3 = q3 + (q0 * gz + q1 * gy - q2 * gx) * halfT;

		// normalise quaternion
		norm = Math.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
		q0 = q0 / norm;
		q1 = q1 / norm;
		q2 = q2 / norm;
		q3 = q3 / norm;

		qFilt = updateAndAdjust(new Quaternion(q0, q1, q2, q3));

		return qFilt;
	}

	@Override
	public Quaternion getFilteredQuaternions() {
		return qFilt;
	}

	@Override
	protected void initCalibration() {
		//not used
	}

	@Override
	public void initInteral() {
		// quaternion elements representing the estimated orientation
		qFilt = initialOrientation;

		// scaled integral error
		exInt = 0;
		eyInt = 0;
		ezInt = 0;

	}

}
