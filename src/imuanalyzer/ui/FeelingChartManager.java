package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Hand;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.controls.LayoutFactory;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.SortedSet;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class FeelingChartManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final long SLEEP_TIME = 250;
	private static final int VALUES_LIMIT = 50;

	protected Hand hand;

	protected Chart2D chart;

	protected UpdaterFeeling updateThread;

	protected boolean enabled = false;

	private static final Color COLOR_LOCK_UP_TABLE[] = { Color.RED,
			Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE,
			Color.PINK };

	public FeelingChartManager(Hand hand) {
		this.hand = hand;
	}

	public void enable() {
		if (!enabled) {
			enabled = true;
			addChart();

			updateThread = new UpdaterFeeling(hand, chart);
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

		chart = new Chart2D();
		LayoutFactory lfct = LayoutFactory.getInstance();
		ChartPanel chartpanel = new ChartPanel(chart);

		for (int i = 0; i < hand.getComfortScale().getCurrentValues().size(); i++) {
			ITrace2D traceX = new Trace2DLtd(VALUES_LIMIT);
			traceX.setName("Nr." + (i + 1));
			traceX.setColor(COLOR_LOCK_UP_TABLE[i % COLOR_LOCK_UP_TABLE.length]);
			chart.addTrace(traceX);
		}

		final JFrame frame = new JFrame("Feelings ");

		ImageIcon icon = new ImageIcon(getClass()
				.getResource("/Icons/hand.png"));
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
				disable();
			}
		});

		frame.setVisible(true);

	}

	private static class UpdaterFeeling extends Thread {

		protected Hand hand;

		protected Chart2D chart;

		protected boolean stop = false;

		private long starttime = System.currentTimeMillis();

		public void run() {
			while (true) {
				if (stop) {
					break;
				}
				try {

					ArrayList<Integer> cScale = hand.getComfortScale()
							.getCurrentValues();

					int sizeDiff = cScale.size() - chart.getTraces().size();
					if (sizeDiff > 0) {
						int start = chart.getTraces().size();
						for (int i = start; i < start + sizeDiff; i++) {
							ITrace2D trace = new Trace2DLtd(VALUES_LIMIT);
							trace.setName("Nr." + (i + 1));
							trace.setColor(COLOR_LOCK_UP_TABLE[i
									% COLOR_LOCK_UP_TABLE.length]);
							chart.addTrace(trace);
						}
					} else if (sizeDiff < 0) {
						for (int i = 0; i < Math.abs(sizeDiff); i++) {
							chart.removeTrace(chart.getTraces().last());
						}
					}

					SortedSet<ITrace2D> traces = chart.getTraces();
					int i = 0;
					for (ITrace2D trace : traces) {
						trace.addPoint(
								((double) System.currentTimeMillis() - this.starttime),
								cScale.get(i));
						i++;
					}

					Thread.sleep(SLEEP_TIME);
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}

		private UpdaterFeeling(Hand hand, Chart2D chart) {
			this.hand = hand;
			this.chart = chart;
		}

		public void setStop(boolean stop) {
			this.stop = stop;
		}

	}

}