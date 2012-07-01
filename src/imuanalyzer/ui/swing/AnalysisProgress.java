package imuanalyzer.ui.swing;

import imuanalyzer.signalprocessing.IProgress;

import javax.swing.JProgressBar;

/**
 * Progressbar control
 * @author "Christopher-Eyk Hrabia"
 *
 */
public class AnalysisProgress extends JProgressBar implements IProgress {

	public AnalysisProgress() {
		this.setStringPainted(true);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -1354965375615244493L;
	int stepSize = 1;

	@Override
	public void setMaxSteps(final int max) {
		setMaximum(max);
	}

	@Override
	public void setStepSize(final int _stepSize) {
		setStep(stepSize);
		stepSize = _stepSize;
	}

	@Override
	public void setStep(final int step) {
		setValue(step);
	}

	@Override
	public void stepUp() {
		setValue(getValue() + stepSize);
		System.out.println(getValue());
	}

}
