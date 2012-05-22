package imuanalyzer.ui;

import imuanalyzer.filter.Quaternion;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;

import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.Map.Entry;

import javax.swing.JPanel;

import com.sun.tools.visualvm.charts.ChartFactory;
import com.sun.tools.visualvm.charts.SimpleXYChartDescriptor;
import com.sun.tools.visualvm.charts.SimpleXYChartSupport;

public class OrientationChart extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final long SLEEP_TIME = 250;
	private static final int VALUES_LIMIT = 50;

	private EnumMap<JointType, SimpleXYChartSupport> charts = new EnumMap<JointType, SimpleXYChartSupport>(
			JointType.class);

	protected Hand hand;

	public OrientationChart(Hand hand) {
		this.hand = hand;

		this.setLayout(new GridLayout(0, 1));

		new Updater(charts, hand).start();

	}

	public void addChart(JointType type) {

		if (charts.get(type) == null) {

			SimpleXYChartDescriptor descriptor = SimpleXYChartDescriptor
					.decimal(-180, 180, 100, 1d, true, VALUES_LIMIT);

			descriptor.addLineItems("Pitch");
			descriptor.addLineItems("Roll");
			descriptor.addLineItems("Yaw");

			descriptor.setDetailsItems(new String[] { "Pitch", "Roll", "Yaw" });
			descriptor.setChartTitle("<html><b>Sensor " + type.toString()
					+ "</b></html>");
			descriptor.setXAxisDescription("<html>Time</html>");
			descriptor.setYAxisDescription("<html>Degree</html>");

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

							Quaternion orientation = hand.getJoint(
									entry.getKey()).getLocalOrientation();

							double[] angles = orientation
									.getAnglesRadFromQuaternion();

							double roll = angles[0] * 180 / Math.PI;
							double pitch = angles[1] * 180 / Math.PI;
							double yaw = angles[2] * 180 / Math.PI;
							long[] values = new long[3];
							values[0] = (long) (pitch);
							values[1] = (long) (roll);
							values[2] = (long) (yaw);


							chart.addValues(System.currentTimeMillis(), values);
							chart.updateDetails(new String[] {
									String.format("%.2f", pitch),
									String.format("%.2f", roll),
									String.format("%.2f", yaw) });
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