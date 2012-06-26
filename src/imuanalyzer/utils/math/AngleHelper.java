package imuanalyzer.utils.math;

/**
 * Helper method for angle calculations
 * @author "Christopher-Eyk Hrabia"
 *
 */
public class AngleHelper {

	public static double radFromDeg(double degrees) {
		return degrees * Math.PI / 180;
	}

	public static double degFromRad(double radian) {
		return radian * 180 / Math.PI;
	}
}
