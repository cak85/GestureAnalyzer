package imuanalyzer.ui;

import imuanalyzer.data.Database;
import imuanalyzer.data.Marker;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Analyses.AnalysesMode;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.ui.swing.charts.AccelerationChartFrame;
import imuanalyzer.ui.swing.charts.JointRelationChartFrame;
import imuanalyzer.ui.swing.charts.OrientationChartFrame;
import imuanalyzer.ui.swing.menu.FinishListenerHandler;
import imuanalyzer.ui.swing.menu.IPopUpFinished;
import imuanalyzer.ui.swing.menu.MenuFactory;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

public class AnalysisUi extends JDialog {

	private static final Logger LOGGER = Logger.getLogger(AnalysisUi.class
			.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -6103905099594851540L;

	protected ArrayList<Marker> markers;

	protected ArrayList<Marker> selectedMarkers = new ArrayList<Marker>();

	protected JList list;

	protected AnalysesMode selectedCalculationMode = AnalysesMode.NONE;

	protected AnalysisUi myInstance;

	protected Database db;

	public enum ReturnCode {
		CANCEL, OK
	};

	private ReturnCode returnCode = ReturnCode.CANCEL;

	boolean showBoxplot2d = false;

	boolean showBoxplotMotionMin3d = true;

	boolean showBoxplotMotionMax3d = false;

	boolean showBoxplotTouch3d = true;

	boolean assumeDynamicCharts = false;

	JComboBox chartList = null;
	JComboBox pointList = null;
	JButton graphButton = null;
	JButton addChart = null;

	MenuFactory menuFactory = null;

	public AnalysisUi(Frame parent, ArrayList<Marker> markers,
			boolean showMotionAnalysis, boolean showTouchAnalysis,
			boolean showChartAnalysis, MenuFactory menuFactory) {
		super(parent, true);
		myInstance = this;
		this.markers = markers;
		this.menuFactory = menuFactory;

		HelpManager.getInstance().enableHelpKey(this.getRootPane(),
				"analysisselection");

		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			LOGGER.error(e);
		}

		this.setTitle("Select dataset and analysis");
		this.setSize(640, 480);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setLayout(new BorderLayout());

		list = new JList(markers.toArray());

		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);

		JScrollPane scrollPane = new JScrollPane(list);

		this.add(scrollPane, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new GridLayout(0, 1));

		JPanel optionsPanel = new JPanel(new FlowLayout());

