package imuanalyzer.signalprocessing;

/**
 * Interface for recorder implementations
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public interface IRecorder {

	/**
	 * Register listener, have to be notified
	 * 
	 * @param listener
	 */
	void setRecordDataNotifyListener(IRecordDataNotify listener);
}
