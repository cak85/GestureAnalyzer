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

		// observ motion
		if (hand.isSaveMovement()
				&& jointType == hand.getSavedMovementStartJoint()) {
			anItem = new JMenuItem("Disable analyze movement");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					disableAnalyzeMovement();
				}
			});
			add(anItem);
		} else {

			anItem = new JMenuItem("Analyze movement");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					analyzeMovement();
				}
			});
			add(anItem);
		}
		// observ touch
		if (hand.getSaveTouchLine()
				&& jointType == hand.getSaveTouchLineJoint()) {
			anItem = new JMenuItem("Disable analyze touch");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					disableAnalyzeTouch();
				}
			});
			add(anItem);
		} else {
			anItem = new JMenuItem("Analyze touch");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					analyzeTouch();
				}
			});
			add(anItem);
		}

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

	private void disableAnalyzeMovement() {
		hand.setSaveMovement(false);
	}

	private void analyzeMovement() {
		hand.setSaveMovement(true);
		hand.setSavedMovementStartJoint(jointType);
	}

	private void disableAnalyzeTouch() {
		hand.setSaveTouchLine(false);
	}

	private void analyzeTouch() {
		hand.setSaveTouchLine(true);
		hand.setSaveTouchLineJoint(jointType);
	}

	private void switchVisibility() {
		Joint j = hand.getJoint(jointType);

		j.setVisible(!j.isVisible());
	}

	private void setManualManipulatedJoint() {
		visual3d.setCurrentManipulatedJoint(jointType);
	}

}