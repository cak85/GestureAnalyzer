/**
 * 
 */
package imuanalyzer.utils.math;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

/**
 * @author "Christopher-Eyk Hrabia"
 *
 */
public class LinearRegressionTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link imuanalyzer.utils.math.LinearRegression#LinearRegression(java.util.ArrayList)}.
	 */
	@Test
	public void testLinearRegression() {
		ArrayList<double[]> values = new ArrayList<double[]>();
		
		values.add(new double[]{187,72});
		values.add(new double[]{170,60});
		values.add(new double[]{180,73});
		values.add(new double[]{184,74});
		values.add(new double[]{178,72});
		values.add(new double[]{180,70});
		values.add(new double[]{172,62});
		values.add(new double[]{176,70});
		values.add(new double[]{186,80});
		values.add(new double[]{177,67});
		LinearRegression regression = new LinearRegression(values);
		assertEquals("m", 0.9119718309859155, regression.getBeta1(),0.000001);
		assertEquals("n", -93.24295774647888, regression.getBeta0(),0.000001);
		assertEquals("R^2", 0.7718977262266415, regression.getR2(),0.000001);
		assertEquals("stderror beta1", 0.1752755574162408, regression.getStdErrorBeta1(),0.000001);
		assertEquals("stderror beta0", 31.38822623917572, regression.getStdErrorBeta0(),0.000001);
		assertEquals("SSTO",306, regression.getSSTO(),0.000001);
		assertEquals("SSE",69.79929577464796, regression.getSSE(),0.000001);
		assertEquals("SSR",236.20070422535233, regression.getSSR(),0.000001);
	}

}
