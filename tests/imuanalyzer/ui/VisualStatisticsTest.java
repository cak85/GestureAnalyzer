/**
 * 
 */
package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Helper;
import imuanalyzer.signalprocessing.IBoxplotData;
import imuanalyzer.signalprocessing.VectorLineStatistics;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.jme3.asset.AssetManager;
import com.jme3.system.JmeSystem;

/**
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class VisualStatisticsTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for
	 * {@link imuanalyzer.ui.VisualTouchLineStatistics#getStatistics()}.
	 */
	@Test
	public void testGetStatistics() {
		AssetManager assetManager = JmeSystem.newAssetManager(Thread
				.currentThread().getContextClassLoader()
				.getResource("com/jme3/asset/Desktop.cfg"));
		Boxplot3d vstat = new Boxplot3d(assetManager);
		ArrayList<IBoxplotData> stats = new ArrayList<IBoxplotData>();
		stats.add(new VectorLineStatistics("Test", Helper
				.getExampleTouchlineEven()));
		stats.add(new VectorLineStatistics("Test", Helper
				.getExampleTouchlineOdd()));
		vstat.setStatistics(stats);

	}

}
