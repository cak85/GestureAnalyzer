package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Joint;
import imuanalyzer.signalprocessing.Hand.JointType;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

class Visual3dPopUpMenu extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4904278609301818369L;

	JointType jointType;

	Hand hand;

	Visual3d visual3d;

	public Visual3dPopUpMenu(Visual3d visual3d, Hand hand, JointType jointType) {
		this.visual3d = visual3d;
		this.jointType = jointType;
		this.hand = hand;

		JMenuItem anItem;
		
		// observ
		anItem = new JMenuItem("Analyze movement");
		anItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				analyzeMovement();
			}
		});
		add(anItem);

		// manipulate
		anItem = new JMenuItem("Manual manipulation");
		anItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setManualManipulatedJoint();
			}
		});
		add(anItem);
		
		// visibility
		anItem = new JMenuItem("Visible On/Off");
		anItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switchVisibility();
			}
		});
		add(anItem);

	}

	private void analyzeMovement() {
		hand.setSaveMovement(true);
		hand.setSavedMovementStartJoint(jointType);
	}

	private void switchVisibility() {
		Joint j = hand.getJoint(jointType);

		j.setVisible(!j.isVisible());
	}

	private void setManualManipulatedJoint() {
		visual3d.setCurrentManipulatedJoint(jointType);
	}

}