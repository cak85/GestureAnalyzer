package imuanalyzer.filter;

import imuanalyzer.utils.math.AngleHelper;
import Jama.Matrix;

//TODO move to math
public class Quaternion implements Comparable<Quaternion> {

	public final static Quaternion EMPTY = new Quaternion();

	private double w; // w
	private double x; // x rol bank
	private double y; // y pitch attidute
	private double z; // z yaw heading

	public Quaternion() {
		this(1, 0, 0, 0);
	}

	public Quaternion(Matrix matrix) {
		this(matrix.get(0, 0), matrix.get(1, 0), matrix.get(2, 0), matrix.get(
				3, 0));
	}

	public Quaternion(double[] quad) throws Exception {
		if (quad.length != 4) {
			throw new Exception("Array must have length of 4");
		}
		this.w = quad[0];
		this.x = quad[1];
		this.y = quad[2];
		this.z = quad[3];
	}

	public Quaternion(double real, double i, double j, double k) {
		this.w = real;
		this.x = i;
		this.y = j;
		this.z = k;
	}

	public Quaternion(Quaternion quad) {
		this.w = quad.w;
		this.x = quad.x;
		this.y = quad.y;
		this.z = quad.z;
	}

	/**
	 * Assumes the angles are in radian.
	 * @param roll
	 * @param pitch
	 * @param yaw
	 */
	public Quaternion(double roll, double pitch, double yaw) {
		double c1 = Math.cos(yaw / 2);
		double s1 = Math.sin(yaw / 2);
		double c2 = Math.cos(pitch / 2);
		double s2 = Math.sin(pitch / 2);
		double c3 = Math.cos(roll / 2);
		double s3 = Math.sin(roll / 2);
		double c1c2 = c1 * c2;
		double s1s2 = s1 * s2;
		w = c1c2 * c3 - s1s2 * s3;
		x = c1c2 * s3 + s1s2 * c3;
		y = s1 * c2 * c3 + c1 * s2 * s3;
		z = c1 * s2 * c3 - s1 * c2 * s3;

	}

	public void set(double real, double i, double j, double k) {
		this.w = real;
		this.x = i;
		this.y = j;
		this.z = k;
	}

	public Matrix getQuaternionAsVector() {
		Matrix result = new Matrix(4, 1);
		result.set(0, 0, w);
		result.set(1, 0, x);
		result.set(2, 0, y);
		result.set(3, 0, z);
		return result;
	}

	public double dotProdcut(Quaternion q) {
		return dotProdcut(this, q);
	}

	public static double dotProdcut(Quaternion q1, Quaternion q2) {
		double dotProduct;
		dotProduct = q1.w * q2.w;
		dotProduct += q1.x * q2.x;
		dotProduct += q1.y * q2.y;
		dotProduct += q1.z * q2.z;
		return dotProduct;
	}

	public Quaternion quaternionProduct(Matrix matrix) {
		double b1 = matrix.get(0, 0);
		double b2 = matrix.get(1, 0);
		double b3 = matrix.get(2, 0);
		double b4 = matrix.get(3, 0);
		return quaternionProduct(this, new Quaternion(b1, b2, b3, b4));
	}

	public Quaternion quaternionProduct(Quaternion quaternion) {
		return quaternionProduct(this, quaternion);
	}

	public static Quaternion quaternionProduct(Matrix quaternion, Matrix matrix) {
		double a1 = quaternion.get(0, 0);
		double a2 = quaternion.get(1, 0);
		double a3 = quaternion.get(2, 0);
		double a4 = quaternion.get(3, 0);
		double b1 = matrix.get(0, 0);
		double b2 = matrix.get(1, 0);
		double b3 = matrix.get(2, 0);
		double b4 = matrix.get(3, 0);

		return quaternionProduct(new Quaternion(a1, a2, a3, a4),
				new Quaternion(b1, b2, b3, b4));
	}

