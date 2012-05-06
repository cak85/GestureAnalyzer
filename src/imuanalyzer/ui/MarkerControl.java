package imuanalyzer.ui;

import imuanalyzer.data.Database;
import imuanalyzer.data.Marker;
import imuanalyzer.signalprocessing.IOrientationSensors;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public class MarkerControl extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5817037604009410498L;

	Database db;

	IOrientationSensors sensor;

	JComboBox markerComboBox;
	
	Marker currentActiveMarker;

	public MarkerControl(IOrientationSensors _sensor) {
		this.sensor = _sensor;

		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.setLayout(new FlowLayout());

		ImageIcon icon = new ImageIcon(getClass().getResource(
				"/Icons/player_back.gif"));

		JButton buttonBack = new JButton(icon);
		buttonBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonBack.setContentAreaFilled(false);
		buttonBack.setBorderPainted(false);
		buttonBack.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("Test");

			}
		});

		this.add(buttonBack);

		icon = new ImageIcon(getClass().getResource("/Icons/player_rec.gif"));

		final JButton buttonRec = new JButton(icon);
		buttonRec.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonRec.setContentAreaFilled(false);
		buttonRec.setBorderPainted(false);

		this.add(buttonRec);

		icon = new ImageIcon(getClass().getResource("/Icons/player_stop.gif"));

		final JButton buttonStop = new JButton(icon);
		buttonRec.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonStop.setContentAreaFilled(false);
		buttonStop.setBorderPainted(false);
		buttonStop.setEnabled(false);
		buttonStop.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				currentActiveMarker.setEnd(new Date(new java.util.Date().getTime()));
				buttonRec.setEnabled(true);
				buttonStop.setEnabled(false);
				sensor.setRecording(false);

			}
		});

		buttonRec.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				buttonRec.setEnabled(false);
				buttonStop.setEnabled(true);
				currentActiveMarker=new Marker(markerComboBox.getSelectedItem()
						.toString(), "");
				db.setMarker(currentActiveMarker);
				updateMarkers();
				sensor.setRecording(true);
			}
		});

		this.add(buttonStop);

		icon = new ImageIcon(getClass().getResource("/Icons/player_for.gif"));

		JButton buttonForward = new JButton(icon);
		buttonForward.setMargin(new java.awt.Insets(0, 0, 0, 0));
		buttonForward.setContentAreaFilled(false);
		buttonForward.setBorderPainted(false);
		buttonForward.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("Test");

			}
		});

		this.add(buttonForward);

		markerComboBox = new JComboBox();
		markerComboBox.setEditable(true);

		updateMarkers();

		this.add(markerComboBox);

	}

	private void updateMarkers() {
		markerComboBox.removeAllItems();
		ArrayList<Marker> markers = db.getAvailableMarkers();
		int i = 0;
		for (i=0; i < markers.size(); i++) {
		
			markerComboBox.addItem(markers.get(i).getName());
		}
		markerComboBox.setSelectedIndex(i-1);
	}
}
