package imuanalyzer.device;

public interface IIMUDataProvider {
	ImuEventManager getEventManager();

	void calibrate();

	void setPortName(String portName) throws Exception;

	void close();
}
