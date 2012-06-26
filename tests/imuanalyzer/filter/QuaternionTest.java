package imuanalyzer.filter;

import static org.junit.Assert.*;
import imuanalyzer.utils.math.AngleHelper;

import org.junit.Before;
import org.junit.Test;

public class QuaternionTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testPowDouble() {
		Quaternion input = new Quaternion(AngleHelper.radFromDeg(80) , 0, 0);

		double[] result = input.getAnglesRad();

		result = input.pow(0.5).getAnglesRad();

		for (int i = 0; i < result.length; i++) {
			result[i] = AngleHelper.degFromRad(result[i]);
		}

		assertEquals("angle x", 40, result[0], 0.000001);
		assertEquals("angle y", 0, result[1], 0.000001);
		assertEquals("angle z", 0, result[2], 0.000001);
	}

}
