package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;

import java.util.ArrayList;

public class JointRelationChartManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final long UPDATE_CYCLE = 250;
	private static final int VALUES_LIMIT = 250;

	private ArrayList<JointRelationChartFrame> relations = new ArrayList<JointRelationChartFrame>();

	protected Hand hand;

	protected UpdaterRelation thread;

	public JointRelationChartManager(Hand hand) {
		this.hand = hand;
	}

	public void addDynamicChart(final JointType type1, final JointType type2) {

		for (JointRelationChartFrame jR : relations) {
			if (jR.equals(type1, type2)) {
				return;
			}
		}

		relations.add(new JointRelationChartFrame(this, hand, type1, type2,
				VALUES_LIMIT));

		if (relations.size() == 1) {
			thread = new UpdaterRelation(relations);
			thread.start();
		}
	}

	public void removeDynamicChart(final JointType type1, final JointType type2) {
		for (JointRelationChartFrame jR : relations) {
			if (jR.equals(type1, type2)) {
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
	public JointRelationChartFrame getStaticChart(final JointType type1,
			final JointType type2, final int valueMax) {

		return (new JointRelationChartFrame(this, hand, type1, type2, valueMax));
	}

	public ArrayList<JointRelationChartFrame> getRelations() {
		return relations;
	}

	private static class UpdaterRelation extends Thread {

		private ArrayList<JointRelationChartFrame> relations;

		private boolean stop = false;

		public UpdaterRelation(ArrayList<JointRelationChartFrame> relations) {
			this.relations = relations;
		}

		public void run() {
			while (true) {
				if (stop) {
					break;
				}
				try {

					for (JointRelationChartFrame jR : relations) {
						jR.update();
					}

					Thread.sleep(UPDATE_CYCLE);
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