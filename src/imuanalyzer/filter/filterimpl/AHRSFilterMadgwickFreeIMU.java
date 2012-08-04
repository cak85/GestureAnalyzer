package imuanalyzer.filter.filterimpl;

import imuanalyzer.filter.Filter;
import imuanalyzer.filter.Quaternion;

/**
 * Based on Arduino implementation of FreeIMU Library, this code is also based on madgwick
 * http://www.varesano.net/projects/hardware/FreeIMU
 * @author "Christopher-Eyk Hrabia"
 *
 */
public class AHRSFilterMadgwickFreeIMU extends Filter {

	double iq0, iq1, iq2, iq3;
	double exInt, eyInt, ezInt; // scaled integral error
	double twoKp; // 2 * proportional gain (Kp)
	double twoKi; // 2 * integral gain (Ki)

	double integralFBx, integralFBy, integralFBz;

	Quaternion qFilt;

	public AHRSFilterMadgwickFreeIMU() {
		initInteral();
	}

	@Override
	public Quaternion filterStep(double gx, double gy, double gz, double ax,
			double ay, double az, double mx, double my, double mz, float temp) {

		double recipNorm;
		double q0q0, q0q1, q0q2, q0q3, q1q1, q1q2, q1q3, q2q2, q2q3, q3q3;
		double halfex = 0.0f, halfey = 0.0f, halfez = 0.0f;
		double qa, qb, qc;

		double q0, q1, q2, q3; // quaternion of sensor frame relative to
								// auxiliary frame

		q0 = qFilt.get(0);
		q1 = qFilt.get(1);
		q2 = qFilt.get(2);
		q3 = qFilt.get(3);

		// Auxiliary variables to avoid repeated arithmetic
		q0q0 = q0 * q0;
		q0q1 = q0 * q1;
		q0q2 = q0 * q2;
		q0q3 = q0 * q3;
		q1q1 = q1 * q1;
		q1q2 = q1 * q2;
		q1q3 = q1 * q3;
		q2q2 = q2 * q2;
		q2q3 = q2 * q3;
		q3q3 = q3 * q3;

		// Use magnetometer measurement only when valid (avoids NaN in
		// magnetometer normalisation)
		if ((mx != 0.0) && (my != 0.0) && (mz != 0.0)) {
			double hx, hy, bx, bz;
			double halfwx, halfwy, halfwz;

			// Normalise magnetometer measurement
			recipNorm = 1f / Math.sqrt(mx * mx + my * my + mz * mz);
			mx *= recipNorm;
			my *= recipNorm;
			mz *= recipNorm;

			// Reference direction of Earth's magnetic field
			hx = 2.0 * (mx * (0.5f - q2q2 - q3q3) + my * (q1q2 - q0q3) + mz
					* (q1q3 + q0q2));
			hy = 2.0 * (mx * (q1q2 + q0q3) + my * (0.5f - q1q1 - q3q3) + mz
					* (q2q3 - q0q1));
			bx = Math.sqrt(hx * hx + hy * hy);
			bz = 2.0 * (mx * (q1q3 - q0q2) + my * (q2q3 + q0q1) + mz
					* (0.5f - q1q1 - q2q2));

			// Estimated direction of magnetic field
			halfwx = bx * (0.5f - q2q2 - q3q3) + bz * (q1q3 - q0q2);
			halfwy = bx * (q1q2 - q0q3) + bz * (q0q1 + q2q3);
			halfwz = bx * (q0q2 + q1q3) + bz * (0.5f - q1q1 - q2q2);

			// Error is sum of cross product between estimated direction and
			// measured direction of field vectors
			halfex = (my * halfwz - mz * halfwy);
			halfey = (mz * halfwx - mx * halfwz);
			halfez = (mx * halfwy - my * halfwx);
		}

		// Compute feedback only if accelerometer measurement valid (avoids NaN
		// in accelerometer normalisation)
		if ((ax != 0.0) && (ay != 0.0) && (az != 0.0)) {
			double halfvx, halfvy, halfvz;

			// Normalise accelerometer measurement
			recipNorm = 1f / Math.sqrt(ax * ax + ay * ay + az * az);
			ax *= recipNorm;
			ay *= recipNorm;
			az *= recipNorm;

			// Estimated direction of gravity
			halfvx = q1q3 - q0q2;
			halfvy = q0q1 + q2q3;
			halfvz = q0q0 - 0.5f + q3q3;

			// Error is sum of cross product between estimated direction and
			// measured direction of field vectors
			halfex += (ay * halfvz - az * halfvy);
			halfey += (az * halfvx - ax * halfvz);
			halfez += (ax * halfvy - ay * halfvx);
		}

		// Apply feedback only when valid data has been gathered from the
		// accelerometer or magnetometer
		if (halfex != 0.0 && halfey != 0.0 && halfez != 0.0) {
			// Compute and apply integral feedback if enabled
			if (twoKi > 0.0) {
				// integral error scaled by Ki
				integralFBx += twoKi * halfex * samplePeriod; 
				integralFBy += twoKi * halfey * samplePeriod;
				integralFBz += twoKi * halfez * samplePeriod;
				gx += integralFBx; // apply integral feedback
				gy += integralFBy;
				gz += integralFBz;
			} else {
				integralFBx = 0.0f; // prevent integral windup
				integralFBy = 0.0f;
				integralFBz = 0.0f;
			}

			// Apply proportional feedback
			gx += twoKp * halfex;
			gy += twoKp * halfey;
			gz += twoKp * halfez;
		}

		// Integrate rate of change of quaternion
		gx *= (0.5f * samplePeriod); // pre-multiply common factors
		gy *= (0.5f * samplePeriod);
		gz *= (0.5f * samplePeriod);
		qa = q0;
		qb = q1;
		qc = q2;
		q0 += (-qb * gx - qc * gy - q3 * gz);
		q1 += (qa * gx + qc * gz - q3 * gy);
		q2 += (qa * gy - qb * gz + q3 * gx);
		q3 += (qa * gz + qb * gy - qc * gx);

		// Normalise quaternion
		recipNorm = 1f / Math.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
		q0 *= recipNorm;
		q1 *= recipNorm;
		q2 *= recipNorm;
		q3 *= recipNorm;

		qFilt = updateAndAdjust(new Quaternion(q0, q1, q2, q3));

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
		exInt = 0.0;
		eyInt = 0.0;
		ezInt = 0.0;
		twoKp = (2.0 * 0.5); // 2 * proportional gain;
		twoKi = (2.0 * 0.1); // 2 * integral gain;
		integralFBx = 0.0;
		integralFBy = 0.0;
		integralFBz = 0.0;

	}

	@Override
	public int getNumberOfParameters() {
		return 2;
	}

	@Override
	public double getParameter(int index) {

		switch (index) {
		case 0:
			return twoKp;
		case 1:
		default:
			return twoKi;
		}
	}

	@Override
	public void setParameter(int index, double value) {
		switch (index) {
		case 0:
			twoKp = value;
			break;
		case 1:
		default:
			twoKi = value;
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

	public String getParameterName(int index) {
		switch (index) {
		case 0:
			return "Kp";
		case 1:
		default:
			return "Ki";
		}
	}

}
