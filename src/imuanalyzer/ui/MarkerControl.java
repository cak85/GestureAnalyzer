package imuanalyzer.ui;

import imuanalyzer.data.Database;
import imuanalyzer.data.Marker;
import imuanalyzer.signalprocessing.Analyses;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.IOrientationSensors;
import imuanalyzer.signalprocessing.Joint;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	
	private static final Logger LOGGER = Logger
	.getLogger(MarkerControl.class.getName());


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

	public MarkerControl(MainFrame frame, Visual3d visual3d,
			IOrientationSensors _sensor, Hand hand) {
		this.sensor = _sensor;
		this.hand = hand;
		this.frame = frame;
		myInstance = this;

		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			LOGGER.error(e);
		}

		this.setLayout(new FlowLayout());

		ImageIcon icon = new ImageIcon(getClass().getResource(
				"/Icons/player_back.gif"));

		JButton buttonBack = new JButton(icon);
		buttonBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonBack.setContentAreaFilled(false);
		buttonBack.setBorderPainted(false);
		buttonBack.setToolTipText("Select previous marker");
		buttonBack.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				decreaseSelectedMarker();
			}
		});

		this.add(buttonBack);

		icon = new ImageIcon(getClass().getResource("/Icons/player_rec.gif"));

		buttonRec = new JButton(icon);
		buttonRec.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonRec.setContentAreaFilled(false);
		buttonRec.setToolTipText("Record movement");
		buttonRec.setBorderPainted(false);

		this.add(buttonRec);

		icon = new ImageIcon(getClass().getResource("/Icons/player_stop.gif"));

		buttonStop = new JButton(icon);
		buttonStop.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonStop.setContentAreaFilled(false);
		buttonStop.setBorderPainted(false);
		buttonStop.setEnabled(false);
		buttonStop.setToolTipText("Stop recording");
		buttonStop.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				stopRecording();
			}
		});

		buttonRec.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Object selectedItem = markerComboBox.getSelectedItem();

				if (selectedItem != null) {
					startRecording(selectedItem.toString());
				} else {
					JOptionPane.showMessageDialog(myInstance,
							"Please enter a valid marker name", "Information",
							JOptionPane.OK_OPTION);
				}
			}
		});

		this.add(buttonStop);

		// forward button
		icon = new ImageIcon(getClass().getResource("/Icons/player_for.gif"));

		JButton buttonForward = new JButton(icon);
		buttonForward.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonForward.setContentAreaFilled(false);
		buttonForward.setBorderPainted(false);
		buttonForward.setToolTipText("Select next marker");
		buttonForward.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				increaseSelectedMarker();
			}
		});

		this.add(buttonForward);

		// eject button
		icon = new ImageIcon(getClass().getResource("/Icons/player_eject.gif"));

		JButton buttonEject = new JButton(icon);
		buttonEject.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonEject.setContentAreaFilled(false);
		buttonEject.setBorderPainted(false);
		buttonEject.setToolTipText("Open markers for analyzing");
		buttonEject.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				startAnaylses();
			}
		});

		this.add(buttonEject);

		// eject button
		icon = new ImageIcon(getClass().getResource("/Icons/player_delete.gif"));

		JButton buttonDelete = new JButton(icon);
		buttonDelete.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonDelete.setContentAreaFilled(false);
		buttonDelete.setBorderPainted(false);
		buttonDelete.setToolTipText("Delete current marker and dependent data");
		buttonDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				deleteMarker();
			}
		});

		this.add(buttonDelete);
		
		// save button
		icon = new ImageIcon(getClass().getResource("/Icons/Save.png"));

		JButton buttonSave = new JButton(icon);
		buttonSave.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonSave.setContentAreaFilled(false);
		buttonSave.setBorderPainted(false);
		buttonSave.setToolTipText("Save current marker to csv");
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

		this.add(markerComboBox);

	}
	
	private void saveMarker(){
		JFileChooser fileChooser = new JFileChooser(".");
	    FileFilter filterCSV = new ExtensionFileFilter("CSV", new String[] { "CSV" });
	    fileChooser.setFileFilter(filterCSV);
	    int status = fileChooser.showSaveDialog(frame);
	    if (status == JFileChooser.APPROVE_OPTION) {
	      File selectedFile = fileChooser.getSelectedFile();
	      //TODO ensure extension
	      
	      System.out.println(selectedFile.getAbsolutePath());	    
	      db.writeImuDataToCsv(getCurrentMarker(),selectedFile.getAbsolutePath());
	    } else if (status == JFileChooser.CANCEL_OPTION) {
	      System.out.println(JFileChooser.CANCEL_OPTION);
	    }

	}
	
	private Marker getCurrentMarker(){
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
		buttonRec.setEnabled(false);
		buttonStop.setEnabled(true);

		currentActiveMarker = new Marker(markerName, "");
		db.setMarker(currentActiveMarker);
		updateMarkers();
		storeInitialHandPosition(currentActiveMarker);
		storeJointMapping(currentActiveMarker);
		sensor.setRecording(true);
	}

	private void stopRecording() {
		currentActiveMarker.setEnd(new Date(new java.util.Date().getTime()));
		buttonRec.setEnabled(true);
		buttonStop.setEnabled(false);
		sensor.setRecording(false);
		db.setMarker(currentActiveMarker);
	}

	private void startAnaylses() {
		ArrayList<Marker> markers = db.getAvailableMarkers();
		if (markers.size() > 0) {

			MarkerCombinationSeletor selector = new MarkerCombinationSeletor(
					frame, markers);

			Analyses newAnalyses = new Analyses();
			newAnalyses.calculate(selector.getSelectedMarkers(),
					sensor.getCurrentFilter(),
					hand.getSavedMovementStartJoint());
			frame.getVisual3d().setAnalyses(newAnalyses);
			JOptionPane.showMessageDialog(myInstance, "Calculation complete",
					"Information", JOptionPane.OK_OPTION);
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
		if (index > 1) {
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
		}
	}
}
