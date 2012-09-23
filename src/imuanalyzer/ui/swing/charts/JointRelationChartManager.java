package imuanalyzer.ui.swing.charts;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.utils.parallel.IIntervalUpdate;
import imuanalyzer.utils.parallel.IntervalUpdater;

import java.util.ArrayList;

/**
 * Manager for several relation chart frames,
 * handles updates, creation and so on
 * 
 * @author Christopher-Eyk Hrabia
 *
 */
public class JointRelationChartManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final long UPDATE_CYCLE = 250;
	private static final int VALUES_LIMIT = 250;

	private ArrayList<IIntervalUpdate> relations = new ArrayList<IIntervalUpdate>();

	protected Hand hand;

	protected IntervalUpdater thread;

	public JointRelationChartManager(Hand hand) {
		this.hand = hand;
	}

	public void addDynamicChart(final JointType type1, final JointType type2,
			boolean visible) {

		for (IIntervalUpdate jR : relations) {
			JointRelationChartFrame frame = (JointRelationChartFrame) jR;
			if (frame.equals(type1, type2)) {
				return;
			}
		}
		JointRelationChartFrame frame = new JointRelationChartFrame("Live",
				this, hand, type1, type2, VALUES_LIMIT, true);
		frame.setVisible(visible);
		relations.add(frame);

		if (relations.size() == 1) {
			thread = new IntervalUpdater(relations, UPDATE_CYCLE);
			thread.start();
		}
	}

	public void removeDynamicChart(final JointType type1, final JointType type2) {
		for (IIntervalUpdate jR : relations) {
			JointRelationChartFrame frame = (JointRelationChartFrame) jR;
			if (frame.equals(type1, type2)) {
				relations.remove(jR);
				return;
			}
		}
		if (relations.size() == 0) {
			thread.setStop(true);
		}

	}

	/**
	 * get a non automatic updated chart frame with preset max value count
	 * 
	 * @param type1
	 * @param type2
	 * @param valueMax
	 * @return
	 */
	public JointRelationChartFrame getStaticChart(String namePostfix,
			final JointType type1, final JointType type2, final int valueMax) {

		return (new JointRelationChartFrame(namePostfix, this, hand, type1,
				type2, valueMax, false));
	}

	public ArrayList<JointRelationChartFrame> getCharts() {
		ArrayList<JointRelationChartFrame> relationFrames = new ArrayList<JointRelationChartFrame>();

		for (IIntervalUpdate jR : relations) {
			JointRelationChartFrame frame = (JointRelationChartFrame) jR;
			relationFrames.add(frame);
		}

		return relationFrames;
	}

}