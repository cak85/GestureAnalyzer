package imuanalyzer.ui;

import imuanalyzer.data.Database;
import imuanalyzer.data.Marker;
import imuanalyzer.signalprocessing.Analyses;
import imuanalyzer.signalprocessing.Analyses.AnalysesMode;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.IOrientationSensors;
import imuanalyzer.signalprocessing.IPlaybackNotify;
import imuanalyzer.signalprocessing.Joint;
import imuanalyzer.signalprocessing.MotionAnalysis;
import imuanalyzer.signalprocessing.Playback;
import imuanalyzer.signalprocessing.TouchAnalysis;
import imuanalyzer.ui.AnalysisUi.ReturnCode;
import imuanalyzer.ui.swing.charts.AccelerationChartManager;
import imuanalyzer.ui.swing.charts.FeelingChartManager;
import imuanalyzer.ui.swing.charts.JointRelationChartManager;
import imuanalyzer.ui.swing.charts.NonDynamicChartFiller;
import imuanalyzer.ui.swing.charts.OrientationChartManager;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

/**
 * Control dataset handling and analysis
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

	MainFrame frame;

	ArrayList<Marker> markers;

	Playback playback;

	Visual3d visual3d;

	JToggleButton buttonPlay;

	JToggleButton buttonRec;

	JToggleButton buttonRepeat;

	protected OrientationChartManager chartOrientation;
	protected AccelerationChartManager chartsAcceleration;
	protected FeelingChartManager feelingChart;
	protected JointRelationChartManager chartsRelation;

	public MarkerControl(MainFrame _frame, Visual3d _visual3d,
			IOrientationSensors _sensor, Hand hand,
			OrientationChartManager _chartOrientation,
			AccelerationChartManager _chartsAcceleration,
			FeelingChartManager _feelingChart,
			JointRelationChartManager _chartsRelation) {
		this.sensor = _sensor;
		this.hand = hand;
		this.frame = _frame;
		this.visual3d = _visual3d;
		chartOrientation = _chartOrientation;
		chartsAcceleration = _chartsAcceleration;
		feelingChart = _feelingChart;
		chartsRelation = _chartsRelation;

		myInstance = this;

		HelpManager.getInstance().enableHelpKey(this, "datasettoolbar");

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
		icon = new ImageIcon(getClass().getResource(
				"/Icons/sq_br_prev_select.png"));
		buttonBack.setRolloverIcon(icon);
		buttonBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonBack.setContentAreaFilled(false);
		buttonBack.setToolTipText("Select previous dataset");
		buttonBack.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				decreaseSelectedMarker();
			}
		});

		this.add(buttonBack);

		icon = new ImageIcon(getClass().getResource("/Icons/sq_br_rec.png"));

		buttonRec = new JToggleButton(icon);
		icon = new ImageIcon(getClass()
				.getResource("/Icons/sq_br_stop_red.png"));
		buttonRec.setSelectedIcon(icon);
		icon = new ImageIcon(getClass().getResource(
				"/Icons/sq_br_rec_select.png"));
		buttonRec.setRolloverIcon(icon);
		buttonRec.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonRec.setContentAreaFilled(false);
		buttonRec.setToolTipText("Record movement");
		buttonRec.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("F8"), "Record");
		buttonRec.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!buttonRec.isSelected()) {
					stopRecording();
				} else {
					Object selectedItem = markerComboBox.getSelectedItem();

					if (selectedItem != null) {
						startRecording(selectedItem.toString());
					} else {
						JOptionPane.showMessageDialog(myInstance,
								"Please enter a valid dataset name",
								"Information", JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		});
		buttonRec.getActionMap().put("Record", new ActionAdapter() {
			@Override
			public boolean isEnabled() {
				if (buttonRec.isSelected()) {
					buttonRec.setSelected(false);
					stopRecording();
				} else {
					Object selectedItem = markerComboBox.getSelectedItem();

					if (selectedItem != null) {
						buttonRec.setSelected(true);
						startRecording(selectedItem.toString());
					} else {
						JOptionPane.showMessageDialog(myInstance,
								"Please enter a valid dataset name",
								"Information", JOptionPane.WARNING_MESSAGE);
					}
				}
				return false;
			}
		});
		this.add(buttonRec);

		icon = new ImageIcon(getClass().getResource("/Icons/sq_next.png"));

		buttonPlay = new JToggleButton(icon);
		icon = new ImageIcon(getClass().getResource(
				"/Icons/sq_br_stop_green.png"));
		buttonPlay.setSelectedIcon(icon);
		icon = new ImageIcon(getClass()
				.getResource("/Icons/sq_next_select.png"));
		buttonPlay.setRolloverIcon(icon);
		buttonPlay.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonPlay.setContentAreaFilled(false);
		buttonPlay.setToolTipText("Play current dataset");
		buttonPlay.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("F6"), "Play");
		buttonPlay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (buttonPlay.isSelected()) {
					startPlayback();
				} else {
					stopPlayback();
				}
			}
		});
		buttonPlay.getActionMap().put("Play", new ActionAdapter() {
			@Override
			public boolean isEnabled() {
				if (buttonPlay.isSelected()) {
					buttonPlay.setSelected(false);
					stopPlayback();
				} else {
					buttonPlay.setSelected(true);
					startPlayback();
				}
				return false;
			}
		});
		this.add(buttonPlay);

		icon = new ImageIcon(getClass().getResource(
				"/Icons/playback_reloaded_button.png"));
		buttonRepeat = new JToggleButton(icon);
		icon = new ImageIcon(getClass().getResource(
				"/Icons/playback_reloaded_button_transparent.png"));
		buttonRepeat.setSelectedIcon(icon);
		icon = new ImageIcon(getClass().getResource(
				"/Icons/playback_reloaded_button_select.png"));
		buttonRepeat.setRolloverIcon(icon);
		buttonRepeat.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonRepeat.setContentAreaFilled(false);
		buttonRepeat.setToolTipText("Repeat one");
		this.add(buttonRepeat);

		// forward button
		icon = new ImageIcon(getClass().getResource("/Icons/sq_br_next.png"));

		JButton buttonForward = new JButton(icon);
		icon = new ImageIcon(getClass().getResource(
				"/Icons/sq_br_next_select.png"));
		buttonForward.setRolloverIcon(icon);

		buttonForward.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonForward.setContentAreaFilled(false);
		buttonForward.setToolTipText("Select next dataset");
		buttonForward.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				increaseSelectedMarker();
			}
		});

		this.add(buttonForward);

		// analysis button
		icon = new ImageIcon(getClass().getResource("/Icons/chart_line_2.png"));

		JButton buttonAnalysis = new JButton(icon);
		icon = new ImageIcon(getClass().getResource(
				"/Icons/chart_line_2_select.png"));
		buttonAnalysis.setRolloverIcon(icon);
		buttonAnalysis.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonAnalysis.setContentAreaFilled(false);
		buttonAnalysis.setToolTipText("Open datasets for analyzing");
		buttonAnalysis.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				startAnaylsis();
			}
		});

		this.add(buttonAnalysis);

		// eject buttonframe
		icon = new ImageIcon(getClass().getResource("/Icons/trash.png"));

		JButton buttonDelete = new JButton(icon);
		icon = new ImageIcon(getClass().getResource("/Icons/trash_select.png"));
		buttonDelete.setRolloverIcon(icon);
		buttonDelete.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonDelete.setContentAreaFilled(false);
		buttonDelete
				.setToolTipText("Delete current dataset and dependent data");
		buttonDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				deleteMarker();
			}
		});

		this.add(buttonDelete);

		// CSV button
		icon = new ImageIcon(getClass().getResource("/Icons/csv_text.png"));

		JButton buttonCSV = new JButton(icon);
		icon = new ImageIcon(getClass().getResource(
				"/Icons/csv_text_select.png"));
		buttonCSV.setRolloverIcon(icon);
		buttonCSV.setContentAreaFilled(false);
		buttonCSV.setToolTipText("Save current dataset's raw data to csv");
		buttonCSV.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveMarker();
			}
		});

		this.add(buttonCSV);

		JPanel comboBoxPanel = new JPanel(new GridLayout(0, 1));
		LineBorder roundedLineBorder = new LineBorder(Color.lightGray, 1, true);
		Border margin = new EmptyBorder(5, 10, 5, 10);
		comboBoxPanel.setBorder(new CompoundBorder(roundedLineBorder, margin));
		comboBoxPanel.add(new JLabel("Dataset:"));

		markerComboBox = new JComboBox();
		markerComboBox.setToolTipText("Select or create new dataset");
		markerComboBox.setEditable(true);

		updateMarkers();
		markerComboBox.setSelectedIndex(markerComboBox.getItemCount() - 2);
		markerComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				int index = markerComboBox.getSelectedIndex();
				if (index > -1) {
					if (index == markers.size()) {
						String str = JOptionPane.showInputDialog(frame,
								"Enter name for new dataset: ",
								"Add new dataset", 1);
						if (!str.isEmpty()) {
							currentActiveMarker = new Marker(str, "");
							db.setMarker(currentActiveMarker);
							updateMarkers();
						}
					} else {
						currentActiveMarker = markers.get(index);
					}
				}
			}
		});
		comboBoxPanel.add(markerComboBox);
		this.add(comboBoxPanel);

		playback.setNotifyer(new IPlaybackNotify() {

			@Override
			public void playbackStopped() {
				buttonPlay.setSelected(false);
			}

			@Override
			public void loopCyclePassed() {
				visual3d.clearLiveMovement();
				visual3d.clearTouchLines();
			}
		});

	}

	private void startPlayback() {
		if (sensor.isConnected()) {
			JOptionPane.showMessageDialog(myInstance,
					"Please disconnect device before starting playback",
					"Information", JOptionPane.WARNING_MESSAGE);
		} else {
			playback.play(currentActiveMarker, buttonRepeat.isSelected());
		}
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

			LOGGER.info(selectedFile.getAbsolutePath());
			db.writeImuDataToCsv(getCurrentMarker(),
					selectedFile.getAbsolutePath());
		} else if (status == JFileChooser.CANCEL_OPTION) {
			LOGGER.info(JFileChooser.CANCEL_OPTION);
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
				db.deleteFeelingData(currentActiveMarker);
				db.removeMarker(currentActiveMarker);
				updateMarkers();
			}
		}
	}

	private String generateMarkerName(String markerName) {
		// remove trailing digits
		String cleanMarkerName = markerName.replaceAll("[\\d]*$", "");

		int markerNumber = 1;
		if (cleanMarkerName.length() != markerName.length()) {
			markerNumber = Integer.parseInt(markerName.replaceAll(
					cleanMarkerName, ""));
		}

		// remove trailing whitespaces
		cleanMarkerName = cleanMarkerName.replaceAll("[\\W]*$", "");

		for (int i = 0; i < markers.size(); i++) {
			Marker m = markers.get(i);
			String combinedName;
			if (cleanMarkerName.isEmpty()) {
				combinedName = "" + markerNumber;
			} else {
				combinedName = cleanMarkerName + " " + markerNumber;
			}
			if (m.getName().equals(combinedName)) {
				markerNumber++;
				i = -1;// start again
			}
		}

		if (cleanMarkerName.isEmpty()) {
			return "" + markerNumber;
		} else {
			return cleanMarkerName + " " + markerNumber;
		}
	}

	private void startRecording(String markerName) {

		if (currentActiveMarker.isUsed()) {
			System.out.println(markerName);

			markerName = generateMarkerName(markerName);

			System.out.println(markerName);
			currentActiveMarker = new Marker(markerName, "");
			db.setMarker(currentActiveMarker);
			updateMarkers();
		}
		storeInitialHandPosition(currentActiveMarker);
		storeJointMapping(currentActiveMarker);
		sensor.setRecording(true);
	}

	private void stopRecording() {
		if (currentActiveMarker != null) {
			currentActiveMarker
					.setEnd(new Date(new java.util.Date().getTime()));
			db.setMarker(currentActiveMarker);
		}
		sensor.setRecording(false);

		if (db.getImuData(currentActiveMarker).size() == 0) {
			int selection = JOptionPane
					.showOptionDialog(
							frame,
							"The recording does not include any data, do you want to keep it anyway?",
							"Recording result", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, new String[] {
									"Yes", "No" }, "No");
			if (selection == JOptionPane.NO_OPTION) {
				deleteMarker();
			}
		}
	}

	private void stopPlayback() {
		playback.stop();
	}

	private void startAnaylsis() {

		boolean showChartAnalysis;
		boolean showNonChartAnalyis;

		if (hand.getRunningMotionAnalysis().size() == 0
				&& hand.getRunningTouchAnalysis().size() == 0) {
			showNonChartAnalyis = false;
		} else {
			showNonChartAnalyis = true;
		}

		// check if we should show chart options
		if (chartOrientation.getCharts().size() == 0
				&& chartsAcceleration.getCharts().size() == 0
				&& chartsRelation.getCharts().size() == 0
				&& !feelingChart.isEnabled()) {
			showChartAnalysis = false;
		} else {
			showChartAnalysis = true;
		}
		// check if we should show 3d analysis options
		if (!showChartAnalysis && !showNonChartAnalyis) {
			JOptionPane
					.showMessageDialog(
							myInstance,
							"You need to specify an analysis (3D analysis or graph) on actual model before performing analysis",
							"Error", JOptionPane.WARNING_MESSAGE);
			return;
		}

		ArrayList<Marker> markers = db.getAvailableMarkers();
		if (markers.size() > 0) {

			AnalysisUi selector = new AnalysisUi(frame, markers,
					showNonChartAnalyis, showChartAnalysis);

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

				// start calculation
				NonDynamicChartFiller filler = null;
				AnalysesMode mode = selector.getSelectedCalculationMode();
				if (selector.isAssumeDynamicCharts()
						|| mode.equals(AnalysesMode.GRAPH)) {
					// TODO get max size
					filler = new NonDynamicChartFiller(chartOrientation,
							chartsAcceleration, feelingChart, chartsRelation,
							selectedMarkers.size(), 10000);
				}
				newAnalyses.calculate(mode, selectedMarkers,
						sensor.getCurrentFilter(), currentSavedMotionJoints,
						currentSavedTouchJoints, selector.getSpecialPoints(),
						filler);

				if (!mode.equals(AnalysesMode.GRAPH)) {
					visual3d.setAnalyses(newAnalyses);
				}

				JOptionPane.showMessageDialog(myInstance,
						"Calculation complete", "Information",
						JOptionPane.INFORMATION_MESSAGE);

				if (selector.isShowBoxplot2d()) {
					new Boxplot2d("Analysis statistics",
							newAnalyses.getStatistics());
				}
			}
		} else {
			JOptionPane.showMessageDialog(myInstance, "No markers available",
					"Information", JOptionPane.OK_OPTION);
		}
	}

	private void increaseSelectedMarker() {
		int index = markerComboBox.getSelectedIndex();
		if (index < (markerComboBox.getItemCount() - 2)) {
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
		markerComboBox.addItem("new...");
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
