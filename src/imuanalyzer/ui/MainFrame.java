package imuanalyzer.ui;

import imuanalyzer.data.Marker;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.IOrientationSensors;
import imuanalyzer.signalprocessing.OrientationSensorManagerFactory;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

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

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				new MainFrame();
			}
		});
	}

	private static void setLookAndFeel() {

		// reduce JME outputs
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

	protected JPanel mainPanel;
	protected JPanel settingsPanel;
	protected JPanel fingerSensorMapping;
	protected InfoBox infoBox;

	protected Visual3d visual3d;
	protected IOrientationSensors sensors;

	protected OrientationChartManager chartOrientation;
	protected AccelerationChartManager chartsAcceleration;
	protected FeelingChartManager chartsFeeling;
	protected JointRelationChartManager chartsRelation;

	protected Hand hand;

	public MainFrame() {
		
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		
		HelpManager.getInstance().enableHelpKey(this.getRootPane(), "start");

		configureFrame();

		try {
			sensors = OrientationSensorManagerFactory
					.getLiveOrientationManager();
		} catch (Exception e) {
			LOGGER.error(e);
			System.exit(-1);
		}

		hand = new Hand(sensors, Marker.getDefaultMarker());

		createTabs();

		create3dPanel();

		createChartManager();

		createMenu();

		createToolbars();

		createSettingsTab();

		this.setVisible(true);

		pack();
		
	}

	private void createTabs() {
		// create tabs
		JTabbedPane jtp = new JTabbedPane();
		getContentPane().add(jtp);

		mainPanel = new JPanel(new GridBagLayout());
		jtp.addTab("Main", mainPanel);

		settingsPanel = new JPanel(new GridBagLayout());
		jtp.addTab("Settings", settingsPanel);
	}

	private void configureFrame() {
		this.setTitle("IMUAnalyzer");
		this.setSize(1024, 768);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ImageIcon icon = new ImageIcon(getClass()
				.getResource("/Icons/hand.png"));
		this.setIconImage(icon.getImage());
	}

	protected void createMenu() {
		JMenuBar menuBar = new MainMenuBar(hand, visual3d, sensors,
				chartOrientation, chartsAcceleration, chartsFeeling,
				chartsRelation);

		this.setJMenuBar(menuBar);
	}

	protected void create3dPanel() {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 2;
		c.weighty = 2;
		c.gridwidth = 3;
		c.gridheight = 3;
		c.gridx = 0;
		c.gridy = 2;
		c.insets = new Insets(10, 0, 0, 0);
		visual3d = new Visual3d(hand);
				
		JPanel jme3Panel = visual3d.get3dPanel();
		mainPanel.add(jme3Panel, c);
	}

	protected void createChartManager() {

		chartOrientation = new OrientationChartManager(hand);

		chartsAcceleration = new AccelerationChartManager(hand);

		chartsFeeling = new FeelingChartManager(hand);

		chartsRelation = new JointRelationChartManager(hand);

	}

	protected void createSettingsTab() {
		fingerSensorMapping = new FingerSensorMapping(hand,
				OrientationSensorManagerFactory.NUMBER_OF_SENSORS);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 0;
		settingsPanel.add(fingerSensorMapping, c);
	}

	protected void createToolbars() {

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		c.gridwidth = 5;
		c.gridheight = 2;
		c.gridx = 0;
		c.gridy = 0;
		TopToolbar topBar = new TopToolbar(visual3d);
		mainPanel.add(topBar, c);

		infoBox = new InfoBox();

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 3; // aligned with button 2
		c.gridwidth = 2; // 1 columns wide
		c.gridy = 2;
		c.gridheight = 1;
		c.insets = new Insets(10, 0, 0, 0);

		mainPanel.add(infoBox, c);
		visual3d.setInfoBox(infoBox);

		JPanel toolBarPanel = new ToolbarPanel(hand, sensors, this);

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 3; // aligned with button 2
		c.gridwidth = 2; // 1 columns wide
		c.gridy = 3;
		c.gridheight = 3;

		mainPanel.add(toolBarPanel, c);

		// marker panel
		MarkerControl markerControl = new MarkerControl(this, visual3d,
				sensors, hand);

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 3; // 1 columns wide
		c.gridy = 5;
		c.gridheight = 0;
		c.insets = new Insets(0, 0, 0, 0);

		mainPanel.add(markerControl, c);
	}
}
