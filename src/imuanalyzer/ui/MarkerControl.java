package imuanalyzer.ui;

import imuanalyzer.data.Database;
import imuanalyzer.data.Marker;
import imuanalyzer.signalprocessing.Analyses;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.IOrientationSensors;
import imuanalyzer.signalprocessing.Joint;
import imuanalyzer.signalprocessing.MotionAnalysis;
import imuanalyzer.signalprocessing.Playback;
import imuanalyzer.signalprocessing.TouchAnalysis;
import imuanalyzer.ui.MarkerAnalysesUi.ReturnCode;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

/**
 * TODO seperate logic and gui into different classes
 * 
 */
public class MarkerControl extends JPanel {

	private static final Logger LOGGER = Logger.getLogger(MarkerControl.class
			.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -5817037604009410498L;

	Database db;

	IOrientationSensors sensor;

	JComboBox markerComboBox;

	Marker currentActiveMarker;

	Hand hand;

	MarkerControl myInstance;

	JButton buttonStop;

	JButton buttonRec;

	MainFrame frame;

	ArrayList<Marker> markers;

	Playback playback;

	Visual3d visual3d;

	public MarkerControl(MainFrame frame, Visual3d visual3d,
			IOrientationSensors _sensor, Hand hand) {
		this.sensor = _sensor;
		this.hand = hand;
		this.frame = frame;
		this.visual3d = visual3d;
		myInstance = this;

		playback = new Playback(hand, sensor);

		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			LOGGER.error(e);
		}

		this.setLayout(new FlowLayout());

		ImageIcon icon = new ImageIcon(getClass().getResource(
				"/Icons/sq_br_prev.png"));

		JButton buttonBack = new JButton(icon);
		buttonBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonBack.setContentAreaFilled(false);
		buttonBack.setToolTipText("Select previous marker");
		buttonBack.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				decreaseSelectedMarker();
			}
		});

		this.add(buttonBack);

		icon = new ImageIcon(getClass().getResource("/Icons/sq_br_rec.png"));

		buttonRec = new JButton(icon);
		buttonRec.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonRec.setContentAreaFilled(false);
		buttonRec.setToolTipText("Record movement");
		buttonRec.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Object selectedItem = markerComboBox.getSelectedItem();

				if (selectedItem != null) {
					startRecording(selectedItem.toString());
				} else {
					JOptionPane.showMessageDialog(myInstance,
							"Please enter a valid marker name", "Information",
							JOptionPane.WARNING_MESSAGE);
				}
			}
		});

		this.add(buttonRec);

		icon = new ImageIcon(getClass().getResource("/Icons/sq_br_stop.png"));

		buttonStop = new JButton(icon);
		buttonStop.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonStop.setContentAreaFilled(false);
		buttonStop.setToolTipText("Stop recording or playback");
		buttonStop.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				stopRecording();
			}
		});

		this.add(buttonStop);

		icon = new ImageIcon(getClass().getResource("/Icons/sq_next.png"));

		JButton buttonPlay = new JButton(icon);
		buttonPlay.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonPlay.setContentAreaFilled(false);
		buttonPlay.setToolTipText("Playback of current marker");
		buttonPlay.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				playback.play(currentActiveMarker);
			}
		});

		this.add(buttonPlay);

		// forward button
		icon = new ImageIcon(getClass().getResource("/Icons/sq_br_next.png"));

		JButton buttonForward = new JButton(icon);
		buttonForward.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonForward.setContentAreaFilled(false);
		buttonForward.setToolTipText("Select next marker");
		buttonForward.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				increaseSelectedMarker();
			}
		});

		this.add(buttonForward);

		// eject button
		icon = new ImageIcon(getClass().getResource("/Icons/chart_line_2.png"));

		JButton buttonEject = new JButton(icon);
		buttonEject.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonEject.setContentAreaFilled(false);
		buttonEject.setToolTipText("Open markers for analyzing");
		buttonEject.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				startAnaylses();
			}
		});

		this.add(buttonEject);

		// eject button
		icon = new ImageIcon(getClass().getResource("/Icons/trash.png"));

		JButton buttonDelete = new JButton(icon);
		buttonDelete.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonDelete.setContentAreaFilled(false);
		buttonDelete.setToolTipText("Delete current marker and dependent data");
		buttonDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				deleteMarker();
			}
		});

		this.add(buttonDelete);

		// save button
		icon = new ImageIcon(getClass().getResource("/Icons/csv_text.png"));

		JButton buttonSave = new JButton(icon);
		buttonSave.setContentAreaFilled(false);
		buttonSave.setToolTipText("Save current marker's raw data to csv");
		buttonSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveMarker();
			}
		});

		this.add(buttonSave);

		markerComboBox = new JComboBox();
		markerComboBox.setToolTipText("Select or create new marker");
		markerComboBox.setEditable(true);

		updateMarkers();
		markerComboBox.setSelectedIndex(markerComboBox.getItemCount() - 1);
		markerComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				int index = markerComboBox.getSelectedIndex();
				if (index > -1) {
					currentActiveMarker = markers.get(index);
				}
			}
		});

		this.add(markerComboBox);

	}

	private void saveMarker() {
		JFileChooser fileChooser = new JFileChooser(".");
		FileFilter filterCSV = new ExtensionFileFilter("CSV",
				new String[] { "csv" });
		fileChooser.setFileFilter(filterCSV);
		int status = fileChooser.showSaveDialog(frame);
		if (status == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			if (!selectedFile.getAbsolutePath().endsWith(".csv")) {
				selectedFile = new File(selectedFile + ".csv");
			}

			System.out.println(selectedFile.getAbsolutePath());
			db.writeImuDataToCsv(getCurrentMarker(),
					selectedFile.getAbsolutePath());
		} else if (status == JFileChooser.CANCEL_OPTION) {
			System.out.println(JFileChooser.CANCEL_OPTION);
		}

	}

	private Marker getCurrentMarker() {
		return markers.get(markerComboBox.getSelectedIndex());
	}

	private void deleteMarker() {
		int index = markerComboBox.getSelectedIndex();
		if (index > -1) {
			currentActiveMarker = markers.get(index);
			if (currentActiveMarker != null) {
				db.deleteImuData(currentActiveMarker);
				db.removeMarker(currentActiveMarker);
				updateMarkers();
			}
		}
	}

	private void startRecording(String markerName) {

		currentActiveMarker = new Marker(markerName, "");
		db.setMarker(currentActiveMarker);
		updateMarkers();
		storeInitialHandPosition(currentActiveMarker);
		storeJointMapping(currentActiveMarker);
		sensor.setRecording(true);
	}

	private void stopRecording() {
		currentActiveMarker.setEnd(new Date(new java.util.Date().getTime()));
		sensor.setRecording(false);
		db.setMarker(currentActiveMarker);
		playback.stop();
	}

	private void startAnaylses() {
		if (hand.getRunningMotionAnalysis().size() == 0
				&& hand.getRunningTouchAnalysis().size() == 0) {
			JOptionPane
					.showMessageDialog(
							myInstance,
							"You need to specify a analysis on actual model before performing analysis",
							"Error", JOptionPane.WARNING_MESSAGE);
			return;
		}

		ArrayList<Marker> markers = db.getAvailableMarkers();
		if (markers.size() > 0) {

			MarkerAnalysesUi selector = new MarkerAnalysesUi(frame, markers);

			if (selector.getReturnCode() == ReturnCode.CANCEL) {
				updateMarkers();
				return;
			}

			Analyses newAnalyses = new Analyses();

			ArrayList<Marker> selectedMarkers = selector.getSelectedMarkers();

			if (selectedMarkers.size() > 0) {

				ArrayList<JointType> currentSavedMotionJoints = new ArrayList<Hand.JointType>();

				for (MotionAnalysis m : hand.getRunningMotionAnalysis()) {
					currentSavedMotionJoints
							.add(m.getObservedJoint().getType());
				}

				ArrayList<JointType> currentSavedTouchJoints = new ArrayList<Hand.JointType>();

				for (TouchAnalysis t : hand.getRunningTouchAnalysis()) {
					currentSavedTouchJoints.add(t.getObservedJoint().getType());
				}

				newAnalyses.calculate(selector.getSelectedCalculationMode(),
						selectedMarkers, sensor.getCurrentFilter(),
						currentSavedMotionJoints, currentSavedTouchJoints);
				visual3d.setAnalyses(newAnalyses);
				JOptionPane.showMessageDialog(myInstance,
						"Calculation complete", "Information",
						JOptionPane.INFORMATION_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(myInstance, "No markers available",
					"Information", JOptionPane.OK_OPTION);
		}
	}

	private void increaseSelectedMarker() {
		int index = markerComboBox.getSelectedIndex();
		if (index < (markerComboBox.getItemCount() - 1)) {
			index++;
		}
		markerComboBox.setSelectedIndex(index);
	}

	private void decreaseSelectedMarker() {
		int index = markerComboBox.getSelectedIndex();
		if (index > 0) {
			index--;
		}
		markerComboBox.setSelectedIndex(index);
	}

	/**
	 * Update marker combobox list
	 */
	private void updateMarkers() {
		markerComboBox.removeAllItems();
		markers = db.getAvailableMarkers();
		int i = 0;
		for (i = 0; i < markers.size(); i++) {

			markerComboBox.addItem(markers.get(i).getName());
		}
		markerComboBox.setSelectedIndex(i - 1);

		if (markers.size() > markerComboBox.getSelectedIndex()
				&& markers.size() > 0) {
			currentActiveMarker = markers
					.get(markerComboBox.getSelectedIndex());
		}
	}

	private void storeJointMapping(Marker marker) {
		for (Entry<JointType, Joint> entry : hand.getJointSet()) {
			JointType type = entry.getKey();
			Joint joint = entry.getValue();
			// write to db
			db.setJointSensorMapping(marker, type, joint.getSensorID());
		}
	}

	/**
	 * save current local hand orientations as initial orientation for saved
	 * movement
	 */
	private void storeInitialHandPosition(Marker marker) {
		for (Entry<JointType, Joint> entry : hand.getJointSet()) {
			JointType type = entry.getKey();
			Joint joint = entry.getValue();
			// write to db
			db.setInitialOrientation(marker, type, joint.getLocalOrientation());
			db.setInitialPosition(marker, type, joint.getLocalPosition());
		}
	}
}
