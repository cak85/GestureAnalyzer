package imuanalyzer.ui.swing;

import imuanalyzer.data.Database;
import imuanalyzer.data.DatasetMetadata;
import imuanalyzer.signalprocessing.Analysis;
import imuanalyzer.signalprocessing.Analysis.AnalysesMode;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.IOrientationSensors;
import imuanalyzer.signalprocessing.IPlaybackNotify;
import imuanalyzer.signalprocessing.Joint;
import imuanalyzer.signalprocessing.MotionAnalysis;
import imuanalyzer.signalprocessing.OrientationSensorManagerFactory;
import imuanalyzer.signalprocessing.Playback;
import imuanalyzer.signalprocessing.TouchAnalysis;
import imuanalyzer.ui.jmonkey.Visual3d;
import imuanalyzer.ui.swing.AnalysisUi.ReturnCode;
import imuanalyzer.ui.swing.charts.AccelerationChartManager;
import imuanalyzer.ui.swing.charts.Boxplot2d;
import imuanalyzer.ui.swing.charts.FeelingChartManager;
import imuanalyzer.ui.swing.charts.JointRelationChartManager;
import imuanalyzer.ui.swing.charts.NonDynamicChartFiller;
import imuanalyzer.ui.swing.charts.OrientationChartManager;
import imuanalyzer.ui.swing.extensions.ActionAdapter;
import imuanalyzer.ui.swing.extensions.ExtensionFileFilter;
import imuanalyzer.ui.swing.help.HelpManager;
import imuanalyzer.ui.swing.menu.FinishListenerHandler;
import imuanalyzer.ui.swing.menu.IPopUpFinished;
import imuanalyzer.ui.swing.menu.MenuFactory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

/**
 * Control dataset handling and analysis recognition and configuration
 * 
 */
public class BottomToolbarPanel extends JPanel {

