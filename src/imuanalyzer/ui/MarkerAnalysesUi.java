package imuanalyzer.ui;

import imuanalyzer.data.Marker;
import imuanalyzer.signalprocessing.Analyses.AnalysesMode;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MarkerAnalysesUi extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6103905099594851540L;

	ArrayList<Marker> markers;

	ArrayList<Marker> selectedMarkers = new ArrayList<Marker>();

	JList list;

	AnalysesMode selectedCalculationMode = AnalysesMode.NONE;

	MarkerAnalysesUi myInstance;
	
	public MarkerAnalysesUi(Frame parent, ArrayList<Marker> markers) {
		super(parent, true);
		myInstance=this;
		this.markers = markers;

		this.setTitle("Select markers for analyzing");
		this.setSize(640, 480);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setLayout(new BorderLayout());

		list = new JList(markers.toArray());

		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);

		this.add(list, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton sumButton = new JButton("Motion sum");
		sumButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				collectSelection();
				if (selectedMarkers.size() > 0) {
					selectedCalculationMode = AnalysesMode.SUM;
					setVisible(false);
				} else {
					JOptionPane.showMessageDialog(myInstance, "You need to select at least one element",
							"Information", JOptionPane.OK_OPTION);
				}
			}
		});
		buttonPanel.add(sumButton);

		JButton avgButton = new JButton("Motion average");
		avgButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				collectSelection();
				if (selectedMarkers.size() > 0) {
					selectedCalculationMode = AnalysesMode.AVG;
					setVisible(false);
				} else {
					JOptionPane.showMessageDialog(myInstance, "You need to select at least one element",
							"Information", JOptionPane.OK_OPTION);
				}
			}
		});
		buttonPanel.add(avgButton);

		this.add(buttonPanel, BorderLayout.SOUTH);

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

}
