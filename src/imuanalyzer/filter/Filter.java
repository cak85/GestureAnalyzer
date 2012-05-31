package imuanalyzer.filter;

import org.apache.log4j.Logger;

import Jama.Matrix;

public abstract class Filter {

	private static final Logger LOGGER = Logger.getLogger(Filter.class
			.getName());

	protected final static double ACCELERATION_LOW_PASS = 4; // 6
	protected static final int ACCELERATION_AVG_BUFFER_LENGTH = 25;

	// useful for restriction updating....
	protected volatile IFilterListener listener = null;

	protected volatile boolean calibrationMode = false;

	protected volatile int obsMethod = 0;

	// sampling period in seconds on host filter updating routine
	protected double samplePeriod = 0.025;

	protected Quaternion initialOrientation = new Quaternion(1, 0, 0, 0);

	protected Quaternion staticGravityAcceleration = new Quaternion(0, 0, 0,
			-250);

	protected Quaternion currentDynAcceleration;

	// TODO try longer shorter buffer
	protected RunningAvg a_x_avg = new RunningAvg(
			ACCELERATION_AVG_BUFFER_LENGTH);
	protected RunningAvg a_y_avg = new RunningAvg(
			ACCELERATION_AVG_BUFFER_LENGTH);
	protected RunningAvg a_z_avg = new RunningAvg(
			ACCELERATION_AVG_BUFFER_LENGTH);

	protected Quaternion gravity_orientation = new Quaternion();

	// trägheitsfaktor
	protected static final float velocityScale = 1;// 0.1f;

	protected double currentVelocityX = 0;
	protected double currentVelocityY = 0;
	protected double currentVelocityZ = 0;

	public void init() {
		initInteral();
	}

	// abstract methods

	protected abstract void initInteral();

	protected abstract Quaternion filterStep(double w_x, double w_y,
			double w_z, double a_x, double a_y, double a_z, double m_x,
			double m_y, double m_z);

	public abstract Quaternion getFilteredQuaternions();

	protected void initCalibration() {

	}

