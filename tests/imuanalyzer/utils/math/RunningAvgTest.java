/**
 * 
 */
package imuanalyzer.utils.math;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @author "Christopher-Eyk Hrabia"
 *
 */
public class RunningAvgTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link imuanalyzer.utils.math.RunningAvg#getAvg()}.
	 */
	@Test
	public void testGetAvg() {
		RunningAvg avg = new RunningAvg(5);
		avg.add(1);
		avg.add(2);
		avg.add(3);
		avg.add(4);
		avg.add(5);
		
		assertEquals("getAvg", 3, avg.getAvg(),0.000001);
		
		avg.add(6);
		avg.add(7);
		avg.add(8);
		
		assertEquals("getAvg", 6, avg.getAvg(),0.000001);
		
		avg.add(6);
		avg.add(13);
		
		assertEquals("getAvg", 8, avg.getAvg(),0.000001);
		
	}

}
