package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Joint;
import imuanalyzer.signalprocessing.Hand.JointType;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

class Visual3dHandPopUpMenu extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4904278609301818369L;

	JointType jointType;

	Hand hand;

	Visual3d visual3d;

	public Visual3dHandPopUpMenu(Visual3d visual3d, Hand hand, JointType jointType) {
		this.visual3d = visual3d;
		this.jointType = jointType;
		this.hand = hand;

		JMenuItem anItem;

		// observ motion
		if (hand.getMotionAnalysis(jointType)!=null) {
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
		if (hand.getTouchAnalysis(jointType) !=null) {
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
		hand.removeSaveMotionJoint(jointType);
		visual3d.clearLiveMovement();
	}

	private void analyzeMovement() {
		try {
			if(hand.addSaveMotionJoint(jointType)){
				visual3d.clearLiveMovement();
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					e.getMessage(), "Error",
					JOptionPane.ERROR);
		}
	}

	private void disableAnalyzeTouch() {
		hand.removeSaveTouchLineJoint(jointType);
	}

	private void analyzeTouch() {
		hand.addSaveTouchLineJoint(jointType);
	}

	private void switchVisibility() {
		Joint j = hand.getJoint(jointType);

		j.setVisible(!j.isVisible());
	}

	private void setManualManipulatedJoint() {
		visual3d.setCurrentManipulatedJoint(jointType);
	}

}