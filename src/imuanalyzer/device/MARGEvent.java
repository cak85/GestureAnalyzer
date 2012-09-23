package imuanalyzer.device;

/**
 * One set of updated raw MARG data delivered as event
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public class MARGEvent {

	/**
	 * Raw sensors data
	 */
	private MARGRawData[] data;

	/**
	 * sampleperiod
	 */
	private double samplePeriod;

	public MARGEvent(MARGRawData[] data) {
		this.data = data;
	}

	public MARGEvent(MARGRawData[] data, double samplePeriod) {
		this.data = data;
		this.samplePeriod = samplePeriod;
	}

	/**
	 * get all data of all sensors
	 * 
	 * @return
	 */
	public MARGRawData[] getData() {
		return data;
	}

	/**
	 * @return the samplePeriod
	 */
	public double getSamplePeriod() {
		return samplePeriod;
	}

	/**
	 * @param samplePeriod
	 *            the samplePeriod to set
	 */
	public void setSamplePeriod(double samplePeriod) {
		this.samplePeriod = samplePeriod;
	}

}
