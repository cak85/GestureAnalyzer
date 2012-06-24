package imuanalyzer.ui.swing.charts;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.utils.parallel.IIntervalUpdate;
import imuanalyzer.utils.parallel.IntervalUpdater;

import java.util.ArrayList;

public class AccelerationChartManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final long UPDATE_CYCLE = 250;
	private static final int VALUES_LIMIT = 50;

	private ArrayList<IIntervalUpdate> charts = new ArrayList<IIntervalUpdate>();

	protected Hand hand;

	protected IntervalUpdater thread;

	public AccelerationChartManager(Hand hand) {
		this.hand = hand;
	}

	public void addChart(final JointType type) {
		for (IIntervalUpdate jR : charts) {
			AccelerationChartFrame frame = (AccelerationChartFrame) jR;
			if (frame.equals(type)) {
				return;
			}
		}

		AccelerationChartFrame frame = new AccelerationChartFrame(this, hand,
				type, VALUES_LIMIT);

		charts.add(frame);
		if (charts.size() == 1) {
			thread = new IntervalUpdater(charts, UPDATE_CYCLE);
			thread.start();
		}

	}

	public AccelerationChartFrame getStaticChart(final JointType type,
			int valueLimit) {

		return new AccelerationChartFrame(this, hand, type, valueLimit);
	}

	public void removeChart(JointType type) {

		for (IIntervalUpdate jR : charts) {
			AccelerationChartFrame frame = (AccelerationChartFrame) jR;
			if (frame.equals(type)) {
				charts.remove(jR);
				return;
			}
		}
		if (charts.size() == 0) {
			thread.setStop(true);
		}
	}

	public ArrayList<AccelerationChartFrame> getCharts() {
		ArrayList<AccelerationChartFrame> frames = new ArrayList<AccelerationChartFrame>();

		for (IIntervalUpdate jR : charts) {
			AccelerationChartFrame frame = (AccelerationChartFrame) jR;
			frames.add(frame);
		}

		return frames;
	}

}