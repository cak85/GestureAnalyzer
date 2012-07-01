package imuanalyzer.signalprocessing;

/**
 * Interface for class which needs to provide some progress informations
 * @author "Christopher-Eyk Hrabia"
 *
 */
public interface IProgress {
	void setMaxSteps(int max);
	void setStepSize(int stepSize);
	void setStep(int step);
	void stepUp();
}
