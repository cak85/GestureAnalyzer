package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

class Visual3dHandPopUpMenu extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4904278609301818369L;
	
	private static final Logger LOGGER = Logger
	.getLogger(Visual3dHandPopUpMenu.class.getName());

	JointType jointType;

	Hand hand;

	Visual3d visual3d;

	public Visual3dHandPopUpMenu(Visual3d visual3d, Hand hand,
			JointType jointType) {
		this.visual3d = visual3d;
		this.jointType = jointType;
		this.hand = hand;

		JMenuItem anItem;

		// observ motion
		if (hand.getMotionAnalysis(jointType) != null) {
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
		if (hand.getTouchAnalysis(jointType) != null) {
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
		// color settings
		anItem = new JMenuItem("Color settings");
		anItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startColorSettings();
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

	private void startColorSettings() {
		LOGGER.debug(""+jointType);
		new ColorSettingsDialog(this,visual3d.getJointSetting(jointType));
	}

	private void disableAnalyzeMovement() {
		hand.removeSaveMotionJoint(jointType);
		visual3d.clearLiveMovement();
	}

	private void analyzeMovement() {
		try {
			if (hand.addSaveMotionJoint(jointType)) {
				visual3d.clearLiveMovement();
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
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
		JointSetting settting = visual3d.getJointSetting(jointType);
		settting.setVisible(!settting.isVisible());
	}

	private void setManualManipulatedJoint() {
		visual3d.setCurrentManipulatedJoint(jointType);
	}

}