	public static Quaternion quaternionProduct(Quaternion quaternion,
			Quaternion matrix) {
		double a0 = quaternion.w;
		double a1 = quaternion.x;
		double a2 = quaternion.y;
		double a3 = quaternion.z;
		double b0 = matrix.w;
		double b1 = matrix.x;
		double b2 = matrix.y;
		double b3 = matrix.z;

		Quaternion quad = new Quaternion();

		quad.w = a0 * b0 - a1 * b1 - a2 * b2 - a3 * b3;
		quad.x = a0 * b1 + a1 * b0 + a2 * b3 - a3 * b2;
		quad.y = a0 * b2 - a1 * b3 + a2 * b0 + a3 * b1;
		quad.z = a0 * b3 + a1 * b2 - a2 * b1 + a3 * b0;

		return quad;
	}

	public static Quaternion times(Quaternion quad, double t) {

		return new Quaternion(quad.w * t, quad.x * t, quad.y * t, quad.z * t);
	}

	public Quaternion times(double t) {

		return times(this, t);
	}

	public Quaternion minus(Quaternion quad) {
		return minus(this, quad);
	}

	public static Quaternion minus(Quaternion a, Quaternion b) {
		return new Quaternion(a.w - b.w, a.x - b.x, a.y - b.y, a.z - b.z);
	}

	public Quaternion plus(Quaternion quad) {
		return plus(this, quad);
	}

	public static Quaternion plus(Quaternion a, Quaternion b) {
		return new Quaternion(a.w + b.w, a.x + b.x, a.y + b.y, a.z + b.z);
	}

	public double getNorm() {
		double norm = Math.sqrt(this.dotProdcut(this));
		return norm;
	}

	public Quaternion normalized() {
		double norm = getNorm();
		w /= norm;
		x /= norm;
		y /= norm;
		z /= norm;
		return this;
	}

	public double get(int index) {
		if (index == 0)
			return w;
		else if (index == 1)
			return x;
		else if (index == 2)
			return y;
		else
			return z;
	}

	public void set(int index, double value) {
		if (index == 0)
			w = value;
		else if (index == 1)
			x = value;
		else if (index == 2)
			y = value;
		else
			z = value;
	}

	public Quaternion getConjugate() {
		return new Quaternion(this.w, -this.x, -this.y, -this.z);
	}

	public Quaternion pow(double power) {
		return pow(this, power);
	}

	public static Quaternion pow(Quaternion q, double exponent) {

		return log(q).times(exponent).exp();
		// if (Math.abs(q.w) > .9999f || exponent == 1) {
		// return q;
		// } else if (exponent == 0) {
		// return new Quaternion();
		// }
		//
		// // Extract the half angle alpha (alpha = theta/2)
		//
		// double alpha = Math.acos(q.w);
		//
		// // Compute new alpha value
		//
		// double newAlpha = alpha * exponent;
		//
		// double mult = Math.sin(newAlpha) / Math.sin(alpha);
		//
		// // Compute new wxyz values
		//
		// return new Quaternion(Math.cos(newAlpha), q.x * mult, q.y * mult, q.z
		// * mult);
	}

	public static Quaternion log(Quaternion input) {
		double a = Math.acos(input.getW());
		double sina = Math.sin(a);
		Quaternion ret = new Quaternion();

		ret.w = 0;
		if (sina > 0) {
			ret.x = a * input.x / sina;
			ret.y = a * input.y / sina;
			ret.z = a * input.z / sina;
		} else {
			ret.x = ret.y = ret.z = 0;
		}
		return ret;
	}

	public Quaternion log() {
		return log(this);
	}

	public Quaternion exp() {
		return exp(this);
	}

	public static Quaternion exp(Quaternion input) {
		double mag = Math.exp(input.w);

		double arg = Math.sqrt(input.x * input.x + input.y * input.y + input.z
				* input.z);

		double multiplier = mag * Math.sin(arg) / arg;

		return new Quaternion(mag * Math.cos(arg), input.x * multiplier,
				input.y * multiplier, input.z * multiplier);
	}

	public double[] getAnglesRad() {
		return getAnglesRad(this);
	}

	public double[] getAnglesDeg() {
		return getAnglesDeg(this);
	}

	public static double[] getAnglesDeg(Quaternion q) {
		double[] result = q.getAnglesRad();

		for (int i = 0; i < result.length; i++) {
			result[i] = AngleHelper.degFromRad(result[i]);
		}
		return result;
	}

