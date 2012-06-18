package imuanalyzer.device;

import imuanalyzer.utils.SensorVector;

import java.util.Hashtable;

/**
 * UNUSED!!!!!!!!!!!!!!!!!!!!!
 * 
 * @author toffer
 * 
 */
public class IMU {

	/**
	 * Just for testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			IMU imu = new IMU(0, 16);
			while (true) {
				imu.updateData();
				SensorVector accel = imu.getAccelerometer();
				SensorVector magneto = imu.getMagnetometer();
				SensorVector gyro = imu.getGyroskope();
				System.out.println("Accelerometer" + accel.x + ";" + accel.y
						+ ";" + accel.z + ";");
				System.out.println("Gyroskope" + magneto.x + ";" + magneto.y
						+ ";" + magneto.z + ";");
				System.out.println("Magnetometer" + gyro.x + ";" + gyro.y + ";"
						+ gyro.z + ";");

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	int comport = 0;

	int sensorIndex = 0;

	SensorVector accelerometer = new SensorVector();

	SensorVector magnetometer = new SensorVector();

	SensorVector gyroskope = new SensorVector();

	Hashtable<String, Integer> comportMapping = new Hashtable<String, Integer>();

	public IMU(int index, int comport) {
		this.comport = comport;
		this.sensorIndex = index;

		initComportMapping();
		initDevice(comport);

	}

	public IMU(int index, String comport) {
		this.sensorIndex = index;

		initComportMapping();
		this.comport = comportMapping.get(comport);
		initDevice(this.comport);
	}

	private void initComportMapping() {
		for (int i = 0; i < 16; i++) {
			comportMapping.put("/dev/ttyS" + i, i);
		}
		for (int i = 16; i < 22; i++) {
			comportMapping.put("/dev/ttyUSB" + (i - 16), i);
		}
		for (int i = 0; i < 17; i++) {
			comportMapping.put("COM" + i, i);
		}
	}

	/**
	 * Update gyroskope, magnetometer and accelerometer from real device
	 * 
	 * @throws Exception
	 */
	private void updateData() throws Exception {
		int ret = getDataFromDevice(sensorIndex, accelerometer, gyroskope,
				magnetometer);

		switch (ret) {
		case 0:
			System.out.println("Success");
			break;
		default:
			throw new Exception("Error Device Accesss - " + ret);
		}
	}

	public int getSensorIndex() throws Exception {
		return sensorIndex;
	}

	public SensorVector getAccelerometer() throws Exception {
		// updateData();
		return accelerometer;
	}

	public SensorVector getMagnetometer() throws Exception {
		// updateData();
		return magnetometer;
	}

	public SensorVector getGyroskope() throws Exception {
		// updateData();
		return gyroskope;
	}

	@Override
	protected void finalize() throws Throwable {
		closeDevice();
		super.finalize();
	}

	static {
		try {
			System.loadLibrary("IMUReader");
		} catch (Exception e) {
			System.out.println("Could not find library");
			e.printStackTrace();
		}
	}

	private native void initDevice(int comport);

	private native void closeDevice();

	private native int getDataFromDevice(int sensorIndex, SensorVector accel,
			SensorVector gyro, SensorVector magneto);

}