	protected Matrix GradientDescent(double a_x, double a_y, double a_z,
			double m_x, double m_y, double m_z, double mu, Matrix qObserv) {
		int i = 0;
		double q1, q2, q3, q4;
		Matrix f_obb = new Matrix(6, 1);
		Matrix Jacobian = new Matrix(6, 4);
		Matrix Df = new Matrix(4, 1);
		double norm;
		double bx, bz, by;
		Matrix result = new Matrix(4, 1);

		q1 = qObserv.get(0, 0);
		q2 = qObserv.get(1, 0);
		q3 = qObserv.get(2, 0);
		q4 = qObserv.get(3, 0);

		norm = Math.sqrt(m_x * m_x + m_y * m_y + m_z * m_z);
		m_x /= norm;
		m_y /= norm;
		m_z /= norm;

		while (i < 10) {
			// compute the direction of the magnetic field
			Matrix quaternion = new Quaternion(q1, q2, q3, q4)
					.getQuaternionAsVector();
			Matrix bRif = magneticCompensation(new Quaternion(q1, q2, q3, q4),
					m_x, m_y, m_z);
			bx = bRif.get(0, 0);
			by = bRif.get(1, 0);
			bz = bRif.get(2, 0);

			// compute the objective functions
			f_obb.set(0, 0, 2 * (q2 * q4 - q1 * q3) - a_x);
			f_obb.set(1, 0, 2 * (q1 * q2 + q3 * q4) - a_y);
			f_obb.set(2, 0, 2 * (0.5 - q2 * q2 - q3 * q3) - a_z);
			f_obb.set(3, 0, 2 * bx * (0.5 - q3 * q3 - q4 * q4) + 2 * by
					* (q1 * q4 + q2 * q3) + 2 * bz * (q2 * q4 - q1 * q3) - m_x);
			f_obb.set(4, 0, 2 * bx * (q2 * q3 - q1 * q4) + 2 * by
					* (0.5 - q2 * q2 - q4 * q4) + 2 * bz * (q1 * q2 + q3 * q4)
					- m_y);
			f_obb.set(5, 0, 2 * bx * (q1 * q3 + q2 * q4) + 2 * by
					* (q3 * q4 - q1 * q2) + 2 * bz * (0.5 - q2 * q2 - q3 * q3)
					- m_z);

			// compute the jacobian
			Jacobian.set(0, 0, -2 * q3);
			Jacobian.set(0, 1, 2 * q4);
			Jacobian.set(0, 2, -2 * q1);
			Jacobian.set(0, 3, 2 * q2);
			Jacobian.set(1, 0, 2 * q2);
			Jacobian.set(1, 1, 2 * q1);
			Jacobian.set(1, 2, 2 * q4);
			Jacobian.set(1, 3, 2 * q3);
			Jacobian.set(2, 0, 0);
			Jacobian.set(2, 1, -4 * q2);
			Jacobian.set(2, 2, -4 * q3);
			Jacobian.set(2, 3, 0);

			Jacobian.set(3, 0, 2 * by * q4 - 2 * bz * q3);
			Jacobian.set(3, 1, 2 * by * q3 + 2 * bz * q4);
			Jacobian.set(3, 2, -4 * bx * q3 + 2 * by * q2 - 2 * bz * q1);
			Jacobian.set(3, 3, -4 * bx * q4 + 2 * by * q1 + 2 * bz * q2);
			Jacobian.set(4, 0, -2 * bx * q4 + 2 * bz * q2);
			Jacobian.set(4, 1, 2 * bx * q3 - 4 * by * q2 + 2 * bz * q1);
			Jacobian.set(4, 2, 2 * bx * q2 + 2 * bz * q4);
			Jacobian.set(4, 3, -2 * bx * q1 - 4 * by * q4 + 2 * bz * q3);
			Jacobian.set(5, 0, 2 * bx * q3 - 2 * by * q2);
			Jacobian.set(5, 1, 2 * bx * q4 - 2 * by * q1 - 4 * bz * q2);
			Jacobian.set(5, 2, 2 * bx * q1 + 2 * by * q4 - 4 * bz * q3);
			Jacobian.set(5, 3, 2 * bx * q2 + 2 * by * q3);

			Df = Jacobian.transpose().times(f_obb);

			norm = Math.sqrt(Df.get(0, 0) * Df.get(0, 0) + Df.get(1, 0)
					* Df.get(1, 0) + Df.get(2, 0) * Df.get(2, 0) + Df.get(3, 0)
					* Df.get(3, 0));

			Df = Df.times(1 / norm);

			result = quaternion.minus(Df.times(mu));

			q1 = result.get(0, 0);
			q2 = result.get(1, 0);
			q3 = result.get(2, 0);
			q4 = result.get(3, 0);

			norm = Math.sqrt(q1 * q1 + q2 * q2 + q3 * q3 + q4 * q4);
			result = result.times(1 / norm);

			q1 = result.get(0, 0);
			q2 = result.get(1, 0);
			q3 = result.get(2, 0);
			q4 = result.get(3, 0);

			i = i + 1;
		}

		return result;
	}

