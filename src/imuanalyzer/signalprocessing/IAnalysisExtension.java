package imuanalyzer.signalprocessing;

/**
 * Interfaces for extending analysis with class which will be updated on every
 * motion step
 * @author Christopher-Eyk Hrabia
 * 
 */
public interface IAnalysisExtension {
	/**
	 * Notify about update
	 * @param time current timestamp
	 * @param hand current hand
	 * @param handIdx index of current hand
	 */
	void update(long time, Hand hand, int handIdx);

	/**
	 * Notify about finished analysis
	 */
	void finished();
}
