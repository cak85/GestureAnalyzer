package imuanalyzer.ui;

import imuanalyzer.signalprocessing.FeelingScale;
import imuanalyzer.signalprocessing.Hand;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

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

		FeelingScale feeling = hand.getComfortScale();

		HelpManager.getInstance().enableHelpKey(this,"feeling") ;

		this.setLayout(new BorderLayout());

		//slider description
		sliderDescription = new JTextField(feeling.getDescription());
		sliderDescription.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void removeUpdate(DocumentEvent e) {
						try {
							hand.getComfortScale().setDescription(
									e.getDocument().getText(0,
											e.getDocument().getLength()));
						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
					}

					@Override
					public void insertUpdate(DocumentEvent e) {
						try {
							hand.getComfortScale().setDescription(
									e.getDocument().getText(0,
											e.getDocument().getLength()));
						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
					}

					@Override
					public void changedUpdate(DocumentEvent e) {
						try {
							hand.getComfortScale().setDescription(
									e.getDocument().getText(0,
											e.getDocument().getLength()));
						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
					}
				});

		this.add(sliderDescription, BorderLayout.NORTH);

		sliderPanel = new JPanel();

		sliderPanel.setLayout(new GridBagLayout());

		// add sliders
		for (int i = 0; i < feeling.getCurrentValues().size(); i++) {
			addSlider();
		}

		this.add(sliderPanel, BorderLayout.CENTER);
		// slider min/max configuration
		JPanel sliderConfigPanel = new JPanel();
		sliderConfigPanel.setLayout(new GridLayout(1, 0));

		sliderConfigPanel.add(new JLabel("Min", SwingConstants.RIGHT));

		SpinnerModel minSpinnerModel = new SpinnerNumberModel(feeling.getMin(),
				-100, 100, 1);
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

		sliderConfigPanel.add(new JLabel("Max", SwingConstants.RIGHT));

		SpinnerModel maxSpinnerModel = new SpinnerNumberModel(feeling.getMax(),
				-100, 100, 1);
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

		sliderConfigPanel.add(new JLabel("#", SwingConstants.RIGHT));

		SpinnerModel countSpinnerModel = new SpinnerNumberModel(Math.max(
				feeling.getCurrentValues().size(), 1), 1, 10, 1);
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
				hand.getComfortScale().setNrOfValues(value);
				writeBackSliderValues();
			}
		});

		sliderConfigPanel.add(countSpinner);

		this.add(sliderConfigPanel, BorderLayout.SOUTH);
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

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 0;
		c.weighty = 1;
		c.weightx = 1;
		c.gridx = comfortSliders.size();
		c.gridwidth = 1;
		c.gridy = 0;
		c.gridheight = 1;
		c.insets = new Insets(0, 0, 0, 0);

		comfortSliders.add(comfortSlider);
		sliderPanel.add(comfortSlider, c);
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
