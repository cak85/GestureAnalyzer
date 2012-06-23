package imuanalyzer.ui;

import imuanalyzer.data.Database;
import imuanalyzer.data.Marker;
import imuanalyzer.signalprocessing.Analyses.AnalysesMode;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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

	boolean assumeDynamicCharts = false;

	JComboBox specialPointsList;

	public AnalysisUi(Frame parent, ArrayList<Marker> markers) {
		super(parent, true);
		myInstance = this;
		this.markers = markers;

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

		this.add(list, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new GridLayout(0, 1));

		JPanel optionsPanel = new JPanel(new FlowLayout());

		JCheckBox checkShowBoxplot2d = new JCheckBox("Show boxplot 2D ");
		checkShowBoxplot2d.setSelected(false);
		checkShowBoxplot2d.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				JCheckBox source = (JCheckBox) arg0.getSource();
				showBoxplot2d = source.isSelected();
			}
		});

		JCheckBox checkAssumeCharts = new JCheckBox(
				"Assume dynamic chart setting ");
		checkAssumeCharts.setSelected(false);
		checkAssumeCharts.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				JCheckBox source = (JCheckBox) arg0.getSource();
				assumeDynamicCharts = source.isSelected();
			}
		});

		JPanel specialPointsPanel = new JPanel(new FlowLayout());

		specialPointsPanel.add(new JLabel("Custom % "));

		SpinnerModel percentSpinnerModel = new SpinnerNumberModel(25, 0, 100, 1);
		final JSpinner percentSpinner = new JSpinner(percentSpinnerModel);

		specialPointsPanel.add(percentSpinner);

		specialPointsList = new JComboBox();
		specialPointsList.setEditable(false);

		specialPointsPanel.add(specialPointsList);

		JButton addPoint = new JButton("Add");

		addPoint.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int newValue = (Integer) percentSpinner.getValue();
				for (int i = 0; i < specialPointsList.getItemCount(); i++) {
					if (newValue == ((Integer) specialPointsList.getItemAt(i))) {
						return;
					}

				}
				specialPointsList.addItem(newValue);
				specialPointsList.setSelectedIndex(specialPointsList
						.getItemCount() - 1);
			}
		});

		specialPointsPanel.add(addPoint);

		JButton removePoint = new JButton("Remove");

		removePoint.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = specialPointsList.getSelectedIndex();
				if (index > -1) {
					specialPointsList.removeItemAt(index);
				}
			}
		});

		specialPointsPanel.add(removePoint);

		optionsPanel.add(checkShowBoxplot2d);
		optionsPanel.add(checkAssumeCharts);

		bottomPanel.add(optionsPanel);
		
		bottomPanel.add(specialPointsPanel);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton boxplotButton = new JButton("Boxplot");
		boxplotButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				collectSelection();
				if (selectedMarkers.size() > 0) {
					selectedCalculationMode = AnalysesMode.SUM;
					returnCode = ReturnCode.OK;
					setVisible(false);
				} else {
					JOptionPane.showMessageDialog(myInstance,
							"You need to select at least one element",
							"Information", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		buttonPanel.add(boxplotButton);

		JButton avgButton = new JButton("Motion Average");
		avgButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				collectSelection();
				if (selectedMarkers.size() > 0) {
					selectedCalculationMode = AnalysesMode.AVG;
					returnCode = ReturnCode.OK;
					setVisible(false);
				} else {
					JOptionPane.showMessageDialog(myInstance,
							"You need to select at least one element",
							"Information", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		buttonPanel.add(avgButton);

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

		bottomPanel.add(buttonPanel);

		this.add(bottomPanel, BorderLayout.SOUTH);

		this.setVisible(true);

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
		for (int i = 0; i < specialPointsList.getItemCount(); i++) {
			specialPoints
					.add(((Integer) specialPointsList.getItemAt(i)) / 100f);

		}

		return specialPoints;
	}

	public boolean isAssumeDynamicCharts() {
		return assumeDynamicCharts;
	}

}
