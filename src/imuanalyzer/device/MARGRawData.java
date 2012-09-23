package imuanalyzer.device;

import java.util.Date;

import imuanalyzer.utils.SensorVector;

/**
 * This class is only a container for raw data of one MARG
 * 
 */
public class MARGRawData {

	int id;
	SensorVector accelerometer;
	SensorVector gyroskope;
	SensorVector magnetometer;

	double temp = 0;

	Date timeStamp;

	double samplePeriod = 0;

	public MARGRawData() {
		accelerometer = new SensorVector();
		gyroskope = new SensorVector();
		magnetometer = new SensorVector();
	}

	public MARGRawData(int id, Date timeStamp, double samplePeriod,
			SensorVector accelerometer, SensorVector gyroskope,
			SensorVector magnetometer) {
		this(id, timeStamp, samplePeriod, accelerometer, gyroskope,
				magnetometer, 0);
	}

	public MARGRawData(int id, Date timeStamp, double samplePeriod,
			SensorVector accelerometer, SensorVector gyroskope,
			SensorVector magnetometer, double temp) {
		this.id = id;
		this.timeStamp = timeStamp;
		this.samplePeriod = samplePeriod;
		this.accelerometer = accelerometer;
		this.gyroskope = gyroskope;
		this.magnetometer = magnetometer;
		this.temp = temp;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public SensorVector getAccelerometer() {
		return accelerometer;
	}

	public void setAccelerometer(SensorVector accelerometer) {
		this.accelerometer = accelerometer;
	}

	public SensorVector getGyroskope() {
		return gyroskope;
	}

	public void setGyroskope(SensorVector gyroskope) {
		this.gyroskope = gyroskope;
	}

	public SensorVector getMagnetometer() {
		return magnetometer;
	}

	public void setMagnetometer(SensorVector magnetometer) {
		this.magnetometer = magnetometer;
	}

	public double getSamplePeriod() {
		return samplePeriod;
	}

	public void setSamplePeriod(double samplePeriod) {
		this.samplePeriod = samplePeriod;
	}

	/**
	 * @return the temp
	 */
	public double getRawTemp() {
		return temp;
	}

	/**
	 * @param temp
	 *            the temp to set
	 */
	public void setRawTemp(double temp) {
		this.temp = temp;
	}

}
