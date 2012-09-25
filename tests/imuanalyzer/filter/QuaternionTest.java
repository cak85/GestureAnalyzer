package imuanalyzer.filter;

import static org.junit.Assert.*;
import imuanalyzer.utils.math.AngleHelper;
import imuanalyzer.utils.math.Quaternion;

import org.junit.Before;
import org.junit.Test;

public class QuaternionTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testMultiplicationAndConjugate() {
		Quaternion qInitial = new Quaternion(AngleHelper.radFromDeg(40),
				AngleHelper.radFromDeg(10), AngleHelper.radFromDeg(20));
		Quaternion qTransition = new Quaternion(AngleHelper.radFromDeg(20),
				AngleHelper.radFromDeg(30), AngleHelper.radFromDeg(10));

		Quaternion qFinal = qTransition.quaternionProduct(qInitial);

		Quaternion qResolveInitial = qTransition.getConjugate()
				.quaternionProduct(qFinal);

		Quaternion qResolveTransition = qFinal.quaternionProduct(qInitial
				.getConjugate());

		qInitial.printDegree(3);

		qResolveInitial.printDegree(3);

		qResolveTransition.printDegree(3);

		qTransition.printDegree(3);

		assertEquals("q1=q4", qResolveInitial.equals(qInitial), true);

		assertEquals("q1=q4", qResolveTransition.equals(qTransition), true);

		Quaternion q1 = new Quaternion(AngleHelper.radFromDeg(0),
				AngleHelper.radFromDeg(-11.459), AngleHelper.radFromDeg(0));

		Quaternion q2 = new Quaternion(AngleHelper.radFromDeg(-34.463),
				AngleHelper.radFromDeg(0), AngleHelper.radFromDeg(0));

		Quaternion q3 = new Quaternion(AngleHelper.radFromDeg(-70),
				AngleHelper.radFromDeg(0), AngleHelper.radFromDeg(0));

		q3.quaternionProduct(q2.quaternionProduct(q1)).printDegree(3);

		q1.quaternionProduct(q2.quaternionProduct(q3)).printDegree(3);

		Quaternion q4 = q2.quaternionProduct(q3);
		q4.printDegree(3);

