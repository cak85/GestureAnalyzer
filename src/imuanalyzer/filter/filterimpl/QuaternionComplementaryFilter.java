package imuanalyzer.filter.filterimpl;

import imuanalyzer.filter.Filter;
import imuanalyzer.filter.IIRFilter;
import imuanalyzer.filter.Quaternion;

import java.util.ArrayList;
import java.util.List;

public class QuaternionComplementaryFilter extends Filter {

	private static final int IIR_LENGTH = 10;

	// TODO Ist verantwortlich für das weiterdriften (original war 0.98 aber
	// damit drift mit 1 oder 0.99 weniger)
	private static final double k = 0.99;

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

	int step;

	IIRFilter magnFilter;
	IIRFilter accFilter;

	Quaternion qFilt;
	Quaternion qGyroFilt;
	Quaternion qObserv;

	int calibrationStep = 0;
	Quaternion calibrationAVG;

	public static final int CALIBRATION_AVG_LENGTH = 100;

	public QuaternionComplementaryFilter() {
		initInteral();
	}

	@Override
	public void initInteral() {

		calibrationAVG = new Quaternion();

		step = 0;

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

		qFilt = new Quaternion(initialOrientation);
		qGyroFilt = new Quaternion(qFilt);
		qObserv = new Quaternion(qFilt);

	}

	@Override
	public Quaternion filterStep(double w_x, double w_y, double w_z,
			double a_x, double a_y, double a_z, double m_x, double m_y,
			double m_z) {

		double norm = 0;

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

		if (AccObservX.size() < IIR_LENGTH) {
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
		if (MagnObservX.size() < IIR_LENGTH) {
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

		if (step < IIR_LENGTH) {

		} else {
			accFilter.Applyfilter(AccObservX, AccFiltX);
			accFilter.Applyfilter(AccObservY, AccFiltY);
			accFilter.Applyfilter(AccObservZ, AccFiltZ);

			magnFilter.Applyfilter(MagnObservX, MagnFiltX);
			magnFilter.Applyfilter(MagnObservY, MagnFiltY);
			magnFilter.Applyfilter(MagnObservZ, MagnFiltZ);

			a_x = AccFiltX.get(AccFiltX.size() - 1);
			a_y = AccFiltY.get(AccFiltY.size() - 1);
			a_z = AccFiltZ.get(AccFiltZ.size() - 1);

			// System.out.println("Accel: " + a_x*100 + " " + a_y*100 + " " +
			// a_z*100);

			m_x = MagnFiltX.get(MagnFiltX.size() - 1);
			m_y = MagnFiltY.get(MagnFiltY.size() - 1);
			m_z = MagnFiltZ.get(MagnFiltZ.size() - 1);

			double normA = Math.sqrt(a_x * a_x + a_y * a_y + a_z * a_z);
			double normM = Math.sqrt(m_x * m_x + m_y * m_y + m_z * m_z);

			a_x /= normA;
			a_y /= normA;
			a_z /= normA;

			m_x /= normM;
			m_y /= normM;
			m_z /= normM;

			Quaternion qFilt = this.qFilt;

			double accelNorm = currentDynAcceleration.getNorm();
			// System.out.printf("AccelNorm: %.3f\n", accelNorm);

			if (accelNorm == 0) { //no Acceleration = no Rotation
				//I think this is not necessary because orientation was already
				//adjusted last time
				//this.qFilt = updateAndAdjust(qFilt);
				return qFilt;
			}

			// after testing it seems to be better to disable computing this
			// average smoothing by using the last orientation
			// Quaternion dq = (Quaternion.quaternionProduct(qFilt,
			// new Quaternion(0, w_x, w_y, w_z))).times(0.5);
			Quaternion dq = new Quaternion(0, w_x, w_y, w_z);
			qGyroFilt = qFilt.plus(dq.times(samplePeriod));

			norm = qGyroFilt.getNorm();

			qGyroFilt = qGyroFilt.times(1 / norm);

			double dqNorm = dq.getNorm();

			if (this.obsMethod == 0) {
				double mu = 10 * dqNorm * samplePeriod;
				qObserv = new Quaternion(GradientDescent(a_x, a_y, a_z, m_x,
						m_y, m_z, mu, qFilt.getQuaternionAsVector()));
			} else {
				qObserv = new Quaternion(gaussNewtonMethod(a_x, a_y, a_z, m_x,
						m_y, m_z, qFilt.getQuaternionAsVector()));
			}

			if (calibrationMode) {
				if (calibrationStep < CALIBRATION_AVG_LENGTH) {
					calibrationAVG = calibrationAVG.plus(qFilt);
					System.out.println("AVG Sum");
					calibrationAVG.print(6);
				} else if (calibrationStep == CALIBRATION_AVG_LENGTH) {
					calibrationAVG = calibrationAVG
							.times(1 / (double) CALIBRATION_AVG_LENGTH);
				} else if (calibrationStep > CALIBRATION_AVG_LENGTH * 3) {
					calibrationMode = false;
				} else if (calibrationStep > CALIBRATION_AVG_LENGTH) {
					// System.out.println("AVG:");
					// calibrationAVG.print(1, 6);
					// System.out.println("QFILT:");
					// qFilt.print(1, 6);
					// System.out.println("Diff:");
					// calibrationAVG.minus(qFilt).print(1, 6);
					// System.out.println("Observ:");
					// qObserv.print(1, 6);, w
					// System.out.println("Adjust:");
					Quaternion adjust = qGyroFilt.times(k).plus(
							qObserv.times(1 - k));
					// adjust.print(1, 6);
					System.out.println("Adjust-Qfilt:");
					Quaternion diff = adjust.minus(qFilt);

					norm = diff.getNorm();
					System.out.println("" + norm);
				}
				calibrationStep++;
			} else {
				qFilt = qGyroFilt.times(k).plus(qObserv.times(1 - k));
			}
			norm = qFilt.getNorm();

			this.qFilt = updateAndAdjust(qFilt.times(1 / norm));

		}

		step++;

		return this.qFilt;

	}

	@Override
	public Quaternion getFilteredQuaternions() {
		return qFilt;
	}

	@Override
	protected void initCalibration() {
		calibrationStep = 0;
		calibrationAVG = new Quaternion();
	}

}
