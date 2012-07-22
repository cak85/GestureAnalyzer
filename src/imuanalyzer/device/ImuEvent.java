package imuanalyzer.device;


public class ImuEvent {

	private ImuRawData[] data;
	
	private double samplePeriod;

	public ImuEvent(ImuRawData[] data) {
		this.data = data;
	}

	public ImuRawData[] getData() {
		return data;
	}

	/**
	 * @return the samplePeriod
	 */
	public double getSamplePeriod() {
		return samplePeriod;
	}

	/**
	 * @param samplePeriod the samplePeriod to set
	 */
	public void setSamplePeriod(double samplePeriod) {
		this.samplePeriod = samplePeriod;
	}

}
