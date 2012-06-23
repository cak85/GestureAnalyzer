package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.ui.swing.OrientationChartFrame;

import java.util.EnumMap;
import java.util.Map.Entry;

public class OrientationChartManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final long SLEEP_TIME = 250;
	private static final int VALUES_LIMIT = 50;

	private EnumMap<JointType, OrientationChartFrame> charts = new EnumMap<JointType, OrientationChartFrame>(
			JointType.class);

	protected Hand hand;

	protected UpdaterOrientation thread;

	public OrientationChartManager(Hand hand) {
		this.hand = hand;
	}

	public void addDynamicChart(final JointType type) {

		if (charts.get(type) == null) {

			charts.put(type, new OrientationChartFrame(this, hand, type, 1,
					VALUES_LIMIT));

			if (charts.size() == 1) {
				thread = new UpdaterOrientation(charts);
				thread.start();
			}
		}
	}

	public OrientationChartFrame getStaticChart(final JointType type,
			int nrOfTraceGroups, int valueLimit) {

		if (charts.get(type) == null) {

			return new OrientationChartFrame(this, hand, type, nrOfTraceGroups,
					valueLimit);
		}

		return null;
	}

	public void removeChart(JointType type) {
		OrientationChartFrame chart = charts.get(type);
		if (chart != null) {
			charts.remove(type);
		}
		if (charts.size() == 0) {
			thread.setStop(true);
		}
	}

	public EnumMap<JointType, OrientationChartFrame> getCharts() {
		return charts;
	}

	private static class UpdaterOrientation extends Thread {

		private EnumMap<JointType, OrientationChartFrame> charts;

		private boolean stop = false;

		public UpdaterOrientation(
				EnumMap<JointType, OrientationChartFrame> charts) {
			this.charts = charts;
		}

		public void run() {
			while (true) {
				if (stop) {
					break;
				}
				try {

					for (Entry<JointType, OrientationChartFrame> entry : charts
							.entrySet()) {

						OrientationChartFrame chart = entry.getValue();

						if (chart != null) {
							chart.update(0);
						}
					}
					Thread.sleep(SLEEP_TIME);
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}

		public void setStop(boolean stop) {
			this.stop = stop;
		}
	}

}