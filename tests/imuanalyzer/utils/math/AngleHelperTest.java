package imuanalyzer.utils.math;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class AngleHelperTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testRadFromDegAndViceVersa() {
		assertEquals("rad <--> deg", 20,  AngleHelper.degFromRad(AngleHelper.radFromDeg(20)),0.000001);
	}

}
