package imuanalyzer.ui.swing.menu;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.IOrientationSensors;
import imuanalyzer.ui.jmonkey.Visual3d;
import imuanalyzer.ui.swing.AboutDialog;
import imuanalyzer.ui.swing.extensions.ExtensionFileFilter;
import imuanalyzer.ui.swing.help.HelpManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

/**
 * Class for holding all stuff of the main menu bar
 * @author Christopher-Eyk Hrabia
 *
 */
public class MainMenuBar extends JMenuBar {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6031023287066374514L;

	protected Visual3d visual3d;
	protected IOrientationSensors sensors;

	protected Hand hand;

	private JMenuBar instance;
	
	protected MenuFactory menuFactory;

	public MainMenuBar(Hand _hand, Visual3d _visual3d,
			IOrientationSensors _sensors,
			MenuFactory _menuFactory) {
		instance = this;
		hand = _hand;
		visual3d = _visual3d;
		sensors = _sensors;
		this.menuFactory=_menuFactory;

		JMenu menu;
		JMenuItem menuItem;

		// Build tools menu
		menu = new JMenu("Control");

		HelpManager.getInstance().enableHelpKey(menu, "controlmenu");

		// disable movement saving
		menuItem = new JMenuItem("Clear live movement-anaylsis");
		HelpManager.getInstance().enableHelpKey(menuItem, "controlmenu");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				hand.disableMotionAnalysis();
				visual3d.clearLiveMovement();
			}
		});
		menu.add(menuItem);

		// disable movement saving
		menuItem = new JMenuItem("Clear live touch-anaylsis");
		HelpManager.getInstance().enableHelpKey(menuItem, "controlmenu");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				hand.disableTouchAnalysis();
				visual3d.clearTouchLines();
			}
		});
		menu.add(menuItem);

		// clear analysis
		menuItem = new JMenuItem("Clear offline-analysis");
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

		//Current implementation with temperature correction does not need further calibration
//		menuItem = new JMenuItem("Calibration");
//		menuItem.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				JOptionPane
//						.showMessageDialog(
//								instance,
//								"Please position all sensor boards in stable position with bottom flat on ground\nCalibration is finished when the orange LED on the device lights up",
//								"Information", JOptionPane.INFORMATION_MESSAGE);
//				sensors.calibrate();
//				visual3d.resetHand();
//			}
//		});
//
//		menu.add(menuItem);

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

		HelpManager.getInstance().enableHelpKey(menu, "viewmenu");

		menu.add(menuFactory.createChartMenu());

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
		checkMenuItem.setSelected(false);
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

		// Build the ? menu.
		menu = new JMenu("?");

		menuItem = new JMenuItem("Help");

		HelpManager.getInstance().enableHelpOnButton(menuItem, "start");

		menu.add(menuItem);

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
