package imuanalyzer.ui.swing.charts;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.Joint;
import imuanalyzer.ui.swing.help.HelpManager;
import imuanalyzer.utils.parallel.IIntervalUpdate;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.controls.LayoutFactory;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.SortedSet;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * Frame which includes acceleration graph
 * @author Christopher-Eyk Hrabia
 *
 */
public class AccelerationChartFrame extends JFrame implements IIntervalUpdate {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7861543140008750123L;

	private AccelerationChartFrame instance;

	protected AccelerationChartManager manager;

	protected Hand hand;

	protected JointType type;

	protected Chart2D chart;

	private long starttime = 0;

	public AccelerationChartFrame(AccelerationChartManager _manager,
			final Hand hand, final JointType type, final int valueLimit) {
		super("Acceleration " + type);
		instance = this;
		this.manager = _manager;
		this.hand = hand;
		this.type = type;
		
		HelpManager.getInstance().enableHelpKey(this, "diagramms");

		chart = new Chart2D();

		chart.getAxisY().setAxisTitle(new AxisTitle("Acceleration in m/s^2"));
		chart.getAxisX().setAxisTitle(new AxisTitle("Time"));

		ITrace2D traceX = new Trace2DLtd(valueLimit);
		traceX.setName("X");
		traceX.setColor(Color.RED);
		chart.addTrace(traceX);

		ITrace2D traceY = new Trace2DLtd(valueLimit);
		traceY.setName("Y");
		traceY.setColor(Color.BLUE);
		chart.addTrace(traceY);

		ITrace2D traceZ = new Trace2DLtd(valueLimit);
		traceZ.setName("Z");
		traceZ.setColor(Color.GREEN);
		chart.addTrace(traceZ);

		LayoutFactory lfct = LayoutFactory.getInstance();
		ChartPanel chartpanel = new ChartPanel(chart);

		ImageIcon icon = new ImageIcon(getClass()
				.getResource("/Icons/hand.png"));
		this.setIconImage(icon.getImage());
		// add the chart to the this:
		this.getContentPane().add(chartpanel);
		this.setJMenuBar(lfct.createChartMenuBar(chartpanel, false));
		this.setSize(500, 200);
		// Enable the termination button [cross on the upper right edge]:
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				instance.setVisible(false);
				instance.dispose();
				manager.removeChart(type);
			}
		});
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof JointType) {
			return type == (JointType) obj;
		}

		return super.equals(obj);
	}

	public JointType getJointType() {
		return type;
	}

	public Hand getHand() {
		return hand;
	}

	public void setHand(Hand hand) {
		this.hand = hand;
	}

	public void update(long currentTime) {
		
		// leave first update for getting starttime
		if (starttime == 0) {
			starttime = currentTime;
			return;
		}

		Joint joint = hand.getJoint(type);

		float[] acceleration = joint.getAcceleration();

		SortedSet<ITrace2D> traces = chart.getTraces();
		int i = 0;
		for (ITrace2D trace : traces) {
			trace.addPoint(
					((double) currentTime - this.starttime),
					acceleration[i]);
			i++;
		}
	}
}