	protected Matrix gaussNewtonMethod(double a_x, double a_y, double a_z,
			double m_x, double m_y, double m_z, Matrix qObserv) {
		double norm;
		Matrix n = new Matrix(4, 1);
		Matrix bRif = new Matrix(3, 1);
		Matrix jacobian = new Matrix(6, 4);
		Matrix R = new Matrix(6, 6);
		Matrix y_e = new Matrix(6, 1);
		Matrix y_b = new Matrix(6, 1);

		double a = qObserv.get(1, 0);
		double b = qObserv.get(2, 0);
		double c = qObserv.get(3, 0);
		double d = qObserv.get(0, 0);

		int i = 0;

		Matrix n_k = new Matrix(4, 1);
		n_k.set(0, 0, a);
		n_k.set(1, 0, b);
		n_k.set(2, 0, c);
		n_k.set(3, 0, d);

		norm = Math.sqrt(m_x * m_x + m_y * m_y + m_z * m_z);
		m_x /= norm;
		m_y /= norm;
		m_z /= norm;

		while (i < 3) {
			Quaternion q = new Quaternion(d, a, b, c);

			bRif = magneticCompensation(q, m_x, m_y, m_z);
			double bx = bRif.get(0, 0);
			double by = bRif.get(1, 0);
			double bz = bRif.get(2, 0);

			// Jacobian Computation
			double j11 = (2 * a * a_x + 2 * b * a_y + 2 * c * a_z);
			double j12 = (-2 * b * a_x + 2 * a * a_y + 2 * d * a_z);
			double j13 = (-2 * c * a_x - 2 * d * a_y + 2 * a * a_z);
			double j14 = (2 * d * a_x - 2 * c * a_y + 2 * b * a_z);

			double j21 = (2 * b * a_x - 2 * a * a_y - 2 * d * a_z);
			double j22 = (2 * a * a_x + 2 * b * a_y + 2 * c * a_z);
			double j23 = (2 * d * a_x - 2 * c * a_y + 2 * b * a_z);
			double j24 = (2 * c * a_x + 2 * d * a_y - 2 * a * a_z);

			double j31 = (2 * c * a_x + 2 * d * a_y - 2 * a * a_z);
			double j32 = (-2 * d * a_x + 2 * c * a_y - 2 * b * a_z);
			double j33 = (2 * a * a_x + 2 * b * a_y + 2 * c * a_z);
			double j34 = (-2 * b * a_x + 2 * a * a_y + 2 * d * a_z);

			double j41 = (2 * a * m_x + 2 * b * m_y + 2 * c * m_z);
			double j42 = (-2 * b * m_x + 2 * a * m_y + 2 * m_z * d);
			double j43 = (-2 * c * m_x - 2 * d * m_y + 2 * a * m_z);
			double j44 = (2 * d * m_x - 2 * c * m_y + 2 * b * m_z);

			double j51 = (2 * b * m_x - 2 * a * m_y - 2 * d * m_z);
			double j52 = (2 * a * m_x + 2 * b * m_y + 2 * c * m_z);
			double j53 = (2 * d * m_x - 2 * c * m_y + 2 * b * m_z);
			double j54 = (2 * c * m_x + 2 * d * m_y - 2 * a * m_z);

			double j61 = (2 * c * m_x + 2 * d * m_y - 2 * a * m_z);
			double j62 = (-2 * d * m_x + 2 * c * m_y - 2 * b * m_z);
			double j63 = (2 * a * m_x + 2 * b * m_y + 2 * c * m_z);
			double j64 = (-2 * b * m_x + 2 * a * m_y + 2 * d * m_z);

			jacobian.set(0, 0, j11);
			jacobian.set(0, 1, j12);
			jacobian.set(0, 2, j13);
			jacobian.set(0, 3, j14);

			jacobian.set(1, 0, j21);
			jacobian.set(1, 1, j22);
			jacobian.set(1, 2, j23);
			jacobian.set(1, 3, j24);

			jacobian.set(2, 0, j31);
			jacobian.set(2, 1, j32);
			jacobian.set(2, 2, j33);
			jacobian.set(2, 3, j34);

			jacobian.set(3, 0, j41);
			jacobian.set(3, 1, j42);
			jacobian.set(3, 2, j43);
			jacobian.set(3, 3, j44);

			jacobian.set(4, 0, j51);
			jacobian.set(4, 1, j52);
			jacobian.set(4, 2, j53);
			jacobian.set(4, 3, j54);

			jacobian.set(5, 0, j61);
			jacobian.set(5, 1, j62);
			jacobian.set(5, 2, j63);
			jacobian.set(5, 3, j64);
			jacobian = jacobian.times(-1);
			// End Jacobian Computation

			// DCM Rotation Matrix

			R.set(0, 0, d * d + a * a - b * b - c * c);
			R.set(0, 1, 2 * (a * b - c * d));
			R.set(0, 2, 2 * (a * c + b * d));
			R.set(1, 0, 2 * (a * b + c * d));
			R.set(1, 1, d * d + b * b - a * a - c * c);
			R.set(1, 2, 2 * (b * c - a * d));
			R.set(2, 0, 2 * (a * c - b * d));
			R.set(2, 1, 2 * (b * c + a * d));
			R.set(2, 2, d * d + c * c - b * b - a * a);

			R.set(3, 3, d * d + a * a - b * b - c * c);
			R.set(3, 4, 2 * (a * b - c * d));
			R.set(3, 5, 2 * (a * c + b * d));
			R.set(4, 3, 2 * (a * b + c * d));
			R.set(4, 4, d * d + b * b - a * a - c * c);
			R.set(4, 5, 2 * (b * c - a * d));
			R.set(5, 3, 2 * (a * c - b * d));
			R.set(5, 4, 2 * (b * c + a * d));
			R.set(5, 5, d * d + c * c - b * b - a * a);

			R.set(3, 0, 0);
			R.set(3, 1, 0);
			R.set(3, 2, 0);
			R.set(4, 0, 0);
			R.set(4, 1, 0);
			R.set(4, 2, 0);
			R.set(5, 0, 0);
			R.set(5, 1, 0);
			R.set(5, 2, 0);

			R.set(0, 3, 0);
			R.set(0, 4, 0);
			R.set(0, 5, 0);
			R.set(1, 3, 0);
			R.set(1, 4, 0);
			R.set(1, 5, 0);
			R.set(2, 3, 0);
			R.set(2, 4, 0);
			R.set(2, 5, 0);
			// End DCM

			// Reference Vector

			y_e.set(0, 0, 0);
			y_e.set(1, 0, 0);
			y_e.set(2, 0, 1);
			y_e.set(3, 0, bx);
			y_e.set(4, 0, by);
			y_e.set(5, 0, bz);
			// Body frame Vector

			y_b.set(0, 0, a_x);
			y_b.set(1, 0, a_y);
			y_b.set(2, 0, a_z);
			y_b.set(3, 0, m_x);
			y_b.set(4, 0, m_y);
			y_b.set(5, 0, m_z);

			// Gauss Newton Step
			n = n_k.minus((jacobian.transpose().times(jacobian)).inverse()
					.times(jacobian.transpose())
					.times((y_e.minus(R.times(y_b)))));

			double normGauss = Math.sqrt(n.get(0, 0) * n.get(0, 0)
					+ n.get(1, 0) * n.get(1, 0) + n.get(2, 0) * n.get(2, 0)
					+ n.get(3, 0) * n.get(3, 0));

			n = n.times(1 / normGauss);

			a = n.get(0, 0);
			b = n.get(1, 0);
			c = n.get(2, 0);
			d = n.get(3, 0);

			n_k.set(0, 0, a);
			n_k.set(1, 0, b);
			n_k.set(2, 0, c);
			n_k.set(3, 0, d);

			i++;
		}
		return new Quaternion(d, a, b, c).getQuaternionAsVector();

	}

