package imuanalyzer.device;

import java.sql.Date;

import imuanalyzer.tools.SensorVector;

/**
 * This class is only a container for raw data of one Imu
 * 
 */
public class ImuRawData {

	int id;
	SensorVector accelerometer;
	SensorVector gyroskope;
	SensorVector magnetometer;

	Date sampleperiod;

	public ImuRawData() {
		accelerometer = new SensorVector();
		gyroskope = new SensorVector();
		magnetometer = new SensorVector();
	}

	public ImuRawData(int id, SensorVector accelerometer,
			SensorVector gyroskope, SensorVector magnetometer) {
		this(id, new Date(0), accelerometer, gyroskope, magnetometer);
	}

	public ImuRawData(int id, Date samplePeriod, SensorVector accelerometer,
			SensorVector gyroskope, SensorVector magnetometer) {
		this.id = id;
		this.sampleperiod = samplePeriod;
		this.accelerometer = accelerometer;
		this.gyroskope = gyroskope;
		this.magnetometer = magnetometer;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getSampleperiod() {
		return sampleperiod;
	}

	public void setSampleperiod(Date sampleperiod) {
		this.sampleperiod = sampleperiod;
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

}
