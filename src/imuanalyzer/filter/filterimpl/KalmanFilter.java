package imuanalyzer.filter.filterimpl;

import imuanalyzer.filter.Filter;
import imuanalyzer.filter.IIRFilter;
import imuanalyzer.filter.Quaternion;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import Jama.Matrix;

public class KalmanFilter extends Filter {
	// the variance of the roll
	private final double sigmaRoll = Math.pow((0.5647 / 180 * Math.PI), 2);
	// the variance of the pitch measure
	private final double sigmaPitch = Math.pow((0.5674 / 180 * Math.PI), 2);
	// the variance of the yaw measure

	private final double sigmaYaw = Math.pow((0.5394 / 180 * Math.PI), 2);

	Matrix weOld;

	public List<Double> aAcc;
	public List<Double> bAcc;
	public List<Double> aMagn;
	public List<Double> bMagn;

	public List<Double> AccObservX;
	public List<Double> AccObservY;
	public List<Double> AccObservZ;

	public List<Double> AccFiltX;
	public List<Double> AccFiltY;
	public List<Double> AccFiltZ;

	public List<Double> MagnObservX;
	public List<Double> MagnObservY;
	public List<Double> MagnObservZ;

	public List<Double> MagnFiltX;
	public List<Double> MagnFiltY;
	public List<Double> MagnFiltZ;

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

	Stack<double[]> MagnOsserv;
	Stack<double[]> AccOsserv;

	int countdata;
	IIRFilter magnFilter;
	IIRFilter accFilter;

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

		MagnOsserv = new Stack<double[]>();
		AccOsserv = new Stack<double[]>();

		countdata = 0;

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

		aAcc = new ArrayList<Double>();
		bAcc = new ArrayList<Double>();
		aMagn = new ArrayList<Double>();
		bMagn = new ArrayList<Double>();

		aAcc.add(1.0);
		aAcc.add(-2.9529);
		aAcc.add(2.9069);
		aAcc.add(-0.954);

		bAcc.add(0.000001597);
		bAcc.add(0.000004792);
		bAcc.add(0.000004792);
		bAcc.add(0.000001597);

		accFilter = new IIRFilter(aAcc, bAcc);

		aMagn.add(1.0);
		aMagn.add(-1.73);
		aMagn.add(0.76);

		bMagn.add(0.0078);
		bMagn.add(0.0156);
		bMagn.add(0.0078);

		magnFilter = new IIRFilter(aMagn, bMagn);

		AccObservX = new ArrayList<Double>();
		AccObservY = new ArrayList<Double>();
		AccObservZ = new ArrayList<Double>();
		AccFiltX = new ArrayList<Double>();
		AccFiltY = new ArrayList<Double>();
		AccFiltZ = new ArrayList<Double>();

		MagnObservX = new ArrayList<Double>();
		MagnObservY = new ArrayList<Double>();
		MagnObservZ = new ArrayList<Double>();
		MagnFiltX = new ArrayList<Double>();
		MagnFiltY = new ArrayList<Double>();
		MagnFiltZ = new ArrayList<Double>();

		weOld = new Matrix(4, 1);
		weOld = weOld.times(0.0);

	}

	@Override
	public Quaternion filterStep(double w_x, double w_y, double w_z,
			double a_x, double a_y, double a_z, double m_x, double m_y,
			double m_z) {
		double norm;
		Matrix temp;// =new Matrix(4,4);
		Matrix dq = new Matrix(4, 1);
		double mu;

		// normalise the accelerometer measurement
		norm = Math.sqrt(a_x * a_x + a_y * a_y + a_z * a_z);
		a_x /= norm;
		a_y /= norm;
		a_z /= norm;

		// normalise the magnetometer measurement
		norm = Math.sqrt(m_x * m_x + m_y * m_y + m_z * m_z);
		m_x /= norm;
		m_y /= norm;
		m_z /= norm;

		if (AccObservX.size() < 10) {
			AccObservX.add(a_x);
			AccObservY.add(a_y);
			AccObservZ.add(a_z);
		} else {
			AccObservX.remove(0);
			AccObservY.remove(0);
			AccObservZ.remove(0);

			AccObservX.add(a_x);
			AccObservY.add(a_y);
			AccObservZ.add(a_z);
		}
		if (MagnObservX.size() < 10) {
			MagnObservX.add(m_x);
			MagnObservY.add(m_y);
			MagnObservZ.add(m_z);
		} else {
			MagnObservX.remove(0);
			MagnObservY.remove(0);
			MagnObservZ.remove(0);
			MagnObservX.add(m_x);
			MagnObservY.add(m_y);
			MagnObservZ.add(m_z);

			// Filter stabilization
			accFilter.Applyfilter(AccObservX, AccFiltX);
			accFilter.Applyfilter(AccObservY, AccFiltY);
			accFilter.Applyfilter(AccObservZ, AccFiltZ);

			magnFilter.Applyfilter(MagnObservX, MagnFiltX);
			magnFilter.Applyfilter(MagnObservY, MagnFiltY);
			magnFilter.Applyfilter(MagnObservZ, MagnFiltZ);
		}

		if (countdata > 10) {
			a_x = AccFiltX.get(AccFiltX.size() - 1);
			a_y = AccFiltY.get(AccFiltY.size() - 1);
			a_z = AccFiltZ.get(AccFiltZ.size() - 1);

			m_x = MagnFiltX.get(MagnFiltX.size() - 1);
			m_y = MagnFiltY.get(MagnFiltY.size() - 1);
			m_z = MagnFiltZ.get(MagnFiltZ.size() - 1);

			// normalise the accelerometer measurement
			norm = Math.sqrt(a_x * a_x + a_y * a_y + a_z * a_z);
			a_x /= norm;
			a_y /= norm;
			a_z /= norm;

			// normalise the magnetometer measurement
			norm = Math.sqrt(m_x * m_x + m_y * m_y + m_z * m_z);
			m_x /= norm;
			m_y /= norm;
			m_z /= norm;
		}

		countdata++;

		if (this.obsMethod == 0) {
			// after testing it seems to be better to disable computing this
			// average smoothing by using the last orientation
			// dq = (Quaternion.quaternionProduct(state_observed, new
			// Quaternion(
			// 0.0, w_x, w_y, w_z).getQuaternionAsVector())).times(0.5)
			// .getQuaternionAsVector();
			dq = new Quaternion(0.0, w_x, w_y, w_z).getQuaternionAsVector();
			norm = Math.sqrt(dq.get(0, 0) * dq.get(0, 0) + dq.get(1, 0)
					* dq.get(1, 0) + dq.get(2, 0) * dq.get(2, 0) + dq.get(3, 0)
					* dq.get(3, 0));
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

		// this is a bit uggly because of double casting
		Quaternion ret = updateAndAdjust(new Quaternion(state_filtered));

		state_filtered = ret.getQuaternionAsVector();

		return ret;
	}

	@Override
	public Quaternion getFilteredQuaternions() {
		return new Quaternion(state_filtered);
	}

	@Override
	protected void initCalibration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCurrentFilteredQuaternion(Quaternion quaternion) {
		state_observed = new Quaternion(quaternion).getQuaternionAsVector();
		state_filtered = new Quaternion(quaternion).getQuaternionAsVector();
	}
}
