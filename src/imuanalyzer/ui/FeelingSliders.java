package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Hand;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class FeelingSliders extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9003921402495644631L;

	protected ArrayList<JSlider> comfortSliders = new ArrayList<JSlider>();

	protected JTextField sliderDescription;

	protected Hand hand;

	protected JPanel sliderPanel;
	
	protected MainFrame mainFrame;

	public FeelingSliders(Hand _hand) {
		this.hand = _hand;
		
		HelpManager.getInstance().enableHelpKey(this, "feeling");

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

		sliderDescription = new JTextField("Feeling:");
		this.add(sliderDescription, c);

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.VERTICAL;
		c.ipady = 0; // reset to default
		c.weighty = 0; // request any extra vertical space
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 1;
		c.gridheight = 2;
		c.insets = new Insets(0, 10, 0, 0);

		sliderPanel = new JPanel();

		sliderPanel.setLayout(new FlowLayout());

		addSlider();

		this.add(sliderPanel, c);
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
				for (JSlider js : comfortSliders) {
					js.setMinimum(value);
				}
				hand.getComfortScale().setMin(value);
				writeBackSliderValues();
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
				for (JSlider js : comfortSliders) {
					js.setMaximum(value);
				}
				hand.getComfortScale().setMax(value);
				writeBackSliderValues();
			}
		});

		sliderConfigPanel.add(maxSpinner);

		sliderConfigPanel.add(new JLabel("#"));

		SpinnerModel countSpinnerModel = new SpinnerNumberModel(1, 1, 10, 1);
		JSpinner countSpinner = new JSpinner(countSpinnerModel);
		countSpinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				int value = (Integer) s.getValue();

				if (comfortSliders.size() > value) {
					removeSlider();
				}
				if (comfortSliders.size() < value) {
					addSlider();
				}
				writeBackSliderValues();
			}
		});

		sliderConfigPanel.add(countSpinner);

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
	}

	private void addSlider() {

		JSlider comfortSlider = new JSlider(SwingConstants.VERTICAL, hand
				.getComfortScale().getMin(), hand.getComfortScale().getMax(),
				hand.getComfortScale().getCurrentValues().get(0));
		comfortSlider.setMajorTickSpacing(5);
		comfortSlider.setMinorTickSpacing(1);
		comfortSlider.setPaintTicks(true);
		comfortSlider.setPaintLabels(true);
		comfortSlider.setSnapToTicks(true);
		comfortSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				writeBackSliderValues();
			}
		});

		comfortSliders.add(comfortSlider);
		sliderPanel.add(comfortSlider);
		this.updateUI();
	}
	
	private void writeBackSliderValues() {
		ArrayList<Integer> values = new ArrayList<Integer>();

		for (JSlider s : comfortSliders) {
			values.add(s.getValue());
		}
		hand.getComfortScale().setCurrentValues(values);
	}

	private void removeSlider() {
		if (comfortSliders.size() > 0) {
			JSlider slider = comfortSliders.get(comfortSliders.size() - 1);
			sliderPanel.remove(slider);
			comfortSliders.remove(slider);
		}
		this.updateUI();
	}
}
