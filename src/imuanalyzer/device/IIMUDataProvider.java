package imuanalyzer.device;

public interface IIMUDataProvider {
	ImuEventManager getEventManager();

	void calibrate();

	void connectToPort(String portName) throws Exception;

	void close();
	
	boolean isConnected();
	
	/**
	 * Enables status feedback from connection thread
	 * @param notifier
	 */
	void registerStatusNotifier(IImuReaderStatusNotifier notifier);
	
	void deregisterStatusNotifier(IImuReaderStatusNotifier notifier);
	
}
