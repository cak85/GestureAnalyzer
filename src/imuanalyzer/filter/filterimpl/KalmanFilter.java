package imuanalyzer.filter.filterimpl;

import imuanalyzer.filter.Filter;
import imuanalyzer.utils.math.AngleHelper;
import imuanalyzer.utils.math.Quaternion;
import Jama.Matrix;

/**
 * Based on http://code.google.com/p/9dof-orientation-estimation/
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class KalmanFilter extends Filter {
	// the variance of the roll, calculated as square of standardabweichung
	private static double sigmaRoll = Math.pow(
			AngleHelper.radFromDeg((0.184814356)), 2); // original 0.5647
	// the variance of the pitch measure, calculated as square of
	// standardabweichung
	private static double sigmaPitch = Math.pow(
			AngleHelper.radFromDeg((0.210655094)), 2); // original 0.5674
	// the variance of the yaw measure, calculated as square of
	// standardabweichung
	private static double sigmaYaw = Math.pow(
			AngleHelper.radFromDeg((0.178495138)), 2); // original 0.5394

	Matrix weOld;

	double w_x_old;
	double w_y_old;
	double w_z_old;

	Matrix F;
	Matrix H;
	Matrix Q;
	Matrix R;

	Matrix state_filtered; // output of the filter. This is
							// the update of the state

	Matrix state_observed; // the observation calculated from
							// the accelerometer and the
							// magnetometer y(k).

	Matrix P_predicted; // the variance of prediction of the
						// state
	Matrix P_Update; // the variance of the update of the
						// state
	Matrix K; // the gain of the filter

	public KalmanFilter() {

		initInteral();

	}

	@Override
	public void initInteral() {

		F = new Matrix(4, 4);
		H = Matrix.identity(4, 4);
		Q = new Matrix(4, 4);
		R = Matrix.identity(4, 4);

		state_filtered = new Matrix(4, 1); // output of the filter. This is
											// the update of the state
		state_observed = new Matrix(4, 1); // the observation calculated from
											// the accelerometer and the
											// magnetometer y(k).

		P_predicted = new Matrix(4, 4); // the variance of prediction of the
										// state
		P_Update = new Matrix(4, 4); // the variance of the update of the
										// state
		K = new Matrix(4, 4); // the gain of the filter

		// Compute matrix Q (costant if the time interval is costant)
		Q.set(0, 0, sigmaRoll + sigmaPitch + sigmaYaw);
		Q.set(0, 1, -sigmaRoll + sigmaPitch - sigmaYaw);
		Q.set(0, 2, -sigmaRoll - sigmaPitch + sigmaYaw);
		Q.set(0, 3, sigmaRoll - sigmaPitch - sigmaYaw);
		Q.set(1, 0, -sigmaRoll + sigmaPitch - sigmaYaw);
		Q.set(1, 1, sigmaRoll + sigmaPitch + sigmaYaw);
		Q.set(1, 2, sigmaRoll - sigmaPitch - sigmaYaw);
		Q.set(1, 3, -sigmaRoll - sigmaPitch + sigmaYaw);
		Q.set(2, 0, -sigmaRoll - sigmaPitch + sigmaYaw);
		Q.set(2, 1, sigmaRoll - sigmaPitch - sigmaYaw);
		Q.set(2, 2, sigmaRoll + sigmaPitch + sigmaYaw);
		Q.set(2, 3, -sigmaRoll + sigmaPitch - sigmaYaw);
		Q.set(3, 0, sigmaRoll - sigmaPitch - sigmaYaw);
		Q.set(3, 1, -sigmaRoll - sigmaPitch + sigmaYaw);
		Q.set(3, 2, -sigmaRoll + sigmaPitch - sigmaYaw);
		Q.set(3, 3, sigmaRoll + sigmaPitch + sigmaYaw);

		R = new Matrix(4, 4);
		R.set(0, 0, 0.2);
		R.set(0, 1, 0);
		R.set(0, 2, 0);
		R.set(0, 3, 0);

		R.set(1, 0, 0);
		R.set(1, 1, 0.2);
		R.set(1, 2, 0);
		R.set(1, 3, 0);

		R.set(2, 0, 0);
		R.set(2, 1, 0);
		R.set(2, 2, 0.2);
		R.set(2, 3, 0);

		R.set(3, 0, 0);
		R.set(3, 1, 0);
		R.set(3, 2, 0);
		R.set(3, 3, 0.2);

		// Initial conditions !!!!!!!!!!!!!!!!!!!!!!!!!!!
		state_observed = new Quaternion(initialOrientation)
				.getQuaternionAsVector();
		state_filtered = new Quaternion(initialOrientation)
				.getQuaternionAsVector();
		P_Update = Matrix.identity(4, 4).times(0.1);

		weOld = new Matrix(4, 1);
		weOld = weOld.times(0.0);

	}

	@Override
	public Quaternion filterStep(double w_x, double w_y, double w_z,
			double a_x, double a_y, double a_z, double m_x, double m_y,
			double m_z, double temperatur) {
		double norm;
		Matrix temp;// =new Matrix(4,4);
		double mu;

		Matrix state_filtered = new Matrix(this.state_filtered.getArray());

		// normalise the accelerometer measurement
		norm = Math.sqrt(a_x * a_x + a_y * a_y + a_z * a_z);
		if (norm == 0) {
			return updateAndAdjust(new Quaternion(state_filtered));
		}
		a_x /= norm;
		a_y /= norm;
		a_z /= norm;

		// normalise the magnetometer measurement
		norm = Math.sqrt(m_x * m_x + m_y * m_y + m_z * m_z);
		if (norm == 0) {
			return updateAndAdjust(new Quaternion(state_filtered));
		}
		m_x /= norm;
		m_y /= norm;
		m_z /= norm;

		if (this.obsMethod == 0) {
			norm = Math.sqrt(w_x * w_x + w_y * w_y + w_z * w_z);
			if (norm == 0) {
				return updateAndAdjust(new Quaternion(state_filtered));
			}
			mu = 10 * norm * samplePeriod;
			state_observed = GradientDescent(a_x, a_y, a_z, m_x, m_y, m_z, mu,
					state_filtered);
		} else {
			state_observed = gaussNewtonMethod(a_x, a_y, a_z, m_x, m_y, m_z,
					state_filtered);
		}

		// KALMAN FILTERING

		// F matrix computing
		F.set(0, 0, 1);
		F.set(0, 1, -samplePeriod / 2 * w_x);
		F.set(0, 2, -samplePeriod / 2 * w_y);
		F.set(0, 3, -samplePeriod / 2 * w_z);
		F.set(1, 0, samplePeriod / 2 * w_x);
		F.set(1, 1, 1);
		F.set(1, 2, samplePeriod / 2 * w_z);
		F.set(1, 3, -samplePeriod / 2 * w_y);
		F.set(2, 0, samplePeriod / 2 * w_y);
		F.set(2, 1, -samplePeriod / 2 * w_z);
		F.set(2, 2, 1);
		F.set(2, 3, samplePeriod / 2 * w_x);
		F.set(3, 0, samplePeriod / 2 * w_z);
		F.set(3, 1, samplePeriod / 2 * w_y);
		F.set(3, 2, -samplePeriod / 2 * w_x);
		F.set(3, 3, 1);

		Matrix state_predicted; // the predictiion of the state
		// x(k|k-1), calculated from the
		// gyro

		// state prediction
		state_predicted = F.times(state_filtered);

		// calculate the variance of the prediction
		P_predicted = F.times(P_Update);
		P_predicted = P_predicted.times(F.transpose().plus(Q));

		// compute the gain of the filter K
		temp = H.times(P_predicted).times(H.transpose()).plus(R);
		temp = temp.inverse();
		K = P_predicted.times(H.transpose()).times(temp);

		// update the state of the system (this is the output of the filter)
		temp = state_observed.minus(H.times(state_predicted));
		state_filtered = state_predicted.plus(K.times(temp));

		// compute the variance of the state filtered
		temp = Matrix.identity(4, 4).minus(K.times(H));
		P_Update = temp.times(P_predicted);

		norm = Math.sqrt(state_filtered.get(0, 0) * state_filtered.get(0, 0)
				+ state_filtered.get(1, 0) * state_filtered.get(1, 0)
				+ state_filtered.get(2, 0) * state_filtered.get(2, 0)
				+ state_filtered.get(3, 0) * state_filtered.get(3, 0));

		state_filtered = state_filtered.times(1 / norm);

		Quaternion ret = updateAndAdjust(new Quaternion(state_filtered));

		this.state_filtered = ret.getQuaternionAsVector();

		return ret;
	}

	@Override
	public Quaternion getFilteredQuaternions() {
		return new Quaternion(state_filtered);
	}

	@Override
	public int getNumberOfParameters() {
		return 4;
	}

	@Override
	public double getParameter(int index) {

		switch (index) {
		case 0:
			return sigmaRoll;
		case 1:
			return sigmaPitch;
		case 2:
			return sigmaYaw;
		default:
			return obsMethod;
		}
	}

	@Override
	public void setParameter(int index, double value) {
		switch (index) {
		case 0:
			sigmaRoll = value;
			break;
		case 1:
			sigmaPitch = value;
			break;
		case 2:

			sigmaYaw = value;
		case 3:
		default:
			obsMethod = (int) value;
			break;
		}
	}

	@Override
	public double getMaxValueFromParameter(int index) {
		return 1;
	}

	@Override
	public double getMinValueFromParameter(int index) {
		return 0;
	}

	@Override
	public String getParameterName(int index) {
		switch (index) {
		case 0:
			return "Sigma Roll";
		case 1:
			return "Sigma Pitch";
		case 2:
			return "Sigma Yaw";
		case 3:
		default:
			return "Optimization Algorithm";
		}
	}
	
	@Override
	public String getParameterDescription(int index) {
		switch (index) {
		case 0:
			return "Variance Sigma of Roll";
		case 1:
			return "Variance Sigma of Pitch";
		case 2:
			return "Variance Sigma of Yaw";
		case 3:
		default:
			return "Optimization Algorithm select 0 = Gradient-Decent-Method ; 1 = Gaus-Newton-Method ";
		}
	}

}
