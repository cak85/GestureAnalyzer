package imuanalyzer.signalprocessing;

/**
 * Interface for class which needs to provide some progress informations
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public interface IProgress {

	/**
	 * set maximum number of progress steps
	 * 
	 * @param max
	 */
	void setMaxSteps(int max);

	/**
	 * set step size of every progress increase
	 * 
	 * @param stepSize
	 */
	void setStepSize(int stepSize);

	/**
	 * set number of steps
	 * 
	 * @param step
	 */
	void setStep(int step);

	/**
	 * Step up only one time
	 */
	void stepUp();
}
