package imuanalyzer.ui;

import imuanalyzer.configuration.Configuration;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.IOrientationSensors;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ToolbarPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7650659036965941580L;
	
	protected Hand hand;
	
	protected IOrientationSensors sensors;
	
	protected JSlider comfortSlider;

	public ToolbarPanel(Hand _hand, IOrientationSensors _sensors) {
		this.hand = _hand;
		this.sensors = _sensors;

		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);

		// comfort slider
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 0;
		c.gridheight = 1;
		c.insets = new Insets(10, 10, 0, 0);

		this.add(new JLabel("Comfort:"), c);

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.VERTICAL;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 1;
		c.gridheight = 2;
		c.insets = new Insets(0, 10, 0, 0);

		comfortSlider = new JSlider(SwingConstants.VERTICAL, hand
				.getComfortScale().getMin(), hand.getComfortScale().getMax(),
				hand.getComfortScale().getCurrentValue());
		comfortSlider.setMajorTickSpacing(5);
		comfortSlider.setMinorTickSpacing(1);
		comfortSlider.setPaintTicks(true);
		comfortSlider.setPaintLabels(true);
		comfortSlider.setSnapToTicks(true);
		comfortSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				hand.getComfortScale().setCurrentValue(source.getValue());
			}
		});

		this.add(comfortSlider, c);

		// slider min/max configuration
		JPanel sliderConfigPanel = new JPanel();
		sliderConfigPanel.setLayout(new FlowLayout());

		sliderConfigPanel.add(new JLabel("Min"));

		SpinnerModel minSpinnerModel = new SpinnerNumberModel(hand
				.getComfortScale().getMin(), -100, 100, 1);
		JSpinner minSpinner = new JSpinner(minSpinnerModel);
		minSpinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				int value = (Integer) s.getValue();
				comfortSlider.setMinimum(value);
				hand.getComfortScale().setMin(value);
			}
		});

		sliderConfigPanel.add(minSpinner);

		sliderConfigPanel.add(new JLabel("Max"));

		SpinnerModel maxSpinnerModel = new SpinnerNumberModel(hand
				.getComfortScale().getMax(), -100, 100, 1);
		JSpinner maxSpinner = new JSpinner(maxSpinnerModel);
		maxSpinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				int value = (Integer) s.getValue();
				comfortSlider.setMaximum(value);
				hand.getComfortScale().setMax(value);
			}
		});

		sliderConfigPanel.add(maxSpinner);

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.VERTICAL;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 3;
		c.gridheight = 1;
		c.insets = new Insets(0, 10, 0, 0);
		this.add(sliderConfigPanel, c);

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
