package imuanalyzer.ui.swing.charts;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;

import java.util.EnumMap;
import java.util.Map.Entry;

/**
 * Manager for several orientation chart frames, handles updates, creation and
 * so on
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public class OrientationChartManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final long UPDATE_CYCLE = 250;
	private static final int VALUES_LIMIT = 50;

	private EnumMap<JointType, OrientationChartFrame> charts = new EnumMap<JointType, OrientationChartFrame>(
			JointType.class);

	protected Hand hand;

	/**
	 * Thread for periodic update
	 */
	protected UpdaterOrientation thread;

	public OrientationChartManager(Hand hand) {
		this.hand = hand;
	}

	public void addDynamicChart(final JointType type, boolean visible) {

		if (charts.get(type) == null) {

			OrientationChartFrame frame = new OrientationChartFrame(this, hand,
					type, 1, VALUES_LIMIT);
			charts.put(type, frame);
			frame.setVisible(visible);

			if (charts.size() == 1) {
				thread = new UpdaterOrientation(charts);
				thread.start();
			}
		}
	}

	/**
	 * Get static non updated chart with predefined value count
	 * 
	 * @param type
	 * @param nrOfTraceGroups
	 * @param valueLimit
	 * @return
	 */
	public OrientationChartFrame getStaticChart(final JointType type,
			int nrOfTraceGroups, int valueLimit) {

		return new OrientationChartFrame(null, hand, type, nrOfTraceGroups,
				valueLimit);

	}

	/**
	 * Remove one maintained chart
	 * @param type
	 */
	public void removeChart(JointType type) {
		OrientationChartFrame chart = charts.get(type);
		if (chart != null) {
			charts.remove(type);
		}
		if (charts.size() == 0) {
			thread.setStop(true);
		}
	}

	/**
	 * Get all managed charts
	 * @return
	 */
	public EnumMap<JointType, OrientationChartFrame> getCharts() {
		return charts;
	}

	/**
	 * Update thread for orientation charts
	 * @author Christopher-Eyk Hrabia
	 *
	 */
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
							chart.update(System.currentTimeMillis(), 0);
						}
					}
					Thread.sleep(UPDATE_CYCLE);
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