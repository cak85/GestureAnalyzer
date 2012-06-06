package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.IOrientationSensors;
import imuanalyzer.signalprocessing.Joint;
import imuanalyzer.signalprocessing.Hand.JointType;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map.Entry;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

public class MainMenuBar extends JMenuBar {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6031023287066374514L;

	protected Visual3d visual3d;
	protected IOrientationSensors sensors;

	protected OrientationChart chartOrientation;
	protected AccelerationChart chartsAcceleration;

	protected Hand hand;

	JMenuBar instance;

	public MainMenuBar(Hand _hand, Visual3d _visual3d,
			IOrientationSensors _sensors, OrientationChart _chartOrientation,
			AccelerationChart _chartsAcceleration) {
		instance = this;
		hand = _hand;
		visual3d = _visual3d;
		sensors = _sensors;
		chartOrientation = _chartOrientation;
		chartsAcceleration = _chartsAcceleration;

		JMenu menu;
		JMenuItem menuItem;

		// Build tools menu
		menu = new JMenu("Control");

		// disable movement saving
		menuItem = new JMenuItem("Clear live movement anaylsis");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				hand.disableMotionAnalysis();
				visual3d.clearLiveMovement();
			}
		});
		menu.add(menuItem);

		// disable movement saving
		menuItem = new JMenuItem("Clear live touch anaylsis");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				hand.disableTouchAnalysis();
				visual3d.clearTouchLines();
			}
		});
		menu.add(menuItem);

		// clear analysis
		menuItem = new JMenuItem("Clear analysis");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				visual3d.setAnalyses(null);
			}
		});
		menu.add(menuItem);

		// reset hand
		menuItem = new JMenuItem("Reset hand");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				visual3d.resetHand();
			}
		});
		menu.add(menuItem);

		menuItem = new JMenuItem("Calibration");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				sensors.calibrate();
				visual3d.resetHand();
			}
		});

		menu.add(menuItem);

		menuItem = new JMenuItem("Take screenshot");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser(".");
				FileFilter filterCSV = new ExtensionFileFilter("PNG",
						new String[] { "png" });
				fileChooser.setFileFilter(filterCSV);
				int status = fileChooser.showSaveDialog(instance);
				if (status == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();

					if (!selectedFile.getAbsolutePath().endsWith(".png")) {
						selectedFile = new File(selectedFile + ".png");
					}

					System.out.println(selectedFile.getAbsolutePath());
					visual3d.takeScreenshot(selectedFile.getAbsolutePath());

				} else if (status == JFileChooser.CANCEL_OPTION) {
					System.out.println(JFileChooser.CANCEL_OPTION);
				}

			}
		});

		menu.add(menuItem);

		this.add(menu);

		// build view menu

		menu = new JMenu("View");

		JMenu submenuAdd = new JMenu("Add chart");
		JMenu submenuRemove = new JMenu("Remove chart");

		menu.add(submenuAdd);
		menu.add(submenuRemove);

		for (Entry<JointType, Joint> entry : hand.getJointSet()) {

			Joint j = entry.getValue();
			final JointType type = entry.getKey();

			JMenu jointMenuAdd = new JMenu(j.getName());

			JMenuItem submenuitemAddOrientation = new JMenuItem("Orientation");
			submenuitemAddOrientation.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					chartOrientation.addChart(type);
				}
			});
			jointMenuAdd.add(submenuitemAddOrientation);

			JMenuItem submenuitemAddAccelerartion = new JMenuItem(
					"Acceleration");
			submenuitemAddAccelerartion.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					chartsAcceleration.addChart(type);
				}
			});

			jointMenuAdd.add(submenuitemAddAccelerartion);
			submenuAdd.add(jointMenuAdd);

			JMenu jointMenuRemove = new JMenu(j.getName());

			JMenuItem submenuitemRemoveOrientation = new JMenuItem(
					"Orientation");
			submenuitemRemoveOrientation
					.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							chartOrientation.removeChart(type);
						}
					});
			jointMenuRemove.add(submenuitemRemoveOrientation);
			JMenuItem submenuitemRemoveAcceleration = new JMenuItem(
					"Acceleration");
			submenuitemRemoveAcceleration
					.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							chartsAcceleration.removeChart(type);
						}
					});
			jointMenuRemove.add(submenuitemRemoveAcceleration);
			submenuRemove.add(jointMenuRemove);
		}

		JCheckBoxMenuItem checkMenuItem = new JCheckBoxMenuItem("Right Hand");
		checkMenuItem.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				JCheckBoxMenuItem checkMenuItem = (JCheckBoxMenuItem) arg0
						.getSource();
				visual3d.setRightHand(checkMenuItem.isSelected());
			}
		});
		menu.add(checkMenuItem);
		
		checkMenuItem = new JCheckBoxMenuItem("Show live hand");
		checkMenuItem.setSelected(true);
		checkMenuItem.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				JCheckBoxMenuItem checkMenuItem = (JCheckBoxMenuItem) arg0
						.getSource();
				visual3d.setLiveHandVisible(checkMenuItem.isSelected());
			}
		});
		menu.add(checkMenuItem);

		checkMenuItem = new JCheckBoxMenuItem("Show Skeleton");
		checkMenuItem.setSelected(true);
		checkMenuItem.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				JCheckBoxMenuItem checkMenuItem = (JCheckBoxMenuItem) arg0
						.getSource();
				visual3d.setSkeletonVisible(checkMenuItem.isSelected());
			}
		});
		menu.add(checkMenuItem);

		checkMenuItem = new JCheckBoxMenuItem("Device dummy visible");
		checkMenuItem.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				JCheckBoxMenuItem checkMenuItem = (JCheckBoxMenuItem) arg0
						.getSource();
				visual3d.setDeviceVisible(checkMenuItem.isSelected());
			}
		});
		menu.add(checkMenuItem);

		checkMenuItem = new JCheckBoxMenuItem("Show FPS");
		checkMenuItem.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				JCheckBoxMenuItem checkMenuItem = (JCheckBoxMenuItem) arg0
						.getSource();
				visual3d.setShowFPS(checkMenuItem.isSelected());
			}
		});
		menu.add(checkMenuItem);

		checkMenuItem = new JCheckBoxMenuItem("Show 3d engine statistics");
		checkMenuItem.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				JCheckBoxMenuItem checkMenuItem = (JCheckBoxMenuItem) arg0
						.getSource();
				visual3d.setShowStatistics(checkMenuItem.isSelected());
			}
		});
		menu.add(checkMenuItem);

		checkMenuItem = new JCheckBoxMenuItem("Show grid");
		checkMenuItem.setSelected(true);
		checkMenuItem.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				JCheckBoxMenuItem checkMenuItem = (JCheckBoxMenuItem) arg0
						.getSource();
				visual3d.setGridVisibility(checkMenuItem.isSelected());
			}
		});
		menu.add(checkMenuItem);

		checkMenuItem = new JCheckBoxMenuItem("Show coordinate axis");
		checkMenuItem.setSelected(true);
		checkMenuItem.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				JCheckBoxMenuItem checkMenuItem = (JCheckBoxMenuItem) arg0
						.getSource();
				visual3d.setCoordinateAxisVisibile(checkMenuItem.isSelected());
			}
		});
		menu.add(checkMenuItem);

		this.add(menu);

		// Build the about menu.
		menu = new JMenu("About");

		menuItem = new JMenuItem("About");

		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				AboutDialog ad = new AboutDialog();
				ad.setVisible(true);

			}
		});

		menu.add(menuItem);

		this.add(menu);
	}
}
