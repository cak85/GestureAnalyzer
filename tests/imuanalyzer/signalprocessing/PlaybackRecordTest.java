/**
 * 
 */
package imuanalyzer.signalprocessing;

import static org.junit.Assert.assertEquals;
import imuanalyzer.data.Database;
import imuanalyzer.data.Marker;
import imuanalyzer.device.IIMUDataProvider;
import imuanalyzer.device.IImuReaderStatusNotifier;
import imuanalyzer.device.ImuEvent;
import imuanalyzer.device.ImuEventManager;
import imuanalyzer.device.ImuRawData;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.filter.Quaternion;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.utils.SensorVector;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class PlaybackRecordTest {

	Database db;
	Hand hand;
	Marker currentActiveMarker;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		db = Database.getInstance();
		currentActiveMarker = new Marker("UNITTESTER", "UNITTESTER");
		db.setMarker(currentActiveMarker);
	}

	@After
	public void tearDown() throws Exception {
		db.removeMarker(currentActiveMarker);
	}

	private ImuRawData[] buildData(Date date, double a_x, double a_y,
			double a_z, double g_x, double g_y, double g_z, double m_x,
			double m_y, double m_z) {
		return new ImuRawData[] {
				new ImuRawData(0, date, 0.031, new SensorVector(a_x, a_y, a_z),
						new SensorVector(g_x, g_y, g_z), new SensorVector(m_x,
								m_y, m_z)),
				new ImuRawData(1, date, 0.031, new SensorVector(a_x, a_y, a_z),
						new SensorVector(g_x, g_y, g_z), new SensorVector(m_x,
								m_y, m_z)) };
	}

	@Test
	public void testRecordAndPlay() throws Exception {

		IMUReaderMockUp imuReaderMockUp = new IMUReaderMockUp();

		int numberOfSensor = 2;
		OrientationSensorManager sensors = new OrientationSensorManager(
				FilterTypes.QUATERNION_COMPLEMENTARY, numberOfSensor);
		sensors.setImuReader(imuReaderMockUp);

		hand = new Hand(sensors, currentActiveMarker);

		JointType type = JointType.values()[0];
		JointType type2 = JointType.values()[1];

		hand.setSensorID(type, 0);
		hand.setSensorID(type2, 1);

		storeInitialHandPosition(currentActiveMarker);
		storeJointMapping(currentActiveMarker);

		int testItems = 20;

		// testdata
		ArrayList<ImuRawData[]> testData = new ArrayList<ImuRawData[]>();
		for (int i = 0; i < testItems / 5; i++) {

			testData.add(buildData(new Date(), -2, 256, -5, -0.83, -0.83, 0.55,
					166, 11, 61));
			Thread.sleep(31);
			testData.add(buildData(new Date(), -5, 256, -1, -1.6, -0.27, 0.06,
					170, 5, 61));
			Thread.sleep(31);
			testData.add(buildData(new Date(), -9, 255, 3, -2, -0.27, 0.13,
					165, 9, 62));
			// Thread.sleep(31);
			// testData.add(buildData(new Date(), -9, 256, 4, -1.18, -0.34, 0.2,
			// 170, 9,
			// 62));
			// Thread.sleep(31);
			// testData.add(buildData(new Date(), -7, 255, 1, -0.76, 0, 0.76,
			// 166, 8, 63));
			// Thread.sleep(31);

			testData.add(buildData(new Date(), -20, 240, 74, 91.89, 5.21,
					10.50, 150, -79, 67));
			Thread.sleep(31);

			testData.add(buildData(new Date(), -17, 236, 53, 38.88, 0.55, 6.26,
					154, -67, 63));
			Thread.sleep(31);
		}

		sensors.setRecording(true);

		ArrayList<Quaternion> orientations = new ArrayList<Quaternion>();

		for (int i = 0; i < testItems; i++) {

			ImuRawData[] data = testData.get(i);
			ImuEvent event = new ImuEvent(data);

			imuReaderMockUp.eventManager.fireEvent(event);
			Thread.sleep(31);
			orientations.add(hand.getJoint(type).getLocalOrientation());
		}

		currentActiveMarker.setEnd(new Date(new java.util.Date().getTime()));
		sensors.setRecording(false);
		db.setMarker(currentActiveMarker);

		// test playback
		ArrayList<ImuRawData> rawData = db.getImuData(currentActiveMarker);

		assertEquals("DataSize", testData.size() * numberOfSensor,
				rawData.size());

		// compare if recorded data is the same like the testdata
		for (int i = 0; i < rawData.size(); i = i + numberOfSensor) { // check
																		// only
																		// one
																		// senosr
			// accelerometer
			double a = rawData.get(i).getAccelerometer().x;
			double b = testData.get(i / numberOfSensor)[0].getAccelerometer().x;
			assertEquals("Ax", a, b, 0);
			a = rawData.get(i).getAccelerometer().y;
			b = testData.get(i / numberOfSensor)[0].getAccelerometer().y;
			assertEquals("Ay", a, b, 0);
			a = rawData.get(i).getAccelerometer().z;
			b = testData.get(i / numberOfSensor)[0].getAccelerometer().z;
			assertEquals("Az", a, b, 0);

			// gyroscobe
			a = rawData.get(i).getGyroskope().x;
			b = testData.get(i / numberOfSensor)[0].getGyroskope().x;
			assertEquals("Gx", a, b, 0);
			a = rawData.get(i).getGyroskope().y;
			b = testData.get(i / numberOfSensor)[0].getGyroskope().y;
			assertEquals("Gy", a, b, 0);
			a = rawData.get(i).getGyroskope().z;
			b = testData.get(i / numberOfSensor)[0].getGyroskope().z;
			assertEquals("Gz", a, b, 0);

			// magnetometer
			a = rawData.get(i).getMagnetometer().x;
			b = testData.get(i / numberOfSensor)[0].getMagnetometer().x;
			assertEquals("Mx", a, b, 0);
			a = rawData.get(i).getMagnetometer().y;
			b = testData.get(i / numberOfSensor)[0].getMagnetometer().y;
			assertEquals("My", a, b, 0);
			a = rawData.get(i).getMagnetometer().z;
			b = testData.get(i / numberOfSensor)[0].getMagnetometer().z;
			assertEquals("Mz", a, b, 0);
		}

		hand = new Hand(sensors, currentActiveMarker);

		hand.setSensorID(type, 0);
		hand.setSensorID(type2, 1);
		
		// set initial orientation
		for (Entry<JointType, Joint> entry : hand.getJointSet()) {
			JointType t = entry.getKey();
			Joint joint = entry.getValue();
			// initial orientation
			joint.setLocalOrientation(db.getInitialOrientation(
					currentActiveMarker, t));
			joint.setLocalPosition(db
					.getInitialPosition(currentActiveMarker, t));
		}

		// calculate movement based on recorded raw data
		if (rawData.size() > 0) {

			Date currentPeriod = rawData.get(0).getTimeStamp();
			ImuRawData[] currentSet = new ImuRawData[numberOfSensor];
			
			int indexCalculatedOrientation = 0;

			for (int i = 0; i < rawData.size(); i++) {

				ImuRawData newData = rawData.get(i);
				Date newPeriod = newData.getTimeStamp();

				if (newPeriod.compareTo(currentPeriod) == 0
						&& newData.getId() < currentSet.length) {
					// order array by id
					currentSet[newData.getId()] = newData;
				} else {
					System.out.println("Test Sampleperiod: "+currentSet[0].getSamplePeriod());
					sensors.processImuData(currentSet.clone(),
							currentSet[0].getSamplePeriod());
					
					currentPeriod = newData.getTimeStamp();
									
					

					Quaternion orient = hand.getJoint(type)
							.getLocalOrientation();

					Quaternion orientSave = orientations
							.get(indexCalculatedOrientation);

					System.out.println(orient);
					System.out.println(orientSave);

					assertEquals("Ow " + indexCalculatedOrientation, orient.get(0), orientSave.get(0), 0.01);
					assertEquals("Ox " + indexCalculatedOrientation, orient.get(1), orientSave.get(1), 0.01);
					assertEquals("Oy " + indexCalculatedOrientation, orient.get(2), orientSave.get(2), 0.01);
					assertEquals("Oz " + indexCalculatedOrientation, orient.get(3), orientSave.get(3), 0.01);
					
					currentSet[newData.getId()] = newData;
					indexCalculatedOrientation++;
				}
			}
		}

	}

	private void storeJointMapping(Marker marker) {
		for (Entry<JointType, Joint> entry : hand.getJointSet()) {
			JointType type = entry.getKey();
			Joint joint = entry.getValue();
			// write to db
			db.setJointSensorMapping(marker, type, joint.getSensorID());
		}
	}

	/**
	 * save current local hand orientations as initial orientation for saved
	 * movement
	 */
	private void storeInitialHandPosition(Marker marker) {
		for (Entry<JointType, Joint> entry : hand.getJointSet()) {
			JointType type = entry.getKey();
			Joint joint = entry.getValue();
			// write to db
			db.setInitialOrientation(marker, type, joint.getLocalOrientation());
			db.setInitialPosition(marker, type, joint.getLocalPosition());
		}
	}

	class IMUReaderMockUp implements IIMUDataProvider {

		ImuEventManager eventManager = new ImuEventManager();

		public IMUReaderMockUp() {
		}

		@Override
		public ImuEventManager getEventManager() {
			return eventManager;
		}

		@Override
		public void calibrate() {
		}

		@Override
		public void connectToPort(String portName) throws Exception {
		}

		@Override
		public void close() {
		}

		@Override
		public boolean isConnected() {
			return true;
		}

		@Override
		public void registerStatusNotifier(IImuReaderStatusNotifier notifier) {
		}

		@Override
		public void deregisterStatusNotifier(IImuReaderStatusNotifier notifier) {
		}

	}

}