	protected Matrix magneticCompensation(Quaternion q, double m_x, double m_y,
			double m_z) {

		Quaternion h = new Quaternion();
		Quaternion temp;
		// compute the direction of the magnetic field
		Quaternion quaternion = q;
		Quaternion quaternion_conjugate = q.getConjugate();

		// magnetic field compensation
		temp = Quaternion.quaternionProduct(quaternion, new Quaternion(0.0,
				m_x, m_y, m_z));
		h = Quaternion.quaternionProduct(temp, quaternion_conjugate);
		double bx = Math.sqrt((h.getX() * h.getX() + h.getY() * h.getY()));
		double bz = h.getZ();

		double norm = Math.sqrt(bx * bx + bz * bz);
		bx /= norm;
		bz /= norm;
		Matrix result = new Matrix(3, 1);
		result.set(0, 0, bx);
		result.set(1, 0, 0);
		result.set(2, 0, bz);
		return result;
	}

	public void filterStep(double samplePeriod, double w_x, double w_y,
			double w_z, double a_x, double a_y, double a_z, double m_x,
			double m_y, double m_z) {

		this.samplePeriod = samplePeriod;

		// TODO möglicherweise besser die gerade berechnete orientierung zu
		// nutzen, allerdings kann so die beschleunigung direkt für die
		// bestimmung der Orientierung verwendet werden
		// currentDynAcceleration = calculateDynAcceleration(a_x, a_y, a_z,
		// getFilteredQuaternions());

		Quaternion currentOrientation = filterStep(w_x, w_y, w_z, a_x, a_y,
				a_z, m_x, m_y, m_z);

		currentDynAcceleration = calculateDynAcceleration(a_x, a_y, a_z,
				currentOrientation);

		Quaternion convertedDynAccelration = convertAccelerationToMetersPerSquareSecond(currentDynAcceleration);

		calculateMoveAndVelocity(convertedDynAccelration, samplePeriod);

		listener.updateAcceleration(convertedDynAccelration);

		// System.out.printf("X: %.3f Y: %.3f Z: %.3f\n", currentPosition.x,
		// currentPosition.y, currentPosition.z);

	}

	private static final double GRAVITAION = 9.80665;

	private Quaternion convertAccelerationToMetersPerSquareSecond(
			Quaternion dynAcceleration) {
		// 1g=256 sensor resolution; 1g= 9.81m/s^2
		double x = dynAcceleration.getX() / 256 * GRAVITAION;
		double y = dynAcceleration.getY() / 256 * GRAVITAION;
		double z = dynAcceleration.getZ() / 256 * GRAVITAION;

		return new Quaternion(0, x, y, z);
	}