	/**
	 * 
	 * @param q
	 * @return double array with roll,pitch,yaw
	 */
	public static double[] getAnglesRad(Quaternion q) {

		double[] angles = new double[3];

		double sqw = q.w * q.w;
		double sqx = q.x * q.x;
		double sqy = q.y * q.y;
		double sqz = q.z * q.z;
		double unit = sqx + sqy + sqz + sqw; // if normalised is one, otherwise
												// is correction factor
		double test = q.x * q.y + q.z * q.w;
		if (test > 0.499 * unit) { // singularity at north pole
			angles[2] = 2 * Math.atan2(q.x, q.w);
			angles[1] = Math.PI / 2;
			angles[0] = 0;
			return angles;
		}
		if (test < -0.499 * unit) { // singularity at south pole
			angles[2] = -2 * Math.atan2(q.x, q.w);
			angles[1] = -Math.PI / 2;
			angles[0] = 0;
			return angles;
		}
		angles[2] = Math.atan2(2 * q.y * q.w - 2 * q.x * q.z, sqx - sqy - sqz
				+ sqw);
		angles[1] = Math.asin(2 * test / unit);
		angles[0] = Math.atan2(2 * q.x * q.w - 2 * q.y * q.z, -sqx + sqy - sqz
				+ sqw);
		return angles;
	}

	public static Quaternion getQuaternionFromAnglesRad(double[] angles) {

		double c1 = Math.cos(angles[0] / 2);
		double c2 = Math.cos(angles[1] / 2);
		double c3 = Math.cos(angles[2] / 2);

		double s1 = Math.sin(angles[0] / 2);
		double s2 = Math.sin(angles[1] / 2);
		double s3 = Math.sin(angles[2] / 2);

		Quaternion quad = new Quaternion();

		quad.w = (c1 * c2 * c3 + s1 * s2 * s3);
		quad.x = (s1 * c2 * c3 - c1 * s2 * s3);
		quad.y = (c1 * s2 * c3 + s1 * c2 * s3);
		quad.z = (c1 * c2 * s3 - s1 * s2 * c3);

		return quad;
	}

	public void print(int precision) {
		System.out.println();
		String format = "\t%." + precision + "f\n";
		System.out.printf(format, this.w);
		System.out.printf(format, this.x);
		System.out.printf(format, this.y);
		System.out.printf(format, this.z);
	}

	public void printDegree(int precision) {

		double[] degree = this.getAnglesDeg();
		System.out.println();
		String format = "\t%." + precision + "f\n";
		System.out.printf(format, degree[0]);
		System.out.printf(format, degree[1]);
		System.out.printf(format, degree[2]);
	}

	@Override
	public String toString() {
		String format = "\n \t%.3f";

		String out = String.format(format, this.w);
		out += String.format(format, this.x);
		out += String.format(format, this.y);
		out += String.format(format, this.z);
		return out;
	}

	public double getW() {
		return w;
	}

	public void setQ1(double q1) {
		this.w = q1;
	}

	public double getX() {
		return x;
	}

	public void setQ2(double q2) {
		this.x = q2;
	}

	public double getY() {
		return y;
	}

	public void setQ3(double q3) {
		this.y = q3;
	}

	public double getZ() {
		return z;
	}

	public void setQ4(double q4) {
		this.z = q4;
	}

	public void clear() {
		w = 0;
		x = 0;
		y = 0;
		z = 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Quaternion)) {
			return false;
		} else {
			Quaternion obj_q = (Quaternion) obj;
			return fuzzyEquals(this.w, obj_q.w) && fuzzyEquals(this.x, obj_q.x)
					&& fuzzyEquals(this.y, obj_q.y)
					&& fuzzyEquals(this.z, obj_q.z);
		}
	}

	public static boolean fuzzyEquals(double a, double b) {
		return fuzzyEquals(a, b, 0.00000000000001);
	}

	public static boolean fuzzyEquals(double a, double b, double eps) {
		return Math.abs(a - b) < (eps * Math.abs(a));
	}

	@Override
	public int compareTo(Quaternion o) {
		double tNorm = this.getNorm();
		double oNorm = o.getNorm();
		int retVal = 0;

		if (tNorm < oNorm) {
			retVal = -1;
		} else if (tNorm > oNorm) {
			retVal = 1;
		}

		return retVal;
	}
}
