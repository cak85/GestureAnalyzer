package imuanalyzer.ui;

import imuanalyzer.data.Marker;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;

public class MarkerCombinationSeletor extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6103905099594851540L;

	ArrayList<Marker> markers;

	ArrayList<Marker> selectedMarkers = new ArrayList<Marker>();

	JList list;

	public MarkerCombinationSeletor(Frame parent, ArrayList<Marker> markers) {
		super(parent,true);
		this.markers = markers;

		this.setTitle("Select markers for analyzing");
		this.setSize(640, 480);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setLayout(new BorderLayout());

		list = new JList(markers.toArray());

		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);

		this.add(list, BorderLayout.CENTER);

		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				collectSelection();
				setVisible(false); 
			}
		});		
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);
		
		this.add(buttonPanel, BorderLayout.SOUTH);

		this.setVisible(true);

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