		q1.quaternionProduct(q4).printDegree(3);

	}

	@Test
	public void testLog() {

		Quaternion input = new Quaternion(0.766, 0.643, 0, 0);

		input = input.log();

		assertEquals("w", 0, input.getW(), 0.001);
		assertEquals("x", 0.698, input.getX(), 0.001);
		assertEquals("y", 0, input.getY(), 0.001);
		assertEquals("z", 0, input.getY(), 0.001);
	}

	@Test
	public void testExp() {
		int angleX = 80;
		int angleY = 0;
		int angleZ = 0;

		Quaternion input = new Quaternion(AngleHelper.radFromDeg(angleX),
				AngleHelper.radFromDeg(angleY), AngleHelper.radFromDeg(angleZ));

		input = input.exp();

		assertEquals("w", 1.722, input.getW(), 0.001);
		assertEquals("x", 1.290, input.getX(), 0.001);
		assertEquals("y", 0, input.getY(), 0.001);
		assertEquals("z", 0, input.getY(), 0.001);
	}

	@Test
	public void testPowToSquare() {

		// power must be higher than 1
		float power = 0.5f;

		int angleX = 80;
		int angleY = 40;
		int angleZ = 20;

		Quaternion input = new Quaternion(AngleHelper.radFromDeg(angleX),
				AngleHelper.radFromDeg(angleY), AngleHelper.radFromDeg(angleZ));

		// input.print(3);

		Quaternion resPow = input.pow(power);

		// double[] angles = resPow.getAnglesDeg();

		// for (int i = 0; i < angles.length; i++) {
		// System.out.println(angles[i]);
		// }

		assertEquals("angle w", 0.914, resPow.get(0), 0.001);
		assertEquals("angle x", 0.350, resPow.get(1), 0.001);
		assertEquals("angle y", 0.187, resPow.get(2), 0.001);
		assertEquals("angle z", 0.084, resPow.get(3), 0.001);

	}

	@Test
	public void testPowGreater2() {

		// power must be higher than 1
		int maxPower = 100;

		for (int power = 1; power < maxPower; power++) {

			int angleX = 80;
			int angleY = 40;
			int angleZ = 20;

			Quaternion input = new Quaternion(AngleHelper.radFromDeg(angleX),
					AngleHelper.radFromDeg(angleY),
					AngleHelper.radFromDeg(angleZ));

			Quaternion resPow = input.pow(power);

			Quaternion resMult = input;

			// compare power with step by step multiplication
			for (int i = 1; i < power; i++) {
				resMult = resMult.quaternionProduct(input);
			}

			double[] resultPow = resPow.getAnglesDeg();

			double[] resultMult = resMult.getAnglesDeg();

			assertEquals("angle x", resultMult[0], resultPow[0], 0.000001);
			assertEquals("angle y", resultMult[1], resultPow[1], 0.000001);
			assertEquals("angle z", resultMult[2], resultPow[2], 0.000001);

		}
	}

	@Test
	public void testElementOrder() {
		// Test for order of roll pitch yaw
		Quaternion quat;

		quat = new Quaternion(Math.PI / 2, Math.PI / 4, Math.PI / 6);

		double[] rad = quat.getAnglesRad();

		Quaternion newQuat = new Quaternion(rad[0], rad[1], rad[2]);

		double[] radRes = newQuat.getAnglesRad();

		assertEquals(radRes[0], rad[0], 0.000001);
		assertEquals(radRes[1], rad[1], 0.000001);
		assertEquals(radRes[2], rad[2], 0.000001);

		// ////////////////////

		com.jme3.math.Quaternion quat2;

		float angles[] = { (float) Math.PI / 2, (float) Math.PI / 4,
				(float) Math.PI / 6 };
		quat2 = new com.jme3.math.Quaternion(angles);

		float[] rad2 = quat2.toAngles(null);

		com.jme3.math.Quaternion newQuat2 = new com.jme3.math.Quaternion(rad2);

		float[] rad2Res = newQuat2.toAngles(null);

		assertEquals(rad2Res[0], rad2[0], 0.000001);
		assertEquals(rad2Res[1], rad2[1], 0.000001);
		assertEquals(rad2Res[2], rad2[2], 0.000001);
	}

	@Test
	public void testDotProduct() {
		Quaternion q1 = new Quaternion(Math.PI, 0, 0);

		// if equal result is 1
		assertEquals(q1.dotProdcut(q1), 1, 0.000000000001);

	}

	@Test
	public void testConversation() {

		double[] rad = { AngleHelper.radFromDeg(60),
				AngleHelper.radFromDeg(60), AngleHelper.radFromDeg(60) };

		Quaternion quat1 = new Quaternion(rad[0], rad[1], rad[2]);

		double[] rad2 = quat1.getAnglesRad();

		assertEquals(rad[0], rad2[0], 0.000000000001);

		assertEquals(rad[1], rad2[1], 0.000000000001);

		assertEquals(rad[2], rad2[2], 0.000000000001);

		double[] angles = quat1.getAnglesDeg();

		assertEquals(angles[0], 60, 0.000000000001);

		assertEquals(angles[1], 60, 0.000000000001);

		assertEquals(angles[2], 60, 0.000000000001);
	}

	@Test
	public void testAverageCalculation() {
		Quaternion quat1 = new Quaternion(AngleHelper.radFromDeg(60),
				AngleHelper.radFromDeg(40), AngleHelper.radFromDeg(20));

		Quaternion quatRes = quat1;

		int trials = 2;

		for (int i = 1; i < trials; i++) {
			quatRes = quatRes.quaternionProduct(quat1);
		}

		quatRes = quat1.pow(1 / (float) trials);

		double[] angles = quatRes.getAnglesDeg();

		// for (int i = 0; i < angles.length; i++) {
		// System.out.println(angles[i]);
		// }

		assertEquals(angles[0], 60, 0.000000000001);

		assertEquals(angles[1], 60, 0.000000000001);

		assertEquals(angles[2], 60, 0.000000000001);

	}
}
