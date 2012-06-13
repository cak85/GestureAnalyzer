package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class FingerSensorMapping extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2313286213104983633L;

	public static final String IMAGE = "/Background/hand.png";

	private BackgroundPanel background;

	private EnumMap<JointType, JSpinner> spinners = new EnumMap<JointType, JSpinner>(
			JointType.class);

	int numberOfSensors;

	Hand hand;

	public FingerSensorMapping(Hand hand, int numberOfSensors) {
		this.numberOfSensors = numberOfSensors;
		this.hand = hand;

		BufferedImage img = null;
		try {
			img = ImageIO.read(getClass().getResourceAsStream(IMAGE));
		} catch (IOException e) {
		}
		background = new BackgroundPanel(img, BackgroundPanel.SCALED, 0.0f,
				0.0f);
		background.setLayout(null);

		this.setLayout(new BorderLayout());

		this.add(background, BorderLayout.CENTER);

		JLabel infoText = new JLabel(
				"<html><h2>Select sensor-id's for hand links</h2></html>");

		this.add(infoText, BorderLayout.NORTH);

		addHandSpinner(JointType.HAND_ROOT, 245, 350);

		addHandSpinner(JointType.THUMB_TOP, 30, 275);

		addHandSpinner(JointType.THUMB_MID, 76, 318);

		addHandSpinner(JointType.THUMB_BOTTOM, 120, 370);

		addHandSpinner(JointType.INDEX_TOP, 120, 75);

		addHandSpinner(JointType.INDEX_MID, 127, 135);

		addHandSpinner(JointType.INDEX_BOTTOM, 160, 195);

		addHandSpinner(JointType.MIDDLE_TOP, 235, 40);

		addHandSpinner(JointType.MIDDLE_MID, 232, 110);

		addHandSpinner(JointType.MIDDLE_BOTTOM, 230, 170);

		addHandSpinner(JointType.RING_TOP, 327, 58);

		addHandSpinner(JointType.RING_MID, 312, 120);

		addHandSpinner(JointType.RING_BOTTOM, 300, 190);

		addHandSpinner(JointType.LITTLE_TOP, 400, 150);

		addHandSpinner(JointType.LITTLE_MID, 380, 190);

		addHandSpinner(JointType.LITTLE_BOTTOM, 360, 230);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				resetAllSpinners();

			}
		});

		buttonPanel.add(resetButton);

		this.add(buttonPanel, BorderLayout.SOUTH);

	}

	private JSpinner addHandSpinner(JointType finger, int xOffset, int yOffset) {

		int defaultValue = hand.getJoint(finger).getSensorID() + 1;

		SpinnerModel spinnerModel = new SpinnerNumberModel(defaultValue, 0,
				numberOfSensors, 1);
		JSpinner spinner = new JSpinner(spinnerModel);
		Insets insets = background.getInsets();
		spinner.setBounds(xOffset + insets.left, yOffset + insets.top, 45, 25);

		spinner.addChangeListener(new SensorChangeListener(finger, hand));

		spinners.put(finger, spinner);
		background.add(spinner);

		updateSpinnerColor(defaultValue-1, spinner);
		return spinner;
	}

	private void resetAllSpinners() {
		for (Entry<JointType, JSpinner> e : spinners.entrySet()) {
			e.getValue().setValue(0);
		}
	}

	private void updateSpinnerColor(int id, JSpinner spinner) {
		JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner
				.getEditor();

		if (id > -1) {
			editor.getTextField().setBackground(Color.green);
		} else {
			editor.getTextField().setBackground(Color.white);
		}
	}

	class SensorChangeListener implements ChangeListener {

		JointType f;
		Hand hand;

		public SensorChangeListener(JointType f, Hand hand) {
			this.f = f;
			this.hand = hand;
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			JSpinner s = (JSpinner) e.getSource();
			int id = ((Integer) s.getValue()) - 1;
			hand.setSensorID(f, id);
			hand.saveJointSensorMapping(f);

			updateSpinnerColor(id, s);
		}

	}

}
