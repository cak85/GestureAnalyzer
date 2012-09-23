package imuanalyzer.filter.filterimpl;

import imuanalyzer.filter.Filter;
import imuanalyzer.utils.math.Quaternion;

/**
 * MadgwickAHRS class. Implementation of Madgwick's IMU and AHRS algorithms.
 * See: http://www.x-io.co.uk/node/8#open_source_ahrs_and_imu_algorithms
 * 
 * This is madgwicks algorithm with magnetic distortion correction and gradient
 * decent corrective step
 */
public class CFMadgwick extends Filter {

	private Quaternion state_filtered;

	private static double beta = 0.0;
	private static final double dT = 0.5;

	public CFMadgwick() {
		super();
		initInteral();
	}

	@Override
	public void initInteral() {

		state_filtered = new Quaternion(initialOrientation);
	}

	@Override
	public Quaternion filterStep(double gx, double gy, double gz, double ax,
			double ay, double az, double mx, double my, double mz, double temp) {

		// short name local variable for
		// readability
		double q1 = state_filtered.getW();
		double q2 = state_filtered.getX();
		double q3 = state_filtered.getY();
		double q4 = state_filtered.getZ();

		double norm;
		double hx, hy, _2bx, _2bz;
		double s1, s2, s3, s4;
		double qDot1, qDot2, qDot3, qDot4;

		// Auxiliary variables to avoid repeated arithmetic
		double _2q1mx;
		double _2q1my;
		double _2q1mz;
		double _2q2mx;
		double _4bx;
		double _4bz;
		double _2q1 = 2f * q1;
		double _2q2 = 2f * q2;
		double _2q3 = 2f * q3;
		double _2q4 = 2f * q4;
		double _2q1q3 = 2f * q1 * q3;
		double _2q3q4 = 2f * q3 * q4;
		double q1q1 = q1 * q1;
		double q1q2 = q1 * q2;
		double q1q3 = q1 * q3;
		double q1q4 = q1 * q4;
		double q2q2 = q2 * q2;
		double q2q3 = q2 * q3;
		double q2q4 = q2 * q4;
		double q3q3 = q3 * q3;
		double q3q4 = q3 * q4;
		double q4q4 = q4 * q4;

		// Normalise accelerometer measurement
		norm = (double) Math.sqrt(ax * ax + ay * ay + az * az);
		if (norm == 0f)
			return updateAndAdjust(state_filtered); // handle NaN
		norm = 1 / norm; // use reciprocal for division
		ax *= norm;
		ay *= norm;
		az *= norm;

		// Normalise magnetometer measurement
		norm = (double) Math.sqrt(mx * mx + my * my + mz * mz);
		if (norm == 0f)
			return updateAndAdjust(state_filtered); // handle NaN
		norm = 1 / norm; // use reciprocal for division
		mx *= norm;
		my *= norm;
		mz *= norm;

		// Reference direction of Earth's magnetic field
		_2q1mx = 2f * q1 * mx;
		_2q1my = 2f * q1 * my;
		_2q1mz = 2f * q1 * mz;
		_2q2mx = 2f * q2 * mx;
		hx = mx * q1q1 - _2q1my * q4 + _2q1mz * q3 + mx * q2q2 + _2q2 * my * q3
				+ _2q2 * mz * q4 - mx * q3q3 - mx * q4q4;
		hy = _2q1mx * q4 + my * q1q1 - _2q1mz * q2 + _2q2mx * q3 - my * q2q2
				+ my * q3q3 + _2q3 * mz * q4 - my * q4q4;
		_2bx = (double) Math.sqrt(hx * hx + hy * hy);
		_2bz = -_2q1mx * q3 + _2q1my * q2 + mz * q1q1 + _2q2mx * q4 - mz * q2q2
				+ _2q3 * my * q4 - mz * q3q3 + mz * q4q4;
		_4bx = 2f * _2bx;
		_4bz = 2f * _2bz;

		// Gradient decent algorithm corrective step
		s1 = -_2q3 * (2f * q2q4 - _2q1q3 - ax) + _2q2
				* (2f * q1q2 + _2q3q4 - ay) - _2bz * q3
				* (_2bx * (0.5f - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx)
				+ (-_2bx * q4 + _2bz * q2)
				* (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my) + _2bx
				* q3
				* (_2bx * (q1q3 + q2q4) + _2bz * (0.5f - q2q2 - q3q3) - mz);
		s2 = _2q4 * (2f * q2q4 - _2q1q3 - ax) + _2q1
				* (2f * q1q2 + _2q3q4 - ay) - 4f * q2
				* (1 - 2f * q2q2 - 2f * q3q3 - az) + _2bz * q4
				* (_2bx * (0.5f - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx)
				+ (_2bx * q3 + _2bz * q1)
				* (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my)
				+ (_2bx * q4 - _4bz * q2)
				* (_2bx * (q1q3 + q2q4) + _2bz * (0.5f - q2q2 - q3q3) - mz);
		s3 = -_2q1 * (2f * q2q4 - _2q1q3 - ax) + _2q4
				* (2f * q1q2 + _2q3q4 - ay) - 4f * q3
				* (1 - 2f * q2q2 - 2f * q3q3 - az) + (-_4bx * q3 - _2bz * q1)
				* (_2bx * (0.5f - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx)
				+ (_2bx * q2 + _2bz * q4)
				* (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my)
				+ (_2bx * q1 - _4bz * q3)
				* (_2bx * (q1q3 + q2q4) + _2bz * (0.5f - q2q2 - q3q3) - mz);
		s4 = _2q2 * (2f * q2q4 - _2q1q3 - ax) + _2q3
				* (2f * q1q2 + _2q3q4 - ay) + (-_4bx * q4 + _2bz * q2)
				* (_2bx * (0.5f - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx)
				+ (-_2bx * q1 + _2bz * q3)
				* (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my) + _2bx
				* q2
				* (_2bx * (q1q3 + q2q4) + _2bz * (0.5f - q2q2 - q3q3) - mz);
		norm = 1f / (double) Math.sqrt(s1 * s1 + s2 * s2 + s3 * s3 + s4 * s4); // normalise
																				// step
																				// magnitude
		s1 *= norm;
		s2 *= norm;
		s3 *= norm;
		s4 *= norm;

		// Compute rate of change of quaternion
		qDot1 = dT * (-q2 * gx - q3 * gy - q4 * gz) - beta * s1;
		qDot2 = dT * (q1 * gx + q3 * gz - q4 * gy) - beta * s2;
		qDot3 = dT * (q1 * gy - q2 * gz + q4 * gx) - beta * s3;
		qDot4 = dT * (q1 * gz + q2 * gy - q3 * gx) - beta * s4;

		// Integrate to yield quaternion
		q1 += qDot1 * samplePeriod;
		q2 += qDot2 * samplePeriod;
		q3 += qDot3 * samplePeriod;
		q4 += qDot4 * samplePeriod;
		norm = 1f / (double) Math.sqrt(q1 * q1 + q2 * q2 + q3 * q3 + q4 * q4); // normalise
																				// quaternion

		state_filtered = updateAndAdjust(new Quaternion(q1 * norm, q2 * norm,
				q3 * norm, q4 * norm));

		return state_filtered;
	}

	@Override
	public Quaternion getFilteredQuaternions() {
		return state_filtered;
	}

	@Override
	public int getNumberOfParameters() {
		return 1;
	}

	@Override
	public double getParameter(int index) {
		return beta;
	}

	@Override
	public void setParameter(int index, double value) {
		beta = value;
	}

	@Override
	public double getMaxValueFromParameter(int index) {
		return 1;
	}

	@Override
	public double getMinValueFromParameter(int index) {
		return 0;
	}

	public String getParameterName(int index) {
		return "Beta";
	}
	
	public String getParameterDescription(int index) {
		return "Gyroskop Bias Gain";
	}

}
