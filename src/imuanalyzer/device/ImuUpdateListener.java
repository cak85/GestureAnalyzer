package imuanalyzer.device;

public interface ImuUpdateListener {
	public void notifyImuDataUpdate(ImuEvent event);
}
