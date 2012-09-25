package imuanalyzer.ui.swing;

import imuanalyzer.data.Database;
import imuanalyzer.data.DatasetMetadata;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.ui.swing.extensions.BackgroundPanel;
import imuanalyzer.ui.swing.extensions.RelativeLayout;
import imuanalyzer.ui.swing.help.HelpManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Panel for the configuration of the finger sensor mapping
 * @author Christopher-Eyk Hrabia
 *
 */
public class FingerSensorMapping extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2313286213104983633L;

	public static final String IMAGE = "/Background/hand_edit.png";

	private BackgroundPanel background;

	private EnumMap<JointType, JSpinner> spinners = new EnumMap<JointType, JSpinner>(
			JointType.class);

	int numberOfSensors;

	Hand hand;

	JComboBox<String> datasetComboBox;
	protected ArrayList<DatasetMetadata> markers;
	protected DatasetMetadata currentActiveMarker;

	Database db;

	FingerSensorMapping myInstance;

	public FingerSensorMapping(Hand hand, int numberOfSensors) {
		this.numberOfSensors = numberOfSensors;
		this.hand = hand;
		myInstance = this;

		HelpManager.getInstance().enableHelpKey(this, "mapping");

		try {
			db = Database.getInstance();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		BufferedImage img = null;
		try {
			img = ImageIO.read(getClass().getResourceAsStream(IMAGE));
		} catch (IOException e) {
		}
		background = new BackgroundPanel(img, BackgroundPanel.SCALED, 0.0f,
				0.0f);
		background.setLayout(new RelativeLayout(438, 595));

		JPanel picturePanel = new JPanel();

		picturePanel.setLayout(new GridBagLayout());

		this.setLayout(new BorderLayout());

		JPanel datasetPanel = new JPanel(new FlowLayout());

		JLabel infoText = new JLabel(
				"<html><h2>Select sensor-id's for hand links of dataset: </h2></html>",
				SwingConstants.CENTER);

		datasetPanel.add(infoText);

		datasetComboBox = new JComboBox<String>();
		datasetComboBox.setToolTipText("Select dataset joint mapping");
		datasetComboBox.setEditable(true);

		updateDatasetList();
		datasetComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				updateCurrentMarker();
			}

		});

		datasetPanel.add(datasetComboBox);

		this.add(datasetPanel, BorderLayout.NORTH);

		this.add(picturePanel, BorderLayout.CENTER);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.VERTICAL;
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(0, 0, 0, 0);

		picturePanel.add(background, c);

		addHandSpinner(JointType.HAND_ROOT, 225, 340);

		addHandSpinner(JointType.THUMB_TOP, 22, 235);

		addHandSpinner(JointType.THUMB_MID, 25, 295);

		addHandSpinner(JointType.THUMB_BOTTOM, 70, 350);

		addHandSpinner(JointType.INDEX_TOP, 127, 65);

		addHandSpinner(JointType.INDEX_MID, 127, 135);

		addHandSpinner(JointType.INDEX_BOTTOM, 127, 195);

		addHandSpinner(JointType.MIDDLE_TOP, 220, 40);

		addHandSpinner(JointType.MIDDLE_MID, 212, 110);

		addHandSpinner(JointType.MIDDLE_BOTTOM, 212, 170);

		addHandSpinner(JointType.RING_TOP, 315, 74);

		addHandSpinner(JointType.RING_MID, 307, 135);

		addHandSpinner(JointType.RING_BOTTOM, 295, 190);

		addHandSpinner(JointType.LITTLE_TOP, 400, 150);

		addHandSpinner(JointType.LITTLE_MID, 380, 190);

		addHandSpinner(JointType.LITTLE_BOTTOM, 360, 230);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				resetAllSpinners();

			}
		});

		buttonPanel.add(resetButton);

		this.add(buttonPanel, BorderLayout.SOUTH);

		this.addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if ((HierarchyEvent.SHOWING_CHANGED & e.getChangeFlags()) != 0
						&& myInstance.isShowing()) {
					updateDatasetList();
					updateCurrentMarker();
				}
			}
		});
		updateCurrentMarker();

	}

	protected void updateCurrentMarker() {
		int index = datasetComboBox.getSelectedIndex();
		if (index > 1) {
			currentActiveMarker = markers.get(index);
			updateSpinners();
		} else {
			currentActiveMarker = hand.getCurrentDatasetDescription();
		}
	}

	/**
	 * Update marker combobox list
	 */
	private void updateDatasetList() {
		datasetComboBox.removeAllItems();
		markers = db.getAvailableMarkers(true);
		for (int i = 0; i < markers.size(); i++) {

			datasetComboBox.addItem(markers.get(i).getName());
		}
		datasetComboBox.setSelectedIndex(0);
	}

	private JSpinner addHandSpinner(JointType finger, int xOffset, int yOffset) {

		int defaultValue = hand.getJoint(finger).getSensorID() + 1;

		SpinnerModel spinnerModel = new SpinnerNumberModel(defaultValue, 0,
				numberOfSensors, 1);
		JSpinner spinner = new JSpinner(spinnerModel);
		Insets insets = background.getInsets();
		spinner.setBounds(xOffset + insets.left, yOffset + insets.top, 45, 25);

		spinner.addChangeListener(new SensorChangeListener(finger, hand));

		spinners.put(finger, spinner);
		background.add("" + xOffset + "," + yOffset, spinner);

		updateSpinnerColor(defaultValue - 1, spinner);
		return spinner;
	}

	/**
	 * set all markers to zero
	 */
	private void resetAllSpinners() {
		for (Entry<JointType, JSpinner> e : spinners.entrySet()) {
			e.getValue().setValue(0);
		}
	}

	/**
	 * Load mapping from db to spinners
	 */
	private void updateSpinners() {
		for (Entry<JointType, JSpinner> e : spinners.entrySet()) {
			int id = db.getJointSensorMapping(currentActiveMarker, e.getKey());
			e.getValue().setValue(id + 1);
			updateSpinnerColor(id, e.getValue());
		}
	}

	/***
	 * Update color of spinner regarding sensor id
	 * 
	 * @param id
	 * @param spinner
	 */
	private void updateSpinnerColor(int id, JSpinner spinner) {
		JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner
				.getEditor();

		if (id > -1) {
			editor.getTextField().setBackground(Color.orange);
		} else {
			editor.getTextField().setBackground(Color.white);
		}
	}

	/***
	 * handle sensor id changes
	 * 
	 * @author "Christopher-Eyk Hrabia"
	 * 
	 */
	class SensorChangeListener implements ChangeListener {

		JointType f;
		Hand hand;

		public SensorChangeListener(JointType f, Hand hand) {
			this.f = f;
			this.hand = hand;
		}

		@Override
		public void stateChanged(ChangeEvent e) {

			JSpinner s = (JSpinner) e.getSource();
			int id = ((Integer) s.getValue()) - 1;
			// set mapping of current live motion hand
			if (hand.getCurrentDatasetDescription() == currentActiveMarker) {

				hand.setSensorID(f, id);
				hand.saveJointSensorMapping(f);

			} else { // set stored mapping
				db.setJointSensorMapping(currentActiveMarker, f, id);
			}
			updateSpinnerColor(id, s);
		}

	}

}
