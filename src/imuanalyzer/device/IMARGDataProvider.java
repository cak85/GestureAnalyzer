package imuanalyzer.device;

/**
 * Interface for a tech indepent access to MARG hardware
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public interface IMARGDataProvider {
	MARGEventManager getEventManager();

	/**
	 * execute device calibration (not useful with current automatic temperature
	 * correction)
	 */
	void calibrate();

	/**
	 * Connect to hardware port with name
	 * 
	 * @param portName
	 * @throws Exception
	 */
	void connectToPort(String portName) throws Exception;

	/**
	 * Close connection
	 */
	void close();

	/**
	 * Check conncetion
	 * 
	 * @return true if connection is established
	 */
	boolean isConnected();

	/**
	 * Enables status feedback from connection thread will inform about
	 * connection errors
	 * 
	 * @param listerner
	 */
	void registerStatusListener(IMARGReaderStatusListener listerner);

	/**
	 * Deregister listener
	 * 
	 * @param listener
	 */
	void deregisterStatusListener(IMARGReaderStatusListener listener);

}
