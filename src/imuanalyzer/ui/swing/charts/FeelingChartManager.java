package imuanalyzer.ui.swing.charts;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.utils.parallel.IIntervalUpdate;
import imuanalyzer.utils.parallel.IntervalUpdater;

import java.util.ArrayList;

/**
 * Manager for several feeling chart frames,
 * handles updates, creation and so on
 * 
 * @author Christopher-Eyk Hrabia
 *
 */
public class FeelingChartManager {

	private static final long SLEEP_TIME = 250;
	private static final int VALUES_LIMIT = 50;

	protected Hand hand;

	FeelingChartFrame frame;

	protected IntervalUpdater updateThread;

	protected boolean enabled = false;

	public FeelingChartManager(Hand hand) {
		this.hand = hand;
	}

	public void enable(boolean visible) {
		if (!enabled) {
			enabled = true;

			ArrayList<IIntervalUpdate> list = new ArrayList<IIntervalUpdate>();
			frame = new FeelingChartFrame(this, hand, VALUES_LIMIT);
			frame.setVisible(visible);
			list.add(frame);
			updateThread = new IntervalUpdater(list, SLEEP_TIME);
			updateThread.start();
		}
	}

	public void disable() {
		if (enabled) {
			updateThread.setStop(true);
			enabled = false;
		}
	}

	public FeelingChartFrame getStaticChart(int valueLimit){
		return  new FeelingChartFrame(null, hand,valueLimit);
	}

	public boolean isEnabled() {
		return enabled;
	}

}