	protected void calculateMoveAndVelocity(Quaternion acceleration, double time) {

		double v_x = currentVelocityX + acceleration.getX() * time;
		double v_y = currentVelocityY + acceleration.getY() * time;
		double v_z = currentVelocityZ + acceleration.getZ() * time;

		Quaternion move = new Quaternion(0, (v_x * time) * velocityScale,
				(v_y * time) * velocityScale, (v_z * time) * velocityScale);

		if (move.getNorm() > 0) {
			listener.updateMove(move);
		}
	}
	
	LowPass accelLowPass = new LowPass(0.5f);

	private Quaternion calculateDynAcceleration(double a_x, double a_y,
			double a_z, Quaternion currentOrientation) {
		
		//first low pass
		Quaternion accelRaw = new Quaternion(0, a_x, a_y, a_z);
		
		accelRaw= accelLowPass.filter(accelRaw);

		//running average
		a_x_avg.add(accelRaw.getX());
		a_y_avg.add(accelRaw.getY());
		a_z_avg.add(accelRaw.getZ());

		double current_avg_x = a_x_avg.getAvg();
		double current_avg_y = a_y_avg.getAvg();
		double current_avg_z = a_z_avg.getAvg();

		double current_avg_x_diff = current_avg_x - a_x;
		double current_avg_y_diff = current_avg_y - a_y;
		double current_avg_z_diff = current_avg_z - a_z;

		// System.out.println("AVG Diff x:"+current_avg_x_diff+" y:"+current_avg_y_diff+" z:"+current_avg_z_diff);
		
		// if the difference of current acceleration and the running average of
		// a period is mininmal
		// we assume current acceleration is gravity
		// the requirement for this assumption is that we could not achieve a
		// static acceleration
		if (current_avg_x_diff < 1 && current_avg_y_diff < 1
				&& current_avg_z_diff < 1) {
			// staticGravityAcceleration = new Quaternion(0, current_avg_x,
			// current_avg_y, current_avg_z);
			staticGravityAcceleration = accelRaw;
			// LOGGER.debug("Gravity");
			// staticGravityAcceleration.print(3);
			// LOGGER.debug("Gravity updated");
			gravity_orientation = currentOrientation;
			currentVelocityX = 0;
			currentVelocityY = 0;
			currentVelocityZ = 0;
		}

		// System.out.println("AccelRaw");
		// accelRaw.print(3);
		// System.out.println("GQ");
		// gravity_orientation.print(3);
		//
		// System.out.println("CQ");
		// currentOrientation.print(3);

		// get orientation difference between current orientation and
		// orientation messured during definition of gravity
		Quaternion DQ = gravity_orientation.getConjugate().quaternionProduct(
				currentOrientation);
		// System.out.println("DQ");
		// DQ.print(3);

		// rotate gravity along inverse direction of device
		DQ = DQ.getConjugate();

		// rotate gravity vector
		Quaternion rotatedStaticAcceleration = DQ.quaternionProduct(
				staticGravityAcceleration).quaternionProduct(DQ.getConjugate());
		// System.out.println("CGQ");
		// rotatedStaticAcceleration.print(3);

		// substract gravity in bodyframe from current raw acceleration
		accelRaw = accelRaw.minus(rotatedStaticAcceleration);

		// System.out.println("Norm "+);
		// This is an acceleration low pass
		if (accelRaw.getNorm() < ACCELERATION_LOW_PASS) {
			accelRaw.clear();
		}

		// System.out.println("AccelDyn");
		// accelRaw.print(3);
		return accelRaw;
	}

	public Quaternion updateAndAdjust(Quaternion quad) {
		if (listener != null) {
			return listener.updateOrientation(quad);
		} else {
			return quad;
		}
	}

	public void setInitialOrientationQuaternion(double w, double x, double y,
			double z) {
		try {
			setInitialOrientationQuaternion(new Quaternion(w, x, y, z));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setInitialOrientationQuaternion(Quaternion quad)
			throws Exception {
		initialOrientation = quad;
	}

	public void setObsMethod(int param) {
		this.obsMethod = param;
	}

	public int getObsMethod() {
		return obsMethod;
	}

	public void setCalibrationMode(boolean calibrationMode) {
		this.calibrationMode = calibrationMode;
		if (this.calibrationMode) {
			LOGGER.debug("Start calibration mode");
			initCalibration();
		}
	}

	public IFilterListener getListener() {
		return listener;
	}

	public void setListener(IFilterListener additionalCorrection) {
		if (additionalCorrection != null) {
			this.initialOrientation = additionalCorrection
					.getInitialOrientation();
		}
		this.listener = additionalCorrection;
	}
}
