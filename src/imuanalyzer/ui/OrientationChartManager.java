package imuanalyzer.ui;

import imuanalyzer.filter.Quaternion;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.controls.LayoutFactory;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.SortedSet;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class OrientationChartManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final long SLEEP_TIME = 250;
	private static final int VALUES_LIMIT = 50;

	private EnumMap<JointType, Chart2D> charts = new EnumMap<JointType, Chart2D>(
			JointType.class);

	protected Hand hand;

	protected UpdaterOrientation thread;

	public OrientationChartManager(Hand hand) {
		this.hand = hand;
	}

	public void addChart(final JointType type) {

		if (charts.get(type) == null) {

			Chart2D chart = new Chart2D();
			
			chart.getAxisX().setAxisTitle(new AxisTitle("Angle in degree"));
			chart.getAxisY().setAxisTitle(new AxisTitle("Time"));

			LayoutFactory lfct = LayoutFactory.getInstance();
			ChartPanel chartpanel = new ChartPanel(chart);

			ITrace2D traceX = new Trace2DLtd(VALUES_LIMIT);
			traceX.setName("Pitch");
			traceX.setColor(Color.RED);
			chart.addTrace(traceX);

			ITrace2D traceY = new Trace2DLtd(VALUES_LIMIT);
			traceY.setName("Roll");
			traceY.setColor(Color.BLUE);
			chart.addTrace(traceY);

			ITrace2D traceZ = new Trace2DLtd(VALUES_LIMIT);
			traceZ.setName("Yaw");
			traceZ.setColor(Color.GREEN);
			chart.addTrace(traceZ);

			final JFrame frame = new JFrame("Orientation " + type);

			ImageIcon icon = new ImageIcon(getClass().getResource(
					"/Icons/hand.png"));
			frame.setIconImage(icon.getImage());
			// add the chart to the frame:
			frame.getContentPane().add(chartpanel);
			frame.setSize(400, 200);
			frame.setJMenuBar(lfct.createChartMenuBar(chartpanel, false));
			// Enable the termination button [cross on the upper right edge]:
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					frame.setVisible(false);
					frame.dispose();
					removeChart(type);
				}
			});

			frame.setVisible(true);

			charts.put(type, chart);

			if (charts.size() == 1) {
				thread = new UpdaterOrientation(charts, hand);
				thread.start();
			}

		}
	}

	public void removeChart(JointType type) {
		Chart2D chart = charts.get(type);
		if (chart != null) {
			charts.remove(type);
		}
		if (charts.size() == 0) {
			thread.setStop(true);
		}
	}

	private static class UpdaterOrientation extends Thread {

		protected Hand hand;

		private EnumMap<JointType, Chart2D> charts;

		private long starttime = System.currentTimeMillis();

		private boolean stop = false;

		public UpdaterOrientation(EnumMap<JointType, Chart2D> charts, Hand hand) {
			this.charts = charts;
			this.hand = hand;
		}

		public void run() {
			while (true) {
				if (stop) {
					break;
				}
				try {

					for (Entry<JointType, Chart2D> entry : charts.entrySet()) {

						Chart2D chart = entry.getValue();

						if (entry.getValue() != null) {

							Quaternion orientation = hand.getJoint(
									entry.getKey()).getLocalOrientation();

							double[] angles = orientation
									.getAnglesRadFromQuaternion();

							SortedSet<ITrace2D> traces = chart.getTraces();
							int i = 0;
							for (ITrace2D trace : traces) {
								trace.addPoint(
										((double) System.currentTimeMillis() - this.starttime),
										angles[i] * 180 / Math.PI);
								i++;
							}

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