	private static final Logger LOGGER = Logger.getLogger(BottomToolbarPanel.class
			.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -5817037604009410498L;

	private Database db;

	protected IOrientationSensors sensor;

	protected JComboBox<String> markerComboBox;

	protected Hand hand;

	private BottomToolbarPanel myInstance;

	protected MainFrame frame;

	protected DatasetMetadata currentActiveMarker;

	protected ArrayList<DatasetMetadata> markers;

	protected Playback playback;

	protected Visual3d visual3d;

	protected JToggleButton buttonPlay;

	protected JToggleButton buttonRec;

	protected JToggleButton buttonRepeat;

	protected JToggleButton buttonSpeed;

	protected AnalysisProgress progress;

	protected OrientationChartManager chartOrientation;
	protected AccelerationChartManager chartsAcceleration;
	protected FeelingChartManager feelingChart;
	protected JointRelationChartManager chartsRelation;

	protected MenuFactory menuFactory;

	public BottomToolbarPanel(MainFrame _frame, Visual3d _visual3d,
			IOrientationSensors _sensor, Hand hand,
			OrientationChartManager _chartOrientation,
			AccelerationChartManager _chartsAcceleration,
			FeelingChartManager _feelingChart,
			JointRelationChartManager _chartsRelation, MenuFactory _menuFactory) {
		this.sensor = _sensor;
		this.hand = hand;
		this.frame = _frame;
		this.visual3d = _visual3d;
		chartOrientation = _chartOrientation;
		chartsAcceleration = _chartsAcceleration;
		feelingChart = _feelingChart;
		chartsRelation = _chartsRelation;
		menuFactory = _menuFactory;

		myInstance = this;
		
		HelpManager help = HelpManager.getInstance();

		help.enableHelpKey(this, "datasettoolbar");

		playback = new Playback(hand, sensor);

		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			LOGGER.error(e);
		}

		this.setLayout(new GridBagLayout());

		JPanel buttonPanel = new JPanel();

		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 0;
		c.weighty = 1;
		c.weightx = 1;
		c.gridx = 0;
		c.gridwidth = 1;
		c.gridy = 0;
		c.gridheight = 1;
		c.insets = new Insets(0, 0, 0, 0);

		this.add(buttonPanel, c);

		JPanel progressPanel = new JPanel(new BorderLayout());

		progress = new AnalysisProgress();
		progress.setVisible(false);
		progressPanel.add(progress, BorderLayout.CENTER);

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0;
		c.weighty = 1;
		c.weightx = 1;
		c.gridx = 0;
		c.gridwidth = 1;
		c.gridy = 1;
		c.gridheight = 1;
		c.insets = new Insets(0, 0, 0, 0);

		this.add(progressPanel, c);

		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

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
		help.enableHelpKey(buttonBack, "datasettoolbar_back");

		buttonPanel.add(buttonBack);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

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
		buttonPanel.add(buttonRec);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		help.enableHelpKey(buttonRec, "datasettoolbar_record");
		
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
		buttonPanel.add(buttonPlay);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		help.enableHelpKey(buttonPlay, "datasettoolbar_play");
		
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
		buttonPanel.add(buttonRepeat);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		help.enableHelpKey(buttonRepeat, "datasettoolbar_repeat");

		// forward button
		icon = new ImageIcon(getClass().getResource("/Icons/sq_br_next.png"));

		JButton buttonForward = new JButton(icon);
		icon = new ImageIcon(getClass().getResource(
				"/Icons/sq_br_next_select.png"));
		buttonForward.setRolloverIcon(icon);
		help.enableHelpKey(buttonForward, "datasettoolbar_next");

		buttonForward.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonForward.setContentAreaFilled(false);
		buttonForward.setToolTipText("Select next dataset");
		buttonForward.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				increaseSelectedMarker();
			}
		});

		buttonPanel.add(buttonForward);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		buttonPanel.add(new JLabel("Speed: "));

		// speed slider
		final JSlider speedSlider = new JSlider(SwingConstants.HORIZONTAL,
				(int) (1 / playback.getSpeed()), 9, 1);
		speedSlider.setMajorTickSpacing(4);
		speedSlider.setMinorTickSpacing(1);
		speedSlider.setPaintTicks(true);
		speedSlider.setPaintLabels(true);
		speedSlider.setSnapToTicks(true);
		speedSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				int value = ((JSlider) e.getSource()).getValue();
				if (buttonSpeed.isSelected()) {// slow down
					playback.setSpeed(1 * (float) value);
				} else { // speed uo
					playback.setSpeed(1 / (float) value);
				}
			}
		});
		help.enableHelpKey(speedSlider, "datasettoolbar_speed");

		buttonPanel.add(speedSlider);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		// speed switcher
		icon = new ImageIcon(getClass().getResource("/Icons/sq_br_up.png"));
		buttonSpeed = new JToggleButton(icon);
		icon = new ImageIcon(getClass().getResource("/Icons/sq_br_down.png"));
		buttonSpeed.setSelectedIcon(icon);
		final ImageIcon speedUpSelect = new ImageIcon(getClass().getResource(
				"/Icons/sq_br_up_select.png"));
		buttonSpeed.setRolloverIcon(speedUpSelect);
		buttonSpeed.setSelectedIcon(icon);
		final ImageIcon speedDownSelect = new ImageIcon(getClass().getResource(
				"/Icons/sq_br_down_select.png"));
		buttonSpeed.setRolloverSelectedIcon(speedDownSelect);
		buttonSpeed.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonSpeed.setContentAreaFilled(false);
		buttonSpeed
				.setToolTipText("Change behavior of speed slider from speed up to speed down or vice versa");
		buttonSpeed.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (buttonSpeed.isSelected()) {
					speedSlider.setValue(1);
					speedSlider
							.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
				} else {
					speedSlider.setValue(1);
					speedSlider
							.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				}
			}
		});
		help.enableHelpKey(buttonSpeed, "datasettoolbar_speedupdown");

		buttonPanel.add(buttonSpeed);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

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
				Runnable run = new Runnable() {

					@Override
					public void run() {
						startAnaylsis();
					}
				};
				Thread t = new Thread(run);
				t.start();
			}
		});

		buttonPanel.add(buttonAnalysis);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		help.enableHelpKey(buttonAnalysis, "datasettoolbar_analysis");

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

		buttonPanel.add(buttonCSV);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		help.enableHelpKey(buttonCSV, "datasettoolbar_csv");

		JPanel comboBoxPanel = new JPanel(new BorderLayout());
		LineBorder roundedLineBorder = new LineBorder(Color.lightGray, 1, true);
		Border margin = new EmptyBorder(5, 10, 5, 10);
		comboBoxPanel.setBorder(new CompoundBorder(roundedLineBorder, margin));
		comboBoxPanel.add(new JLabel("Dataset:"), BorderLayout.NORTH);

		markerComboBox = new JComboBox<String>();
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
						if (str != null && !str.isEmpty()) {
							currentActiveMarker = new DatasetMetadata(str, "");
							db.setMarker(currentActiveMarker);
							updateMarkers();
						}
					} else {
						currentActiveMarker = markers.get(index);
					}
				}
			}
		});

		comboBoxPanel.add(markerComboBox, BorderLayout.CENTER);
		buttonPanel.add(comboBoxPanel);
		help.enableHelpKey(markerComboBox, "datasettoolbar_select");

		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		// edit button
		icon = new ImageIcon(getClass().getResource("/Icons/edit.png"));

		final JButton buttonEditData = new JButton(icon);
		icon = new ImageIcon(getClass().getResource("/Icons/edit_select.png"));
		buttonEditData.setRolloverIcon(icon);
		buttonEditData.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonEditData.setContentAreaFilled(false);
		buttonEditData
				.setToolTipText("Edit current dataset and dependent data");
		buttonEditData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				final int numberOfItems = markerComboBox.getItemCount();

				final int selectedIndex = markerComboBox.getSelectedIndex();

				FinishListenerHandler finishHandler = new FinishListenerHandler();

				finishHandler.addFinishListener(new IPopUpFinished() {

					@Override
					public void notifyFinished() {
						updateMarkers();
						if (numberOfItems != markerComboBox.getItemCount()) {
							int newIndex = selectedIndex - 1;
							if (newIndex > -1) {
								markerComboBox.setSelectedIndex(newIndex);
							}
						}
					}
				});

				JPopupMenu popup = menuFactory.getDatasetPopUpMenu(
						getCurrentMarker(), finishHandler);

				popup.show(buttonEditData, 0, 0);
			}
		});

		buttonPanel.add(buttonEditData);
		help.enableHelpKey(buttonEditData, "datasettoolbar_edit");

		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

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
			
			Object[] options = {"all sensors in one row", "every sensor in a single row"};
			JOptionPane.showOptionDialog(
					frame, "Choose data output format", 
					"Data ouput format", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, options, options[0]);
