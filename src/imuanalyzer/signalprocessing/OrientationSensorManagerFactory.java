package imuanalyzer.signalprocessing;

import imuanalyzer.configuration.Configuration;

public class OrientationSensorManagerFactory {

	public static IOrientationSensors getOrientationSensor() throws Exception {

		IOrientationSensors sensors;

		sensors = new OrientationSensorManager(Configuration.getInstance()
				.getFilterType());

		return sensors;
	}

}
