package imuanalyzer.ui.swing;

import imuanalyzer.configuration.Configuration;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.signalprocessing.IOrientationSensors;
import imuanalyzer.ui.HelpManager;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Panel with different non default
 * @author "Christopher-Eyk Hrabia"
 *
 */
public class SettingsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4820785871426946118L;

	OrientationFilterTunePanel filterTuner;

	public SettingsPanel(final IOrientationSensors sensors) {

		this.setLayout(new GridBagLayout());
		// Filter selection

		JPanel filterPanel = createFilterPanel(sensors);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.ipady = 0; // reset to default
		c.weighty = 1; // request any extra vertical space
		c.weightx = 1;
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 0;
		c.insets = new Insets(20, 20, 20, 20);

		this.add(filterPanel, c);

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.ipady = 0; // reset to default
		c.weighty = 1; // request any extra vertical space
		c.weightx = 1;
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 1;
		c.insets = new Insets(20, 20, 20, 20);

		filterTuner = new OrientationFilterTunePanel(sensors);

		this.add(filterTuner, c);

	}

	private JPanel createFilterPanel(final IOrientationSensors sensors) {
		JPanel filterPanel = new JPanel(new GridLayout(0, 1));

		JLabel filterLabel = new JLabel("Sensor-Fusion-Algorithm:",
				SwingConstants.LEFT);

		HelpManager.getInstance().enableHelpKey(filterLabel,
				"sensorfusionalgorithm");

		filterPanel.add(filterLabel);

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
				filterTuner.update();
			}
		});
		HelpManager.getInstance().enableHelpKey(filterTypes,
				"sensorfusionalgorithm");

		filterPanel.add(filterTypes);
		return filterPanel;
	}
}
