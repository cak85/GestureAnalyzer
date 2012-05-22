package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.Joint;

import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.Map.Entry;

import javax.swing.JPanel;

import com.sun.tools.visualvm.charts.ChartFactory;
import com.sun.tools.visualvm.charts.SimpleXYChartDescriptor;
import com.sun.tools.visualvm.charts.SimpleXYChartSupport;

public class AccelerationChart extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final long SLEEP_TIME = 250;
	private static final int VALUES_LIMIT = 50;

	private EnumMap<JointType, SimpleXYChartSupport> charts = new EnumMap<JointType, SimpleXYChartSupport>(
			JointType.class);

	protected Hand hand;

	public AccelerationChart(Hand hand) {
		this.hand = hand;

		this.setLayout(new GridLayout(0, 1));

		new Updater(charts, hand).start();

	}

	public void addChart(JointType type) {

		if (charts.get(type) == null) {

			SimpleXYChartDescriptor descriptor = SimpleXYChartDescriptor
					.decimal(-12, 12, 100, 1d, true, VALUES_LIMIT);

			descriptor.addLineItems("X");
			descriptor.addLineItems("Y");
			descriptor.addLineItems("Z");

			descriptor.setDetailsItems(new String[] { "X", "Y", "Z" });
			descriptor.setChartTitle("<html><b>Acceleration Sensor " + type.toString()
					+ "</b></html>");
			descriptor.setXAxisDescription("<html>Time</html>");
			descriptor.setYAxisDescription("<html>m/s^2</html>");

			SimpleXYChartSupport chart = ChartFactory
					.createSimpleXYChart(descriptor);

			this.add(chart.getChart());

			charts.put(type, chart);
		}
	}

	public void removeChart(JointType type) {
		SimpleXYChartSupport chart = charts.get(type);
		if (chart != null) {
			this.remove(chart.getChart());
			charts.put(type, null);
		}
	}

	private static class Updater extends Thread {

		protected Hand hand;

		private EnumMap<JointType, SimpleXYChartSupport> charts;

		public void run() {
			while (true) {
				try {

					for (Entry<JointType, SimpleXYChartSupport> entry : charts
							.entrySet()) {

						SimpleXYChartSupport chart = entry.getValue();

						if (entry.getValue() != null) {
							

							Joint joint = hand.getJoint(
									entry.getKey());

							float[] acceleration = joint.getAcceleration();
							long[] values = new long[3];
							values[0] = (long) (acceleration[0]);
							values[1] = (long) (acceleration[1]);
							values[2] = (long) (acceleration[2]);

							chart.addValues(System.currentTimeMillis(), values);
							chart.updateDetails(new String[] {
									String.format("%.2f", acceleration[0]),
									String.format("%.2f", acceleration[1]),
									String.format("%.2f", acceleration[2]) });
						}
					}
					Thread.sleep(SLEEP_TIME);
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}

		private Updater(EnumMap<JointType, SimpleXYChartSupport> charts,
				Hand hand) {
			this.charts = charts;
			this.hand = hand;
		}
	}

}