		JCheckBox checkShowBoxplot2d = new JCheckBox("Show boxplot 2D ");
		checkShowBoxplot2d.setSelected(showBoxplot2d);
		checkShowBoxplot2d.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				JCheckBox source = (JCheckBox) arg0.getSource();
				showBoxplot2d = source.isSelected();
			}
		});
		if (!showMotionAnalysis && !showTouchAnalysis) {
			checkShowBoxplot2d.setEnabled(false);
		}
		optionsPanel.add(checkShowBoxplot2d);

		optionsPanel.add(new JLabel("Show boxplot 3D: "));

		JCheckBox checkShowBoxplotTouch3d = new JCheckBox("Touch");
		checkShowBoxplotTouch3d.setSelected(showBoxplotTouch3d);
		checkShowBoxplotTouch3d.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				JCheckBox source = (JCheckBox) arg0.getSource();
				showBoxplotTouch3d = source.isSelected();
			}
		});
		if (!showTouchAnalysis) {
			checkShowBoxplotTouch3d.setEnabled(false);
		}
		optionsPanel.add(checkShowBoxplotTouch3d);

		JCheckBox checkShowBoxplotMotionMin3d = new JCheckBox("Motion min");
		checkShowBoxplotMotionMin3d.setSelected(showBoxplotMotionMin3d);
		checkShowBoxplotMotionMin3d.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				JCheckBox source = (JCheckBox) arg0.getSource();
				showBoxplotMotionMin3d = source.isSelected();
			}
		});
		if (!showMotionAnalysis) {
			checkShowBoxplotMotionMin3d.setEnabled(false);
		}
		optionsPanel.add(checkShowBoxplotMotionMin3d);

		JCheckBox checkShowBoxplotMotionMax3d = new JCheckBox("Motion max");
		checkShowBoxplotMotionMax3d.setSelected(showBoxplotMotionMax3d);
		checkShowBoxplotMotionMax3d.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				JCheckBox source = (JCheckBox) arg0.getSource();
				showBoxplotMotionMax3d = source.isSelected();
			}
		});
		if (!showMotionAnalysis) {
			checkShowBoxplotMotionMax3d.setEnabled(false);
		}
		optionsPanel.add(checkShowBoxplotMotionMax3d);

		bottomPanel.add(optionsPanel);

		JPanel specialPointsPanel = createSpecialPointsPanel(showMotionAnalysis
				|| showTouchAnalysis);

		bottomPanel.add(specialPointsPanel);

		bottomPanel.add(createChartsPanel(showChartAnalysis));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton boxplotButton = new JButton("Boxplot");
		boxplotButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				handleButton(AnalysesMode.SUM);
			}
		});
		if (!showMotionAnalysis && !showTouchAnalysis) {
			boxplotButton.setEnabled(false);
		}
		buttonPanel.add(boxplotButton);

		JButton avgButton = new JButton("Motion Average");
		avgButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				handleButton(AnalysesMode.AVG);
			}
		});
		if (!showMotionAnalysis && !showTouchAnalysis) {
			avgButton.setEnabled(false);
		}
		buttonPanel.add(avgButton);

		graphButton = new JButton("Charts only");
		graphButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				handleButton(AnalysesMode.GRAPH);
			}
		});
		if (!showChartAnalysis) {
			graphButton.setEnabled(false);
		}
		buttonPanel.add(graphButton);

		JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				collectSelection();
				for (Marker m : selectedMarkers) {
					db.removeMarker(m);
				}
				setVisible(false);
			}
		});
		buttonPanel.add(deleteButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
		buttonPanel.add(cancelButton);

		bottomPanel.add(buttonPanel);

		this.add(bottomPanel, BorderLayout.SOUTH);

		this.setVisible(true);

	}

	protected JPanel createSpecialPointsPanel(boolean enable) {
		JPanel specialPointsPanel = new JPanel(new FlowLayout());

		specialPointsPanel.add(new JLabel("Custom % "));

		SpinnerModel percentSpinnerModel = new SpinnerNumberModel(25, 0, 100, 1);
		final JSpinner percentSpinner = new JSpinner(percentSpinnerModel);

		specialPointsPanel.add(percentSpinner);

		pointList = new JComboBox();
		pointList.setEditable(false);

		specialPointsPanel.add(pointList);

		JButton addPoint = new JButton("Add");

		addPoint.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int newValue = (Integer) percentSpinner.getValue();
				for (int i = 0; i < pointList.getItemCount(); i++) {
					if (newValue == ((Integer) pointList.getItemAt(i))) {
						return;
					}
				}
				pointList.addItem(newValue);
				pointList.setSelectedIndex(pointList.getItemCount() - 1);
			}
		});

		specialPointsPanel.add(addPoint);

		JButton removePoint = new JButton("Remove");

		removePoint.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = chartList.getSelectedIndex();
				if (index > -1) {
					pointList.removeItemAt(index);
				}
			}
		});
		specialPointsPanel.add(removePoint);

		if (!enable) {
			pointList.setEnabled(false);
			removePoint.setEnabled(false);
			addPoint.setEnabled(false);
			percentSpinner.setEnabled(false);
		}
		return specialPointsPanel;
	}

	protected JPanel createChartsPanel(boolean showChartAnalysis) {
		final JPanel chartPanel = new JPanel(new FlowLayout());

		chartPanel.add(new JLabel("Charts: "));

		chartList = new JComboBox();
		chartList.setEditable(false);

		chartPanel.add(chartList);

		addChart = new JButton("Add");

		addChart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				FinishListenerHandler finishHandler = new FinishListenerHandler();
				finishHandler.addFinishListener(new IPopUpFinished() {

					@Override
					public void notifyFinished() {
						refreshChartList();
					}
				});
				JPopupMenu popUp = menuFactory.getChartPopUpMenu(finishHandler);
				popUp.show(chartPanel, addChart.getLocation().x,
						addChart.getLocation().y);
			}
		});

		chartPanel.add(addChart);

		JCheckBox checkAssumeCharts = new JCheckBox(
				"Assume dynamic chart setting ");
		checkAssumeCharts.setSelected(false);
		checkAssumeCharts.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				JCheckBox source = (JCheckBox) arg0.getSource();
				assumeDynamicCharts = source.isSelected();
				if (addChart != null) {
					addChart.setEnabled(!assumeDynamicCharts);
					chartList.setEnabled(!assumeDynamicCharts);
				}
			}
		});
		if (!showChartAnalysis) {
			checkAssumeCharts.setEnabled(false);
		}
		chartPanel.add(checkAssumeCharts);

		return chartPanel;
	}

	private void refreshChartList() {
		chartList.removeAllItems();

		int numberOfCharts = 0;

		// acceleration
		for (AccelerationChartFrame chart : menuFactory.getChartsAcceleration()
				.getCharts()) {
			chartList.addItem("Acceleration "
					+ Hand.jointTypeToName(chart.getType()));
			LOGGER.debug("accel");
			numberOfCharts++;
		}
		// feeling
		if (menuFactory.getFeelingChart().isEnabled()) {
			chartList.addItem("Feeling");
			numberOfCharts++;
		}

		// orientations
		Set<Entry<JointType, OrientationChartFrame>> orientationCharts = menuFactory
				.getChartOrientation().getCharts().entrySet();
		for (Entry<JointType, OrientationChartFrame> chart : orientationCharts) {
			chartList.addItem("Orientation "
					+ Hand.jointTypeToName(chart.getKey()));
			numberOfCharts++;
		}

		// relationssp
		for (JointRelationChartFrame chart : menuFactory.getChartsRelation()
				.getCharts()) {
			chartList.addItem("Relation "
					+ Hand.jointTypeToName(chart.getType1()) + " / "
					+ Hand.jointTypeToName(chart.getType2()));
			numberOfCharts++;
		}

		graphButton.setEnabled(numberOfCharts > 0);
	}

	private void handleButton(AnalysesMode mode) {
		collectSelection();
		if (selectedMarkers.size() > 0) {
			selectedCalculationMode = mode;
			returnCode = ReturnCode.OK;
			setVisible(false);
		} else {
			JOptionPane.showMessageDialog(myInstance,
					"You need to select at least one element", "Information",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	public AnalysesMode getSelectedCalculationMode() {
		return selectedCalculationMode;
	}

	private void collectSelection() {
		for (int i = 0; i < list.getSelectedIndices().length; i++) {
			selectedMarkers.add(markers.get(list.getSelectedIndices()[i]));
		}
	}

	public ArrayList<Marker> getSelectedMarkers() {
		return selectedMarkers;
	}

	public ReturnCode getReturnCode() {
		return returnCode;
	}

	public boolean isShowBoxplot2d() {
		return showBoxplot2d;
	}

	public ArrayList<Float> getSpecialPoints() {
		ArrayList<Float> specialPoints = new ArrayList<Float>();
		int itemCount = pointList.getItemCount();
		for (int i = 0; i < itemCount; i++) {
			String item = String.valueOf(pointList.getItemAt(i));
			if (!item.isEmpty()) {
				specialPoints.add(Integer.valueOf(item) / 100f);
			}
		}

		return specialPoints;
	}

	public boolean isAssumeDynamicCharts() {
		return assumeDynamicCharts;
	}

	public boolean isShowBoxplotMotionMin3d() {
		return showBoxplotMotionMin3d;
	}

	public boolean isShowBoxplotMotionMax3d() {
		return showBoxplotMotionMax3d;
	}

	public boolean isShowBoxplotTouch3d() {
		return showBoxplotTouch3d;
	}

}
