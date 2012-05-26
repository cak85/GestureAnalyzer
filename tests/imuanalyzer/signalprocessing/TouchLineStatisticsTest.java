/**
 * 
 */
package imuanalyzer.signalprocessing;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.jme3.math.Vector3f;

/**
 * @author "Christopher-Eyk Hrabia"
 *
 */
public class TouchLineStatisticsTest {
	
	ArrayList<TouchLine> touchlines;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		touchlines = Helper.getExampleTouchlineOdd();
	}

	/**
	 * Test method for {@link imuanalyzer.signalprocessing.TouchLineStatistics#TouchLineStatistics(java.util.ArrayList)}.
	 */
	@Test
	public void testTouchLineStatistics() {
		TouchLineStatistics stat = new TouchLineStatistics(touchlines);
		assertEquals("Max", 5, stat.getMax(),0);
		assertEquals("Min", 1, stat.getMin(),0);
		assertEquals("Median", 3, stat.getMedian(),0);
		assertEquals("UQuantile", 4, stat.getUpperQuantile(),0);
		assertEquals("LQuantile", 2, stat.getLowerQuantile(),0);
	}

}
