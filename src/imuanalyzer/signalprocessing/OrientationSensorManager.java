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

import java.util.Comparator;
import java.util.Date;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;

public class OrientationSensorManager implements IOrientationSensors {

	/**
	 * compares FilterMapping by priority of its listener
	 */
	class PriorityComparator implements Comparator<FilterMapping> {

		@Override
		public int compare(FilterMapping o1, FilterMapping o2) {
			int prio1 = o1.getListener().getPriority();
			int prio2 = o2.getListener().getPriority();

			if (prio1 > prio2) {
				return 1;
			} else if (prio1 < prio2) {
				return -1;
			} else {
				return 0;
			}
		}

	}

	class FilterMapping {

		private Filter filter;

		private IFilterListener listener;

		public FilterMapping(IFilterListener listener, FilterTypes filterType) {
			filter = FilterFactory.getFilter(filterType);
			this.listener = listener;
			filter.setListener(listener);
		}

		public void setFilter(Filter filter) {
			this.filter = filter;
			filter.setListener(listener);
		}

		public Filter getFilter() {
			return filter;
		}

		public IFilterListener getListener() {
			return listener;
		}
	}

	private static final Logger LOGGER = Logger
			.getLogger(OrientationSensorManager.class.getName());

	private long lastFilterUpdate = 0;

	private TreeSet<FilterMapping> filters = new TreeSet<FilterMapping>(
			new PriorityComparator());

	private ImuReader imureader;

	private Object filterEditLock = new Object();

	private FilterTypes currentType;

	private Database database = Database.getInstance();

	boolean isRecording = false;

	public OrientationSensorManager(FilterTypes filterType, int numberOfImus)
			throws Exception {
		currentType = filterType;
	}

	public void setImuReader(ImuReader imureader) throws Exception {
		try {

			this.imureader = imureader;

			lastFilterUpdate = System.currentTimeMillis();

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

			//update filters in logical order
			for (FilterMapping fm : filters) {
				int id = fm.getListener().getSensorID();

				if (id > -1) {

					SensorVector accel = data[id].getAccelerometer();
					SensorVector magneto = data[id].getMagnetometer();
					SensorVector gyro = data[id].getGyroskope();

					if (localIsRecording) { // should never be true on
											// processing recorded data
						database.writeImuData(data[id].getId(), accel, gyro,
								magneto, samplePeriod, timestamp);
					}

					fm.getFilter().filterStep(samplePeriod,
							gyro.x * Math.PI / 180, gyro.y * Math.PI / 180,
							gyro.z * Math.PI / 180, accel.x, accel.y, accel.z,
							magneto.x, magneto.y, magneto.z);
				}

			}

		}
	}

	public void setFilterType(FilterTypes filterType) {
		currentType = filterType;
		synchronized (filterEditLock) {

			for (FilterMapping fm : filters) {
				fm.setFilter(FilterFactory.getFilter(filterType));
			}
		}
	}

	@Override
	public void calibrate() {
		if (imureader != null) {
			imureader.calibrate();
			for (FilterMapping fm : filters) {
				fm.getFilter().setCalibrationMode(true);
				fm.getFilter().init();
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
	public void addListener(IFilterListener listener) {
		FilterMapping fm = new FilterMapping(listener, currentType);

		filters.add(fm);
	}

	@Override
	public void removeListner(IFilterListener listner) {
		for (FilterMapping fm : filters) {
			if(fm.getListener().equals(listner)){
				filters.remove(fm);
				break;
			}
			
		}
	}

	@Override
	public void setRecording(boolean isRecording) {
		this.isRecording = isRecording;

	}

}
