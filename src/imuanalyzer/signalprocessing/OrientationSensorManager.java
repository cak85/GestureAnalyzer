package imuanalyzer.signalprocessing;

import imuanalyzer.data.Database;
import imuanalyzer.device.ImuEvent;
import imuanalyzer.device.ImuRawData;
import imuanalyzer.device.ImuReader;
import imuanalyzer.device.ImuUpdateListener;
import imuanalyzer.filter.Filter;
import imuanalyzer.filter.FilterFactory;
import imuanalyzer.filter.FilterFactory.FilterTypes;
import imuanalyzer.filter.IFilterListener;
import imuanalyzer.tools.SensorVector;
import imuanalyzer.tools.parallel.LoopBody;
import imuanalyzer.tools.parallel.Parallel;

import java.util.Date;
import java.util.Vector;

import org.apache.log4j.Logger;

public class OrientationSensorManager implements IOrientationSensors {

	private static final Logger LOGGER = Logger
			.getLogger(OrientationSensorManager.class.getName());

	private long lastFilterUpdate = 0;

	private Vector<Vector<Filter>> filters = new Vector<Vector<Filter>>();

	private ImuReader imureader;

	private Object filterEditLock = new Object();

	private FilterTypes currentType;

	private Database database = Database.getInstance();

	int numberOfImus;

	boolean isRecording = false;

	public OrientationSensorManager(FilterTypes filterType, int numberOfImus)
			throws Exception {

		currentType = filterType;
		setNumberOfImus(numberOfImus);
	}

	private void setNumberOfImus(int number) {
		this.numberOfImus = number;

		filters.clear();
		for (int j = 0; j < numberOfImus; j++) {
			filters.add(new Vector<Filter>());
		}
	}

	public void setImuReader(ImuReader imureader) throws Exception {
		try {

			this.imureader = imureader;

			lastFilterUpdate = System.currentTimeMillis();

			setNumberOfImus(imureader.getNumberOfIMUs());

			// TODO..?
			// Pipe filterPipe = new Pipe(numberOfImus);
			//
			// filterPipe.addAccelerometerFilter(new ThresholdPreprocessor(5));
			//
			// filterPipe.addMagnetometerFilter(new ThresholdPreprocessor(16));
			//
			// filterPipe.addGyroscopeFilter(new ThresholdPreprocessor(0.05));
			// filterPipe.addAccelerometerFilter(new
			// RunningAvgPreprocessor(10,numberOfImus));
			//
			// filterPipe.addMagnetometerFilter(new
			// RunningAvgPreprocessor(10,numberOfImus));
			//
			// filterPipe.addGyroscopeFilter(new
			// RunningAvgPreprocessor(10,numberOfImus));

			imureader.getEventManager()
			// .addEventListener(filterPipe);
			// filterPipe.getEventManager()
					.addEventListener(new ImuUpdateListener() {

						@Override
						public void notifyImuDataUpdate(final ImuEvent event) {

							long newFilterUpdate = System.currentTimeMillis();
							final double samplePeriod = ((double) newFilterUpdate - (double) lastFilterUpdate)
									/ (double) 1000;
							// LOGGER.debug("SamplePeriod: " + samplePeriod);

							lastFilterUpdate = newFilterUpdate;

							processImuData(event.getData(), samplePeriod);
						}

					});
		} catch (Exception e) {
			throw e;
		}
	}

	public void processImuData(final ImuRawData data[],
			final double samplePeriod) {
		synchronized (filterEditLock) {

			final Date timestamp = new Date();

			// necessary for alway getting full sets in db
			final boolean localIsRecording = isRecording;

			Parallel.For(0, numberOfImus, new LoopBody<Integer>() {

				@Override
				public void run(Integer i) {

					SensorVector accel = data[i].getAccelerometer();
					SensorVector magneto = data[i].getMagnetometer();
					SensorVector gyro = data[i].getGyroskope();

					if (localIsRecording) { // should never be true on processing recorded data
						database.writeImuData(data[i].getId(), accel, gyro, magneto,
								samplePeriod, timestamp);
					}
					// it is possible to assign
					// several filters to one sensor
					for (Filter f : filters.get(data[i].getId())) {

						f.filterStep(samplePeriod, gyro.x * Math.PI / 180,
								gyro.y * Math.PI / 180, gyro.z * Math.PI / 180,
								accel.x, accel.y, accel.z, magneto.x,
								magneto.y, magneto.z);
					}

				}
			});
		}
	}

	public void setFilterType(FilterTypes filterType) {
		currentType = filterType;
		synchronized (filterEditLock) {

			for (int i = 0; i < filters.size(); i++) {
				for (int j = 0; j < filters.get(i).size(); i++) {
					Filter filter = filters.get(i).get(j);
					IFilterListener currentListner = filter.getListener();
					Filter newFilter = FilterFactory.getFilter(filterType);
					newFilter.setListener(currentListner);
					filters.get(i).set(j, newFilter);
				}
			}
		}
	}

	@Override
	public int getNumberOfSensors() {
		return numberOfImus;
	}

	@Override
	public void calibrate() {
		if (imureader != null) {
			imureader.calibrate();
			for (Vector<Filter> vf : filters) {
				for (Filter f : vf) {
					f.setCalibrationMode(true);
					f.init();
				}
			}
		}

	}

	@Override
	public boolean connect(String port) {
		try {
			imureader.setPortName(port);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void disconnect() {
		imureader.close();

	}

	@Override
	public Vector<FilterTypes> getAvailableFilters() {
		Vector<FilterTypes> types = new Vector<FilterFactory.FilterTypes>();
		types.add(FilterTypes.AHRS);
		types.add(FilterTypes.KALMAN);
		types.add(FilterTypes.QUATERNION_COMPLEMENTARY);
		types.add(FilterTypes.AHRSMAHONY);
		types.add(FilterTypes.AHRSMADGWICK);
		return types;
	}

	@Override
	public FilterTypes getCurrentFilter() {
		return currentType;
	}

	@Override
	public void addListener(int i, IFilterListener listner) {
		if (i > -1 && i < filters.size()) {
			Filter newFilter = FilterFactory.getFilter(currentType);
			newFilter.setListener(listner);
			newFilter.init();
			filters.get(i).add(newFilter);
		}
	}

	@Override
	public void removeListner(int i, IFilterListener listner) {
		for (Filter f : filters.get(i)) {
			if (f.getListener().equals(listner)) {
				filters.get(i).remove(f);
				break;
			}
		}
	}

	public int getNumberOfImus() {
		return numberOfImus;
	}

	@Override
	public void setRecording(boolean isRecording) {
		this.isRecording = isRecording;

	}

}
