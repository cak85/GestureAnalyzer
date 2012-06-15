/**
 * 
 */
package imuanalyzer.ui;

import java.util.ArrayList;

import imuanalyzer.signalprocessing.Helper;
import imuanalyzer.signalprocessing.IBoxplotData;
import imuanalyzer.signalprocessing.VectorLineStatistics;

import org.junit.Before;
import org.junit.Test;

/**
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class Boxplot2dTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testConstructor() {
		VectorLineStatistics stat = new VectorLineStatistics("Test",
				Helper.getExampleTouchlineOddWithOutliners());

		ArrayList<IBoxplotData> statistics = new ArrayList<IBoxplotData>();
		statistics.add(stat);
		new Boxplot2d("Test-Statistics", statistics);

		statistics.clear();

		new Boxplot2d("Test-Statistics", statistics);
	}

}
