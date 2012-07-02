package imuanalyzer.ui;

import imuanalyzer.device.CommPortLister;
import imuanalyzer.device.IImuReaderStatusNotifier;
import imuanalyzer.signalprocessing.IOrientationSensors;
import imuanalyzer.utils.os.OSValidator;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ConnectionPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9021696224045648401L;

	JComboBox cbPort;

	IOrientationSensors sensor;

	JButton connectButton;

	ConnectionPanel instance;

	public ConnectionPanel(IOrientationSensors _sensor) {
		this.sensor = _sensor;
		instance = this;

		sensor.getImuDataProvider().registerStatusNotifier(
				new IImuReaderStatusNotifier() {
					@Override
					public void notifyImuReaderError(String string) {
						JOptionPane.showMessageDialog(instance, string,
								"Error", JOptionPane.WARNING_MESSAGE);
						disconnect();
					}
				});

		setLayout(new GridBagLayout());
		
		JLabel connectionLabel = new JLabel("Connection:");
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		
		this.add(connectionLabel,c);

		HelpManager.getInstance().enableHelpKey(this, "connection");

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 1;

		cbPort = new JComboBox(CommPortLister.getSerialList());

		this.add(cbPort, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0; // reset to default
		c.weighty = 1; // request any extra vertical space
		c.gridx = 1; // aligned with button 2
		c.gridwidth = 1;
		c.gridy = 2; // third row

		connectButton = new JButton("Connect");
		connectButton.setBackground(Color.orange);
		connectButton.setToolTipText("Connect with IMU-Reader device");

		connectButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (connectButton.getText().equals("Connect")) {
					connect();
				} else {
					disconnect();
				}
			}
		});

		this.add(connectButton, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0; // reset to default
		c.weighty = 0.5; // request any extra vertical space
		// c.anchor = GridBagConstraints.PAGE_END; // bottom of space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 2 columns wide
		c.gridy = 2; // third row

		JButton refreshButton = new JButton("Refresh");
		refreshButton.setToolTipText("Refresh available COM-Ports");

		refreshButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				refreshAndDisconnect();
			}

		});

		this.add(refreshButton, c);
	}

	protected void connect() {
		
		if (sensor.connect((String) cbPort.getSelectedItem())) {
			connectButton.setText("Disconnect");
			connectButton.setBackground(Color.green);
		} else {
			JOptionPane.showMessageDialog(this, "Could not connect",
					"Connection", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void disconnect() {
		connectButton.setText("Connect");
		connectButton.setBackground(Color.orange);
		sensor.disconnect();
	}

	private void refreshAndDisconnect() {
		sensor.disconnect();
		cbPort.removeAllItems();
		Vector<String> items = CommPortLister.getSerialList();

		for (int i = 0; i < items.size(); i++) {
			cbPort.addItem(items.get(i));
		}
		
		//select last index of comports on windows because of upcounting com order
		//on unix based OS the USB comport is before ttyS**
		if (OSValidator.isWindows()) {
			cbPort.setSelectedIndex(items.size() - 1);
		}
	}
}
