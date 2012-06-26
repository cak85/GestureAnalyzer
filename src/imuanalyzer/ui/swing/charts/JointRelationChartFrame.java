package imuanalyzer.ui.swing.charts;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.Joint;
import imuanalyzer.signalprocessing.Restriction;
import imuanalyzer.utils.math.AngleHelper;
import imuanalyzer.utils.math.LinearRegression;
import imuanalyzer.utils.math.RunningAvg;
import imuanalyzer.utils.parallel.IIntervalUpdate;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.controls.LayoutFactory;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public  class JointRelationChartFrame extends JFrame implements IIntervalUpdate{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3870599753179111998L;

	protected double[] currentRelation = { 0, 0, 0 };

	protected JointType type1;
	protected JointType type2;

	protected Hand hand;

	private JFrame instance;

	protected Chart2D chart;

	protected ArrayList<ArrayBlockingQueue<double[]>> values = new ArrayList<ArrayBlockingQueue<double[]>>();

	protected double minX = Double.MAX_VALUE;

	protected double maxX = 0;

	ArrayList<ITrace2D> rawPoints = new ArrayList<ITrace2D>();

	ArrayList<ITrace2D> regressionCurves = new ArrayList<ITrace2D>();

	JLabel resultLabel = new JLabel("   ");
	JLabel resultLabelR2 = new JLabel("   ");

	RunningAvg mAvg = new RunningAvg(10);

	RunningAvg r2Avg = new RunningAvg(10);

	public JointRelationChartFrame(final JointRelationChartManager manager,
			final Hand hand, final JointType type1, final JointType type2,
			final int valueLimit) {
		super("Relation " + type1 + " / " + type2);
		instance = this;

		this.setLayout(new BorderLayout());

		this.hand = hand;
		this.type1 = type1;
		this.type2 = type2;

		chart = new Chart2D();

		String j1Name = hand.getJoint(type1).getInfoName();
		String j2Name = hand.getJoint(type2).getInfoName();

		chart.getAxisX().setAxisTitle(
				new AxisTitle("Angle in degree of " + j1Name));
		chart.getAxisY().setAxisTitle(
				new AxisTitle("Angle in degree of " + j2Name));

		LayoutFactory lfct = LayoutFactory.getInstance();
		ChartPanel chartpanel = new ChartPanel(chart);

		Joint j1 = hand.getJoint(type1);

		Joint j2 = hand.getJoint(type2);

		Restriction r1 = j1.getRestriction();
		Restriction r2 = j2.getRestriction();

		if (r1.isRollAllowed() && r2.isRollAllowed()) {
			ITrace2D traceX = new Trace2DLtd(valueLimit);
			traceX.setName("Roll");
			traceX.setColor(Color.RED);
			chart.addTrace(traceX);
			traceX.setTracePainter(new TracePainterDisc());
			rawPoints.add(traceX);
		}

		if (r1.isPitchAllowed() && r2.isPitchAllowed()) {
			ITrace2D traceY = new Trace2DLtd(valueLimit);
			traceY.setName("Pitch");
			traceY.setColor(Color.BLUE);
			chart.addTrace(traceY);
			traceY.setTracePainter(new TracePainterDisc());
			rawPoints.add(traceY);
		}

		if (r1.isYawAllowed() && r2.isYawAllowed()) {
			ITrace2D traceZ = new Trace2DLtd(valueLimit);
			traceZ.setName("Yaw");
			traceZ.setColor(Color.GREEN);
			chart.addTrace(traceZ);
			traceZ.setTracePainter(new TracePainterDisc());
			rawPoints.add(traceZ);
		}

		for (int i = 0; i < rawPoints.size(); i++) {
			values.add(new ArrayBlockingQueue<double[]>(valueLimit));
			ITrace2D traceR = new Trace2DLtd(2);
			traceR.setName("Regression curve " + (i + 1));
			// TODO every trace needs regression curve color
			traceR.setColor(Color.ORANGE);
			chart.addTrace(traceR);
			regressionCurves.add(traceR);
		}

		ImageIcon icon = new ImageIcon(getClass().getResource(
				"/Icons/hand.png"));
		this.setIconImage(icon.getImage());
		// add the chart to the frame:
		this.add(chartpanel, BorderLayout.CENTER);

		JPanel resultPanel = new JPanel(new FlowLayout());
		resultPanel.add(new JLabel(j2Name + " = "));
		resultPanel.add(resultLabel);
		resultPanel.add(new JLabel("* " + j1Name + " with R^2 = "));
		resultPanel.add(resultLabelR2);
		this.add(resultPanel, BorderLayout.SOUTH);

		this.setSize(400, 200);
		this.setJMenuBar(lfct.createChartMenuBar(chartpanel, false));
		// Enable the termination button [cross on the upper right edge]:
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				instance.setVisible(false);
				instance.dispose();
				manager.removeDynamicChart(type1, type2);
			}
		});
	}

	public boolean equals(final JointType type1, final JointType type2) {
		return this.type1 == type1 && this.type2 == type2;
	}

	public void update() {

		double[] angles1 = hand.getJoint(type1).getRotationBetweenParent()
				.getAnglesRad();

		double[] angles2 = hand.getJoint(type2).getRotationBetweenParent()
				.getAnglesRad();

		double[] newReleation = { angles1[0] / angles2[0],
				angles1[1] / angles2[1], angles1[2] / angles2[2] };

		if (Math.abs(currentRelation[0] - newReleation[0]) > 0.000001
				|| Math.abs(currentRelation[1] - newReleation[1]) > 0.000001
				|| Math.abs(currentRelation[2] - newReleation[2]) > 0.000001) {
			currentRelation = newReleation;
			int i = 0;
			for (ITrace2D trace : rawPoints) {
				double x = Math.abs(AngleHelper.degFromRad(angles1[i]));
				double y = Math.abs(AngleHelper.degFromRad(angles2[i]));
				maxX = Math.max(x, maxX);
				minX = Math.min(x, minX);
				trace.addPoint(x, y);

				ArrayBlockingQueue<double[]> currentValues = values.get(i);

				if (currentValues.remainingCapacity() == 0) {
					currentValues.poll();
				}

				currentValues.add(new double[] { x, y });

				// draw regression curve
				LinearRegression regression = new LinearRegression(
						currentValues);

				double m = regression.getBeta1();

				double n = regression.getBeta0();

				ITrace2D regTrace = regressionCurves.get(i);
				regTrace.addPoint(minX, m * minX + n);
				regTrace.addPoint(maxX, m * maxX + n);

				System.out.println(m);

				mAvg.add(m);

				r2Avg.add(regression.getR2());

				resultLabel.setText(String.format("%.3f", mAvg.getAvg()));

				resultLabelR2
						.setText(String.format("%.3f", r2Avg.getAvg()));

				i++;
			}

		}
	}

	public Hand getHand() {
		return hand;
	}

	public void setHand(Hand hand) {
		this.hand = hand;
	}

	public JointType getType1() {
		return type1;
	}

	public JointType getType2() {
		return type2;
	}
}