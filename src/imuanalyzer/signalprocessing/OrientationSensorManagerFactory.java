package imuanalyzer.signalprocessing;

import imuanalyzer.configuration.Configuration;
import imuanalyzer.device.MARGReader;

/**
 * 
 * @author Christopher-Eyk Hrabia
 *
 */
public class OrientationSensorManagerFactory {
	
	public static final int NUMBER_OF_SENSORS = 4;

	public static IOrientationSensors getLiveOrientationManager() throws Exception {

		OrientationSensorManager sensors;
		
		MARGReader imureader = new MARGReader("/dev/ttyUSB0", NUMBER_OF_SENSORS, true);

		sensors = new OrientationSensorManager(Configuration.getInstance()
				.getFilterType(),NUMBER_OF_SENSORS);
		
		sensors.setImuReader(imureader);

		return sensors;
	}
	
	
	public static IOrientationSensors getOrientationManager() throws Exception {

		OrientationSensorManager sensors;
		
		sensors = new OrientationSensorManager(Configuration.getInstance()
				.getFilterType(),NUMBER_OF_SENSORS);
		
		return sensors;
	}

}
