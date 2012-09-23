package imuanalyzer.ui.swing.charts;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.ui.swing.help.HelpManager;
import imuanalyzer.utils.parallel.IIntervalUpdate;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
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

/**
 * Frame which shows graphs of configured feeling scales
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public class FeelingChartFrame extends JFrame implements IIntervalUpdate {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8237378119385121069L;

	private static final Color COLOR_LOCK_UP_TABLE[] = { Color.RED,
			Color.ORANGE, Color.MAGENTA, Color.GREEN, Color.CYAN, Color.BLUE,
			Color.PINK };

	private long starttime = 0;

	protected Chart2D chart;

	protected FeelingChartManager manager;

	int valuesLimit;

	Hand hand;

	FeelingChartFrame instance;

	public FeelingChartFrame(FeelingChartManager _manager, Hand hand,
			int valueLimit) {
		super("Feelings");
		this.manager = _manager;
		this.valuesLimit = valueLimit;
		this.hand = hand;
		instance = this;
		
		HelpManager.getInstance().enableHelpKey(this, "diagramms");

		chart = new Chart2D();
		LayoutFactory lfct = LayoutFactory.getInstance();
		ChartPanel chartpanel = new ChartPanel(chart);

		for (int i = 0; i < hand.getComfortScale().getCurrentValues().size(); i++) {
			ITrace2D traceX = new Trace2DLtd(valueLimit);
			traceX.setName(hand.getComfortScale().getDescription(i));
			traceX.setColor(COLOR_LOCK_UP_TABLE[i % COLOR_LOCK_UP_TABLE.length]);
			chart.addTrace(traceX);
		}

		chart.getAxisY().setAxisTitle(new AxisTitle("Feeling value"));
		chart.getAxisX().setAxisTitle(new AxisTitle("Time"));

		ImageIcon icon = new ImageIcon(getClass()
				.getResource("/Icons/hand.png"));
		this.setIconImage(icon.getImage());
		// add the chart to the frame:
		this.getContentPane().add(chartpanel);
		this.setSize(500, 200);
		this.setJMenuBar(lfct.createChartMenuBar(chartpanel, false));
		// Enable the termination button [cross on the upper right edge]:
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				instance.setVisible(false);
				instance.dispose();
				if (manager != null) {
					manager.disable();
				}
			}
		});
	}

	public void update(long currentTime) {
		// leave first update for getting starttime
		if (starttime == 0) {
			starttime = currentTime;
			return;
		}

		ArrayList<Integer> cScale = hand.getComfortScale().getCurrentValues();

		int sizeDiff = cScale.size() - chart.getTraces().size();
		if (sizeDiff > 0) {
			int start = chart.getTraces().size();
			for (int i = start; i < start + sizeDiff; i++) {
				ITrace2D trace = new Trace2DLtd(valuesLimit);
				trace.setName(hand.getComfortScale().getDescription(i));
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
			trace.addPoint(((double) currentTime - this.starttime),
					cScale.get(i));
			i++;
		}
	}

	public Hand getHand() {
		return hand;
	}

	public void setHand(Hand hand) {
		this.hand = hand;
	}
}
