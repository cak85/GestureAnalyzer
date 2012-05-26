/**
 * 
 */
package imuanalyzer.ui;

import static org.junit.Assert.*;

import imuanalyzer.signalprocessing.Helper;
import imuanalyzer.signalprocessing.TouchLineStatistics;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.jme3.asset.AssetManager;
import com.jme3.system.JmeSystem;

/**
 * @author "Christopher-Eyk Hrabia"
 *
 */
public class VisualTouchLineStatisticsTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link imuanalyzer.ui.VisualTouchLineStatistics#getStatistics()}.
	 */
	@Test
	public void testGetStatistics() {
		AssetManager assetManager  = JmeSystem.newAssetManager(
                Thread.currentThread().getContextClassLoader()
                .getResource("com/jme3/asset/Desktop.cfg"));
		VisualTouchLineStatistics vstat = new VisualTouchLineStatistics(assetManager);
		ArrayList<TouchLineStatistics> stats = new ArrayList<TouchLineStatistics>();
		stats.add(new TouchLineStatistics(Helper.getExampleTouchlineEven()));
		stats.add(new TouchLineStatistics(Helper.getExampleTouchlineOdd()));
		vstat.setStatistics(stats);
		
	}

}
