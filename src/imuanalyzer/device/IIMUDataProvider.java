package imuanalyzer.device;

public interface IIMUDataProvider {
	ImuEventManager getEventManager();

	void calibrate();

	void connectToPort(String portName) throws Exception;

	void close();
	
	boolean isConnected();
}
