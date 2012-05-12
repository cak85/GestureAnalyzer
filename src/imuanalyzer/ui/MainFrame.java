package imuanalyzer.ui;

import imuanalyzer.configuration.Configuration;
import imuanalyzer.data.Marker;
import imuanalyzer.device.ImuReader;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.IOrientationSensors;
import imuanalyzer.signalprocessing.Joint;
import imuanalyzer.signalprocessing.OrientationSensorManagerFactory;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map.Entry;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = Logger
	.getLogger(MainFrame.class.getName());

	public static void main(String[] args) {

		setLookAndFeel();

		new MainFrame();
	}

	private static void setLookAndFeel() {
		try {
			UIManager
					.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected Visual3d visual3d;

	protected JPanel mainPanel;
	protected JPanel settingsPanel;

	protected IOrientationSensors sensors;

	protected SensorChart chart;

	protected FingerSensorMapping fingerSensorMapping;

	protected Hand hand;

	public MainFrame() {

		try {
			sensors = OrientationSensorManagerFactory
					.getLiveOrientationManager();
		} catch (Exception e) {
			LOGGER.error(e);
			System.exit(-1);
		}

		hand = new Hand(sensors, Marker.getDefaultMarker());

		this.setTitle("IMUAnalyzer");
		this.setSize(1024, 768);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JTabbedPane jtp = new JTabbedPane();
		getContentPane().add(jtp);

		mainPanel = new JPanel(new GridBagLayout());
		jtp.addTab("Main", mainPanel);

		settingsPanel = new JPanel(new GridBagLayout());
		jtp.addTab("Settings", settingsPanel);

		createMenu();

		createGraphPanel();

		createToolbar();

		createSettingsTab();

		create3dPanel();

		this.setVisible(true);
	}

	protected void createMenu() {
		JMenuBar menuBar;
		JMenu menu;
		JMenuItem menuItem;

		// Create the menu bar.
		menuBar = new JMenuBar();

		// Build tools menu
		menu = new JMenu("Tools");

		//disable movement saving
		menuItem = new JMenuItem("Disable movement saving");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				visual3d.clearLiveMovement();
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
		
		//reset hand
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
				for (int i = 0; i < sensors.getNumberOfSensors(); i++) {
					// sensors.setInitialOrientation(i, spatialView.getHand().)
				}
				sensors.calibrate();
				visual3d.adjustBoneJointMapping();

			}
		});

		menu.add(menuItem);

		menuBar.add(menu);

		// build view menu

		menu = new JMenu("View");

		JMenu submenuAdd = new JMenu("Add chart");
		JMenu submenuRemove = new JMenu("Remove chart");

		menu.add(submenuAdd);
		menu.add(submenuRemove);

		for (Entry<JointType, Joint> entry : hand.getJointSet()) {

			Joint j = entry.getValue();
			final JointType type = entry.getKey();

			JMenuItem submenuitemAdd = new JMenuItem(j.getName());
			submenuitemAdd.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					chart.addChart(type);
				}
			});

			submenuAdd.add(submenuitemAdd);

			JMenuItem submenuitemRemove = new JMenuItem(j.getName());
			submenuitemRemove.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					chart.removeChart(type);
				}
			});
			submenuRemove.add(submenuitemRemove);
		}

		JCheckBoxMenuItem checkMenuItem = new JCheckBoxMenuItem("Show Skeleton");
		checkMenuItem.setSelected(true);
		checkMenuItem.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				JCheckBoxMenuItem checkMenuItem = (JCheckBoxMenuItem) arg0
						.getSource();
				visual3d.getVisualHand().setSkeletonVisible(
						checkMenuItem.isSelected());
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

		checkMenuItem = new JCheckBoxMenuItem("Show Statistics");
		checkMenuItem.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				JCheckBoxMenuItem checkMenuItem = (JCheckBoxMenuItem) arg0
						.getSource();
				visual3d.setShowStatistics(checkMenuItem.isSelected());
			}
		});
		menu.add(checkMenuItem);

		menuBar.add(menu);

		// Build the first menu.
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

		menuBar.add(menu);

		this.setJMenuBar(menuBar);
	}

	protected void create3dPanel() {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 40; // make this component tall
		c.weightx = 0.0;
		c.gridwidth = 3;
		c.gridheight = 3;
		c.gridx = 0;
		c.gridy = 1;
		visual3d = new Visual3d(hand);
		mainPanel.add(visual3d.get3dPanel(), c);
	}

	protected void createGraphPanel() {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 40; // make this component tall
		c.weightx = 0.0;
		c.gridwidth = 4;
		c.gridx = 0;
		c.gridy = 0;

		chart = new SensorChart(hand);
		mainPanel.add(chart, c);
	}

	protected void createSettingsTab() {
		fingerSensorMapping = new FingerSensorMapping(hand,
				sensors.getNumberOfSensors());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 0;
		settingsPanel.add(fingerSensorMapping, c);
	}

	protected void createToolbar() {

		JPanel toolBarPanel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 0;

		// JButton buttonCalibration = new JButton("Calibration");
		// buttonCalibration.addActionListener(new ActionListener() {
		//
		// @Override
		// public void actionPerformed(ActionEvent arg0) {
		// for (int i = 0; i < sensors.getNumberOfSensors(); i++) {
		// // sensors.setInitialOrientation(i, spatialView.getHand().)
		// }
		// sensors.calibrate();
		// visual3d.adjustBoneJointMapping();
		// }
		// });
		// toolBarPanel.add(buttonCalibration, c);

		JLabel filterLabel = new JLabel("Filter:");

		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 2; // third row

		toolBarPanel.add(filterLabel, c);

		final JComboBox filterTypes = new JComboBox(
				sensors.getAvailableFilters());

		filterTypes
				.setToolTipText("Select filter for IMU-Orientation calculation");
		filterTypes.setSelectedItem(sensors.getCurrentFilter());

		filterTypes.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				FilterTypes filterType = (FilterTypes) filterTypes
						.getSelectedItem();
				sensors.setFilterType(filterType);
				Configuration.getInstance().setFilterType(filterType);
			}
		});

		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 3; // third row

		toolBarPanel.add(filterTypes, c);

		JLabel connectionLabel = new JLabel("Connection:");

		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 5;

		toolBarPanel.add(connectionLabel, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0; // reset to default
		c.weightx = 1; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 2; // 1 columns wide
		c.gridy = 6;

		ConnectionPanel connectionPanel = new ConnectionPanel(sensors);

		toolBarPanel.add(connectionPanel, c);

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.LAST_LINE_END;
		c.ipady = 0; // reset to default
		c.weighty = 1; // request any extra vertical space
		c.gridx = 3; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 3;

		mainPanel.add(toolBarPanel, c);

		MarkerControl markerControl = new MarkerControl(this, visual3d,
				sensors, hand);

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.LAST_LINE_END;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 4;

		mainPanel.add(markerControl, c);

	}

	public Visual3d getVisual3d() {
		return visual3d;
	}
}