//			boolean dontShow = checkbox.isSelected(); 

			LOGGER.info(selectedFile.getAbsolutePath());
			db.writeImuDataToCsv(getCurrentMarker(),
					selectedFile.getAbsolutePath());
		} else if (status == JFileChooser.CANCEL_OPTION) {
			LOGGER.info(JFileChooser.CANCEL_OPTION);
		}

	}

	private DatasetMetadata getCurrentMarker() {
		int index = markerComboBox.getSelectedIndex();
		return markers.get(index);
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
				index--;
				if (index > -1) {
					markerComboBox.setSelectedIndex(index);
				}
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
			DatasetMetadata m = markers.get(i);
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

		if (currentActiveMarker == null) {
			currentActiveMarker = new DatasetMetadata(markerName, "");
			db.setMarker(currentActiveMarker);
			updateMarkers();
		}

		if (currentActiveMarker.isUsed()) {

			markerName = generateMarkerName(markerName);

			currentActiveMarker = new DatasetMetadata(markerName, "");
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
		boolean showTouchAnalysis;
		boolean showMotionAnalysis;

		if (hand.getRunningMotionAnalysis().size() == 0) {
			showMotionAnalysis = false;
		} else {
			showMotionAnalysis = true;
		}

		if (hand.getRunningTouchAnalysis().size() == 0) {
			showTouchAnalysis = false;
		} else {
			showTouchAnalysis = true;
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

		ArrayList<DatasetMetadata> markers = db.getAvailableMarkers();
		if (markers.size() > 0) {

			MenuFactory menuFactory = new MenuFactory(hand,
					new OrientationChartManager(hand),
					new AccelerationChartManager(hand),
					new FeelingChartManager(hand),
					new JointRelationChartManager(hand), false);

			AnalysisUi selector = new AnalysisUi(frame, showMotionAnalysis,
					showTouchAnalysis, showChartAnalysis, menuFactory);

			if (selector.getReturnCode() == ReturnCode.CANCEL) {
				updateMarkers();
				return;
			}

			Analysis newAnalysis = new Analysis(progress);

			ArrayList<DatasetMetadata> selectedMarkers = selector.getSelectedMarkers();

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

				int maxSize = getMaxDataLength(selectedMarkers)
						/ OrientationSensorManagerFactory.NUMBER_OF_SENSORS;
				LOGGER.debug("Max size = " + maxSize);

				if (selector.isAssumeDynamicCharts()) {
					filler = new NonDynamicChartFiller(chartOrientation,
							chartsAcceleration, feelingChart, chartsRelation,
							selectedMarkers, maxSize,
							selector.isShowRelationsBoxplot());
				} else {
					filler = new NonDynamicChartFiller(
							menuFactory.getChartOrientation(),
							menuFactory.getChartsAcceleration(),
							menuFactory.getFeelingChart(),
							menuFactory.getChartsRelation(), selectedMarkers,
							maxSize, selector.isShowRelationsBoxplot());
				}
				progress.setVisible(true);

				if (selector.isCalculateSingleRelations()) {

					filler.setChartsRelation(null);
				}

				newAnalysis.calculate(mode, selectedMarkers,
						sensor.getCurrentFilter(), currentSavedMotionJoints,
						currentSavedTouchJoints, selector.getSpecialPoints(),
						filler, selector.isShowBoxplotTouch3d(),
						selector.isShowBoxplotMotionMin3d(),
						selector.isShowBoxplotMotionMax3d());

				if (selector.isCalculateSingleRelations()) {
					for (DatasetMetadata m : selectedMarkers) {
						ArrayList<DatasetMetadata> markerList = new ArrayList<DatasetMetadata>();
						markerList.add(m);

						NonDynamicChartFiller relationFiller = new NonDynamicChartFiller(
								null, null, null,
								menuFactory.getChartsRelation(), markerList,
								maxSize, selector.isShowRelationsBoxplot());

						newAnalysis.calculate(
								AnalysesMode.WITHOUTPOSTPROCCESIG, markerList,
								sensor.getCurrentFilter(),
								new ArrayList<Hand.JointType>(1),
								new ArrayList<Hand.JointType>(1),
								new ArrayList<Float>(1), relationFiller, false,
								false, false);
					}
				}

				if (!mode.equals(AnalysesMode.WITHOUTPOSTPROCCESIG)) {
					visual3d.setAnalyses(newAnalysis);
				}

				if (selector.isShowBoxplot2d()) {
					new Boxplot2d("Analysis statistics",
							newAnalysis.getStatistics());
				}

				// reset progress
				progress.setStep(0);
				progress.setVisible(false);

			}
		} else {
			JOptionPane.showMessageDialog(myInstance, "No markers available",
					"Information", JOptionPane.OK_OPTION);
		}
	}

	protected int getMaxDataLength(ArrayList<DatasetMetadata> selectedMarkers) {
		int maxCount = 0;
		for (DatasetMetadata m : selectedMarkers) {
			maxCount = Math.max(maxCount, db.getCount(m));
		}
		return maxCount;
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

	private void storeJointMapping(DatasetMetadata marker) {
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
	private void storeInitialHandPosition(DatasetMetadata marker) {
		for (Entry<JointType, Joint> entry : hand.getJointSet()) {
			JointType type = entry.getKey();
			Joint joint = entry.getValue();
			// write to db
			db.setInitialOrientation(marker, type, joint.getLocalOrientation());
			db.setInitialPosition(marker, type, joint.getLocalPosition());
		}
	}
}
