package imuanalyzer.ui.swing;

import imuanalyzer.data.DatasetMetadata;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.IOrientationSensors;
import imuanalyzer.signalprocessing.OrientationSensorManagerFactory;
import imuanalyzer.ui.jmonkey.Visual3d;
import imuanalyzer.ui.swing.charts.AccelerationChartManager;
import imuanalyzer.ui.swing.charts.FeelingChartManager;
import imuanalyzer.ui.swing.charts.JointRelationChartManager;
import imuanalyzer.ui.swing.charts.OrientationChartManager;
import imuanalyzer.ui.swing.help.HelpManager;
import imuanalyzer.ui.swing.menu.MainMenuBar;
import imuanalyzer.ui.swing.menu.MenuFactory;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
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

/**
 * Main application window this class includes the main method for starting the
 * application
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
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

	/**
	 * Configure application look and feel
	 */
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
	protected JPanel jointMappingSettingsPanel;
	protected JPanel settingsPanel;
	protected JPanel fingerSensorMapping;
	protected JPanel relationPanel;
	protected JPanel constraintPanel;
	protected InfoBox infoBox;
	protected VerticalToolbarPanel rightToolBarPanel;

	protected Visual3d visual3d;
	protected IOrientationSensors sensors;

	protected OrientationChartManager chartOrientation;
	protected AccelerationChartManager chartsAcceleration;
	protected FeelingChartManager chartsFeeling;
	protected JointRelationChartManager chartsRelation;

	protected Hand hand;

	protected MenuFactory menuFactory;

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

		hand = new Hand(sensors, DatasetMetadata.getDefaultMarker());

		createTabs();

		create3dPanel();

		createChartManager();

		menuFactory = new MenuFactory(hand, chartOrientation,
				chartsAcceleration, chartsFeeling, chartsRelation, true);

		visual3d.setMenuFactory(menuFactory);

		createMenu(menuFactory);

		createToolbars(menuFactory);

		menuFactory.setInfoBox(rightToolBarPanel.getInfoBox());

		createSettingsTab();

		this.setVisible(true);

		pack();

	}

	/**
	 * Create application tabs
	 */
	private void createTabs() {
		// create tabs
		JTabbedPane jtp = new JTabbedPane();
		getContentPane().add(jtp);

		mainPanel = new JPanel(new GridBagLayout());
		jtp.addTab("Main", mainPanel);

		jointMappingSettingsPanel = new JPanel(new BorderLayout());
		jtp.addTab("Mapping", jointMappingSettingsPanel);

		relationPanel = new RelationPanel(hand);
		jtp.addTab("Relations", relationPanel);

		constraintPanel = new ConstraintPanel(hand);
		jtp.addTab("Constraints", constraintPanel);

		settingsPanel = new SettingsPanel(sensors);
		jtp.addTab("Settings", settingsPanel);
	}

	/**
	 * Configure frame settings
	 */
	private void configureFrame() {
		this.setTitle("GestureAnalyzer");
		this.setMinimumSize(new Dimension(450, 350));
		this.setPreferredSize(new Dimension(1024, 600));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ImageIcon icon = new ImageIcon(getClass()
				.getResource("/Icons/hand.png"));
		this.setIconImage(icon.getImage());
	}

	/**
	 * Create application menu
	 * 
	 * @param menuFactory
	 */
	protected void createMenu(MenuFactory menuFactory) {
		JMenuBar menuBar = new MainMenuBar(hand, visual3d, sensors, menuFactory);

		this.setJMenuBar(menuBar);
	}

	/**
	 * Create 3d view panel
	 */
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
		Canvas canvas = visual3d.getCanvas();
		canvas.setMinimumSize(new Dimension(10, 10));
		mainPanel.add(canvas, c);

	}

	/**
	 * Create chart maangers
	 */
	protected void createChartManager() {

		chartOrientation = new OrientationChartManager(hand);

		chartsAcceleration = new AccelerationChartManager(hand);

		chartsFeeling = new FeelingChartManager(hand);

		chartsRelation = new JointRelationChartManager(hand);

	}

	/**
	 * Create settings tab
	 */
	protected void createSettingsTab() {
		fingerSensorMapping = new FingerSensorMapping(hand,
				OrientationSensorManagerFactory.NUMBER_OF_SENSORS);

		jointMappingSettingsPanel.add(fingerSensorMapping, BorderLayout.CENTER);
	}

	/**
	 * Create several toolbars on the side, top and bottom
	 * 
	 * @param menuFactory
	 */
	protected void createToolbars(MenuFactory menuFactory) {

		// TOP
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridwidth = 4;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(10, 0, 0, 0);
		TopToolbar topBar = new TopToolbar(sensors, visual3d);
		mainPanel.add(topBar, c);

		// SIDE
		rightToolBarPanel = new VerticalToolbarPanel(hand, sensors, this, visual3d);

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1; // request any extra vertical space
		c.weightx = 0; // request any extra vertical space
		c.gridx = 3; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 2;
		c.gridheight = 3;
		c.insets = new Insets(10, 10, 10, 10);

		mainPanel.add(rightToolBarPanel, c);

		// BOTTOM
		// marker panel
		BottomToolbarPanel markerControl = new BottomToolbarPanel(this,
				visual3d, sensors, hand, chartOrientation, chartsAcceleration,
				chartsFeeling, chartsRelation, menuFactory);

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.weightx = 1; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 4; // 1 columns wide
		c.gridy = 5;
		c.gridheight = 0;
		c.insets = new Insets(10, 0, 10, 0);

		mainPanel.add(markerControl, c);
	}
}
