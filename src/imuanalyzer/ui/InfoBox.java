package imuanalyzer.ui;

import imuanalyzer.filter.Quaternion;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.IJoint;
import imuanalyzer.signalprocessing.Joint;
import imuanalyzer.signalprocessing.Restriction;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class InfoBox extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2627290249147451521L;

	private static final long SLEEP_TIME = 600;

	Hand hand;

	JTable infoTable;

	ArrayList<JointType> observedJoits = new ArrayList<Hand.JointType>();

	public InfoBox(Hand _hand) {
		this.hand = _hand;

		JScrollPane scrollPane;

		infoTable = new JTable(0,2);
		infoTable.setCellSelectionEnabled(false);

		DefaultTableModel model = (DefaultTableModel) infoTable.getModel();
		model.setColumnIdentifiers(new String[]{"Element","Values"});

		scrollPane = new JScrollPane(infoTable);
		scrollPane.setPreferredSize(new Dimension(350, 180));

		this.add(scrollPane);

		//new Updater(observedJoits, hand, model).start();
	}

	public void addJointAngle(JointType type) {
		if (!observedJoits.contains(type)) {
			observedJoits.add(type);
		}
	}
	
	public boolean isObserved(JointType type){
		return observedJoits.contains(type);
	}
	
	public void removeJointAngle(JointType type) {
		observedJoits.remove(type);
	}

	public void addStatistics(JointType type) {

	}

	private static class Updater extends Thread {

		protected Hand hand;

		ArrayList<JointType> observedJoits;

		DefaultTableModel tableModel;

		public void run() {
			while (true) {
				
				tableModel.setRowCount(observedJoits.size());

				// TODO
				for (int i = 0; i < observedJoits.size(); i++) {
					Joint joint = hand.getJoint(observedJoits.get(i));
					IJoint parent = joint.getParent();
					
					Quaternion quat = null;
					if (parent != null) {
						quat = joint.getWorldOrientation().quaternionProduct(
								parent.getWorldOrientation().getConjugate());
					} else {
						quat = joint.getLocalOrientation();
					}
					double[] angles = quat.getAnglesRadFromQuaternion();
					Restriction restriction = joint.getRestriction();

					StringBuffer values = new StringBuffer("");
					if (restriction.isRollAllowed()) {
						values.append("x:");
						values.append(String.format("%.1f", angles[0] * 180 / Math.PI));
					}
					if (restriction.isPitchAllowed()) {
						values.append("y:");
						values.append(String.format("%.1f", angles[1] * 180 / Math.PI));
					}
					if (restriction.isYawAllowed()) {
						values.append("z:");
						values.append(String.format("%.1f", angles[2] * 180 / Math.PI));
					}


					tableModel.setValueAt(joint.getName(), i, 0);
					tableModel.setValueAt(values.toString(), i, 1);
				}

				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		private Updater(ArrayList<JointType> observedJoits, Hand hand,
				DefaultTableModel tableModel) {
			this.observedJoits = observedJoits;
			this.hand = hand;
			this.tableModel = tableModel;
		}
	}
}
