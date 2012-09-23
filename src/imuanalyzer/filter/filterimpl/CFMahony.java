package imuanalyzer.filter.filterimpl;

import imuanalyzer.filter.Filter;
import imuanalyzer.utils.math.Quaternion;

/**
 * MahonyAHRS class. Madgwick's implementation of Mayhony's AHRS algorithm. See:
 * http://www.x-io.co.uk/node/8#open_source_ahrs_and_imu_algorithms
 * 
 * Mahonys AHRS Filter without magnetic distorsion correction
 */
public class CFMahony extends Filter {

	Quaternion state_filtered;

	private static double Kp = 0;
	private static double Ki = 0;
	private final static double dT = 0.5;

	double[] eInt = { 0f, 0f, 0f };

	public CFMahony() {
		super();
		initInteral();
	}

	@Override
	public void initInteral() {

		eInt[0] = 0;
		eInt[1] = 0;
		eInt[2] = 0;

		state_filtered = new Quaternion(initialOrientation);
	}

	@Override
	public Quaternion filterStep(double gx, double gy, double gz, double ax,
			double ay, double az, double mx, double my, double mz, double temp) {
		double q1 = state_filtered.getW();
		double q2 = state_filtered.getX();
		double q3 = state_filtered.getY();
		double q4 = state_filtered.getZ(); // short name local variable for
											// readability

		double norm;
		double hx, hy, bx, bz;
		double vx, vy, vz, wx, wy, wz;
		double ex, ey, ez;
		double pa, pb, pc;

		// Auxiliary variables to avoid repeated arithmetic
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
			return updateAndAdjust(state_filtered);
		; // handle NaN
		norm = 1 / norm; // use reciprocal for division
		ax *= norm;
		ay *= norm;
		az *= norm;

		// Normalise magnetometer measurement
		norm = (double) Math.sqrt(mx * mx + my * my + mz * mz);
		if (norm == 0f)
			return updateAndAdjust(state_filtered);
		; // handle NaN
		norm = 1 / norm; // use reciprocal for division
		mx *= norm;
		my *= norm;
		mz *= norm;

		// Reference direction of Earth's magnetic field
		hx = 2f * mx * (0.5f - q3q3 - q4q4) + 2f * my * (q2q3 - q1q4) + 2f * mz
				* (q2q4 + q1q3);
		hy = 2f * mx * (q2q3 + q1q4) + 2f * my * (0.5f - q2q2 - q4q4) + 2f * mz
				* (q3q4 - q1q2);
		bx = (double) Math.sqrt((hx * hx) + (hy * hy));
		bz = 2f * mx * (q2q4 - q1q3) + 2f * my * (q3q4 + q1q2) + 2f * mz
				* (0.5f - q2q2 - q3q3);

		// Estimated direction of gravity and magnetic field
		vx = 2f * (q2q4 - q1q3);
		vy = 2f * (q1q2 + q3q4);
		vz = q1q1 - q2q2 - q3q3 + q4q4;
		wx = 2f * bx * (0.5f - q3q3 - q4q4) + 2f * bz * (q2q4 - q1q3);
		wy = 2f * bx * (q2q3 - q1q4) + 2f * bz * (q1q2 + q3q4);
		wz = 2f * bx * (q1q3 + q2q4) + 2f * bz * (0.5f - q2q2 - q3q3);

		// Error is cross product between estimated direction and measured
		// direction of gravity
		ex = (ay * vz - az * vy) + (my * wz - mz * wy);
		ey = (az * vx - ax * vz) + (mz * wx - mx * wz);
		ez = (ax * vy - ay * vx) + (mx * wy - my * wx);
		if (Ki > 0f) {
			eInt[0] += ex; // accumulate integral error
			eInt[1] += ey;
			eInt[2] += ez;
		} else {
			eInt[0] = 0.0f; // prevent integral wind up
			eInt[1] = 0.0f;
			eInt[2] = 0.0f;
		}

		// Apply feedback terms
		gx = gx + Kp * ex + Ki * eInt[0];
		gy = gy + Kp * ey + Ki * eInt[1];
		gz = gz + Kp * ez + Ki * eInt[2];

		// Integrate rate of change of quaternion
		pa = q2;
		pb = q3;
		pc = q4;
		q1 = q1 + (-q2 * gx - q3 * gy - q4 * gz) * (dT * samplePeriod);
		q2 = pa + (q1 * gx + pb * gz - pc * gy) * (dT * samplePeriod);
		q3 = pb + (q1 * gy - pa * gz + pc * gx) * (dT * samplePeriod);
		q4 = pc + (q1 * gz + pa * gy - pb * gx) * (dT * samplePeriod);

		// Normalise quaternion
		norm = (double) Math.sqrt(q1 * q1 + q2 * q2 + q3 * q3 + q4 * q4);
		norm = 1.0f / norm;

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
		return 2;
	}

	@Override
	public double getParameter(int index) {

		switch (index) {
		case 0:
			return Kp;
		case 1:
		default:
			return Ki;
		}
	}

	@Override
	public void setParameter(int index, double value) {
		switch (index) {
		case 0:
			Kp = value;
			break;
		case 1:
		default:
			Ki = value;
		}
	}

	@Override
	public double getMaxValueFromParameter(int index) {
		switch (index) {
		case 0:
			return 10;
		case 1:
		default:
			return 1;
		}
	}

	@Override
	public double getMinValueFromParameter(int index) {
		return 0;
	}

	@Override
	public String getParameterName(int index) {
		switch (index) {
		case 0:
			return "Kp";
		case 1:
		default:
			return "Ki";
		}
	}
	@Override
	public String getParameterDescription(int index) {
		switch (index) {
		case 0:
			return "Proportional gain governs rate of convergence to accelerometer/magnetometer";
		case 1:
			return "Integral gain governs rate of convergence of gyroscope biases";
		default:
			return "";
		}
	}
}
