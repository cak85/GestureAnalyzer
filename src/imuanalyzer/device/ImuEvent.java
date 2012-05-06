package imuanalyzer.device;


public class ImuEvent {

	private ImuRawData[] data;

	public ImuEvent(ImuRawData[] data) {
		this.data = data;
	}

	public ImuRawData[] getData() {
		return data;
	}

}
