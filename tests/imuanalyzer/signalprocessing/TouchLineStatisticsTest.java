/**
 * 
 */
package imuanalyzer.signalprocessing;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

/**
 * @author "Christopher-Eyk Hrabia"
 *
 */
public class TouchLineStatisticsTest {
	
	ArrayList<VectorLine> touchlines;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		touchlines = Helper.getExampleTouchlineOdd();
	}

	/**
	 * Test method for {@link imuanalyzer.signalprocessing.VectorLineStatistics#TouchLineStatistics(java.util.ArrayList)}.
	 */
	@Test
	public void testTouchLineStatistics() {
		VectorLineStatistics stat = new VectorLineStatistics(touchlines);
		assertEquals("Max", 5, stat.getMax(),0);
		assertEquals("Min", 1, stat.getMin(),0);
		assertEquals("Median", 3, stat.getMedian(),0);
		assertEquals("UQuantile", 4, stat.getUpperQuantile(),0);
		assertEquals("LQuantile", 2, stat.getLowerQuantile(),0);
	}

}
