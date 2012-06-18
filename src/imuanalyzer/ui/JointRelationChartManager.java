package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.Joint;
import imuanalyzer.signalprocessing.Restriction;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.controls.LayoutFactory;
import info.monitorenter.gui.chart.pointpainters.PointPainterDisc;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.SortedSet;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class JointRelationChartManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final long SLEEP_TIME = 250;
	private static final int VALUES_LIMIT = 250;

	private ArrayList<JointRelation> relations = new ArrayList<JointRelationChartManager.JointRelation>();

	protected Hand hand;

	protected UpdaterRelation thread;

	public JointRelationChartManager(Hand hand) {
		this.hand = hand;
	}

	public void addChart(final JointType type1, final JointType type2) {

		for (JointRelation jR : relations) {
			if (jR.equals(type1, type2)) {
				return;
			}
		}

		relations.add(new JointRelation(this, hand, type1, type2));

		if (relations.size() == 1) {
			thread = new UpdaterRelation(relations);
			thread.start();
		}
	}

	public void removeChart(final JointType type1, final JointType type2) {
		for (JointRelation jR : relations) {
			if (jR.equals(type1, type2)) {
				relations.remove(jR);
				return;
			}
		}
		if (relations.size() == 0) {
			thread.setStop(true);
		}
	}

	private static class JointRelation extends JFrame {

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

		public JointRelation(final JointRelationChartManager manager,
				final Hand hand, final JointType type1, final JointType type2) {
			super("Relation " + type1 + " / " + type2);
			instance = this;

			this.hand = hand;
			this.type1 = type1;
			this.type2 = type2;

			chart = new Chart2D();
			
			chart.getAxisX().setAxisTitle(new AxisTitle("Degree of "+ hand.getJoint(type1).getInfoName()));
			chart.getAxisY().setAxisTitle(new AxisTitle("Degree of "+ hand.getJoint(type2).getInfoName()));
			
			LayoutFactory lfct = LayoutFactory.getInstance();
			ChartPanel chartpanel = new ChartPanel(chart);
			
			Joint j1 = hand.getJoint(type1);

			Joint j2 = hand.getJoint(type2);

			Restriction r1 = j1.getRestriction();
			Restriction r2 = j2.getRestriction();

			if (r1.isRollAllowed() && r2.isRollAllowed()) {
				ITrace2D traceX = new Trace2DLtd(VALUES_LIMIT);
				traceX.setName("Roll");
				traceX.setColor(Color.RED);
				chart.addTrace(traceX);
				traceX.setTracePainter(new TracePainterDisc());
			}

			if (r1.isPitchAllowed() && r2.isPitchAllowed()) {
				ITrace2D traceY = new Trace2DLtd(VALUES_LIMIT);
				traceY.setName("Pitch");
				traceY.setColor(Color.BLUE);
				chart.addTrace(traceY);
				traceY.setTracePainter(new TracePainterDisc());
			}

			if (r1.isYawAllowed() && r2.isYawAllowed()) {
				ITrace2D traceZ = new Trace2DLtd(VALUES_LIMIT);
				traceZ.setName("Yaw");
				traceZ.setColor(Color.GREEN);
				chart.addTrace(traceZ);
				traceZ.setTracePainter(new TracePainterDisc());
			}

			ImageIcon icon = new ImageIcon(getClass().getResource(
					"/Icons/hand.png"));
			this.setIconImage(icon.getImage());
			// add the chart to the frame:
			this.getContentPane().add(chartpanel);
			this.setSize(400, 200);
			this.setJMenuBar(lfct.createChartMenuBar(chartpanel, false));
			// Enable the termination button [cross on the upper right edge]:
			this.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					instance.setVisible(false);
					instance.dispose();
					manager.removeChart(type1, type2);
				}
			});

			setVisible(true);
		}

		public boolean equals(final JointType type1, final JointType type2) {
			return this.type1 == type1 && this.type2 == type2;
		}

		public void update() {

			double[] angles1 = hand.getJoint(type1).getRotationBetweenParent()
					.getAnglesRadFromQuaternion();

			double[] angles2 = hand.getJoint(type2).getRotationBetweenParent()
					.getAnglesRadFromQuaternion();

			double[] newReleation = { angles1[0] / angles2[0],
					angles1[1] / angles2[1], angles1[2] / angles2[2] };

			if (Math.abs(currentRelation[0] - newReleation[0]) > 0.000001
					|| Math.abs(currentRelation[1] - newReleation[1]) > 0.000001
					|| Math.abs(currentRelation[2] - newReleation[2]) > 0.000001) {
				currentRelation = newReleation;
				SortedSet<ITrace2D> traces = chart.getTraces();
				int i = 0;
				for (ITrace2D trace : traces) {
					trace.addPoint(Math.abs(angles1[i]) * 180 / Math.PI,
							Math.abs(angles2[i]) * 180 / Math.PI);
					i++;
				}
			}
		}

	}

	private static class UpdaterRelation extends Thread {

		private ArrayList<JointRelation> relations;

		private boolean stop = false;

		public UpdaterRelation(ArrayList<JointRelation> relations) {
			this.relations = relations;
		}

		public void run() {
			while (true) {
				if (stop) {
					break;
				}
				try {

					for (JointRelation jR : relations) {
						jR.update();
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