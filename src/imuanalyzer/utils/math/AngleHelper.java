package imuanalyzer.utils.math;

/**
 * Helper method for angle calculations
 * @author "Christopher-Eyk Hrabia"
 *
 */
public class AngleHelper {

	/**
	 * Calculate radian from degree
	 * @param degrees
	 * @return radian
	 */
	public static double radFromDeg(double degrees) {
		return degrees * Math.PI / 180;
	}

	/**
	 * calculate degree from radian
	 * @param radian
	 * @return degree
	 */
	public static double degFromRad(double radian) {
		return radian * 180 / Math.PI;
	}
}
