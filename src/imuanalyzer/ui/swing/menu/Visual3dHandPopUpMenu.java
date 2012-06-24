package imuanalyzer.ui.swing.menu;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.ui.ColorSettingsDialog;
import imuanalyzer.ui.InfoBox;
import imuanalyzer.ui.JointSetting;
import imuanalyzer.ui.Visual3d;

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

	InfoBox infoBox;

	public Visual3dHandPopUpMenu(Visual3d visual3d, Hand hand,
			JointType jointType, InfoBox infoBox, MenuFactory menuFactory) {
		this.visual3d = visual3d;
		this.jointType = jointType;
		this.hand = hand;
		this.infoBox = infoBox;

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

		// infobox
		if (infoBox.isObserved(jointType)) {
			anItem = new JMenuItem("Remove info");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					removeInfo();
				}

			});
			add(anItem);
		} else {
			anItem = new JMenuItem("Add info");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					addInfo();
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

		// charts
		add(menuFactory.createJointGraphMenu(hand.getJoint(jointType),
				"Show chart"));

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

	private void addInfo() {
		infoBox.addInfo(hand.getJoint(jointType));
	}

	private void removeInfo() {
		infoBox.removeInfo(hand.getJoint(jointType));
	}

	private void startColorSettings() {
		LOGGER.debug("" + jointType);
		new ColorSettingsDialog(this, visual3d.getJointSetting(jointType));
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
		visual3d.clearTouchLines();
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