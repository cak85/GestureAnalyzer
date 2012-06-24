package imuanalyzer.ui.swing.charts;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.utils.parallel.IIntervalUpdate;
import imuanalyzer.utils.parallel.IntervalUpdater;

import java.util.ArrayList;

public class FeelingChartManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final long SLEEP_TIME = 250;
	private static final int VALUES_LIMIT = 50;

	protected Hand hand;

	FeelingChartFrame frame;

	protected IntervalUpdater updateThread;

	protected boolean enabled = false;

	public FeelingChartManager(Hand hand) {
		this.hand = hand;
	}

	public void enable() {
		if (!enabled) {
			enabled = true;
			addChart();

			ArrayList<IIntervalUpdate> list = new ArrayList<IIntervalUpdate>();
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

	protected void addChart() {
		frame = new FeelingChartFrame(this, hand, VALUES_LIMIT);
	}
	
	public FeelingChartFrame getStaticChart(int valueLimit){
		return  new FeelingChartFrame(null, hand,valueLimit);
	}

	public boolean isEnabled() {
		return enabled;
	}

}