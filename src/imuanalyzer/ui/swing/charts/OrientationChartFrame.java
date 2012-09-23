package imuanalyzer.ui.swing.charts;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.ui.swing.help.HelpManager;
import imuanalyzer.utils.math.AngleHelper;
import imuanalyzer.utils.math.Quaternion;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.controls.LayoutFactory;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * Frame which includes orientation graph in degree 
 * TODO only possible axes! see relation..
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public class OrientationChartFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4359480949336974350L;

	Hand hand;

	JointType type;

	Chart2D chart;

	OrientationChartFrame instance;

	private long starttime = 0;

	ArrayList<ITrace2D> traces = new ArrayList<ITrace2D>();

	public OrientationChartFrame(final OrientationChartManager manager,
			final Hand hand, final JointType _type, int nrOfTraceGroups,
			int valuesLimit) {
		super("Orientation " + _type);
		instance = this;
		this.hand = hand;
		this.type = _type;
		
		HelpManager.getInstance().enableHelpKey(this, "diagramms");

		chart = new Chart2D();

		chart.getAxisY().setAxisTitle(new AxisTitle("Angle in degree"));
		chart.getAxisX().setAxisTitle(new AxisTitle("Time"));

		LayoutFactory lfct = LayoutFactory.getInstance();
		ChartPanel chartpanel = new ChartPanel(chart);

		for (int i = 0; i < nrOfTraceGroups; i++) {

			ITrace2D traceX = new Trace2DLtd(valuesLimit);
			traceX.setName("Pitch");
			traceX.setColor(Color.RED);
			chart.addTrace(traceX);
			traces.add(traceX);

			ITrace2D traceY = new Trace2DLtd(valuesLimit);
			traceY.setName("Roll");
			traceY.setColor(Color.BLUE);
			chart.addTrace(traceY);
			traces.add(traceY);

			ITrace2D traceZ = new Trace2DLtd(valuesLimit);
			traceZ.setName("Yaw");
			traceZ.setColor(Color.GREEN);
			chart.addTrace(traceZ);
			traces.add(traceZ);
		}

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
					manager.removeChart(type);
				}
			}
		});
	}

	public void update(long currentTime, int traceGroup) {

		// leave first update for getting starttime
		if (starttime == 0) {
			starttime = currentTime;
			return;
		}

		Quaternion orientation = hand.getJoint(type).getLocalOrientation();

		double[] angles = orientation.getAnglesRad();

		int i = 0;

		int start = traceGroup * 3;

		for (int j = start; j < start + 3; j++) {

			traces.get(j).addPoint(((double) currentTime - this.starttime),
					AngleHelper.degFromRad(angles[i]));
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