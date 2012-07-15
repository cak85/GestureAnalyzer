/**
 * 
 */
package imuanalyzer.signalprocessing;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.jme3.math.Vector3f;

/**
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class VectorLineStatisticsTest {

	ArrayList<VectorLine> touchlines;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		touchlines = Helper.getExampleTouchlineOdd();
	}

	/**
	 * Test method for
	 * {@link imuanalyzer.signalprocessing.VectorLineStatistics#TouchLineStatistics(java.util.ArrayList)}
	 * .
	 */
	@Test
	public void testTouchLineStatistics() {
		VectorLineStatistics stat = new VectorLineStatistics("TEST ",
				touchlines, new ArrayList<Float>());
		assertEquals("Max", 5, stat.getMax(), 0);
		assertEquals("Min", 1, stat.getMin(), 0);
		assertEquals("Median", 3, stat.getMedian(), 0);
		assertEquals("UQuantile", 4, stat.getUpperQuantile(), 0);
		assertEquals("LQuantile", 2, stat.getLowerQuantile(), 0);
	}

	@Test
	public void testAVGCalculation() {
		ArrayList<VectorLine> calcSet = new ArrayList<VectorLine>();

		ArrayList<Vector3f> lineBuffer = new ArrayList<Vector3f>();

		lineBuffer.add(new Vector3f(1, 1, 0));
		lineBuffer.add(new Vector3f(2, 2, 0));

		calcSet.add(new VectorLine(lineBuffer));

		lineBuffer = new ArrayList<Vector3f>();

		lineBuffer.add(new Vector3f(1, 2, 0));
		lineBuffer.add(new Vector3f(2, 3, 0));

		calcSet.add(new VectorLine(lineBuffer));

		lineBuffer = new ArrayList<Vector3f>();

		lineBuffer.add(new Vector3f(1, 3, 0));
		lineBuffer.add(new Vector3f(2, 4, 0));
		lineBuffer.add(new Vector3f(3, 5, 0));

		calcSet.add(new VectorLine(lineBuffer));

		VectorLine result = VectorLineStatistics.calculateAvgLine(calcSet);

		lineBuffer = result.getLineBuffer();

		assertEquals(lineBuffer.get(0).x, 1, 0);
		assertEquals(lineBuffer.get(1).x, 2, 0);
		assertEquals(lineBuffer.get(2).x, 3, 0);
		
		assertEquals(lineBuffer.get(0).y, 2, 0);
		assertEquals(lineBuffer.get(1).y, 3, 0);
		assertEquals(lineBuffer.get(2).y, 5, 0);

	}

}
