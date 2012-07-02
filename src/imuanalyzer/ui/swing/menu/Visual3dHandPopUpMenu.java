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

public class Visual3dHandPopUpMenu extends JPopupMenu {

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

	FinishListenerHandler finishHandler = new FinishListenerHandler();

	public FinishListenerHandler getFinishHandler() {
		return finishHandler;
	}

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
					finishHandler.notifyFinished();
				}
			});
			add(anItem);
		} else {

			anItem = new JMenuItem("Analyze movement");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					analyzeMovement();
					finishHandler.notifyFinished();
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
					finishHandler.notifyFinished();
				}
			});
			add(anItem);
		} else {
			anItem = new JMenuItem("Analyze touch");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					analyzeTouch();
					finishHandler.notifyFinished();
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
					finishHandler.notifyFinished();
				}

			});
			add(anItem);
		} else {
			anItem = new JMenuItem("Add info");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					addInfo();
					finishHandler.notifyFinished();
				}

			});
			add(anItem);
		}

		// add motion info
		if (hand.getMotionAnalysis(jointType) != null) {
			anItem = new JMenuItem("Add motion info");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					addMotionInfo();
					finishHandler.notifyFinished();
				}

			});
			add(anItem);
		}

		// add touch info
		if (hand.getTouchAnalysis(jointType) != null) {
			anItem = new JMenuItem("Add touch info");
			anItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					addTouchInfo();
					finishHandler.notifyFinished();
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
				finishHandler.notifyFinished();
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
				finishHandler.notifyFinished();
			}
		});
		add(anItem);

		// visibility
		anItem = new JMenuItem("Visible On/Off");
		anItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switchVisibility();
				finishHandler.notifyFinished();
			}
		});
		add(anItem);

	}

	private void addMotionInfo() {
		infoBox.addInfo(hand.getMotionAnalysis(jointType));
	}

	private void addTouchInfo() {
		infoBox.addInfo(hand.getTouchAnalysis(jointType));
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