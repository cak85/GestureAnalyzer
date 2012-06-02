package imuanalyzer.ui;

import imuanalyzer.configuration.Configuration;
import imuanalyzer.data.Marker;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.IOrientationSensors;
import imuanalyzer.signalprocessing.Joint;
import imuanalyzer.signalprocessing.OrientationSensorManagerFactory;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(MainFrame.class
			.getName());

	public static void main(String[] args) {

		setLookAndFeel();

		new MainFrame();
	}

	private static void setLookAndFeel() {
		
		//reduce JME outputs
		java.util.logging.Logger.getLogger("").setLevel(Level.WARNING);
		
		try {
			// useful for removing Logo from jtattoo if LICENSE KEY is available
			// Properties props = new Properties();
			// props.put("logoString", "my company");
			// props.put("licenseKey", "INSERT YOUR LICENSE KEY HERE");
			// SmartLookAndFeel.setCurrentTheme(props);
			UIManager
					.setLookAndFeel("com.jtattoo.plaf.graphite.GraphiteLookAndFeel");
			// UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");

			// UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected Visual3d visual3d;

	protected JPanel mainPanel;
	protected JPanel settingsPanel;

	protected IOrientationSensors sensors;

	protected OrientationChart chartOrientation;
	protected AccelerationChart chartsAcceleration;

	protected FingerSensorMapping fingerSensorMapping;

	protected Hand hand;

	private MainFrame instance;

	public MainFrame() {
		instance = this;

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
		ImageIcon icon = new ImageIcon(getClass()
				.getResource("/Icons/hand.png"));
		this.setIconImage(icon.getImage());

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
		menu = new JMenu("Control");

		// disable movement saving
		menuItem = new JMenuItem("Disable movement anaylsis");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				hand.disableMotionAnalysis();
				visual3d.clearLiveMovement();
			}
		});
		menu.add(menuItem);

		// disable movement saving
		menuItem = new JMenuItem("Disable touch anaylsis");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				hand.disableTouchAnalysis();
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

		checkMenuItem = new JCheckBoxMenuItem("Show statistics");
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
		c.gridwidth = 6;
		c.gridx = 0;
		c.gridy = 0;

		JPanel graphPanel = new JPanel();
		graphPanel.setLayout(new GridLayout(1, 2));

		chartOrientation = new OrientationChart(hand);
		graphPanel.add(chartOrientation);

		chartsAcceleration = new AccelerationChart(hand);
		graphPanel.add(chartsAcceleration);

		mainPanel.add(graphPanel, c);
	}

	protected void createSettingsTab() {
		fingerSensorMapping = new FingerSensorMapping(hand,
				OrientationSensorManagerFactory.NUMBER_OF_SENSORS);

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
		c.fill = GridBagConstraints.CENTER;
		c.ipady = 0; // reset to default
		c.weighty = 1; // request any extra vertical space
		c.gridx = 3; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 3;
		c.gridheight = 1;

		mainPanel.add(toolBarPanel, c);

		MarkerControl markerControl = new MarkerControl(this, visual3d,
				sensors, hand);

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.CENTER;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 4;
		c.gridheight = 1;

		mainPanel.add(markerControl, c);

	}

	public Visual3d getVisual3d() {
		return visual3d;
	}
}
