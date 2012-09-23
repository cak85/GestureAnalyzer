package imuanalyzer.ui.swing;

import imuanalyzer.filter.ITuneFilter;
import imuanalyzer.signalprocessing.IOrientationSensors;
import imuanalyzer.ui.swing.help.HelpManager;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Panel for controlling filter parameters
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class OrientationFilterTunePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4255683842847352356L;

	IOrientationSensors orientationManager;

	ArrayList<JPanel> parameterController = new ArrayList<JPanel>();

	public OrientationFilterTunePanel(IOrientationSensors orientationManager) {
		this.orientationManager = orientationManager;
		
		HelpManager.getInstance().enableHelpKey(this, "settings");

		this.setLayout(new GridLayout(0, 1));

		this.add(new JLabel("Sensor-Fusion-Algorithm Configuration:",
				SwingConstants.LEFT));

		update();
	}

	private void clearController() {
		for (JPanel j : parameterController) {
			this.remove(j);
		}
		parameterController.clear();
	}

	public void update() {
		clearController();

		ITuneFilter filterTuning = orientationManager.getCurrentTuning();
		int numberOfParameters = filterTuning.getNumberOfParameters();

		for (int i = 0; i < numberOfParameters; i++) {
			JPanel parm = getControllerPanel(filterTuning, i);
			parameterController.add(parm);
			this.add(parm);
		}

	}

	public JPanel getControllerPanel(final ITuneFilter filterTuning,
			final int index) {
		JPanel controllerPanel = new JPanel(new GridLayout(1, 0));

		JLabel nameLabel = new JLabel(filterTuning.getParameterName(index)
				+ ": ", SwingConstants.RIGHT);
		
		nameLabel.setToolTipText(filterTuning.getParameterDescription(index));
		
		controllerPanel.add(nameLabel);

		SpinnerModel spinnerModel = new SpinnerNumberModel(new Float(
				filterTuning.getParameter(index)), new Float(
				filterTuning.getMinValueFromParameter(index)), new Float(
				filterTuning.getMaxValueFromParameter(index)), new Float(
				0.000001));

		JSpinner parameterSpinner = new JSpinner(spinnerModel);

		final JSpinner.NumberEditor editor = new JSpinner.NumberEditor(
				parameterSpinner, "0.#######");
		parameterSpinner.setEditor(editor);

		parameterSpinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();

				double value = (Float) s.getValue();

				filterTuning.setParameter(index, value);

			}
		});

		parameterSpinner.setPreferredSize(new Dimension(100, 25));
		
		parameterSpinner.setToolTipText(filterTuning.getParameterDescription(index));

		controllerPanel.add(parameterSpinner);

		return controllerPanel;
	}

}
