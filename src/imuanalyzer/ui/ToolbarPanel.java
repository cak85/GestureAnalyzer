package imuanalyzer.ui;

import imuanalyzer.configuration.Configuration;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.IOrientationSensors;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ToolbarPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7650659036965941580L;

	protected Hand hand;

	protected IOrientationSensors sensors;

	public ToolbarPanel(Hand _hand, IOrientationSensors _sensors,
			MainFrame mainFrame) {
		this.hand = _hand;
		this.sensors = _sensors;

		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 0;
		c.gridheight = 3;
		c.insets = new Insets(0, 10, 0, 0);

		JPanel subjectivePanel = new FeelingSliders(hand, mainFrame);

		this.add(subjectivePanel, c);

		// Filter selection

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 4;
		c.gridheight = 1;
		c.insets = new Insets(20, 10, 0, 0);

		JLabel filterLabel = new JLabel("Filter:");

		this.add(filterLabel, c);

		final JComboBox filterTypes = new JComboBox(
				sensors.getAvailableFilters());

		filterTypes
				.setToolTipText("Select filter for IMU-Orientation calculation");
		filterTypes.setSelectedItem(sensors.getCurrentFilter());

		filterTypes.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				FilterTypes filterType = (FilterTypes) filterTypes
						.getSelectedItem();
				sensors.setFilterType(filterType);
				Configuration.getInstance().setFilterType(filterType);
			}
		});

		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 5;
		c.insets = new Insets(0, 10, 0, 0);

		this.add(filterTypes, c);

		// Connection

		JLabel connectionLabel = new JLabel("Connection:");

		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 6;
		c.insets = new Insets(20, 10, 0, 0);

		this.add(connectionLabel, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0; // reset to default
		c.weightx = 1; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 2; // 1 columns wide
		c.gridy = 7;
		c.insets = new Insets(0, 10, 0, 0);

		ConnectionPanel connectionPanel = new ConnectionPanel(sensors);

		this.add(connectionPanel, c);
	}
}
